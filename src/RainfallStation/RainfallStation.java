package RainfallStation;

public class RainfallStation {
    private double x;
    private double y;
    private double rainfall;

    public RainfallStation(double x, double y, double rainfall) {
        this.x = x;
        this.y = y;
        this.rainfall = rainfall;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRainfall() {
        return rainfall;
    }
}
