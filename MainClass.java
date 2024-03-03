import com.oocourse.elevator3.TimableOutput;
import java.util.ArrayList;

public class MainClass {
    private static final int ELEVATOR_INIT_NUM = 6;

    private static final int INIT_ELEVATOR_MAX_NUM = 6;

    private static final int ACCESS_ALL = 0b11111111111;

    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();
        ArrayList<Elevator> elevators = new ArrayList<>();
        RunningElevators runningElevators = new RunningElevators(elevators);
        PersonWaitLine personWaitLine = new PersonWaitLine(runningElevators);
        for (int i = 0; i < ELEVATOR_INIT_NUM; i++) {
            Elevator elevator = new Elevator(i + 1, 1, INIT_ELEVATOR_MAX_NUM,
                    0.4, personWaitLine,ACCESS_ALL);
            elevators.add(elevator);

        }
        runningElevators.drawAccessGraph();
        for (int i = 0; i < ELEVATOR_INIT_NUM;i++) {
            new Thread(elevators.get(i)).start();
        }
        new Thread(new InputHandler(runningElevators, personWaitLine)).start();
        new Thread(personWaitLine).start();
    }
}
