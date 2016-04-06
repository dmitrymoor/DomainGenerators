package ch.uzh.ifi.DomainGenerators;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.uzh.ifi.GraphAlgorithms.Graph;
import ch.uzh.ifi.MechanismDesignPrimitives.AtomicBid;
import ch.uzh.ifi.MechanismDesignPrimitives.CombinatorialType;
import ch.uzh.ifi.MechanismDesignPrimitives.IDomainGenerator;
import ch.uzh.ifi.MechanismDesignPrimitives.Type;

/**
 * The class wraps CATS "regions". Instead of calling CATS it uses CATS output files which should be pre-generated
 * using, for example, "cats -d regions -goods 9 -bids 25". It then analyzes these files and differentiate agents
 * based on dummy goods ids. 
 * @author Dmitry Moor
 */
public class DomainGeneratorCATS implements IDomainGenerator
{

	private static final Logger _logger = LogManager.getLogger(DomainGeneratorCATS.class);
	
	/**
	 * A simple constructor.
	 * @param numberOfGoods the number of goods in the auction
	 * @param numberOfAgents the max number of bids to generate
	 * @param path path to the directory where all CATS files are stored
	 * @throws SpacialDomainGenerationException if cannot create a square grid with the specified number of goods
	 */
	public DomainGeneratorCATS(int numberOfGoods, int numberOfAgents, String path) throws SpacialDomainGenerationException
	{
		_numberOfGoods = numberOfGoods;
		_numberOfAgents = numberOfAgents;
		_path = path;
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
			String filename =  _path + "\\"+ (seed<10?"000":"00")+seed+".txt";
			FileReader input = new FileReader( filename );
			BufferedReader bufRead = new BufferedReader(input);
			String myLine = null;
			
			//First, check if the number of dummy items in the file is sufficient to generate bids for the specified number of agents 
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
			        	//if( numberOfDummyItems < _numberOfAgents)
			        	//{
			        		//bufRead.close();
			        		//throw new RuntimeException("The CATS file does not contain enough bids: " + numberOfDummyItems);
			        	//}
			        	_logger.debug("The number of dummy items is sufficient to generate the required number of bids");
			        	break;
			        }
			    if(isDummyFound) break;
			}
			
			isDummyFound = true;
			if(isDummyFound)
			{
				while ( (myLine = bufRead.readLine()) != null)
				{
				    String[] tokens = myLine.split("\t");
			    	int dummyItemForAgent = (_numberOfGoods - 1) + agentId;				//An id of a dummy item for the specified agent
			    	_logger.debug("Dummy item for agent " + agentId + " is : " + dummyItemForAgent );
			    	
			    	boolean isFound = false;											//True if the dummy item is found among generated bids
			    	double value = 0.;
			    	List<Integer> bundle = new ArrayList<Integer>();
			    	
			    	_logger.debug("Parse tokens: " + myLine + " #tokens=" + tokens.length);
			    	if(tokens.length < 2)												//Line number and a value are necessary tokens 
			    		continue;
			    	
			    	for(int i = 0; i < tokens.length; ++i)
			    	{
			    		if( i == 1)
			    			value = Double.parseDouble( tokens[i] );
			    		
			    		if( tokens[i].equals("#") )
			    			break;
			    		
			    		if( i > 1 && Integer.parseInt( tokens[i] ) < _numberOfGoods )
			    			bundle.add( Integer.parseInt( tokens[i] ) + 1 );
			    		
			    		if( (i>1) && (Integer.parseInt(tokens[i]) == dummyItemForAgent))
			    			isFound = true;
			    	}
			    	if( isFound )
			    	{
			    		bid.add( new AtomicBid(agentId, bundle, value) );
			    		_logger.debug("Found the following bid: " + bundle.toString() + " v= " + value);
			    	}
				}
			}
			else
			{
				_logger.error("The number of dummy items cannot be identified from the file");
				bufRead.close();
				throw new RuntimeException("The number of dummy items cannot be identified from the file");
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
		
		Type ct = new CombinatorialType();
		if(bid.size() > 0)
			bid.stream().forEach( i -> ct.addAtomicBid(i) );
		else
			ct.addAtomicBid(new AtomicBid(agentId, new ArrayList<Integer>(), 0.));
		return ct;
	}

	protected int _numberOfGoods;										//Number of goods in the auction
	protected int _numberOfAgents;										//Number of agents in the auction
	protected String _path;												//Path to the directory with CATS files
	protected SpatialDomainGenerator _spatialDomainGenerator;			//Spatial domain generator
}
