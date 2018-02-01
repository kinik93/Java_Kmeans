package kmeans_algorithm;

/** This is an utility class that implements some static methods to provide different distances */
public final class Distance {

    private Distance(){}


    /**
     *
     * @param p1-first 2D point
     * @param p2-second 2D point
     * @return euclidean distance between p1 and p2
     */
    public static double euclideanDistance(Point p1, Point p2){
        return Math.sqrt(Math.pow(p1.getX()-p2.getX(),2)+Math.pow(p1.getY()-p2.getY(),2));
    }


    /**
     *
     * @param p1-first 2D point
     * @param p2-second 2D point
     * @return manhattan distance between p1 and p2
     */
    public static double manhattanDistance(Point p1, Point p2){
        return Math.abs(p1.getX()-p2.getX())+Math.abs(p1.getY()-p2.getY());
    }
}
