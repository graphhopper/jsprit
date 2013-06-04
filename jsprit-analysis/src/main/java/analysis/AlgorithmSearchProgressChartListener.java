/*******************************************************************************
 * Copyright (c) 2011 Stefan Schroeder.
 * eMail: stefan.schroeder@kit.edu
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package analysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;
import basics.algo.AlgorithmEndsListener;
import basics.algo.IterationEndsListener;


/**
 * VehicleRoutingAlgorithm-Listener to record the solution-search-progress.
 * 
 * <p>Register this listener in VehicleRoutingAlgorithm.
 * 
 * @author stefan schroeder
 * 
 */

public class AlgorithmSearchProgressChartListener implements IterationEndsListener, AlgorithmEndsListener {

	private static Logger log = Logger.getLogger(AlgorithmSearchProgressChartListener.class);

	private double[] bestResults;

	private double[] worstResults;
	
	private double[] avgResults;

	private List<Double> bestResultList = new ArrayList<Double>();

	private List<Double> worstResultList = new ArrayList<Double>();
	
	private List<Double> avgResultList = new ArrayList<Double>();

	private String filename;

	/**
	 * Constructs chart listener with target png-file (filename plus path).
	 * 
	 * @param pngFileName
	 */
	public AlgorithmSearchProgressChartListener(String pngFileName) {
		super();
		this.filename = pngFileName;
	}


	@Override
	public void informAlgorithmEnds(VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
		log.info("create chart " + filename);
		if(bestResultList.isEmpty()){
			log.warn("cannot create chart since no results available.");
			return;
		}
		bestResults = new double[bestResultList.size()];
		worstResults = new double[worstResultList.size()];
		avgResults = new double[avgResultList.size()];
		
		double maxValue = 0.0;
		double minValue = Double.MAX_VALUE;
		
		double[] iteration = new double[bestResultList.size()];
		for (int i = 0; i < bestResultList.size(); i++) {
			if(bestResultList.get(i) < minValue) minValue = bestResultList.get(i);
			if(worstResultList.get(i) < minValue) minValue = worstResultList.get(i);
			if(avgResultList.get(i) < minValue) minValue = avgResultList.get(i);
			
			if(bestResultList.get(i) > maxValue) maxValue = bestResultList.get(i);
			if(worstResultList.get(i) > maxValue) maxValue = worstResultList.get(i);
			if(avgResultList.get(i) > maxValue) maxValue = avgResultList.get(i);
			
			bestResults[i] = bestResultList.get(i);
			worstResults[i] = worstResultList.get(i);
			avgResults[i] = avgResultList.get(i);
			iteration[i] = i;
		}
		XYSeriesCollection coll = new XYSeriesCollection();
		JFreeChart chart = ChartFactory.createXYLineChart("search-progress","iterations", "results",coll, PlotOrientation.VERTICAL,true,true,false);
		addSeries("bestResults", iteration, bestResults, coll);
		addSeries("worstResults", iteration, worstResults, coll);
		addSeries("avgResults", iteration, avgResults, coll);
		
		XYPlot plot = chart.getXYPlot();
		
		NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
		Range rangeY = new Range(minValue-0.05*minValue,maxValue + 0.05*maxValue);
		yAxis.setRange(rangeY);
		
		try {
			ChartUtilities.saveChartAsJPEG(new File(filename), chart, 1000, 600);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addSeries(String string, double[] iteration, double[] results, XYSeriesCollection coll) {
		XYSeries series = new XYSeries(string, true, true);
		for(int i=0;i<iteration.length;i++){
			series.add(iteration[i], results[i]);
		}
		coll.addSeries(series);
	}

	@Override
	public void informIterationEnds(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
		double worst = 0.0;
		double best = Double.MAX_VALUE;
		double sum = 0.0;
		for(VehicleRoutingProblemSolution sol : solutions){
			if(sol.getCost() > worst) worst = sol.getCost();
			if(sol.getCost() < best) best = sol.getCost();
			sum += sol.getCost();
		}
		bestResultList.add(best);
		worstResultList.add(worst);
		avgResultList.add(sum/(double)solutions.size());
	}

}
