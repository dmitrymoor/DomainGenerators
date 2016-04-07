package ch.uzh.ifi.DomainGenerators;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.uzh.ifi.GraphAlgorithms.Graph;
import ch.uzh.ifi.GraphAlgorithms.VertexCell;
import ch.uzh.ifi.MechanismDesignPrimitives.AtomicBid;

/**
 * The class implements functionality for a spatial domain generator (similar to CATS "regions").
 * @author Dmitry Moor
 *
 */
public class SpatialDomainGenerator 
{
	
	private static final Logger _logger = LogManager.getLogger(SpatialDomainGenerator.class);
	
	/**
	 * A standard constructor.
	 */
	public SpatialDomainGenerator()
	{
		init();
	}
	
	/**
	 * Constructor.
	 * @param numberOfGoods - the number of goods in the auction
	 * @param dependencyGraph - the graph representing the spatial domain
	 */
	public SpatialDomainGenerator(int numberOfGoods, Graph dependencyGraph)
	{
		init();
		_numberOfGoods = numberOfGoods;
		_dependencyGraph = dependencyGraph;
			
		_commonValue = new ArrayList<Double>();						//Initialize common values for all goods
		for(int i = 0; i < _numberOfGoods; ++i)
		{
			_commonValue.add( _randGenerator.nextDouble() * _MAX_GOOD_VALUE);
			
			int redColor = (int) (255. * (_MAX_GOOD_VALUE-_commonValue.get(i) ) / _MAX_GOOD_VALUE);
			int greenColor = (int) (255. * _commonValue.get(i) / _MAX_GOOD_VALUE );
			int blueColor = 0;
			int rgbColor = redColor;
			rgbColor = (rgbColor << 8) + greenColor;
			rgbColor = (rgbColor << 8) + blueColor;
			_dependencyGraph.getVertices().get(i).setColor( rgbColor );
		}
	}
	
	/**
	 * Initialization
	 */
	private void init()
	{
		_bids = new LinkedList<AtomicBid>();
		_randGenerator = new Random(0);
	}
	
	/**
	 * The method returns a list of generated atoms.
	 * @return a list of atoms
	 */
	public List<AtomicBid> getBid()
	{
		return _bids;
	}
	
