package ch.uzh.ifi.DomainGenerators;

import java.util.Random;

import ch.uzh.ifi.GraphAlgorithms.Graph;
import ch.uzh.ifi.GraphAlgorithms.Mesh2D;

/**
 * The class provides interfaces for generating a rectangular grid (mesh) augmented
 * with some diagonal edges.
 * @author Dmitry Moor
 *
 */
public class GridGenerator 
{

	/**
	 * Constructor
	 * @param numberOfRows the number of rows of the grid
	 * @param numberOfColumns the number of columns of the grid
	 */
	public GridGenerator(int numberOfRows, int numberOfColumns)
	{
		_numberOfRows = numberOfRows;
		_numberOfColumns = numberOfColumns;
		
		_seed = System.nanoTime();
		_graph = new Mesh2D(_numberOfColumns, _numberOfRows);
	}
	
	/**
	 * The method sets the random seed
	 * @param seed - seed
	 */
	public void setSeed(long seed)
	{
		_seed = seed;
	}
	
	/**
	 * The method generates the proximity graph, i.e., a mesh augmented with
	 * some diagonal links (see K. Leyton-Brown et. al.).
	 */
	public void buildProximityGraph()
	{
		Random generator = new Random(_seed);
		
		for(int n = 0; n < _numberOfRows * _numberOfColumns; ++n)
		{
			if( ! ( ( (n+1) % _numberOfColumns == 0) || ( (n+1) % _numberOfColumns == 1) || ( (n+1) / _numberOfColumns == 0) ||  ( n / _numberOfColumns == _numberOfRows - 1 ) ) )
			{
				if( generator.nextDouble() <= _threeProb)
				{
					int numberOfAdjacentNeighs = _graph.getAdjacencyLists().get(n).size();
					int edgeToRemove = (int) (generator.nextDouble() * numberOfAdjacentNeighs);
					_graph.removeEdge( n + 1, edgeToRemove);
				}
				
				int numberOfDNeigh = 0;
				while( (generator.nextDouble() <= _additionalNeigh) && (numberOfDNeigh < 4) )
				{
					int newDNeigh = 0;
					
					if(numberOfDNeigh == 0)
						newDNeigh = (n+1) - _numberOfColumns - 1;
					else if(numberOfDNeigh == 1)
						newDNeigh = (n+1) - _numberOfColumns + 1;
					else if(numberOfDNeigh == 2)
						newDNeigh = (n+1) + _numberOfColumns + 1;
					else if(numberOfDNeigh == 3)
						newDNeigh = (n+1) + _numberOfColumns - 1;
					
					_graph.addEdge( (n+1), newDNeigh);
					numberOfDNeigh += 1;
				}
			}
		}
	}
	
	/**
	 * The method returns the generated proximity graph.
	 * @return the proximity graph
	 */
	public Graph getGrid()
	{
		return _graph;
	}
	
	private int _numberOfRows;							//Number of rows of the mesh
	private int _numberOfColumns;						//Number of columns of the mesh
	private Graph _graph;								//The proximity graph
	
	private long _seed;									//Random seed
	private double _threeProb = 1.0;					//Probability of removing an edge adjacent to a particular vertex
	private double _additionalNeigh = 0.2;				//Probability of adding an additional neighbor
}
