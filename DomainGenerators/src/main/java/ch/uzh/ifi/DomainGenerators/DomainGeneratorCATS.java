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
 * @author Dmitry
 *
 */
public class DomainGeneratorCATS implements IDomainGenerator
{

	/**
	 * A simple constructor.
	 * @param numberOfGoods the number of goods in the auction
	 * @param numberOfAtoms 
	 * @param grid the spacial proximity graph
	 */
	public DomainGeneratorCATS(int numberOfGoods, Graph grid)
	{
		_numberOfGoods = numberOfGoods;
		_grid = grid;
	}

	/**
	 * (non-Javadoc)
	 * @see ch.uzh.ifi.MechanismDesignPrimitives.IDomainGenerator#generateBid(long, ch.uzh.ifi.MechanismDesignPrimitives.Type)
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
		if(bid.size() == 0)
			throw new RuntimeException("No atoms produced");
	
		Type ct = new CombinatorialType();
		bid.stream().parallel().forEach( i -> ct.addAtomicBid(i) );
		return ct;
	}
	
	private int _numberOfGoods;	
	private Graph _grid;
	private SpatialDomainGenerator _spatialDomainGenerator;
}
