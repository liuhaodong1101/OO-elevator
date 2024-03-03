import com.oocourse.elevator3.ElevatorRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class RunningElevators {
    private final ArrayList<Elevator> elevators;

    private final int[][] accessMatrix = new int[11][11];

    public RunningElevators(ArrayList<Elevator> elevators) {
        this.elevators = elevators;
    }

    public synchronized void addElevator(ElevatorRequest elevatorRequest,
                                         PersonWaitLine personWaitLine) {
        int id = elevatorRequest.getElevatorId();
        int maxNum = elevatorRequest.getCapacity();
        int startFloor = elevatorRequest.getFloor();
        double speed = elevatorRequest.getSpeed();
        int access = elevatorRequest.getAccess();
        Elevator elevator = new Elevator(id,startFloor,maxNum,speed, personWaitLine,access);
        new Thread(elevator).start();
        elevators.add(elevator);
        drawAccessGraph();
    }

    public synchronized void mainTianElevator(int id) {
        Elevator elevator1 = null;
        for (Elevator elevator: elevators) {
            if (elevator.getId() == id) {
                elevator1 = elevator;
                elevator.setNeedMaintain(true);
                synchronized (elevator.getRequestTable()) {
                    elevator.getRequestTable().notify();
                }
            }
        }
        elevators.remove(elevator1);
        drawAccessGraph();
    }

    public synchronized void stopAllElevator() {
        for (Elevator elevator : elevators) {
            synchronized (elevator.getRequestTable()) {
                elevator.getRequestTable().setEnd(true);
                elevator.getRequestTable().notifyAll();
            }
        }
    }

    public synchronized void drawAccessGraph() {
        for (int i = 0; i < 11; i++) {
            for (int j = 0; j < 11; j++) {
                accessMatrix[i][j] = 0;
            }
        }
        for (Elevator elevator : elevators) {
            ArrayList<Integer> nodes = elevator.getAccessArray();
            for (int i = 0; i < nodes.size(); i++) {
                for (int j = i + 1; j < nodes.size(); j++) {
                    accessMatrix[nodes.get(i) - 1][nodes.get(j) - 1] = 1;
                    accessMatrix[nodes.get(j) - 1][nodes.get(i) - 1] = 1;
                }
            }
        }
    }

    public synchronized void findShortestPath(Person person) {
        int n = accessMatrix.length;
        int[] dist = new int[n];
        int[] prev = new int[n];
        boolean[] visited = new boolean[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(prev, -1);
        int source = person.getStartFloor() - 1;
        int destination = person.getPersonRequest().getToFloor() - 1;
        dist[source] = 0;
        for (int i = 0; i < n; i++) {
            int u = -1;
            for (int j = 0; j < n; j++) {
                if (!visited[j] && (u == -1 || dist[j] < dist[u])) {
                    u = j;
                }
            }
            visited[u] = true;
            for (int v = 0; v < n; v++) {
                if (!visited[v] && accessMatrix[u][v] > 0) {
                    int newPath = dist[u] + accessMatrix[u][v];
                    if (newPath < dist[v]) {
                        dist[v] = newPath;
                        prev[v] = u;
                    }
                }
            }
        }
        LinkedList<Integer> path = new LinkedList<>();
        if (dist[destination] == Integer.MAX_VALUE) {
            person.setRoutes(null);
        }
        for (int v = destination; v != -1; v = prev[v]) {
            path.addFirst(v);
        }
        ArrayList<Route> routes = new ArrayList<>();
        for (int i = 0; i < path.size() - 1; i++) {
            routes.add(new Route(path.get(i) + 1, path.get(i + 1) + 1));
        }
        person.setRoutes(routes);
    }

    public synchronized void deliverPerson(Person person) {
        int fitness = Integer.MIN_VALUE;
        int k = 0;
        for (int i = 0; i < elevators.size(); i++) {
            if (elevators.get(i).getFitness(person) > fitness) {
                fitness = elevators.get(i).getFitness(person);
                k = i;
            }
        }
        while (fitness == Integer.MIN_VALUE) {
            k = 0;
            findShortestPath(person);
            for (int i = 0; i < elevators.size(); i++) {
                if (elevators.get(i).getFitness(person) > fitness) {
                    fitness = elevators.get(i).getFitness(person);
                    k = i;
                }
            }
        }
        synchronized (elevators.get(k).getRequestTable()) {
            elevators.get(k).getRequestTable().addPerson(person);
            elevators.get(k).getRequestTable().notifyAll();
        }
    }
}
