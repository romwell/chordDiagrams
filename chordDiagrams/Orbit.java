package chordDiagrams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;


public class Orbit 
{
	/**
	 * Index of the diagram whose orbit is considered
	 * (index in the list of diagID's sorted lexicographically)
	 */
	public int diagIndex;
	
	/**
	 * Diagram ID of the Diagram whose orbit is considered
	 */
	public String diagID;
	
	/**
	 * Stores the actual orbit
	 */
	public HashMap<Integer, OrbitElement> orbit;
	
	
	/**
	 * Stores the length of the orbit modulo 4T,
	 * i.e. #of elements that are not expressed by a relation of 
	 * the form a=b
	 */
	public int shortLength;
	
	/**
	 * Stores basis
	 */
	 private HashSet<String> basis;
	
	/**
	 * Independent relations
	 */
	 private TreeSet<Relation> indRelations;
	
	/**
	 * Keyset for the diagrams
	 */
	 private ArrayList<String> keyset;
	
		


	/**
	 * Creates an instance of the class to store the orbit of a diagram under some action
	 * @param diagID String ID of the diagram
	 * @param basis	basis
	 * @param indRelations independent relations 
	 * @param keyset sorted diagram ID's (so that we could deal with indices instead of strings)
	 */
	public Orbit(String diagID, HashSet<String> basis, TreeSet<Relation> indRelations, ArrayList<String> keyset) 
	{
		this.diagID = diagID;
		this.basis = basis;
		this.indRelations = indRelations;
		this.keyset = keyset;
		this.diagIndex = getIndex(diagID);
		this.shortLength = 0;
		orbit = new HashMap<Integer, OrbitElement>();
	}


	/**
	 * Gets the index of a diagram in the keyset
	 * @param diagID ID of the diagram to look for
	 * @return the index of the diagram in the sorted collection of diagram IDs
	 */
	public int getIndex(String diagID)
	{
		return Collections.binarySearch(keyset, diagID);
	}

	/**
	 * Adds a diagram into the orbit
	 */
	public void addDiagram(String diagID)
	{
		int dIndex = getIndex(diagID);
		if (!orbit.keySet().contains(dIndex))
		{
			boolean inBasis = basis.contains(diagID);
			OrbitElement OE;
			if (inBasis)
			{
				OE = new OrbitElement(dIndex,true,null);
				shortLength+=1;
			}
			else
			{
				Relation R=lookupRelation(dIndex);				
				OE = new OrbitElement(dIndex,false,R);
				if(R.size()>2){shortLength+=1;}
			}
			orbit.put(dIndex, OE);
		}
	}
	
	
	/**
	 * If the diagram is not in the basis, then it is a pivot in the matrix of
	 * independent relations. The method looks up the relation with this diagram.
	 * @param dIndex index of the diagram
	 * @return Relation with this diagram
	 */
	public Relation lookupRelation(int dIndex)
	{
		for(Relation R:indRelations)
		{
			int pivot = R.relation.firstKey();
			if (pivot == dIndex) {return R;}
		}
		return null;
	}
	
	
	
	
	public String toString()
	{
		String S = "Length "+orbit.size()+" : ";
		int c =0;
		for (Integer I:orbit.keySet())			
		{
			OrbitElement OE = orbit.get(I);
			c++;
			S+=OE;
			if (c<orbit.size()){S+=", ";}
		}
		return S;
	}
	
	/**
	 * Instead of diagram indices, diagram ID's are printed
	 * @return
	 */
	public String toStringExtended()
	{
		String S = "Length "+orbit.size()+" : ";
		int c =0;
		for (Integer I:orbit.keySet())			
		{
			OrbitElement OE = orbit.get(I);
			c++;
			String T = keyset.get(OE.diagIndex);
			if (!OE.isInBasis) {T+="*";}				
			S+=T;
			if (c<orbit.size()){S+=", ";}
		}
		return S;
	}
	
	
	/**
	 * Same as toString, shorter form
	 * @return
	 */
	public String toStringShort()
	{
		String S = "Length "+orbit.size()+" : ";
		int c =0;
		for (Integer I:orbit.keySet())			
		{
			OrbitElement OE = orbit.get(I);
			c++;
			S+=OE.diagIndex;
			if (!OE.isInBasis) {S+="*";}				
			if (c<orbit.size()){S+=", ";}
		}
		return S;
	}
	
	/**
	 * Tells whether there were no elements put into the orbit 
	 * @return true if no elements were put here
	 */
	public boolean isEmpty()
	{
		return (orbit.isEmpty());
	}
	
	
	
	/**
	 * Returns the number of elements in this orbit
	 */
	public int length()
	{
		return orbit.size();
	}
	
	/**
	 * Draws the orbit with the specified Diagram Drawer
	 * Elements of the orbit are saved to the filenames of the form
	 * fname_i.PNG
	 * @param D DDrawer to draw with
	 * @param diags
	 * @param fname filename base
	 */
	public void drawOrbit(DiagramDrawer D, HashMap<String, diagram>diags, String fname)
	{
		int i=0;
		for (Integer I:orbit.keySet())
		{
			OrbitElement OE = orbit.get(I);
			String fn = fname+"_"+i;
			OE.drawElement(D, fn, diags, this.keyset);
			i++;
		}
	}
	
	/**
	 * Tells whether an orbit element is interesting
	 * @param I key of the orbit element
	 * @return true if an orbit element is in the basis or 
	 * a nontrivial linear combination of basis elements
	 */
	public boolean isInteresting(Integer I)
	{
		if (orbit.containsKey(I))
		{
			OrbitElement OE = orbit.get(I);
			if ((OE.isInBasis)||(OE.relation.size()>2))
			{
				return true;
			}
		}
		return false;
	}

}
