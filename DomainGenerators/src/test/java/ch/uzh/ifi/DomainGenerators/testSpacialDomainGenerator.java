package ch.uzh.ifi.DomainGenerators;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

import org.junit.Test;

import ch.uzh.ifi.GraphAlgorithms.Graph;
import ch.uzh.ifi.MechanismDesignPrimitives.AtomicBid;
import ch.uzh.ifi.MechanismDesignPrimitives.FocusedBombingStrategy;
import ch.uzh.ifi.MechanismDesignPrimitives.IBombingStrategy;
import ch.uzh.ifi.MechanismDesignPrimitives.JointProbabilityMass;

public class testSpacialDomainGenerator
{
	/*
	 * 
	 */
	@Test
	public void test()
	{
		//List<Integer> list = IntStream.range(0, 5).boxed().map(x->x+1).collect(Collectors.toList());
		//System.out.println("Lst: " + list.toString());
		List<Double> lst = IntStream.range(0, 5).boxed().map(x-> Double.valueOf(x)).collect(Collectors.toList());
		lst.replaceAll( x -> x/2);
		System.out.println(lst.toString());
		double sum = lst.stream().reduce( (x1, x2) -> x1+x2 ).get();
		System.out.println("sum="+sum);
	}
	
	/*
	 * The test for ::pickGoodFromBundle(...)
	 */
	@Test
	public void testPickGoodFromBundle() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException 
	{
		SpatialDomainGenerator spacialDomainGenerator = new SpatialDomainGenerator();
		Method method = spacialDomainGenerator.getClass().getDeclaredMethod("pickGoodFromSet", List.class, List.class);
		method.setAccessible(true);
		
		List<Integer> goods = new LinkedList<Integer>();
		goods.add(1);
		goods.add(2);
		goods.add(3);
		
		List<Double> probabilityDistribution = new LinkedList<Double>();
		probabilityDistribution.add(0.1);
		probabilityDistribution.add(0.3);
		probabilityDistribution.add(0.6);
		
		int nGood1 = 0;
		int nGood2 = 0;
		int nGood3 = 0;
		for(int i = 0; i < 1000000; ++i)
		{
			int good = (int)method.invoke(spacialDomainGenerator, goods, probabilityDistribution);
			if( good == 1)
				nGood1 += 1;
			else if( good == 2)
				nGood2 += 1;
			else if( good == 3)
				nGood3 += 1;
		}

		assertTrue( Math.abs((double)nGood3/(double)nGood1 - probabilityDistribution.get(2) / probabilityDistribution.get(0)) < 1e-1 ); 
		assertTrue( Math.abs((double)nGood3/(double)nGood2 - probabilityDistribution.get(2) / probabilityDistribution.get(1)) < 1e-1 );
	}

	/*
	 * Test for ::addGoodToBundle(...)
	 */
	@Test
	public void testAddGoodToBundle() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException 
	{
		SpatialDomainGenerator spacialDomainGenerator = new SpatialDomainGenerator();
		Method method = spacialDomainGenerator.getClass().getDeclaredMethod("addGoodToBundle", List.class, List.class, Graph.class);
		method.setAccessible(true);
	
		List<Integer> bundle = new LinkedList<Integer>();
		bundle.add(3);
		
		List<Double> probabilityDistribution = new LinkedList<Double>();
		probabilityDistribution.add(0.1);
		probabilityDistribution.add(0.2);	//0.2
		probabilityDistribution.add(0.1);
		probabilityDistribution.add(0.1);
		probabilityDistribution.add(0.1);
		probabilityDistribution.add(0.1);
		probabilityDistribution.add(0.1);
		probabilityDistribution.add(0.1);
		probabilityDistribution.add(0.1);
		
		GridGenerator generator = new GridGenerator(3, 3);
		generator.setSeed(0);
		generator.buildProximityGraph();
		Graph grid = generator.getGrid();
		
		method.invoke(spacialDomainGenerator, bundle, probabilityDistribution, grid);
		int nGood2 = 0;
		int nGood6 = 0;
		int nGoodOther = 0;
		int nSamples = 1000000;
		for(int i = 0; i < 1000000; ++i)
		{
			bundle.clear();
			bundle.add(3);
			method.invoke(spacialDomainGenerator, bundle, probabilityDistribution, grid);
			
			if( bundle.get(1) == 2)
				nGood2 += 1;
			else if( bundle.get(1) == 6)
				nGood6 += 1;
			else
				nGoodOther += 1;
		}

		assertTrue( Math.abs((double)nGood2/(double)nGood6 - probabilityDistribution.get(1) / probabilityDistribution.get(5)) < 1e-1 ); 
		assertTrue( Math.abs((double)nGoodOther/(double)nSamples - spacialDomainGenerator.getJumpProbability() ) < 1e-1 );
	}
	
