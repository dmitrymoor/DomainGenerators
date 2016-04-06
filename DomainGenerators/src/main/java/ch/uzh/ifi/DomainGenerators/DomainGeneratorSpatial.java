package ch.uzh.ifi.DomainGenerators;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.uzh.ifi.MechanismDesignPrimitives.AtomicBid;
import ch.uzh.ifi.MechanismDesignPrimitives.CombinatorialType;
import ch.uzh.ifi.MechanismDesignPrimitives.IDomainGenerator;
import ch.uzh.ifi.MechanismDesignPrimitives.Type;
import ch.uzh.ifi.DomainGenerators.SpatialDomainGenerator;
import ch.uzh.ifi.GraphAlgorithms.Graph;

/**
 * The class generates bids using same approach as in CATS "regions".
 * @author Dmitry Moor
 */
public class DomainGeneratorSpatial implements IDomainGenerator
{
	
	private static final Logger _logger = LogManager.getLogger(DomainGeneratorSpatial.class);
	
	/**
	 * A simple constructor.
	 * @param numberOfGoods the number of goods in the auction
	 * @throws SpacialDomainGenerationException if cannot create a square grid with the specified number of goods
	 */
	public DomainGeneratorSpatial(int numberOfGoods) throws SpacialDomainGenerationException
	{
		_numberOfGoods = numberOfGoods;
		generateGrid();
		_spatialDomainGenerator = new SpatialDomainGenerator(_numberOfGoods, _grid);
	}

	/**
	 * (non-Javadoc)
	 * @see ch.uzh.ifi.MechanismDesignPrimitives.IDomainGenerator#generateBid(long, int)
	 */
	@Override
	public Type generateBid(long seed, int agentId)
	{
		//The seed is used to generate same private values
		try
		{
			_spatialDomainGenerator.generateSpacialBids(seed, agentId);
		}
		catch (SpacialDomainGenerationException e)
		{
			e.printStackTrace();
		}
		
		List<AtomicBid> bid = _spatialDomainGenerator.getBid();
		if(bid.size() == 0)  throw new RuntimeException("No atoms produced");

		Type ct = new CombinatorialType();
		bid.stream().forEach( i -> ct.addAtomicBid(i) );
		return ct;
	}
	
	/**
	 * The method generates a rectangular spatial proximity graph (grid)
	 * @throws SpacialDomainGenerationException if cannot create a square grid
	 */
	private void generateGrid() throws SpacialDomainGenerationException
	{
		int nRows = (int)Math.round( Math.sqrt(_numberOfGoods));
		int nCols = _numberOfGoods / nRows;
		
		if( nRows * nCols != _numberOfGoods ) throw new SpacialDomainGenerationException("Error when computing grid dimensions: nRows=" + nRows + " nCols="+nCols);
		
		GridGenerator gridGenerator = new GridGenerator(nRows, nCols);
		gridGenerator.setSeed(0);
		gridGenerator.buildProximityGraph();
		_grid = gridGenerator.getGrid();
	}
	
	protected int _numberOfGoods;										//Number of goods in the auction
	protected Graph _grid;												//Spatial proximity graph
	protected SpatialDomainGenerator _spatialDomainGenerator;			//Spatial domain generator
}
