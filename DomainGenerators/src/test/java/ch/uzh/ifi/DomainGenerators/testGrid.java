package ch.uzh.ifi.DomainGenerators;

import org.junit.After;
import org.junit.Test;

import ch.uzh.ifi.GraphAlgorithms.Graph;

public class testGrid {

	@After
	public void tearDown() throws Exception 
	{
		
	}

	/*
	 * 
	 */
	@Test
	public void test()
	{
		GridGenerator generator = new GridGenerator(4, 5);
		generator.buildProximityGraph();
		Graph grid = generator.getGrid();
		System.out.println("Grid: \n" + grid.toString());
	}
}
