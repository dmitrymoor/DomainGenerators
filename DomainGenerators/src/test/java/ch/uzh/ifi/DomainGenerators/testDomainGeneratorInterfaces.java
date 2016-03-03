package ch.uzh.ifi.DomainGenerators;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

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
		Type ct1 = llgDomain.generateBid(seed, t1);
		Type ct2 = llgDomain.generateBid(seed, t1);
		
		assertTrue(ct1.getNumberOfAtoms() == 1);
		assertTrue(ct2.getNumberOfAtoms() == 1);
		assertTrue(ct1.getAtom(0).getValue() == ct2.getAtom(0).getValue());
		
		for(int i = 0; i < 1000; ++i)
		{
			Type ct = llgDomain.generateBid(seed + 10*i, t1);
			assertTrue( ct.getNumberOfAtoms() == 1);
			assertTrue( ct.getAtom(0).getValue() >= 0);
			assertTrue( ct.getAtom(0).getValue() <= 1);
		}
	}

	/*
	 * Test for CATS domain generator
	 */
	@Test
	public void testDomainCATS()
	{
		int numberOfGoods = 4;
		int numberOfAtoms = 3;
		List<Integer> items = new LinkedList<Integer>();
		items.add(1);
		items.add(2);
		items.add(3);
		items.add(4);
		
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
		
		
		GridGenerator generator = new GridGenerator(2, 2);
		generator.setSeed(0);
		generator.buildProximityGraph();
		Graph grid = generator.getGrid();
		
		IDomainGenerator catsDomain = new DomainGeneratorCATS(numberOfGoods, numberOfAtoms, grid);
		
		long seed = 1234*234;
		Type ct = new CombinatorialType();
		
		ct = catsDomain.generateBid(seed, t1);
		
		for(int i = 0; i <= 1000; ++i)
		{
			ct = catsDomain.generateBid(seed + 10*i, t1);
			assertTrue(ct.getNumberOfAtoms() >= 3);
			for(int j = 0; j < ct.getNumberOfAtoms(); ++j)
			{
				assertTrue( ct.getAtom(j).getValue() >= 0 );
				for(int k = 0; k < j; ++k)
				{
					if( ct.getAtom(k).getInterestingSet().containsAll(  ct.getAtom(j).getInterestingSet() ) )
					{
						//System.out.println("-> " + ct.toString());
						assertTrue( ct.getAtom(k).getValue() >= ct.getAtom(j).getValue() );
					}
					else if( ct.getAtom(j).getInterestingSet().containsAll(  ct.getAtom(k).getInterestingSet() ) )
					{
						//System.out.println(">> " + ct.toString() + " i="+i);
						assertTrue( ct.getAtom(j).getValue() >= ct.getAtom(k).getValue() );
					}
				}
			}
		}
		//System.out.println("CT: " + ct.toString());
	}
}
