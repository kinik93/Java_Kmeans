package kmeans_algorithm;

import java.util.ArrayList;
import java.util.concurrent.Callable;


public class ComputeCentroidCallable implements Callable<Boolean>{

    private int clust_index;
    private Dataset d;
    private ArrayList<Point> centroids;
    public ComputeCentroidCallable(int clust_index, Dataset d, ArrayList<Point> centroids){
        this.d = d;
        this.clust_index = clust_index;
        this.centroids = centroids;
    }

    @Override
    public Boolean call(){
        double x = 0;
        double y = 0;
        int size = 0;
        for (int i = 0; i < d.get_size(); i++) { //For each point
            if (d.getPoint_at(i).getLabel() == clust_index) { //Take only points in the current centroid
                size++;
                x += d.getPoint_at(i).getX();
                y += d.getPoint_at(i).getY();

            }
        }

        /* Compute the barycenter */
        x = x/size;
        y = y/size;

        centroids.set(clust_index,new Point(x, y));
        centroids.get(clust_index).setLabel(clust_index);

        return Boolean.TRUE;
    }
}
