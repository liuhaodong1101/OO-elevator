import com.oocourse.elevator3.PersonRequest;

import java.util.ArrayList;

public class Person {
    private final PersonRequest personRequest;

    private ArrayList<Route> routes;

    public Person(PersonRequest personRequest, ArrayList<Route> routes) {
        this.personRequest = personRequest;
        this.routes = routes;
    }

    public PersonRequest getPersonRequest() {
        return personRequest;
    }

    public int getStartFloor() {
        if (routes == null) {
            return personRequest.getFromFloor();
        }
        else if (routes.size() > 0) {
            return routes.get(0).getStartFloor();
        } else {
            System.out.println("ERROR_FAIL_TO_GET_START_FLOOR");
            return -1;
        }
    }

    public ArrayList<Route> getRoutes() {
        return routes;
    }

    public int getToFloor() {
        if (routes.size() > 0) {
            return routes.get(0).getToFloor();
        } else {
            System.out.println("ERROR_OF_GET_TO_FLOOR");
            return -1;
        }
    }

    public void setRoutes(ArrayList<Route> routes) {
        this.routes = routes;
    }

    public void finishOneRoute() {
        if (routes == null) {
            System.out.println("ERROR_ROUTES_NULL");
            return;
        }
        if (routes.size() == 0) {
            System.out.println("ERROR_ROUTES_ZERO");
            return;
        }
        Route route = routes.get(0);
        routes.remove(route);
    }
}
