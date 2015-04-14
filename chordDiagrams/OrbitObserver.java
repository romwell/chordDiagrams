package chordDiagrams;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * This class looks for orbits of diagrams in the basis under link permutation
 * 
 * @author Romwell
 *
 */
public class OrbitObserver {

	/**
	 * Number of chords
	 */
	int n;
	
	/**
	 * Number of links
	 */
	int k;
	
	/**
	 * Data structure to store diagrams
	 */
	HashMap<String, diagram> diags;
	
	
	/**
	 * Key set for diagrams
	 */
	ArrayList<String> keys;
	
	
	/**
	 * Data structure to store independent relations
	 */
	public TreeSet<Relation> indRelations;

	
	
	/**
	 * Stores diagID's for the basis
	 */
	public HashSet<String> basis;

	/**
	 * Stores diagID's for the reduced basis
	 */
	public HashSet<String> reducedBasis;

	/**
	 * Stores orbits of the diagrams
	 */
	public ArrayList<Orbit> orbits;
	
	
	/**
	 * Used for recursive calls in link permutation generation; 
	 * i'th entry is true if i'th link is used in the permutation.
	 */
	boolean[] linkplaced;
	
	
	
	/**
	 * Current permutation of links
	 */
	int [] linkpermutation;
	
	/**
	 * Store the labels of orbit elements
	 */
	public HashMap<Integer, String>orbitlabels;

	
	/**
	 * Constructs a new instance of Orbit Observer
	 * @param n
	 * @param k
	 * @param MAXNum
	 * @param gen diagram generator to use
	 */
	public OrbitObserver(int n, int k, int MAXNum, UnframedDiagramGenerator gen)
	{
		this.n = n;
		this.k = k;
		FourTProcessor proc = new FourTProcessor(n,k,MAXNum,gen);
		proc.generateAll4Ts();
		proc.evaluateRelations();
		this.keys = proc.keys;
		this.indRelations = proc.indRelations;
		this.diags = proc.diags;
		basis = new HashSet<String>();
		generateBasis(proc.isInBasis);
		reducedBasis = new HashSet<String>(basis);		
		orbits = new ArrayList<Orbit>();
	}
	
	
	/**
	 * Generates an orbit for a diagram and places it in the list
	 * @param diagID
	 */
	public void generateOrbit(String diagID)
	{
		Orbit O = new Orbit(diagID,basis,indRelations,keys);
		String [] dlinks = diagLinks(diagID);
		int nlinks = dlinks.length;
		linkplaced = new boolean[nlinks];
		linkpermutation = new int[nlinks];
		generateOrbit(0,dlinks,O);		
		orbits.add(O);
	}
	
	/**
	 * Returns the links of the diagram as an array
	 * @param diagID diagram ID
	 * @return array containing the links as strings of colors
	 */
	public String[] diagLinks(String diagID)
	{
		String S = diagID.substring(1, diagID.length()-1);//peel off {} in diagID
		StringTokenizer ST = new StringTokenizer(S,"|");
		String [] dLinks = new String[ST.countTokens()];
		for (int i=0;i<dLinks.length;i++)
		{
			dLinks[i]=ST.nextToken();
		}
		return dLinks;
	}
	
	
	/**
	 * Generates the orbit of a diagram under the permutation of links
	 * @param pos current position in the permutation
	 * @param diagLinks diagram links as strings of colors
	 * @param O orbit to store the orbit elements (link permutations) in
	 */
	public void generateOrbit(int pos, String[]diagLinks, Orbit O)
	{
		if (pos<diagLinks.length)
		{
			for (int i=0;i<diagLinks.length;i++)
			{
				if (!linkplaced[i])
				{
					linkplaced[i]=true;
					linkpermutation[pos]=i;					//PLACE

					generateOrbit(pos+1, diagLinks, O);	//RECURSE	

					linkplaced[i]=false;					//BACKTRACK
				}
			}
		}
		else	//permutation construction completed
		{
			String dID = diagIDfromCurPermutation(diagLinks);
			putCurrentOrbitElement(dID, O);
		}
	}
	
	
	/**
	 * Puts the current orbit element into the orbit.
	 * @param dID diagram ID to put into the orbit
	 * @param O the orbit to put the element in
	 */
	public void putCurrentOrbitElement(String dID, Orbit O)
	{
			O.addDiagram(dID);
			if (reducedBasis.contains(dID))
			{
				reducedBasis.remove(dID);
			}
	}
	
	/**
	 * Takes the current link permutation and returns the diagram ID
	 * @param dlinks
	 * @return index of the diagram in keyset
	 */
	public String diagIDfromCurPermutation(String[]dlinks)
	{
		String S="";
		for (int i=0;i<linkpermutation.length;i++)
		{
			S+=dlinks[linkpermutation[i]];
			if (i<linkpermutation.length-1)
			{
				S+="|";
			}
		}
		
		S="{"+S+"}";
		diagram g = new diagram(S);
		
		return g.diagID; 
	}
	
	
	/**
	 * Generates orbits for all diagrams in the basis
	 */
	public void generateAllOrbits()
	{
		while (reducedBasis.size()>0)
		{
			Iterator<String> I = reducedBasis.iterator();
			String dID= I.next();
			generateOrbit(dID);
		}		
	}
	
	/**
	 * Prints all orbits to command line (primary usage: debugging)
	 */
	public void printOrbits()
	{
		System.out.println("There are "+orbits.size()+" orbits:");
		int count = 0;
		for (Orbit O:orbits)
		{
			count += O.length();
			System.out.println(O);
		}
		System.out.println ("These orbits contain "+count+" out of total "+keys.size()+" diagrams");
	}