	/**
	 * The method generates the required number of atomic bids (bundles + values)
	 * @param seed a random seed
	 * @param agentId an id of an agent for which the bid should be generated
	 * @throws Exception if the newly generated bid already exists
	 */
	public void generateSpacialBids(long seed, int agentId) throws SpacialDomainGenerationException
	{
		_randGenerator.setSeed(seed);
		_bids = new LinkedList<AtomicBid>();
		
		//The list of  IDs of all goods (from 1 to _numberOfGoods)
		List<Integer> goods = IntStream.range(0, _numberOfGoods).boxed().map( x -> x+1 ).collect(Collectors.toList());
		
		//Private values of a bidder for each good
		List<Double> privateValue = IntStream.range(0, _numberOfGoods).boxed().map( x -> -1. * _MAX_GOOD_VALUE * _DEVIATION + _randGenerator.nextDouble() * 2. * _MAX_GOOD_VALUE * _DEVIATION).collect(Collectors.toList());
		
		//Weights ~ bidders preferences
		List<Double> pn = privateValue.stream().map( x -> 1. + (x - _MAX_GOOD_VALUE * _DEVIATION) / (2 * _MAX_GOOD_VALUE * _DEVIATION) ).collect(Collectors.toList());
		
		//Normalize weights
		double total = pn.stream().reduce( (p1, p2) -> p1 + p2 ).get();
		pn.replaceAll( p -> p/total );

		//Choose a good at random with a probability distribution pn and add it to the bundle
		List<Integer> bundle = new ArrayList<Integer>();
		bundle.add(pickGoodFromSet(goods, pn));
			
		while( (_randGenerator.nextDouble() <= _ADDITIONAL_LOCATION && bundle.size() < _numberOfGoods) || bundle.size() == 0 )//TODO use the seed
			addGoodToBundle(bundle, pn, _dependencyGraph);
			
		double value = computeValue(bundle, privateValue);
		if(value < 0) throw new SpacialDomainGenerationException("Negative value for the bundle " + bundle.toString());
		
		_logger.debug("Bundle: " + bundle.toString() + ". Value: " + value + ".");
		
		if( ! isExists(bundle) )
		{
			bundle.sort(null);
			_bids.add( constructAtom( bundle, value, agentId) );
		}
		else throw new SpacialDomainGenerationException("This bundle already exists");
			
		//2. Construct substitutable bids
		double budget = value * _BUDGET_FACTOR;		
		double totalCommonValue = bundle.stream().map( goodId -> _commonValue.get(goodId-1)).reduce( (x1, x2) -> x1 + x2).get();
		double minResaleValue = _RESALE_FACTOR * totalCommonValue;
		
		List<List<Integer> > substitutableBids = new LinkedList<List<Integer> >();
		List<Double> commonValues = new LinkedList<Double>();
		for(int i = 0; i < bundle.size(); ++i)
		{
			List<Integer> bundleI = new ArrayList<Integer>();
			bundleI.add( bundle.get(i));
				
			while(bundleI.size() < bundle.size())
				addGoodToBundle(bundleI, pn, _dependencyGraph);
				
			double totalCommonValueI = bundleI.stream().map( gId -> _commonValue.get( gId-1 ) ).reduce( (x1, x2) -> x1 + x2 ).get();
				
			//Make XOR bids on all bundles B where 0<= value(B) <= budget and totalCommonValue >= minResaleValue
			if( (computeValue(bundleI, privateValue) >= 0) && (computeValue(bundleI, privateValue) <= budget) && (totalCommonValueI >= minResaleValue) )
			{
				substitutableBids.add(bundleI);
				commonValues.add(totalCommonValueI);
			}
		}
		
		//If there are more than _MAX_SUBSTITUTABLE_BIDS such bundles, then bid on the _MAX_SUBSTITUTABLE_BIDS bundles having the largest value
		if( substitutableBids.size() > _MAX_SUBSTITUTABLE_BIDS )
		{
			substitutableBids.sort( (bundleI, bundleJ) -> Double.valueOf(computeValue(bundleI, privateValue)).compareTo(computeValue(bundleJ, privateValue)) );
			substitutableBids = substitutableBids.subList(0, _MAX_SUBSTITUTABLE_BIDS);
		}
		
		for(int i = 0; i < substitutableBids.size(); ++i)
		{
			List<Integer> sBundle = substitutableBids.get(i);
			double itsValue = computeValue(sBundle, privateValue);
			if( ! isExists(sBundle) )
			{
				sBundle.sort(null);
				_bids.add( constructAtom( sBundle, itsValue, agentId) );
			}
		}
	}
	
	/**
	 * The method returns the jump probability
	 * @return the jump probability
	 */
	public double getJumpProbability()
	{
		return _JUMP_PROBABILITY;
	}
	
	/**
	 * The method adds one additional good to the specified bundle.
	 * @param bundle - a bundle to which a good should be added
	 * @param pn - probability distribution over all goods
	 * @param dependencyGraph - the grid of the spatial domain
	 */
	private void addGoodToBundle(List<Integer> bundle, List<Double> pn, Graph dependencyGraph)
	{
		if(_randGenerator.nextDouble() <= _JUMP_PROBABILITY)//TODO: use the seed
		{
			//Add a good which is not adjacent to the bundle
			int newGoodId = -1;
			do
			{
				newGoodId  = (int)(_randGenerator.nextDouble() * _numberOfGoods + 1);
			}
			while( bundle.contains( newGoodId ) && bundle.size() <= _numberOfGoods );
			bundle.add(newGoodId);
		}
		else
		{
			//Compose a list of all goods which are adjacent to a bundle (i.e., to any good from the bundle)
			List<Integer> neighborsOfBundle = new ArrayList<Integer>();
			for(int i = 0; i < bundle.size(); ++i)
			{
				int goodIdx = bundle.get(i)-1;
				List<VertexCell> neighborsOfI = dependencyGraph.getAdjacencyLists().get( goodIdx );
				for(VertexCell vc : neighborsOfI)
					if( (! neighborsOfBundle.contains( vc._v.getID()) ) && (! bundle.contains(vc._v.getID()) ) )
						neighborsOfBundle.add( vc._v.getID());
			}
			if(neighborsOfBundle.size() == 0)	throw new RuntimeException("No neighbors for this bundle");
			
			//Normalize weights over all neighbors of the bundle
			double s = neighborsOfBundle.stream().map( goodId -> pn.get(goodId-1) ).reduce( (x1, x2) -> x1 + x2 ).get();
			
			//Create a probability distribution among neighbors of the bundle
			List<Double> pnNeigh = neighborsOfBundle.stream().map( goodId -> pn.get(goodId-1) / s).collect(Collectors.toList());
			
			//Choose the next good at random among all neighbors of the bundle using a probability distribution pnNeigh
			bundle.add( pickGoodFromSet(neighborsOfBundle, pnNeigh));
		}
	}
	
