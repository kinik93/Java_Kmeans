package kmeans_algorithm;

import java.util.ArrayList;

/**
 * This class represents a dataset in a ArrayList container which is not synchronized and provides some utility methods
 */
public class Dataset {

    private ArrayList<Point> data;
    public Dataset(){
        data = new ArrayList<>(100);
    }



    /** Simply add a kmeans_algorithm.Point to the container
     *
     * @param p is the kmeans_algorithm.Point to be added
     */
    public void addPoint(Point p){
        data.add(p);
    }


    /**
     *
     * @param index of the kmeans_algorithm.Point element to return
     * @return the i-th kmeans_algorithm.Point of the container
     */
    public Point getPoint_at(int index){
        return data.get(index);
    }


    /**
     *
     * @return the dimension of the container
     */
    public int get_size(){
        return data.size();
    }


    /**
     * Print the entire point's dataset
     */
    public void printDataset(){
        for (Point p: data){
            System.out.println(p);
        }
    }
}
