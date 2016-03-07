package ch.uzh.ifi.DomainGenerators;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

import ch.uzh.ifi.GraphAlgorithms.Graph;
import ch.uzh.ifi.MechanismDesignPrimitives.AtomicBid;
import ch.uzh.ifi.MechanismDesignPrimitives.CombinatorialType;
import ch.uzh.ifi.DomainGenerators.DomainGeneratorCATS;
import ch.uzh.ifi.DomainGenerators.DomainGeneratorLLG;
import ch.uzh.ifi.MechanismDesignPrimitives.IDomainGenerator;
import ch.uzh.ifi.MechanismDesignPrimitives.Type;

public class testDomainGeneratorInterfaces 
{
	/*
	 * Test for the LLG domain generator
	 */
	@Test
	public void testDomainLLG()
	{
		List<Integer> items = new LinkedList<Integer>();
		items.add(1);
		items.add(2);
		
		double marginalValueL1 = 0.1;
		//Local bidder
		List<Integer> bundle = new LinkedList<Integer>();
		bundle.add( items.get(0) );
		AtomicBid atom11 = new AtomicBid(1, bundle, marginalValueL1);
		atom11.setTypeComponent( AtomicBid.IsBidder, 1.0);
		atom11.setTypeComponent( AtomicBid.MinValue, 0.0);
		atom11.setTypeComponent( AtomicBid.MaxValue, 1.0);
		
		CombinatorialType t1 = new CombinatorialType();
		t1.addAtomicBid(atom11);
		
		IDomainGenerator llgDomain = new DomainGeneratorLLG();
		long seed = 1234*234;
		Type ct1 = llgDomain.generateBid(seed, 1);
		Type ct2 = llgDomain.generateBid(seed, 2);
		
		assertTrue(ct1.getNumberOfAtoms() == 1);
		assertTrue(ct2.getNumberOfAtoms() == 1);
		assertTrue(ct1.getAtom(0).getValue() == ct2.getAtom(0).getValue());
		
		for(int i = 0; i < 1000; ++i)
		{
			Type ct = llgDomain.generateBid(seed + 10*i, 1);
			assertTrue( ct.getNumberOfAtoms() == 1);
			assertTrue( ct.getAtom(0).getValue() >= 0);
			assertTrue( ct.getAtom(0).getValue() <= 1);
		}
	}

	/**
	 * Test of a CATS domain generator.
	 */
	@Test
	public void testDomainCATS()
	{
		int numberOfAgents = 6;
		int numberOfGoods = 4;
		List<Integer> items = IntStream.range(0, numberOfGoods).boxed().parallel().map(i -> i+1).collect(Collectors.toList());
		
		double marginalValueL1 = 0.1;
		//Local bidder
		AtomicBid atom11 = new AtomicBid(1, Arrays.asList( items.get(0) ), marginalValueL1);
		CombinatorialType t = new CombinatorialType( atom11 );
		
		GridGenerator generator = new GridGenerator(2, 2);
		generator.setSeed(0);
		generator.buildProximityGraph();
		Graph grid = generator.getGrid();
		
		IDomainGenerator catsDomainGenerator = new DomainGeneratorCATS(numberOfGoods, grid);
		
		Type[] ct = new Type[numberOfAgents];
		for(int i = 0; i < numberOfAgents; ++i)
			ct[i] = catsDomainGenerator.generateBid(10*i, i+1);
//		IntStream.range(0, numberOfAgents).boxed().parallel().forEach( i -> ct[i] = catsDomainGenerator.generateBid(10*i, i+1) );
		IntStream.range(0, numberOfAgents).boxed().parallel().forEach( i -> System.out.println(ct[i]));
	}
}
