package ch.uzh.ifi.DomainGenerators;

import ch.uzh.ifi.GraphAlgorithms.Graph;
import ch.uzh.ifi.MechanismDesignPrimitives.AtomicBid;
import ch.uzh.ifi.MechanismDesignPrimitives.JointProbabilityMass;

/*
 * The class implements a spatial domain generation with availabilies uncertainty.
 */
public class SpatialUncertainDomainGenerator extends SpatialDomainGenerator
{

	/*
	 * 
	 */
	public SpatialUncertainDomainGenerator(int numberOfGoods, int numberOfBids, Graph dependencyGraph, int agentId)
	{
		if( numberOfGoods != dependencyGraph.getVertices().size() )	throw new RuntimeException("Number of vertices mismatch");
			
		_dependencyGraph = dependencyGraph;
		_jpmf = new JointProbabilityMass( _dependencyGraph);
		_jpmf.setNumberOfSamples(_numberOfSamples);
		_jpmf.setNumberOfBombsToThrow(_numberOfBombs);
		_jpmf.update();
	}
	
	public void setNumberOfSamples(int numberOfSamples)
	{
		_numberOfSamples = numberOfSamples;
		_jpmf.update();
	}
	
	public void setNumberOfBombs(int numberOfBombs)
	{
		_numberOfBombs = numberOfBombs;
		_jpmf.update();
	}
	
	public void generateBids()
	{
		//generateSpacialBids();
	}
	
	/*
	 * The method returns a joint probability mass function.
	 * @return a joint probability mass function
	 */
	public JointProbabilityMass getJointProbMassFunction()
	{
		return _jpmf;
	}
	
	private Graph _dependencyGraph;
	private JointProbabilityMass _jpmf;
	private int _numberOfSamples = 1000000;
	private int _numberOfBombs = 1;
}
