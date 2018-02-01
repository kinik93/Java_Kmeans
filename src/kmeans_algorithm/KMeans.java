package kmeans_algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public final class KMeans {


    private KMeans(){} /* Instantiate an object doesn't make sense, so there's no way to do it */


    /**
     * Find the point whose minimum distance from all the centroids is max
     */
    private static Point get_max_min_point(Dataset d, ArrayList<Point> centroids){

        /* I can get the point whose minimum distance from all current centroids
         * is max in O(nk). I can't parallelize this job due to the max dependency */

        double max = -1;
        int max_p = 0;

        //For each point
        for (int j=0; j<d.get_size();j++)  {
            //initialize the min
            double min = Distance.euclideanDistance(d.getPoint_at(j),centroids.get(0));

            //For each centroid
            for (int i = 1; i < centroids.size(); i++) {
                if (Distance.euclideanDistance(d.getPoint_at(j),centroids.get(i))<min)

                    //update the min
                    min = Distance.euclideanDistance(d.getPoint_at(j), centroids.get(i));
            }
            if (min > max) {

                //update the max and the index of the point which is the best candidate
                max = min;
                max_p = j;
            }
        }
        return d.getPoint_at(max_p);
    }


    /**
     * Initialize all the stuff for the algorithm choosing the first k centroids
     */
    private static void initialize(Dataset d, ArrayList<Point> centroids, int k){

        for (int i=0;i<d.get_size();i++){
            d.getPoint_at(i).setLabel(-1);
        }

        /* Start adding the first random chosen centroid from all the points */
        centroids.add(d.getPoint_at(new Random().nextInt(d.get_size())));
        centroids.get(0).setLabel(0);

        /* Then I have to choose the remaining k-1 centroids with the max-min distance method*/
        for (int i=1;i<k;i++) {
            centroids.add(get_max_min_point(d, centroids));
            centroids.get(i).setLabel(i);
        }


    }


    /**
     * This method implements a single-thread based version of the label assignment phase
     * @param d dataset
     * @param centroids centroids
     * @return true if there is at least a point that changed its cluster, false otherwise
     */
    private static boolean sequentialAssignLabels(Dataset d, ArrayList<Point> centroids) {

        boolean cluster_changed = false;

        /* For each point */
        for (int i = 0;i<d.get_size();i++) {

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

            /* Check if some points changed their cluster for termination of algorithm */
            if (previous != cluster_index)
                cluster_changed = true;

        }
        return cluster_changed;
    }


    /**
     * This method implements a single-thread based version of the centroids updating phase
     * @param d dataset
     * @param k number of clusters
     * @return updated list of centroids
     */
    private static ArrayList<Point> sequentialUpdateCentroids(Dataset d, int k){

        /* Initialize a new list of centroids */
        ArrayList<Point> centroids = new ArrayList<>(k);

        /* For each cluster */
        for(int clust_index = 0; clust_index < k; clust_index++) {

            double x = 0;
            double y = 0;
            int size = 0;
            for (int i = 0; i < d.get_size(); i++) { //For each point
                if (d.getPoint_at(i).getLabel() == clust_index) { //Take only points in the current cluster

                    size++;
                    x += d.getPoint_at(i).getX();
                    y += d.getPoint_at(i).getY();
                }
            }

            /* Compute the barycenter */
            x = x / size;
            y = y / size;

            centroids.add(new Point(x, y));
            centroids.get(clust_index).setLabel(clust_index);
        }

        return centroids;
    }

    /**
     * Implement a parallel assign-labels phase
     * @param d dataset
     * @param centroids list of centroids
     * @return True if there's at least one point which changed its cluster, false otherwise
     */
    private static boolean parallelAssignLabels(Dataset d, ArrayList<Point> centroids, int threadPoolSize, ExecutorService executor){

        /** Now the main idea to parallelize the label assignment phase:
         *
         *  I could generate n tasks (n number of points) and assign them to some threads, but
         *  generally the number of available processor is enormously minor than the number of points.
         *  So, almost certainly, the executor will spend more time organizing all the tasks and assigning
         *  them to every thread, than the total time that every single thread need to assign just one label.
         *  This fine-granularity idiom would be suitable in programming contexts such CUDA where we can use so many
         *  cores and their management cost is ridiculous.
         *  So in this approach we consider a bigger workflow for each task. The most logical thing to do is to split
         *  the dataset into a number of subsets equal to the number of threads available.
         *  We then assign the labels of each group in parallel.
         */



        /* Here we take the ceiling than we'll check if the index is in the bound at the right time */
        int task_size = d.get_size() / threadPoolSize + ((d.get_size() % threadPoolSize == 0) ? 0 : 1);

        /* Create the tasks with the current centroids */
        List<AssignLabelCallable> tasks = new ArrayList<>();
        for (int j = 0; j < threadPoolSize; j++) {
            tasks.add(new AssignLabelCallable(j * task_size, (j + 1) * task_size, d, centroids));
        }

        boolean cluster_changed = false;

        try {

            /* Assign all the labels and wait till the end of each task */
            List<Future<Boolean>> futures = executor.invokeAll(tasks);

            /* Here we check if at least one point has changed his cluster */
            for (Future<Boolean> f : futures)
                cluster_changed = cluster_changed || f.get().booleanValue();
        }
        catch(InterruptedException | ExecutionException ex){

            System.out.println("Executor exception: " + ex.getMessage());
        }

        return cluster_changed;
    }


    /**
     * For each label, it calculates (in a parallel way) the barycentre of the cluster and makes it the new centroid of that
     */
    private static ArrayList<Point> parallelUpdateCentroids(Dataset d, int k, ExecutorService executor){

        /* First re-initialized adding some fictitious point (prevent a future issue of index out out bound) */
        ArrayList<Point> centroids = new ArrayList<>(k);
        for (int i=0;i<k;i++){
            centroids.add(new Point(-1,-1));
        }


        List<ComputeCentroidCallable> tasks = new ArrayList<>();

        /**
            What about setting all the centroids in a multithreading context with a no-thread-safe structure as
            ArrayList? In this case we avoid a race condition in the centroids ArrayList as we perfectly know
            that each thread operates and compute independent centroids. So for each centroid computed, we just put it
            at the right index in the data structure, i.e. avoiding data collisions in that.
            This "Ad-hoc" solution is better than using concurrent data structures which synchronize the access to the
            whole object.
        */

        for (int clust_ind=0;clust_ind<k;clust_ind++){  //For each centroid, add the relative task

            tasks.add(new ComputeCentroidCallable(clust_ind, d, centroids));
        }

        /* Here the barrier, we have to wait till the end of all the tasks */
        try {

            executor.invokeAll(tasks);
        }
        catch(InterruptedException ex){
            System.out.println("Executor exception: "+ex.getMessage());
        }

        return centroids;

    }


    /**
     * This method implements the algorithm K-means.
     * @see <a href="https://en.wikipedia.org/wiki/K-means_clustering">https://en.wikipedia.org/wiki/K-means_clustering </a>
     * @param d: The dataset you want to explore
     * @param k: The number of clusters
     * @return the time (in milliseconds) spent to execute the core part of the algorithm
     */
    public static double evolve(Dataset d,int k, int threadPoolSize){


        ArrayList<Point> centroids = new ArrayList<>(k);

        /* First initialize all the centroids with the max-min distance in O(nk^2) */
        /* (probably k^2 << n so I really make this in at most O(n^2))*/
        initialize(d,centroids,k);

        long startTime;
        double timeSpent;

        /* Check if the parallelized version was chosen */
        if (threadPoolSize > 1) {

            System.out.println("Parallel version chosen\n");

            ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
            boolean cluster_changed = true;

            startTime = System.nanoTime();

            /* Here the core of the algorithm */
            while (cluster_changed) {

                /* Assign all the labels in parallel */
                cluster_changed = parallelAssignLabels(d,centroids,threadPoolSize,executor);

                /* Update all centroids in parallel */
                centroids = parallelUpdateCentroids(d, k, executor);

            }

            long endTime = System.nanoTime();
            timeSpent = (double) (endTime - startTime) / 1000000; //conversion in milliseconds

            executor.shutdownNow();
        }

        /* Implements the sequential version */
        else if(threadPoolSize == 1){

            System.out.println("Sequential version chosen\n");
            boolean cluster_changed = true;

            startTime = System.nanoTime();
            /* Here the core of the algorithm */
            while (cluster_changed) {

                /* Assign all the labels */
                cluster_changed = sequentialAssignLabels(d,centroids);

                /* Update all centroids */
                centroids = sequentialUpdateCentroids(d, k);

            }

            long endTime = System.nanoTime();

            timeSpent = (double) (endTime - startTime) / 1000000; //conversion in milliseconds
        }

        else{

            System.out.println("You have to specify a valid number of threads\n");
            return 0;
        }

        return timeSpent;
    }

}
