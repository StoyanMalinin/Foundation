package foundation.map.tomtom;

public class Position<T extends Number> {
    private T x;
    private T y;

    public Position(T x, T y) {
        this.x = x;
        this.y = y;
    }

    public T x() {
        return x;
    }
    public T y() {
        return y;
    }
}