	/*
	 * The method generates a single bid twice and tests whether they are equal (given
	 * that they were generated with the same random seed)  
	 */
	@Test
	public void testGenerateBids() throws Exception 
	{
		int numberOfRows = 3;
		int numberOfColumns = 3;
		GridGenerator generator = new GridGenerator(numberOfRows, numberOfColumns);
		generator.setSeed(0);
		generator.buildProximityGraph();
		Graph grid = generator.getGrid();
		
		SpatialDomainGenerator spatialDomainGenerator = new SpatialDomainGenerator(numberOfRows * numberOfColumns, grid);
		spatialDomainGenerator.generateSpacialBids(1234*234, 1);
		List<AtomicBid> bid1 = spatialDomainGenerator.getBid();
		
		System.out.println("bid1: " + bid1.toString());
				
		spatialDomainGenerator.generateSpacialBids(1234*234, 1);
		List<AtomicBid> bid2 = spatialDomainGenerator.getBid();
		
		System.out.println("bid2: " + bid2.toString());
		
		assertTrue(bid1.size() == bid2.size());
		
		for(int i = 0; i < bid1.size(); ++i)
		{
			assertTrue( bid1.get(i).getInterestingSet().containsAll(bid2.get(i).getInterestingSet()) 
					 && bid2.get(i).getInterestingSet().containsAll(bid1.get(i).getInterestingSet()));
			assertTrue( bid1.get(i).getValue() == bid2.get(i).getValue() );
		}
		assertTrue(bid1.get(0) != bid2.get(0));
		
	}
	
	/*
	 * The method generates a single combinatorial bid.
	 */
	@Test
	public void testGenerateBidsWithAvailabilities() throws Exception 
	{
		GridGenerator generator = new GridGenerator(3, 3);
		generator.setSeed(0);
		generator.buildProximityGraph();
		Graph grid = generator.getGrid();
		
		SpatialDomainGenerator spatialDomainGenerator = new SpatialDomainGenerator(9, grid);
		spatialDomainGenerator.generateSpacialBids(0, 1);
		
		List<AtomicBid> bid = spatialDomainGenerator.getBid();
		
		JointProbabilityMass jpmf = new JointProbabilityMass( grid );
		jpmf.setNumberOfSamples(1000000);
		jpmf.setNumberOfBombsToThrow(1);
		IBombingStrategy b1 = new FocusedBombingStrategy(grid, 1., 0.3, 0.2);
		List<IBombingStrategy> bombs = Arrays.asList( b1);
		
		List<Double> pd = Arrays.asList( 1.0 );
		jpmf.setBombs(bombs, pd);
		jpmf.update();
		
		System.out.println("Bid: " + bid.toString());
		
		double prob = jpmf.getMarginalProbability(bid.get(0).getInterestingSet(), null, null);
		System.out.println("p="+prob);
	}
	
	/*
	 * The method generates a single bid.
	 */
	@Test
	public void testGenerateBidsWithAvailabilities1() throws Exception 
	{
		int numberOfGoods = 4;
		int numberOfAtoms = 2;
		GridGenerator generator = new GridGenerator(2, 2);
		generator.setSeed(0);
		generator.buildProximityGraph();
		Graph grid = generator.getGrid();
		
		SpatialDomainGenerator spatialDomainGenerator = new SpatialDomainGenerator(numberOfGoods, grid);
		spatialDomainGenerator.generateSpacialBids(0, 1);
		
		List<AtomicBid> bid = spatialDomainGenerator.getBid();
		
		JointProbabilityMass jpmf = new JointProbabilityMass( grid );
		jpmf.setNumberOfSamples(1000000);
		
		IBombingStrategy b = new FocusedBombingStrategy(grid, 1, 0.3, 0.2);
		List<IBombingStrategy> bombs = new LinkedList<IBombingStrategy>();
		bombs.add(b);
		
		List<Double> probDistribution = new LinkedList<Double>();
		probDistribution.add(1.);
		
		jpmf.setBombs(bombs, probDistribution);
		jpmf.setNumberOfBombsToThrow(1);
		jpmf.update();
		
		System.out.println("Bid: " + bid.toString());
		
		double prob = jpmf.getMarginalProbability(bid.get(0).getInterestingSet(), null, null);
		System.out.println("p="+prob);
	}
}
