package chordDiagrams;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * This class represents an orbit element in an orbit of a diagram under some action.
 * If the element is not in the basis, the relation field contains the relation that
 * relates it to the basis.
 * @author Romwell
 *
 */
public class OrbitElement
{
	/**
	 * Is the orbit element in the basis or is it a linear combination
	 */
	public boolean isInBasis;
	
	
	/**
	 * Index of the diagram 
	 */
	public int diagIndex;
	
	/**
	 * Relation that shows this diagram as a linear combination of diags in the basis
	 */
	public Relation relation;

	
	/**
	 * Creates a new orbit element
	 * @param diagIndex index of the diagram
	 * @param isInBasis true if the diagram is in the basis
	 * @param relation diagram relation if it is not in the basis
	 */
	public OrbitElement(int diagIndex, boolean isInBasis, Relation relation) 
	{
		this.isInBasis = isInBasis;
		this.diagIndex = diagIndex;
		this.relation = relation;
	}

	
	
	public String toString()
	{
		String S="";
		S+=diagIndex;
		if (!isInBasis)		
		{
			S="( "+ S + " ~ " + relation+ " )";
		}
		return S;
	}

	
	public boolean equals(OrbitElement e)
	{
		return (this.diagIndex==e.diagIndex);  //two orbit elements are the same if they are the same diagram
	}
	
	/**
	 * Draws this orbit element and saves it to a file
	 * @param D	Diagram Drawer to draw with
	 * @param fname filename to save to 
	 * @param diags data structure with diagrams
	 * @param keys keyset for the diagrams
	 */
	public void drawElement(DiagramDrawer D, String fname, HashMap<String, diagram> diags, ArrayList<String>keys)
	{
		if (isInBasis)
		{
			diagram g = diags.get(keys.get(diagIndex));
			D.DrawAndSave(g, fname);
		}
		else
		{
			D.DrawRelation(relation, keys, diags, D.dspace, D.dspace);
			D.Save(fname);
		}
	}
	
}
