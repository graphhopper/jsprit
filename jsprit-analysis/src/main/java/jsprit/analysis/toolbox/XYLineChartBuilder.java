/*******************************************************************************
 * Copyright (c) 2014 Stefan Schroeder.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3.0 of the License, or (at your option) any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package jsprit.analysis.toolbox;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author schroeder
 *
 */
public class XYLineChartBuilder {
	
	/**
	 * Helper that just saves the chart as specified png-file. The width of the image is 1000 and height 600.
	 * 
	 * @param chart
	 * @param pngFilename
	 */
	public static void saveChartAsPNG(JFreeChart chart, String pngFilename){
		try {
			ChartUtilities.saveChartAsPNG(new File(pngFilename), chart, 1000, 600);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns a new instance of the builder.
	 * 
	 * @param chartTitle appears on top of the XYLineChart
	 * @param xDomainName appears below the xAxis
	 * @param yDomainName appears beside the yAxis
	 * 
	 * @return the builder
	 */
	public static XYLineChartBuilder newInstance(String chartTitle, String xDomainName, String yDomainName){
		return new XYLineChartBuilder(chartTitle, xDomainName, yDomainName);
	}
	
	private ConcurrentHashMap<String,XYSeries> seriesMap = new ConcurrentHashMap<String, XYSeries>();
	
	private final String xDomain;
	
	private final String yDomain;
	
	private final String chartName;
	
	private XYLineChartBuilder(String chartName, String xDomainName, String yDomainName) {
		this.xDomain=xDomainName;
		this.yDomain=yDomainName;
		this.chartName=chartName;
	}
	
	/**
	 * Adds data to the according series (i.e. XYLine).
	 * 
	 * @param seriesName
	 * @param xVal
	 * @param yVal
	 */
	public void addData(String seriesName, double xVal, double yVal){
		if(!seriesMap.containsKey(seriesName)){
			seriesMap.put(seriesName, new XYSeries(seriesName,true,true));
		}
		seriesMap.get(seriesName).add(xVal, yVal);
	}
	
	/**
	 * Builds and returns JFreeChart.
	 * @return
	 */
	public JFreeChart build(){
		XYSeriesCollection collection = new XYSeriesCollection();
		for(XYSeries s : seriesMap.values()){
			collection.addSeries(s);
		}
		JFreeChart chart = ChartFactory.createXYLineChart(chartName, xDomain, yDomain, collection, PlotOrientation.VERTICAL, true, true, false);
		XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
		plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
		return chart;
	}

}
