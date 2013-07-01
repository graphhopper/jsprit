package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import analysis.ConcurrentBenchmarker.BenchmarkResult;

public class HtmlBenchmarkTableWriter implements BenchmarkWriter{

	private String filename;
	
	public HtmlBenchmarkTableWriter(String filename) {
		this.filename = filename;
	}

	@Override
	public void write(Collection<BenchmarkResult> results) {
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filename)));
			writer.write(openTable() + newline());
			//table head
			writer.write(openRow() + newline());
			writer.write(head("instance") + newline());
			writer.write(head("compTime [sec]") + newline());
			writer.write(head("result") + newline());
			writer.write(head("&Delta; bestKnown [in %]") + newline());
			writer.write(closeRow() + newline());
			//data
			double sum_time = 0.0;
			double sum_result = 0.0;
			double sum_delta = 0.0;
			for(BenchmarkResult result : results){
				sum_time+=result.time;
				sum_result+=result.result;
				sum_delta+=result.delta;
				writer.write(openRow() + newline());
				writer.write(date(result.instance.name) + newline());
				writer.write(date(Double.valueOf(round(result.time,2)).toString()) + newline());
				writer.write(date(Double.valueOf(round(result.result,2)).toString()) + newline());
				writer.write(date(Double.valueOf(round(result.delta*100.0,2)).toString()) + newline());
				writer.write(closeRow() + newline());
			}
			writer.write(openRow() + newline());
			writer.write(date("avg") + newline());
			writer.write(date(Double.valueOf(round(sum_time/(double)results.size(),2)).toString()) + newline());
			writer.write(date(Double.valueOf(round(sum_result/(double)results.size(),2)).toString()) + newline());
			writer.write(date(Double.valueOf(round(sum_delta/(double)results.size()*100.0,2)).toString()) + newline());
			writer.write(closeRow() + newline());
			
			writer.write(closeTable() + newline());
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private double round(Double value, int i) {
		long roundedVal = Math.round(value*Math.pow(10, i));
		return (double)roundedVal/(double)(Math.pow(10, i));
	}

	private String head(String head) {
		return "<th>"+head+"</th>";
	}

	private String closeTable() {
		return "</table>";
	}

	private String openTable() {
		return "<table>";
	}

	private String closeRow() {
		return "</tr>";
	}

	private String date(String date) {
		return "<td>"+date+"</td>";
	}

	private String newline() {
		return "\n";
	}

	private String openRow() {
		return "<tr>";
	}

	

}
