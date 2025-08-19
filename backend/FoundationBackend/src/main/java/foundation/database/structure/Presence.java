package foundation.database.structure;


public record Presence(long timestamp, double x, double y) {
    public double distanceTo(Presence other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}