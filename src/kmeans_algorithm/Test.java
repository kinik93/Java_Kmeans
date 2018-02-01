package kmeans_algorithm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class Test {


    /** Fill the dataset with points readed from your own dataset.
     *  Create your own points.txt by running the python script avaiable at
     *  @see <a href="https://github.com/kinik93/Java_K_means/blob/master/GeneratePoints.py">https://github.com/kinik93/Java_K_means/blob/master/GeneratePoints.py</a>
     */
    private static void fillDataset(Dataset d){
        try{

            BufferedReader in = new BufferedReader(new FileReader("src/points.txt"));
            String str;
            while ((str = in.readLine()) != null){
                String[] tmp=str.split(",");
                d.addPoint(new Point(Double.parseDouble(tmp[0]), Double.parseDouble(tmp[1])));
            }
            in.close();
        }
        catch (IOException e){
            System.out.println("File Read Error");
        }
    }

    public static void main(String[] args){

        Dataset d = new Dataset();

        /* Set here the number of clusters and the number of threads*/
        int k = 3;
        int threadPoolSize = 8;
        double time;


        fillDataset(d);

        System.out.println("Dataset size: "+d.get_size()+" number of threads: "+ threadPoolSize);

        /* Execute the algorithm */
        time = KMeans.evolve(d, k, threadPoolSize);

        System.out.println("Computation time: "+ time +" ms\n");


        /* Uncomment these two lines below to view a graphical rapresentation of the created clusters */

        Grapher g = new Grapher();
        g.draw(d, k);


    }
}
