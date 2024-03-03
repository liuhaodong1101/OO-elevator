import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.TimableOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class Elevator implements Runnable {
    private final int maxNum;
    private  boolean needMaintain = false;

    private  long lastMoveTime = 0;

    private int curFloor;
    private int dir = 1;//-1== down 1==up

    private int curNum = 0;

    private final ArrayList<Integer> accessArray;
    private final int timeForOneMove;

    private final PersonWaitLine personWaitLine;

    public HashMap<Integer, HashSet<Person>> getDestMap() {
        return destMap;
    }

    private final int id;
    private final Control control = new Control();
    private final RequestTable requestTable = new RequestTable();
    private final HashMap<Integer,HashSet<Person>> destMap = new HashMap<>();

    public Elevator(int id,int startFloor,int maxNum, double timeForOneMove,
                    PersonWaitLine personWaitLine,int access) {
        this.id = id;
        this.curFloor = startFloor;
        this.maxNum = maxNum;
        this.timeForOneMove = (int)(1000 * timeForOneMove);
        this.personWaitLine = personWaitLine;
        this.accessArray = calAccessArray(access);
    }

    public void move() {
        long tempTime = timeForOneMove - (System.currentTimeMillis() - lastMoveTime);
        if (tempTime > 0) {
            try {
                Thread.sleep(tempTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (dir == 1) {
            curFloor++;
        } else {
            curFloor--;
        }
        TimableOutput.println(
                String.format("ARRIVE-%d-%d", curFloor,id));
        lastMoveTime = System.currentTimeMillis();
    }

    public int getFitness(Person person) {
        int startFloor = person.getStartFloor();
        int toFloor = person.getToFloor();
        if (!(accessArray.contains(startFloor) && accessArray.contains(toFloor))) {
            return Integer.MIN_VALUE;
        }
        int timeToExtraWait;
        synchronized (requestTable) {
            timeToExtraWait = 10 * (requestTable.personNum(0, "UP_FLOOR") + curNum);
        }
        return -(Math.abs(curFloor - startFloor) + timeToExtraWait) *
                timeForOneMove / (100 * maxNum);
    }

    public void printInAndOut(ArrayList<Person> people, ArrayList<Person> people1) {
        TimableOutput.println(
                String.format("OPEN-%d-%d", curFloor, id));
        printAndAddOutPerson(people);
        printInPerson(people1);
        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        TimableOutput.println(
                String.format("CLOSE-%d-%d", curFloor, id));
    }

    public void openAndClose() {
        SemaphoreExample.acquireSemaphoreForServe(curFloor);
        ArrayList<Person> people = out();
        ArrayList<Person> people1 = in();
        boolean isOnlyIn = true;
        for (Person person: people) {
            if (!hasContainPeople(person,people1)) {
                isOnlyIn = false;
            }
        }
        if (isOnlyIn) {
            SemaphoreExample.acquireSemaphoreForOnlyIn(curFloor);
            printInAndOut(people,people1);
            SemaphoreExample.releaseSemaphoreForOnlyIn(curFloor);
        } else {
            printInAndOut(people,people1);
        }
        SemaphoreExample.releaseSemaphoreForServe(curFloor);
        lastMoveTime = System.currentTimeMillis();
    }

    public void mainTian() {
        synchronized (requestTable) {
            removeRequestTable();
        }
        if (!destMapIsEmpty()) {
            SemaphoreExample.acquireSemaphoreForServe(curFloor);
            TimableOutput.println(String.format("OPEN-%d-%d", curFloor, id));
            ArrayList<Person> people = out();
            printAndAddOutPerson(people);
            removeDestMap();
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            TimableOutput.println(String.format("CLOSE-%d-%d", curFloor, id));
            SemaphoreExample.releaseSemaphoreForServe(curFloor);
            lastMoveTime = System.currentTimeMillis();
        }
        TimableOutput.println(String.format("MAINTAIN_ABLE-%d", id));
    }

    @Override
    public void run() {
        label:
        while (true) {
            String ins;
            ins = control.getIns(this);
            synchronized (requestTable) {
                if (ins.equals("WAIT")) {
                    try {
                        requestTable.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            switch (ins) {
                case "STOP":
                    break label;
                case "MAINTAIN":
                    mainTian();
                    break label;
                case "MOVE":
                    move();
                    break;
                case "REVERSE":
                    dir = -dir;
                    break;
                case "OPEN":
                    openAndClose();
                    break;
                default:
                    break;
            }
        }
    }

    public ArrayList<Person> in() {
        ArrayList<Person> peoples = new ArrayList<>();
        synchronized (requestTable) {
            HashSet<Person> personHashSet = requestTable.
                    getPersonMap().get(curFloor);
            if (personHashSet != null) {
                Iterator<Person> iterator = personHashSet.iterator();
                while (iterator.hasNext()) {
                    Person person = iterator.next();
                    if (curNum < maxNum && isSameDir(person)) {
                        int toFloor = person.getToFloor();
                        if (destMap.get(toFloor) == null) {
                            HashSet<Person> personHashSet1 = new HashSet<>();
                            personHashSet1.add(person);
                            destMap.put(toFloor, personHashSet1);
                        } else {
                            destMap.get(toFloor).add(person);
                        }
                        peoples.add(person);
                        curNum++;
                        iterator.remove();
                    }
                }
            }
        }
        return peoples;
    }

    public ArrayList<Person> out() {
        ArrayList<Person> peoples = new ArrayList<>();
        HashSet<Person> personHashSet = destMap.get(curFloor);
        if (personHashSet != null) {
            Iterator<Person> iterator = personHashSet.iterator();
            while (iterator.hasNext()) {
                Person person = iterator.next();
                iterator.remove();
                person.finishOneRoute();
                peoples.add(person);
                curNum--;
            }
        }
        return peoples;
    }

    public boolean hasContainPeople(Person person,ArrayList<Person> people) {
        for (Person person1 : people) {
            if (person1.getPersonRequest().getPersonId() ==
                    person.getPersonRequest().getPersonId()) {
                return true;
            }
        }
        return false;
    }

    public void removeRequestTable() {
        for (Map.Entry<Integer, HashSet<Person>> entry : requestTable.
                getPersonMap().entrySet()) {
            HashSet<Person> persons = entry.getValue();
            for (Person person : persons) {
                personWaitLine.addPersonToWaitLine(person);
            }
        }
        requestTable.removeAllPerson();
    }

    public boolean destMapIsEmpty() {
        for (Map.Entry<Integer, HashSet<Person>> entry : destMap.entrySet()) {
            HashSet<Person> persons = entry.getValue();
            if (persons.size() > 0) {
                return false;
            }
        }
        return true;
    }

    public void removeDestMap() {
        for (Map.Entry<Integer, HashSet<Person>> entry : destMap.entrySet()) {
            HashSet<Person> persons = entry.getValue();
            for (Person person : persons) {
                TimableOutput.println(
                        String.format("OUT-%d-%d-%d", person.getPersonRequest().getPersonId(),
                                curFloor, id));
                PersonRequest personRequest = new PersonRequest(curFloor,
                        person.getPersonRequest().getToFloor(),
                        person.getPersonRequest().getPersonId());
                personWaitLine.addPersonToWaitLine(new Person(personRequest, null));
            }
        }
        destMap.clear();
    }

    public void printAndAddOutPerson(ArrayList<Person> peoples) {
        for (Person person : peoples) {
            TimableOutput.println(
                    String.format("OUT-%d-%d-%d", person.getPersonRequest().getPersonId(),
                            curFloor, id));
            personWaitLine.addPersonToWaitLine(person);
        }
    }

    public void printInPerson(ArrayList<Person> peoples) {
        for (Person person : peoples) {
            TimableOutput.println(String.format(
                    "IN-%d-%d-%d", person.getPersonRequest().getPersonId(),
                    curFloor, id));
        }
    }

    private ArrayList<Integer> calAccessArray(int access) {
        ArrayList<Integer> arrayList = new ArrayList<>();
        for (int i = 1;i <= 11;i++) {
            if ((access & (1 << (i - 1))) != 0) {
                arrayList.add(i);
            }
        }
        return arrayList;
    }

    public ArrayList<Integer> getAccessArray() {
        return accessArray;
    }

    public RequestTable getRequestTable() {
        return requestTable;
    }

    public boolean isSameDir(Person person) {
        return (person.getToFloor() - person.getStartFloor()) * dir > 0;
    }

    public void setNeedMaintain(boolean needMaintain) {
        this.needMaintain = needMaintain;
    }

    public boolean isNeedMaintain() {
        return needMaintain;
    }

    public int getMaxNum() {
        return maxNum;
    }

    public int getCurFloor() {
        return curFloor;
    }

    public int getCurNum() {
        return curNum;
    }

    public int getId() {
        return id;
    }

    public int getDir() {
        return dir;
    }
}
