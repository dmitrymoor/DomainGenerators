package ch.uzh.ifi.DomainGenerators;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import ch.uzh.ifi.GraphAlgorithms.Graph;
import ch.uzh.ifi.MechanismDesignPrimitives.FocusedBombingStrategy;
import ch.uzh.ifi.MechanismDesignPrimitives.IBombingStrategy;
import ch.uzh.ifi.MechanismDesignPrimitives.JointProbabilityMass;

public class testJointProbabilityMass 
{
	private double TOL = 1e-8;
	

	@Test
	public void testGrid1() 
	{
		GridGenerator generator = new GridGenerator(3, 3);
		generator.setSeed(0);
		generator.buildProximityGraph();
		Graph grid = generator.getGrid();
				
		JointProbabilityMass jpmf = new JointProbabilityMass( grid );
		jpmf.setNumberOfSamples(1000000);
		jpmf.setNumberOfBombsToThrow(1);
		
		IBombingStrategy b = new FocusedBombingStrategy( grid, 1., 1., 1.);
		List<IBombingStrategy> bombingStrategies = new LinkedList<IBombingStrategy>();
		bombingStrategies.add(b);
		
		List<Double> probDistribution = new LinkedList<Double>();
		probDistribution.add(1.);
		
		jpmf.setBombs(bombingStrategies, probDistribution);
		jpmf.update();
		
		List<Integer> bundle = new LinkedList<Integer>();
		bundle.add(1);
		bundle.add(2);
		bundle.add(3);
		bundle.add(4);
		bundle.add(5);
		bundle.add(7);
		
		double prob = jpmf.getMarginalProbability(bundle, null, null);
		System.out.println("p="+prob);
		assertTrue(Math.abs( prob - 0.11) < 1e-2);
	}
	
	/*
	 * jmpf for the LLG domain
	 */
	@Test
	public void testGridLLG() 
	{
		GridGenerator generator = new GridGenerator(1, 2);
		generator.setSeed(0);
		generator.buildProximityGraph();
		Graph grid = generator.getGrid();
		
		//System.out.println("Grid: \n" + grid.toString());
		
		JointProbabilityMass jpmf = new JointProbabilityMass( grid );
		jpmf.setNumberOfSamples(1000000);
		jpmf.setNumberOfBombsToThrow(1);
		
		IBombingStrategy b = new FocusedBombingStrategy( grid, 1., 1., 0.5);
		List<IBombingStrategy> bombingStrategies = new LinkedList<IBombingStrategy>();
		bombingStrategies.add(b);
		
		List<Double> probDistribution = new LinkedList<Double>();
		probDistribution.add(1.0);
		
		jpmf.setBombs( bombingStrategies, probDistribution );
		jpmf.update();
		
		List<Integer> bundle = new LinkedList<Integer>();
		bundle.add(1);
		
		double prob = jpmf.getMarginalProbability(bundle, null, null);
		//System.out.println("p="+prob);
		assertTrue(Math.abs( prob - 0.25) < 1e-2);
		
		prob = jpmf.getMarginalProbability(bundle, null, null);
		assertTrue(Math.abs( prob - 0.25) < 1e-2);
		
		bundle.add(2);
		prob = jpmf.getMarginalProbability(bundle, null, null);
		assertTrue(Math.abs( prob - 0.) < 1e-2);
	} 
	
