package kmeans_algorithm;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class AssignLabelCallable implements Callable<Boolean>{

    private int first;
    private int last;
    private Dataset d;
    private ArrayList<Point> centroids;

    public AssignLabelCallable(int first, int last, Dataset d, ArrayList<Point> centroids) {
        this.first = first;
        this.last = last;
        this.d = d;
        this.centroids = centroids;
    }

    /**
     *   For every point in the subset, it looks for the nearest centroid and sets the right label
     */
    @Override
    public Boolean call() {

        boolean cluster_changed = false;

        /* For every admissible point related to this task */
        int i = first;
        while(i<last && i<d.get_size()){

            Point p = d.getPoint_at(i);
            double dist;

            /* Start taking the minimum equal to the distance from the first centroid */
            double min_distance = Distance.euclideanDistance(centroids.get(0), p);
            int cluster_index = 0;

            /* Then a classical routine for the determination of the centroid with
            /* minimum distance from the current point */
            for (int j = 1; j < centroids.size(); j++) {
                dist = Distance.euclideanDistance(centroids.get(j), p);
                if (dist < min_distance) {
                    min_distance = dist;
                    cluster_index = j;
                }
            }
            int previous = p.getLabel();
            p.setLabel(cluster_index);

            /* Check if some point changed its cluster for termination of algorithm */
            if (previous != cluster_index)
                cluster_changed = true;
            i++;
        }
        return Boolean.valueOf(cluster_changed);
    }
}
