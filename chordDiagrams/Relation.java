package chordDiagrams;

import java.io.Serializable;
import java.util.TreeMap;

/**
 * This class stores a diagram relation
 * @author Romwell
 *
 */
public class Relation  implements Serializable, Comparable<Relation>
{

	/**
	 * All values with absolute value less than epsilon are counted as zeros
	 */
	public static final double epsilon = 0.0000000001; 
	
	/**
	 * Holds DiagramID - Coefficient pairs
	 */
	public TreeMap<Integer, Double> relation;
	
	/**
	 * Relation ID
	 */
	private String relationID="";
	

	/**
	 * Serialization  Version UID
	 */
	public static final long serialVersionUID = 001;
	
	
	/**
	 * Constructs an instance of the relation
	 *
	 */
	public Relation()
	{
		relation = new TreeMap<Integer, Double>();
	}
	
	
	/**
	 * Adds a diagram with a coefficient to the relation
	 * @param diagID	diagram ID	
	 * @param coef		diagram coefficient
	 */ 
	public void putEntry(Integer diagID, double coef)
	{
		if (!isZero(coef))
		{
			if (relation.containsKey(diagID))
			{
				double newval = relation.get(diagID)+coef;
				if (isZero(newval))
				{
					relation.remove(diagID);
				}
				else
				{
					relation.put(diagID, newval);
				}
			}
			else
			{
				relation.put(diagID, coef);
			}
		}
		setID();
	}
	
	
	/**
	 * Tells whether there are any terms in the relations
	 * @return	true, if the relation is not empty; false otherwise.
	 */
	public boolean nonEmpty()
	{
		return (relation.size()>0);
	}
	
	/**
	 * Tells how many terms there are in this relation
	 * @return number of entries in the relation
	 */
	public int size()
	{
		return relation.size();
	}
	

	
	
	/**
	 * Compares this relation to another relation 
	 * @param r	another relation
	 * @return	true, if both relation have exactly the same terms
	 */
	public boolean equals_ex(Relation r)
	{
		for (Integer I:this.relation.keySet())
		{
			if (r.relation.containsKey(I))
			{
				double c1 = this.relation.get(I);
				double c2 = r.relation.get(I);
				if (c1!=c2) 
				{
					return false;
				}
			}
			else
			{
				return false;
			}
		}		
		return true;
	}
	
	
	
	public boolean equals(Relation r)
	{
		return (this.ID().equals(r.ID()));
	}
	
	
	/**
	 * Compares this relation to another via String ID's
	 */
	public int compareTo(Relation r)
	{
		return (this.ID().compareTo(r.ID()));
	}
	
	
	/**
	 * Sets the relation ID string
	 *
	 */
	public void setID()
	{
		String S = "";
		for (Integer I:relation.keySet())
		{
			S+="+"+I+"*"+relation.get(I);			
		}
		this.relationID=S;
	}
	
	/**
	 * Returns relation ID
	 * @return relation ID
	 */
	public String ID()
	{
		return relationID;
	}
	
	/**
	 * Tells if a double is "zero", i.e. less than epsilon
	 * @param d double to check
	 * @return true, if |d|<epsilon
	 */
	public static boolean isZero(double d)
	{
		return (Math.abs(d)<epsilon);
	}
	
	public String toString()
	{
		return ID();
	}
	
	
	/**
	 * Returns a string representation of a coefficient 
	 * @param D coefficient
	 * @return String representation of the coefficient 
	 */
	public static String coefToString(double D)
	{		double epsilon = 0.000001;
			long t = Math.round(D);
			double diff = Math.abs(((t*1.0)-D));
			if (diff<epsilon)
			{
				if (t==1) {return "";} 
				else if (t==-1) {return "-";}
				else {return ""+t;}
			}
			else
			{
				return ""+D;
			}
	
	}
	
}
