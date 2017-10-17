package ch.uzh.ifi.DomainGenerators;

import org.apache.commons.math3.distribution.*;
/**
 * This class generates domains for the data market (see AAMAS'17)
 * @author Dmitry
 *
 */
public class WODDomainGenerator 
{
	/**
	 * The method creates a domain with the specified number of DBs, buyers and sellers. By default, sellers' costs are
	 * uniformly distributed between costL and costH.
	 * @param numberOfDBs number of DBs
	 * @param numberOfSellers number of sellers
	 * @param numberOfBuyers number of buyers
	 * @param costL lower bound of sellers' costs
	 * @param costH upper bound of sellers' costs
	 */
	public WODDomainGenerator(int numberOfDBs, int numberOfSellers, int numberOfBuyers, double costL, double costH)
	{
		_numberOfDBs = numberOfDBs;
		_numberOfSellers = numberOfSellers;
		_numberOfBuyers = numberOfBuyers;
		
		_costL = costL;
		_costH = costH;
	}
	
	private int _numberOfDBs;					//Number of data bases
	private int _numberOfSellers;				//Number of sellers
	private int _numberOfBuyers;				//Number of buyers
	
	private double _costL;
	private double _costH;
}
