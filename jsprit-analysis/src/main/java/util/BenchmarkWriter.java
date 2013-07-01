package util;

import java.util.Collection;

import analysis.ConcurrentBenchmarker.BenchmarkResult;

public interface BenchmarkWriter {
	public void write(Collection<BenchmarkResult> results);
}