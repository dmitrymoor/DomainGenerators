package ch.uzh.ifi.DomainGenerators;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class benchmarkJPMF 
{
	static private int nSamples = 1000000;

	@State(Scope.Benchmark)
	public static class ListWrapper
	{
		public ListWrapper()
		{
			_array = new ArrayList<Integer>();
			for(int i = 0; i < nSamples; ++i)
				_array.add( (int)(1000 * Math.random()) );
			
		}
		
		public List<Integer> getArray()
		{
			return _array;
		}
		
		private List<Integer> _array;
	}
	
	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MICROSECONDS)
	public static int loopBenchmark(ListWrapper listWrapper)
	{	
		List<Integer> array = listWrapper.getArray();
		
		int m = Integer.MIN_VALUE;
		List<Integer> newList = new ArrayList<Integer>();
		for (int i : array)								//map
			newList.add( (int)Math.sqrt(i) );
			
		for (int i : newList)							//reduce
			if (i>m) m=i;
		
		return m;
	}
	
	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MICROSECONDS)
    public static int streamBenchmark(ListWrapper listWrapper) 
	{
		List<Integer> array = listWrapper.getArray();
		int sum = array.stream().map(i->(int)Math.sqrt(i)).reduce(Integer.MIN_VALUE, Math::max);
		return sum;
    }
	
	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MICROSECONDS)
    public static int sreamParallelBenchmark(ListWrapper listWrapper) 
	{	
		List<Integer> array = listWrapper.getArray();
		int sum = array.stream().parallel().map(i->(int)Math.sqrt(i)).reduce(Integer.MIN_VALUE, Math::max);
		return sum;
    }
	
	public static void main(String[] args) throws RunnerException
	{
		Options opt = new OptionsBuilder().include(benchmarkJPMF.class.getSimpleName()).warmupIterations(5).measurementIterations(5).threads(4).forks(1).build();
		new Runner(opt).run();
	}

}