	/**
	 * Same as printOrbits, but with diagID's instead of indices
	 *
	 */
	public void printOrbitsEx()
	{
		System.out.println("There are "+orbits.size()+" orbits:");
		int count = 0;
		for (Orbit O:orbits)
		{
			count += O.length();
			System.out.println(O.toStringExtended());
		}
		System.out.println ("These orbits contain "+count+" out of total "+keys.size()+" diagrams");		
	}

	

	/**
	 * Same as printOrbits, shorter
	 *
	 */
	public void printOrbitsShort()
	{
		System.out.println("There are "+orbits.size()+" orbits:");
		int count = 0;
		for (Orbit O:orbits)
		{
			count += O.length();
			System.out.println(O.toStringShort());
		}
		System.out.println ("These orbits contain "+count+" out of total "+keys.size()+" diagrams");		
	}

	
	/**
	 *  Fills the basis HashSet with basis diagrams
	 * @param isInBasis array that tells which elements are in the bais
	 * (a[i]=true if i is in the basis)
	 */
	public void generateBasis(boolean[] isInBasis)
	{
		for (int i=0;i<isInBasis.length;i++)
		{
			if (isInBasis[i])
			{
				basis.add(keys.get(i));
			}
		}
	}

	
	/**
	 * Draws the orbits with the specified DDrawer
	 * filenames are of the form orbit_n-k_(i)_j.PNG
	 * @param D DDrawer to draw with
	 */
	public void drawOrbits(DiagramDrawer D)
	{
		int i=0;
		for (Orbit O:orbits)
		{
			String fname = "orbit_"+n+"_"+k+"_("+i+")_";
			O.drawOrbit(D, this.diags, fname);
			i++;
		}
	}
	
	
	/**
	 * Generates the labels for orbit elements 
	 * that are in the basis
	 * for subsequent LaTeX printount
	 *
	 */
	private void generateOrbitBaisLabels()
	{
		int oCount=0;
		for (Orbit O : orbits)
		{
			String orbLabel = indexToLetters(oCount);
			int c=1;		//we want our indexing to start with 1
			for(Integer I:O.orbit.keySet())
			{
				OrbitElement OE = O.orbit.get(I);
				String label = "";
				if (OE.isInBasis)					//store labels for basis elements first
				{
					label+=orbLabel+"_{"+c+"}";
					orbitlabels.put(I, label);
				}				
				c++;
			}
			oCount++;
		}
	}
	
	
	/**
	 * Generates labels for orbit elements that are relations;
	 * labels for basis elements must be generated prior to calling
	 * this method
	 *
	 */
	private void generateOrbitRelationLabels()
	{
		for (Orbit O : orbits)
		{
			for(Integer I:O.orbit.keySet())
			{
				OrbitElement OE = O.orbit.get(I);
				String label ="";
				if (!OE.isInBasis)
				{
					int r = 0;
					for (Integer J:OE.relation.relation.keySet())
					{
						if (r > 0)				//we have relation a-b-c-..=0. We need to write down a=b+c+...
						{
							double d = OE.relation.relation.get(J); 
							d = -d;
							if ((r>1)&&(d>0))
							{
								label+="+";
							}
							label+=Relation.coefToString(d);
							label+=orbitlabels.get(J);
						}
						r++;
					}
					orbitlabels.put(I, label);	
				}
			}
		}
	}

	
	/**
	 * Generates labels for elements of all orbits
	 *
	 */
	public void generateOrbitLabels()
	{
		orbitlabels = new HashMap<Integer, String>();
		generateOrbitBaisLabels();
		generateOrbitRelationLabels();
	}
	
	
	/**
	 * Prints orbits labeled
	 */
	public void printOrbitLabels()
	{
		String fname = "orbits_"+n+"-"+k+".tex";
		try
		{
			FileWriter fw = new java.io.FileWriter(fname, false );
			java.io.PrintWriter out = new java.io.PrintWriter( fw, true );


			int M = maxOrbitShortLength();
			out.println("\\begin{center}");
			out.println("Orbits of $C_"+n+"^"+k+"$");
			out.println();
			out.println("\\begin{tabular}");
			String S ="{ ";
			for (int i=0;i<M;i++)
			{
				S+=(" c");
			}
			S+=" }";
			out.println(S);
			for (Orbit O:orbits)
			{
				int count =0;
				S = ""; 
				for (Integer I:O.orbit.keySet())
				{
					if (O.isInteresting(I))
					{
						count ++;						
						S+="$"+orbitlabels.get(I)+"$";
						if (count < O.length()) {S+=" & ";}
					}
				}
				for (int i=count;i<M-1;i++)
				{
					S+=" & ";
				}
				S+=" \\\\";
				out.println(S);
			}
			out.println("\\end{tabular}");
			out.println("\\end{center}");
			
			out.flush();
			out.close();
		}
		catch (IOException e) {
			System.out.println("Aaa! File I/O occured : "+e.getMessage());
		}
	}
	
	
	/**
	 * Calculates the maximum "short" length of an orbit (i.e.,
	 * length moudulo 4T relation, elements expressed by a relation
	 * of the form a=b aren't considered)
	 * @return the biggest "short" length of an orbit
	 */
	public int maxOrbitShortLength()
	{
		int max = 0;
		for (Orbit O:orbits)
		{
			if (O.shortLength>max)
			{
				max = O.shortLength;
			}
		}
		return max;
	}
	
	/**
	 * Generates string orbit label from index
	 * 0->a, 1->b,...,26->aa,27->ab,...
	 * @param i index of the orbit
	 * @return String representation of the index
	 */
	public static String indexToLetters(int i)
	{
		String S="";
		int t = i;
		do
		{
			int r = t%26;
			t = t / 26;
			S+=(char)('a'+r);
		}
		while (t>0);
		return S;
	}
}

