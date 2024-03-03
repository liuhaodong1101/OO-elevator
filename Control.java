import java.util.HashSet;

public class Control {
    public  String getIns(Elevator elevator) {
        RequestTable requestTable = elevator.getRequestTable();
        if (elevator.isNeedMaintain()) {
            return "MAINTAIN";
        }
        if (hasPersonOut(elevator) || hasPersonIn(elevator)) {
            return "OPEN";
        }
        if (elevator.getCurNum() > 0) {
            return "MOVE";
        } else {
            int personNum = requestTable.personNum(0,"UP_FLOOR");
            if (personNum == 0) {
                if (requestTable.isEnd()) {
                    return "STOP";
                } else {
                    return "WAIT";
                }
            } else {
                int frontPersonNum;
                if (elevator.getDir() == 1) {
                    frontPersonNum = requestTable.personNum(elevator.getCurFloor(),"UP_FLOOR");
                } else {
                    frontPersonNum = requestTable.personNum(elevator.getCurFloor(),"DOWN_FLOOR");
                }
                if (frontPersonNum > 0) {
                    return "MOVE";
                } else {
                    return "REVERSE";
                }
            }
        }
    }

    public Boolean hasPersonOut(Elevator elevator) {
        int curFloor = elevator.getCurFloor();
        HashSet<Person> personHashSet = elevator.getDestMap().get(curFloor);
        if (personHashSet != null) {
            return personHashSet.size() > 0;
        }
        return false;
    }

    public Boolean hasPersonIn(Elevator elevator) {
        int curFloor = elevator.getCurFloor();
        synchronized (elevator.getRequestTable()) {
            HashSet<Person> personHashSet = elevator.
                    getRequestTable().getPersonMap().get(curFloor);
            if (personHashSet != null && elevator.getCurNum() < elevator.getMaxNum()) {
                for (Person person : personHashSet) {
                    if (elevator.isSameDir(person)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