	/**
	 * The method sets up a parameter of the spatial domain generator
	 * @param additionalLocation probability to add yet another good into bundle
	 */
	public void setAdditionalLocation(double additionalLocation)
	{
		_ADDITIONAL_LOCATION = additionalLocation;
	}
	
	/**
	 * The method picks a good from the set of goods according to the specified probability distribution.
	 * @param goods - a set of goods from which a new good should be chosen
	 * @param probabilityDistribution - a probability distribution over goods in the set
	 * @return an id of a chosen good
	 */
	private int pickGoodFromSet(List<Integer> goods, List<Double> probabilityDistribution)
	{
		if(goods.size() != probabilityDistribution.size() )	throw new RuntimeException("Dimensionality mismatch");
		if(goods.size() == 0)								throw new RuntimeException("No goods specified");
		for(Double p : probabilityDistribution)
			if( p < 0 || p > 1)								throw new RuntimeException("Incorrect probability: " + p);
		
		do
		{
			int goodIdx = (int)(_randGenerator.nextDouble() * goods.size());
			if( _randGenerator.nextDouble() < probabilityDistribution.get( goodIdx ) )
				return goods.get(goodIdx);
		}
		while(true);
	}
	
	/**
	 * The method computes the value of a bundle for a given bidder as a sum of a common and a private values.
	 * @param bundle - a bundle for which the valu should be computed
	 * @param privateValue - a list of private values for goods by the bidder
	 * @return a value for the bundle
	 */
	private double computeValue(List<Integer> bundle, List<Double> privateValue)
	{
		return bundle.stream().map( gId -> Math.max(0., _commonValue.get( gId-1 ) + privateValue.get( gId-1 ))).reduce( (x1, x2) -> x1 + x2).get() + Math.pow(bundle.size(), 1.+_ADDITIVITY);
	}
	
	/**
	 * The method constructs an atomic bid for a given bundle.
	 * @param bundle
	 * @param value  the value of the bundle
	 * @param agentId  an id of an agent corresponding to the bid
	 * @return an atomic bid
	 */
	private AtomicBid constructAtom(List<Integer> bundle, double value, int agentId)
	{
		AtomicBid atom = new AtomicBid(agentId, bundle, value);
		return atom;
	}
	
	/**
	 * The method checks if an atom for the specified bundle already exists among generated bundles.
	 * @param bundle - a bundle of goods to be checked
	 * @return true if an atom for this bundle exists and false otherwise
	 */
	private boolean isExists(List<Integer> bundle)
	{
		for(AtomicBid bid: _bids)
			if(bid.getInterestingSet().containsAll(bundle) && bundle.containsAll(bid.getInterestingSet()))
				return true;
		return false;
	}
	
	protected int _numberOfGoods;								//Number of goods in the auction
	protected Graph _dependencyGraph;							//Dependency graph for the spatial domain
	protected List<Double> _commonValue;						//Common value of bidders for goods
	protected List<AtomicBid> _bids;							//A list of generated atomic bids
	protected Random _randGenerator;							//Random numbers generator
	
	protected final double _MAX_GOOD_VALUE = 100.;				//Max common value of a good
	protected final double _DEVIATION = 0.5;					//Deviation describes how much a private value can differ from the common value
	protected double _ADDITIONAL_LOCATION = 0.85;				//Probability to add yet another good into bundle
	protected final double _JUMP_PROBABILITY = 0.05;			//Probability that a bundle contains a good which is not adjacent to other goods
	protected final double _BUDGET_FACTOR = 1.5;				//Budget factor used for substitute bids
	protected final double _RESALE_FACTOR = 0.5;				//Resale factor used for substitute bids
	protected final double _ADDITIVITY = 0.2;					//Positive for superadditive goods, negative for subadditive and zero for additive goods
	protected final int _MAX_SUBSTITUTABLE_BIDS = 5;			//Maximum number of substitutes
}
