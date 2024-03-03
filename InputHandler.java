import com.oocourse.elevator3.ElevatorInput;
import com.oocourse.elevator3.ElevatorRequest;
import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.Request;
import com.oocourse.elevator3.MaintainRequest;
import java.io.IOException;

public class InputHandler implements Runnable {
    private final RunningElevators runningElevators;
    private static boolean isEnd = false;

    public static boolean getIsEnd() {
        return isEnd;
    }

    private final PersonWaitLine personWaitLine;

    public void addElevator(ElevatorRequest elevatorRequest) {
        runningElevators.addElevator(elevatorRequest,personWaitLine);
    }

    public void mainTianElevator(int id) {
        runningElevators.mainTianElevator(id);
    }

    public InputHandler(RunningElevators runningElevators, PersonWaitLine personWaitLine) {
        this.runningElevators = runningElevators;
        this.personWaitLine = personWaitLine;
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            Request request = elevatorInput.nextRequest();
            if (request == null) {
                synchronized (personWaitLine) {
                    personWaitLine.notifyAll();
                }
                isEnd = true;
                break;
            } else if (request instanceof PersonRequest) {
                personWaitLine.addPersonToWaitLine(new Person((PersonRequest) request, null));
                personWaitLine.addCount();
            } else if (request instanceof ElevatorRequest) {
                addElevator((ElevatorRequest) request);
            } else if (request instanceof MaintainRequest) {
                mainTianElevator(((MaintainRequest) request).getElevatorId());
            }
        }
        try {
            elevatorInput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
