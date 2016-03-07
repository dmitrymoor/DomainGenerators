package ch.uzh.ifi.DomainGenerators;

import java.util.Arrays;
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
		
	}
	
	/**
	 * (non-Javadoc)
	 * @see ch.uzh.ifi.MechanismDesignPrimitives.IDomainGenerator#generateBid(long, int)
	 */
	@Override
	public Type generateBid(long seed, int agentId) 
	{		
		Random generator = new Random(seed);
		generator.setSeed(seed);
		
		AtomicBid atom;

		if( agentId == 1 )													//If this is a type of a local bidder
		{
			double newValue =  generator.nextDouble();				
			atom = new AtomicBid(agentId, Arrays.asList(1), newValue);
		}
		else if ( agentId == 2 )											//If this is a type of a local bidder
		{
			double newValue =  generator.nextDouble();				
			atom = new AtomicBid(agentId, Arrays.asList(2), newValue);
		}
		else if ( agentId == 3 )											//If this is a type of a global bidder
		{
			double newValue =  2*generator.nextDouble();
			atom = new AtomicBid(agentId, Arrays.asList(1, 2), newValue);
		}
		else throw new RuntimeException("Too many agents for LLG domain.");

		return new CombinatorialType(atom);
	}	
}