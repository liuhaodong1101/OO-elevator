public class Route {
    private final int startFloor;
    private final int toFloor;

    public Route(int startFloor, int toFloor) {
        this.startFloor = startFloor;
        this.toFloor = toFloor;
    }

    public int getStartFloor() {
        return startFloor;
    }

    public int getToFloor() {
        return toFloor;
    }
}
