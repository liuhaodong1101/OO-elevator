import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class RequestTable {
    private final HashMap<Integer, HashSet<Person>> personMap;

    private boolean isEnd;

    public RequestTable() {
        this.personMap = new HashMap<>();
        this.isEnd = false;
    }

    public  boolean isEnd() {
        return isEnd;
    }

    public  void setEnd(boolean end) {
        isEnd = end;
    }

    public synchronized HashMap<Integer, HashSet<Person>> getPersonMap() {
        return personMap;
    }

    public synchronized void addPerson(Person person) {
        int curFloor = person.getStartFloor();
        if (personMap.get(curFloor) == null) {
            HashSet<Person> personHashSet = new HashSet<>();
            personHashSet.add(person);
            personMap.put(curFloor, personHashSet);
        } else {
            personMap.get(curFloor).add(person);
        }
    }

    public synchronized void removeAllPerson() {
        personMap.clear();
    }

    public synchronized int personNum(int floor, String dir) {
        int num = 0;
        for (Map.Entry<Integer, HashSet<Person>> entry :
                personMap.entrySet()) {
            if (dir.equals("UP_FLOOR")) {
                if (entry.getKey() > floor) {
                    num += entry.getValue().size();
                }
            } else {
                if (entry.getKey() < floor) { //DOWN_FLOOR
                    num += entry.getValue().size();
                }
            }
        }
        return num;
    }
}
