package ch.uzh.ifi.DomainGenerators;

import java.util.List;

import ch.uzh.ifi.MechanismDesignPrimitives.AtomicBid;
import ch.uzh.ifi.MechanismDesignPrimitives.CombinatorialType;
import ch.uzh.ifi.MechanismDesignPrimitives.IDomainGenerator;
import ch.uzh.ifi.MechanismDesignPrimitives.Type;
import ch.uzh.ifi.DomainGenerators.SpatialDomainGenerator;
import ch.uzh.ifi.GraphAlgorithms.Graph;

/**
 * The class generates bids using CATS application.
 * @author Dmitry Moor
 */
public class DomainGeneratorCATS implements IDomainGenerator
{
	/**
	 * A simple constructor.
	 * @param numberOfGoods the number of goods in the auction
	 * @param grid the spatial proximity graph
	 */
	public DomainGeneratorCATS(int numberOfGoods)
	{												//TODO: jpmf should be a part of the domain (perhaps another class DomainGeneratorCATSUncertain )
		_numberOfGoods = numberOfGoods;
		generateGrid();
	}

	/**
	 * (non-Javadoc)
	 * @see ch.uzh.ifi.MechanismDesignPrimitives.IDomainGenerator#generateBid(long, int)
	 */
	@Override
	public Type generateBid(long seed, int agentId)
	{
		_spatialDomainGenerator = new SpatialDomainGenerator(_numberOfGoods, _grid, agentId);
		try
		{
			_spatialDomainGenerator.generateSpacialBids(seed);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		List<AtomicBid> bid = _spatialDomainGenerator.getBid();
		if(bid.size() == 0)  throw new RuntimeException("No atoms produced");

		Type ct = new CombinatorialType();
		bid.stream().parallel().forEach( i -> ct.addAtomicBid(i) );
		return ct;
	}
	
	/**
	 * The method generates a rectangular spatial proximity graph (grid)
	 */
	private void generateGrid()
	{
		int nRows = (int)Math.round( Math.sqrt(_numberOfGoods));
		int nCols = _numberOfGoods / nRows;
		
		if( nRows * nCols != _numberOfGoods ) throw new RuntimeException("Error when computing grid dimensions: nRows=" + nRows + " nCols="+nCols);
		
		GridGenerator gridGenerator = new GridGenerator(nRows, nCols);
		gridGenerator.setSeed(0);
		gridGenerator.buildProximityGraph();
		_grid = gridGenerator.getGrid();
	}
	
	protected int _numberOfGoods;										//Number of goods in the auction
	protected Graph _grid;												//Spatial proximity graph
	protected SpatialDomainGenerator _spatialDomainGenerator;			//Spatial domain generator
}