	/*
	 * jmpf for the LLG domain
	 */
	@Test
	public void testGridConditionalLLG() 
	{
		GridGenerator generator = new GridGenerator(1, 2);
		generator.setSeed(0);
		generator.buildProximityGraph();
		Graph grid = generator.getGrid(); 
		
		//System.out.println("Grid: \n" + grid.toString());
		
		JointProbabilityMass jpmf = new JointProbabilityMass( grid );
		jpmf.setNumberOfSamples(1000);
		jpmf.setNumberOfBombsToThrow(1);
		
		IBombingStrategy b = new FocusedBombingStrategy(grid, 1., 0.3, 0.2);
		List<IBombingStrategy> bombs = new LinkedList<IBombingStrategy>();
		bombs.add(b);
		
		List<Double> probDistribution = new LinkedList<Double>();
		probDistribution.add(1.);
		
		jpmf.setBombs(bombs, probDistribution);
		jpmf.update();
		
		List<Integer> bundle = new LinkedList<Integer>();
		bundle.add(1);
		
		double prob = jpmf.getMarginalProbability(bundle, null, null);
		//System.out.println("p="+prob);
		assertTrue(Math.abs( prob - 0.75) < 1e-2);
		
		List<Integer> allocatedGoods = new LinkedList<Integer>();
		List<Double> realizedRVs = new LinkedList<Double>();
		allocatedGoods.add(2);
		realizedRVs.add(0.7);
		
		prob = jpmf.getMarginalProbability(bundle, allocatedGoods, realizedRVs);
		assertTrue(Math.abs( prob - 0.8) < 1e-2);
		
		realizedRVs.clear();
		realizedRVs.add(0.8);
		prob = jpmf.getMarginalProbability(bundle, allocatedGoods, realizedRVs);
		assertTrue(Math.abs( prob - 0.7) < 1e-2);
		
		bundle.add(2);
		prob = jpmf.getMarginalProbability(bundle, null, null);
		assertTrue(Math.abs( prob - 0.7) < 1e-2);
		
		allocatedGoods.clear();
		allocatedGoods.add(1);
		realizedRVs.clear();
		realizedRVs.add(0.7);
		prob = jpmf.getMarginalProbability(bundle, allocatedGoods, realizedRVs);
		assertTrue( Math.abs( prob - 0.7) < 1e-2 );
		
		allocatedGoods.add(2);
		realizedRVs.add(0.8);
		prob = jpmf.getMarginalProbability(bundle, allocatedGoods, realizedRVs);
		assertTrue( Math.abs( prob - 0.7) < 1e-2 );
	}
	
	/*
	 * jmpf for the LLG domain
	 */
	@Test
	public void testGridConditionalLLG_TwoBombs() 
	{
		GridGenerator generator = new GridGenerator(1, 2);
		generator.setSeed(0);
		generator.buildProximityGraph();
		Graph grid = generator.getGrid(); 
		
		//System.out.println("Grid: \n" + grid.toString());
		
		JointProbabilityMass jpmf = new JointProbabilityMass( grid );
		jpmf.setNumberOfSamples(1000);
		jpmf.setNumberOfBombsToThrow(1);
		
		IBombingStrategy b1 = new FocusedBombingStrategy(grid, 1., 0.3, 0.2);
		IBombingStrategy b2 = new FocusedBombingStrategy(grid, 1., 0.4, 0.3);
		List<IBombingStrategy> bombs = new LinkedList<IBombingStrategy>();
		bombs.add(b1);
		bombs.add(b2);
		
		List<Double> probDistribution = new LinkedList<Double>();
		probDistribution.add(0.5);
		probDistribution.add(0.5);
		
		jpmf.setBombs(bombs, probDistribution);
		jpmf.update();
		
		List<Integer> bundle = new LinkedList<Integer>();
		bundle.add(1);
		
		double prob = jpmf.getMarginalProbability(bundle, null, null);
		//System.out.println("p="+prob);
		assertTrue(Math.abs( prob - 0.7) < 1e-2);
		
		List<Integer> allocatedGoods = new LinkedList<Integer>();
		List<Double> realizedRVs = new LinkedList<Double>();
		allocatedGoods.add(2);
		realizedRVs.add(0.7);
		
		prob = jpmf.getMarginalProbability(bundle, allocatedGoods, realizedRVs);
		System.out.println("prob="+prob);
		assertTrue(Math.abs( prob - 0.7) < 1e-2);
		
		realizedRVs.clear();
		realizedRVs.add(0.6);
		assertTrue(Math.abs( prob - 0.7) < 1e-2);
		
		realizedRVs.clear();
		realizedRVs.add(0.8);
		prob = jpmf.getMarginalProbability(bundle, allocatedGoods, realizedRVs);
		assertTrue(Math.abs( prob - 0.7) < 1e-2);
		
		bundle.add(2);
		prob = jpmf.getMarginalProbability(bundle, null, null);
		assertTrue(Math.abs( prob - 0.65) < 1e-2);
		
		allocatedGoods.clear();
		allocatedGoods.add(1);
		realizedRVs.clear();
		realizedRVs.add(0.7);
		prob = jpmf.getMarginalProbability(bundle, allocatedGoods, realizedRVs);
		assertTrue( Math.abs( prob - 0.65) < 1e-2 );
		
		allocatedGoods.add(2);
		realizedRVs.add(0.8);
		prob = jpmf.getMarginalProbability(bundle, allocatedGoods, realizedRVs);
		assertTrue( Math.abs( prob - 0.7) < 1e-2 );
	}
}
