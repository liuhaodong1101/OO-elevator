import java.util.ArrayList;

public class PersonWaitLine implements Runnable {
    private final RunningElevators runningElevators;

    private final ArrayList<Person> personWaitLine = new ArrayList<>();

    private boolean canStop = false;

    private int count = 0;

    public synchronized void addCount() {
        count++;
    }

    public synchronized void subCount() {
        count--;
    }

    public PersonWaitLine(RunningElevators runningElevators) {
        this.runningElevators = runningElevators;
    }

    public synchronized void addPersonToWaitLine(Person person) {
        personWaitLine.add(person);
        notifyAll();
    }

    public synchronized Person getPersonFromWaitLine() {
        if (personWaitLine.size() == 0) {
            return null;
        } else {
            Person person = personWaitLine.get(0);
            personWaitLine.remove(person);
            return person;
        }
    }

    public synchronized void tryDeliverPersonRequest() {
        if (InputHandler.getIsEnd() && count == 0) {
            runningElevators.stopAllElevator();
            canStop = true;
            return;
        }
        Person person = this.getPersonFromWaitLine();
        if (person != null) {
            if (person.getRoutes() == null) {
                runningElevators.findShortestPath(person);
            }
            if (person.getRoutes().size() != 0) {
                runningElevators.deliverPerson(person);
            } else {
                subCount();
            }
        }
        else {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void run() {
        do {
            tryDeliverPersonRequest();
        } while (!canStop);
    }
}
