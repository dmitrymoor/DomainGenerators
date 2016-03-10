package ch.uzh.ifi.DomainGenerators;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.uzh.ifi.MechanismDesignPrimitives.FocusedBombingStrategy;
import ch.uzh.ifi.MechanismDesignPrimitives.IBombingStrategy;
import ch.uzh.ifi.MechanismDesignPrimitives.JointProbabilityMass;

/**
 * Domain generator for CATS "regions" domains with uncertain availability of goods.
 * @author Dmitry Moor
 */
public class DomainGeneratorCATSUncertain extends DomainGeneratorCATS
{
	
	private static final Logger _logger = LogManager.getLogger(DomainGeneratorCATSUncertain.class);
	
	/***
	 * Constructor
	 * @param numberOfGoods number of goods in the auction
	 */
	public DomainGeneratorCATSUncertain(int numberOfGoods) 
	{
		super(numberOfGoods);
		_logger.debug("DomainGeneratorCATSUncertain(numberOfGoods="+numberOfGoods+")");
		_primaryReductionCoef = null;
		_secondaryReductionCoef = null;
		_probabilityOfBeingChosen = null;
		_probabilityOfExplosion = null;
	}

	/**
	 * The method generates a joint probability mass function.
	 */
	public void generateJPMF()
	{
		_logger.debug("-> generateJPMF()");
		if( _primaryReductionCoef == null) 		throw new RuntimeException("Primary reduction coefficients are not specified");
		if( _secondaryReductionCoef == null) 	throw new RuntimeException("Secondary reduction coefficients are not specified");
		if( _probabilityOfBeingChosen == null) 	throw new RuntimeException("Probabilities of being chosen are not specified");
		if( _probabilityOfExplosion == null) 	throw new RuntimeException("Probabilities of explosion are not specified");
		
		List<IBombingStrategy> bombs = IntStream.range(0, _probabilityOfBeingChosen.size()).boxed().parallel().map( i -> new FocusedBombingStrategy( _grid, _probabilityOfExplosion.get(i), _primaryReductionCoef.get(i), _secondaryReductionCoef.get(i))).collect(Collectors.toList());
		_jpmf = new JointProbabilityMass( _grid );
		_jpmf.setNumberOfSamples(_numberOfJPMFSamples);
		_jpmf.setNumberOfBombsToThrow(_numberOfBombsToThrow);
		_jpmf.setBombs(bombs, _probabilityOfBeingChosen);
		_jpmf.update();
		_logger.debug("<- generateJPMF()");
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
		if( Math.abs( probabilityOfBeingChosen.stream().reduce( (x1, x2) -> x1 + x2).get() - 1.) > 1e-6 )
			throw new RuntimeException("The probability distribution must be normalized");
		
		_primaryReductionCoef = primaryReductionCoef;			_logger.debug("Primary reduction coef: " + _primaryReductionCoef.toString());
		_secondaryReductionCoef = secondaryReductionCoef;		_logger.debug("Secondary reduction coef: " + _secondaryReductionCoef.toString());
		_probabilityOfBeingChosen = probabilityOfBeingChosen;	_logger.debug("Probability of being chosen: " + _probabilityOfBeingChosen.toString());
		_probabilityOfExplosion = probabilityOfExplosion;		_logger.debug("Probability of explosion: " + _probabilityOfExplosion.toString());
	}
	
	/**
	 * The method returns the jpmf generated for the domain.
	 * @return the jpmf
	 */
	public JointProbabilityMass getJPMF()
	{
		return _jpmf;
	}
	
	protected JointProbabilityMass _jpmf;					//Joint probability mass function
	protected int _numberOfJPMFSamples = 10000;				//The number of samples to be used for generating of the jpmf
	protected int _numberOfBombsToThrow = 1;				//The number of bombs to throw when sampling the jpmf
	protected List<Double> _primaryReductionCoef;			//Primary reduction coefficients of the focused bombing strategy
	protected List<Double> _secondaryReductionCoef;			//Secondary reduction coefficients of the focused bombing strategy
	protected List<Double> _probabilityOfBeingChosen;		//List of probabilities of different bombs too be chosen
	protected List<Double> _probabilityOfExplosion;			//List of probabilities of each particular bomb to explode
}
