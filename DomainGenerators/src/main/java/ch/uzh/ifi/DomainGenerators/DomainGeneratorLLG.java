package ch.uzh.ifi.DomainGenerators;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import ch.uzh.ifi.MechanismDesignPrimitives.AtomicBid;
import ch.uzh.ifi.MechanismDesignPrimitives.CombinatorialType;
import ch.uzh.ifi.MechanismDesignPrimitives.IDomainGenerator;
import ch.uzh.ifi.MechanismDesignPrimitives.Type;

/**
 * The class implements a domain generator for the LLG domain.
 * @author Dmitry Moor
 *
 */
public class DomainGeneratorLLG implements IDomainGenerator
{

	/**
	 * A simple constructor.
	 */
	public DomainGeneratorLLG()
	{
		_atoms = new LinkedList<AtomicBid>();
	}
	
	@Override
	public Type generateBid(long seed, Type type) 
	{
		assert(type.getNumberOfAtoms() == 1);										//One local or one global bid
		
		Type ct = new CombinatorialType();
		Random generator = new Random(seed);
		generator.setSeed(seed);
		
		//1. Deep copying of all atoms of _agentsTypes[j] followed by reseting a value of this type
		for(int atomIdx = 0; atomIdx < type.getNumberOfAtoms(); ++atomIdx)
		{
			AtomicBid atom = type.getAtom(atomIdx).copyIt();
			
			if( (atom.getAgentId() == 1) || (atom.getAgentId() == 2) )					//If this is a type of a local bidder
			{
				double newValue =  (Double)atom.getTypeComponent(AtomicBid.MinValue) + 
				                  ((Double)atom.getTypeComponent(AtomicBid.MaxValue) - (Double)atom.getTypeComponent(AtomicBid.MinValue) )*generator.nextDouble();
				if( newValue < 0) throw new RuntimeException("Negative value");
				
				atom.setTypeComponent( AtomicBid.Value, newValue);
				//_atoms.get(1).setTypeComponent( AtomicBid.Value, newValue);
			}
			else if (atom.getAgentId() == 3)											//If this is a type of a global bidder
			{
				double newValue =  (Double)atom.getTypeComponent(AtomicBid.MinValue) + 
		                  		  ((Double)atom.getTypeComponent(AtomicBid.MaxValue) - (Double)atom.getTypeComponent(AtomicBid.MinValue) )*generator.nextDouble();
				if( newValue < 0) throw new RuntimeException("Negative value");
				
				atom.setTypeComponent( AtomicBid.Value, newValue);
			}
			else throw new RuntimeException("Too many agents for LLG domain.");
			
			ct.addAtomicBid(atom);
		}
		
		//2. Now reset the type using a specific domain
		//ct.resetType(new DomainGeneratorLLG(),  seed );
		return ct;
	}
	
	private List<AtomicBid> _atoms;						//Atoms to be generated in the LLG domain
}