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

	/*
	 * A simple constructor.
	 * @param numberOfGoods - the number of goods in the auction
	 */
	public DomainGeneratorCATS(int numberOfGoods, int numberOfAtoms, Graph grid)
	{
		_numberOfGoods = numberOfGoods;
		_numberOfAtoms = numberOfAtoms;		
		_grid = grid;
	}

	/*
	 * (non-Javadoc)
	 * @see Mechanisms.IDomainGenerator#generateBid(long, Mechanisms.Type)
	 */
	@Override
	public Type generateBid(long seed, Type type) 
	{
		Type ct = new CombinatorialType();
		_spatialDomainGenerator = new SpatialDomainGenerator(_numberOfGoods, _numberOfAtoms, _grid, type.getAgentId());
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
		
		for(int atomIdx = 0; atomIdx < bid.size(); ++atomIdx)
		{
			AtomicBid atom = type.getAtom(0).copyIt();						//Keep all other type components same except of the bundle/value
			atom.setInterestingSet(bid.get(atomIdx).getInterestingSet());
			atom.setValue(bid.get(atomIdx).getValue());
			ct.addAtomicBid(atom);
		}
		return ct;
	}
	
	private int _numberOfGoods;
	private int _numberOfAtoms;
	private List<AtomicBid> _atoms;						//Atoms to be generated in the LLG domain
	
	private Graph _grid;
	private SpatialDomainGenerator _spatialDomainGenerator;
}
