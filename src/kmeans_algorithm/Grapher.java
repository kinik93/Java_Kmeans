package kmeans_algorithm;

import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.util.ShapeUtilities;

import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * This class wraps the utilities for drawing a dataset points
 *
 * requires JCommon and JFreeChart libraries (Add these by: File->Project structure -> Libraries)
 * @see <a href="http://www.jfree.org/jfreechart/">http://www.jfree.org/jfreechart/</a>
 *
 */
public class Grapher {

    private XYSeriesCollection XYdataset;

    public Grapher(){
        XYdataset = new XYSeriesCollection();
    }

    /**
     * Draw k clusters on the dataset
     * @param dataset represent the 2d dataset
     * @param k represent the number of clusters
     */
    public void draw(Dataset dataset, int k){
        for(int j=0; j<k; j++){
            XYSeries series = new XYSeries(j);
            for (int i=0; i<dataset.get_size();i++){
                if (dataset.getPoint_at(i).getLabel()==j){
                    series.add(dataset.getPoint_at(i).getX(),dataset.getPoint_at(i).getY());
                }
            }
            XYdataset.addSeries(series);
        }

        JFreeChart chart = ChartFactory.createScatterPlot("Title",
                "X-Axis", "Y-Axis", XYdataset);
        TextTitle title = new TextTitle("K means execution");
        title.setFont(new Font("TimesNewRoman", Font.PLAIN, 20));
        chart.setTitle(title);

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(new Color(255,255,255)); //set background color to white
        XYItemRenderer renderer = plot.getRenderer();
        for(int i=0; i<XYdataset.getSeriesCount(); i++){
            renderer.setSeriesShape(i, new Ellipse2D.Double(-1,-1,2,2)); //Set the shape of the point
        }


        ChartFrame frame = new ChartFrame("Java k means", chart);
        frame.pack();
        frame.setVisible(true);
    }
}
