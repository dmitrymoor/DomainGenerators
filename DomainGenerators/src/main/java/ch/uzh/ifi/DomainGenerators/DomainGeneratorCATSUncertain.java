package ch.uzh.ifi.DomainGenerators;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ch.uzh.ifi.MechanismDesignPrimitives.FocusedBombingStrategy;
import ch.uzh.ifi.MechanismDesignPrimitives.IBombingStrategy;
import ch.uzh.ifi.MechanismDesignPrimitives.JointProbabilityMass;

/**
 * Domain generator for CATS "regions" domains with uncertain availability of goods.
 * @author Dmitry Moor
 */
public class DomainGeneratorCATSUncertain extends DomainGeneratorCATS
{

	/***
	 * Constructor
	 * @param numberOfGoods number of goods in the auction
	 */
	public DomainGeneratorCATSUncertain(int numberOfGoods) 
	{
		super(numberOfGoods);
		generateJPMF();
		_primaryReductionCoef = null;
		_secondaryReductionCoef = null;
		_probabilityOfBeingChosen = null;
		_probabilityOfExplosion = null;
	}

	/**
	 * The method generates a joint probability mass function.
	 */
	private void generateJPMF()
	{
		if( _primaryReductionCoef == null) 		throw new RuntimeException("Primary reduction coefficients are not specified");
		if( _secondaryReductionCoef == null) 	throw new RuntimeException("Secondary reduction coefficients are not specified");
		if( _probabilityOfBeingChosen == null) 	throw new RuntimeException("Probabilities of being chosen are not specified");
		if( _probabilityOfExplosion == null) 	throw new RuntimeException("Probabilities of explosion are not specified");
		
		_jpmf = new JointProbabilityMass( _grid );
		_jpmf.setNumberOfSamples(_numberOfJPMFSamples);
		_jpmf.setNumberOfBombsToThrow(_numberOfBombsToThrow);
		
		List<IBombingStrategy> bombs = IntStream.range(0, _probabilityOfBeingChosen.size()).boxed().parallel().map( i -> new FocusedBombingStrategy( _grid, _probabilityOfExplosion.get(i), _primaryReductionCoef.get(i), _secondaryReductionCoef.get(i))).collect(Collectors.toList());
		_jpmf.setBombs(bombs, _probabilityOfBeingChosen);
		_jpmf.update();
	}
	
	/**
	 * The method sets up the number of sample to be used by the JPMF generator.
	 * @param numberOfJPMFSamples
	 */
	public void setNumberOfJPMFSamples(int numberOfJPMFSamples)
	{
		_numberOfJPMFSamples = numberOfJPMFSamples;
	}
	
	/**
	 * The method sets up the number of bombs to be thrown by the JPMF sampling procedure.
	 * @param numberOfBombsToThrow the number of bombs to thrown by JPMF sampling
	 */
	public void setNumberOfBombsToThrow(int numberOfBombsToThrow)
	{
		_numberOfBombsToThrow = numberOfBombsToThrow;
	}
	
	/**
	 * The method sets up parameters for focused bombing strategies.
	 * @param primaryReductionCoef primary reduction coefficients
	 * @param secondaryReductionCoef secondary reduction coefficients
	 * @param probabilityOfBeingChosen probability of a particular bomb to be chosen
	 * @param probabilityOfExplosion probability that a bomb explodes
	 */
	public void setBombsParameters(List<Double> primaryReductionCoef, List<Double> secondaryReductionCoef, List<Double> probabilityOfBeingChosen, List<Double> probabilityOfExplosion)
	{
		if( (primaryReductionCoef.size() != secondaryReductionCoef.size()) || (primaryReductionCoef.size() != probabilityOfBeingChosen.size()) || (primaryReductionCoef.size() != probabilityOfExplosion.size()) )
			throw new RuntimeException("Dimensionality mismatch");
		
		_primaryReductionCoef = primaryReductionCoef;
		_secondaryReductionCoef = secondaryReductionCoef;
		_probabilityOfBeingChosen = probabilityOfBeingChosen;
		_probabilityOfExplosion = probabilityOfExplosion;
	}
	
	protected JointProbabilityMass _jpmf;
	protected int _numberOfJPMFSamples = 10000;
	protected int _numberOfBombsToThrow = 1;
	protected List<Double> _primaryReductionCoef;
	protected List<Double> _secondaryReductionCoef;
	protected List<Double> _probabilityOfBeingChosen;
	protected List<Double> _probabilityOfExplosion;
}
