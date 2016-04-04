package ch.uzh.ifi.DomainGenerators;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.uzh.ifi.GraphAlgorithms.Graph;
import ch.uzh.ifi.MechanismDesignPrimitives.AtomicBid;
import ch.uzh.ifi.MechanismDesignPrimitives.CombinatorialType;
import ch.uzh.ifi.MechanismDesignPrimitives.IDomainGenerator;
import ch.uzh.ifi.MechanismDesignPrimitives.Type;

/**
 * The class wraps CATS "regions".
 * @author Dmitry Moor
 */
public class DomainGeneratorCATS implements IDomainGenerator
{

	private static final Logger _logger = LogManager.getLogger(DomainGeneratorCATS.class);
	
	/**
	 * A simple constructor.
	 * @param numberOfGoods the number of goods in the auction
	 * @param numberOfAgents the max number of bids to generate
	 * @throws SpacialDomainGenerationException if cannot create a square grid with the specified number of goods
	 */
	public DomainGeneratorCATS(int numberOfGoods, int numberOfAgents) throws SpacialDomainGenerationException
	{
		_numberOfGoods = numberOfGoods;
		_numberOfAgents = numberOfAgents;
	}
	
	/**
	 * (non-Javadoc)
	 * @see ch.uzh.ifi.MechanismDesignPrimitives.IDomainGenerator#generateBid(long, int)
	 */
	@Override
	public Type generateBid(long seed, int agentId) 
	{
		//The seed indicates the number of file to be read
		List<AtomicBid> bid = new ArrayList<AtomicBid>();
		try 
		{
			FileReader input = new FileReader("C:\\Users\\Dmitry\\Downloads\\0000.txt");
			BufferedReader bufRead = new BufferedReader(input);
			String myLine = null;
			
			int numberOfDummyItems = 0;
			boolean isDummyFound = false;
			while ( (myLine = bufRead.readLine()) != null)
			{
			    String[] tokens = myLine.split(" ");
			    
			    for (int i = 0; i < tokens.length; ++i)
			        if( tokens[i].equals("dummy") )
			        {
			        	numberOfDummyItems = Integer.parseInt(tokens[i+1]);
			        	isDummyFound = true;
			        	if( numberOfDummyItems < _numberOfAgents) throw new RuntimeException("The CATS file does not contain enough bids: " + numberOfDummyItems);
			        	_logger.debug("Dummy found");
			        	break;
			        }
			    if(isDummyFound) break;
			}
			
			if(isDummyFound)
			{
				while ( (myLine = bufRead.readLine()) != null)
				{
				    String[] tokens = myLine.split("\t");
				
			    	int dummyItemForAgent = agentId == 0 ? -1 :(_numberOfGoods - 1) + agentId;
			    	_logger.debug("Dummy item for the agent is : " + dummyItemForAgent );
			    	
			    	boolean isFound = false;
			    	double value = 0.;
			    	List<Integer> bundle = new ArrayList<Integer>();
			    	
			    	_logger.debug("Parse tokens: " + myLine + " #tokens=" + tokens.length);
			    	if(tokens.length < 2) continue;
			    	for(int i = 0; i < tokens.length; ++i)
			    	{
			    		_logger.debug("i="+i);
			    		if( i == 1)
			    		{
			    			value = Double.parseDouble( tokens[i] );
			    			_logger.debug("Parse value = " + value);
			    		}
			    		if( tokens[i].equals("#") )
			    			break;
			    		
			    		if( i > 1 && Integer.parseInt( tokens[i] ) < _numberOfGoods )
			    			bundle.add( Integer.parseInt( tokens[i] ) );
			    		
			    		if( (i>1) && (dummyItemForAgent != -1) && (Integer.parseInt(tokens[i]) == dummyItemForAgent))
			    			isFound = true;
			    		else if ( dummyItemForAgent == -1 && Integer.parseInt(tokens[i]) >= _numberOfGoods )
			    		{
			    			isFound = false;
			    			break;
			    		}
			    		else if (dummyItemForAgent == -1)
			    			isFound = true;
			    	}
			    	if( isFound )
			    	{
			    		bid.add( new AtomicBid(agentId, bundle, value) );
			    		_logger.debug("Found the following bid: " + bundle.toString() + " v= " + value);
			    	}
				}
			}
			bufRead.close();
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (NumberFormatException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		if(bid.size() == 0)  throw new RuntimeException("No atoms produced");

		Type ct = new CombinatorialType();
		bid.stream().forEach( i -> ct.addAtomicBid(i) );
		return ct;
	}

	protected int _numberOfGoods;										//Number of goods in the auction
	protected int _numberOfAgents;
	protected Graph _grid;												//Spatial proximity graph
	protected SpatialDomainGenerator _spatialDomainGenerator;			//Spatial domain generator
}
