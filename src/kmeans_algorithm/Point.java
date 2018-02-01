package kmeans_algorithm;

/**
 * Class that represent a 2D point
 */
public class Point {

    private double x;

    private double y;

    private int label; // We use this for identification of the cluster in K-means


    /**
     * Create a new point with coordinates (x, y)
     * @param x-coordinate of point
     * @param y-coordinate of point
     */
    public Point(double x, double y){

        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public int getLabel() { return label; }

    public void setLabel(int label) { this.label = label; }

    /**
     * @Override toString method to provide a simple printing of the structure
     * @return
     */
    public String toString(){
        return "\nX: " + x + " , Y: " + y + " , class: " + label;
    }


    public static boolean isEqual(Point p1, Point p2){ return (p1.getX()==p2.getX() && p2.getY()==p1.getY());}
}
