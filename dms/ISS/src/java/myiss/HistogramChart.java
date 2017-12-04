package myiss;

import java.awt.image.BufferedImage;

import java.util.HashMap;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.TextAnchor;


public class HistogramChart  {
	private static final long serialVersionUID = 1L;

	static class LabelGenerator extends StandardCategoryItemLabelGenerator {
		private static final long serialVersionUID = 1L;

		@Override
		public String generateLabel(CategoryDataset categorydataset, int i, int j) {
			return categorydataset.getRowKey(i).toString();
		}
	}

	public static BufferedImage HistogramChart(List<HashMap> data, int width, int height, String category, String value, String title) {
            BufferedImage img = null;
	    JFreeChart jfreechart = createChart(createDataset(data ),category, value, title);
            img = jfreechart.createBufferedImage(width,height);            
            return img;
	}

	private  static CategoryDataset createDataset(List<HashMap> data ) {
		DefaultCategoryDataset defaultcategorydataset = new DefaultCategoryDataset();
                for (HashMap hm : data){
                    defaultcategorydataset.addValue(Tools.parseInt(hm.get("y_arr"),0), "1", Tools.getStringValue(hm.get("x_arr"),"",""));
                }

		return defaultcategorydataset;
	}

	private static JFreeChart createChart(CategoryDataset categorydataset, String category, String value, String title) {
		JFreeChart jfreechart = ChartFactory.createBarChart(title,
				category, value, categorydataset, PlotOrientation.VERTICAL,
				false, true, false);
		CategoryPlot categoryplot = (CategoryPlot) jfreechart.getPlot();
		categoryplot.setRangePannable(true);
		BarRenderer barrenderer = (BarRenderer) categoryplot.getRenderer();
		barrenderer.setDrawBarOutline(false);
                barrenderer.setItemLabelsVisible(true);
                barrenderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
                barrenderer.setBaseItemLabelsVisible(true);
                barrenderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE1, TextAnchor.HALF_ASCENT_RIGHT));
		return jfreechart;
	}



}
