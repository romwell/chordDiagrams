package chordDiagrams;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;


/**
 * Generates all 4T relations for a list of diagrams
 * @author Romwell
 */
public class FourTGenerator {

	/**
	 * Hashmap to store diagrams
	 */
	public HashMap<String, diagram> diags;
	
	/**
	 * The key set for the hash map
	 */
	public ArrayList<String> keys;
	
	
	/**
	 * Data structure to store relations
	 */
	public TreeSet<Relation> relations;

	
	/**
	 * Stores the number of realtions found so far
	 */
	public int count;
	
	
	/**
	 * Hashmap to store current row
	 */
	private Relation cur_row;
	
	
	/**
	 * Number of chords
	 */
	int n;
	
	/**
	 * Number of links
	 */
	int k;
	
	/**
	 * Diagram generator used to generate diagrams
	 */
	UnframedDiagramGenerator diag_gen;
	
	
	
	/**
	 * Initializes an instance of 4T Generator 
	 * @param n number of chords
	 * @param k number of links
	 * @param gen diagram generator to use
	 */
	public FourTGenerator(int n, int k, UnframedDiagramGenerator gen)
	{
		this.n = n; 
		this.k = k;
		count=0;
		this.diag_gen = gen;
		diags = diag_gen.GenerateDiagrams();
		keys = new ArrayList<String>(diags.keySet());
		System.out.println("Number of diagrams: "+diags.size());
		Collections.sort(keys);			
		relations = new TreeSet<Relation>();
		cur_row = new Relation();
	}
	
	
	
	
	
	/**
	 * Generates and accounts for all the 4T realtions for
	 * all the diagrams stored in the list
	 */
	public void generateAll4Ts()
	{
		if (!loadRelations())
		{
			for (String S : diags.keySet()) 
			{
				generateAll4Ts(diags.get(S));
			}
			saveRelations();
		}
		System.out.println("Number of relations: "+relations.size());
	}
	
	
	
	/**
	 * Generates and accounts for all 4T relations for a given diagram
	 * @param g  	the diagram to generate all 4T's for 
	 */
	public void generateAll4Ts(diagram g)
	{
		for (int i=0;i<g.k;i++)
		{
			for (int j=0;j<g.ringsizes[i];j++)
			{
				String[][] new4t = generate4Ts(g, i);
				if (new4t.length==3)
				{
					account4Ts(new4t);
				}
				g.rotatering(i);
			}
		}
	}
	
	
	/**
	 * Finds out what diagrams are in the relation and stores their
	 * indixes (indicating their position in the keys list) in the matrix,
	 * with corresponding coefficients
	 * @param relation the 4T relations: a-b=c-d=x-y stored as String[3][2] array.
	 */
	public void account4Ts(String[][] relation)
	{
		//convert to indices
		int[][] indices = identify4Tdiagrams(relation);
		//store the results in the matrix
		for (int i=0;i<2;i++)				//NO NEED TO wrap around to account a-b=c-d, c-d=x-y, x-y=a-b - last one is derived from 1st two, 
											//will be thrown out anyway during matrix reduction
		{
			int t=(i+1)%3;					
			cur_row = new Relation();				//clear the current row table
			cur_row.putEntry(indices[i][0], 1);		//put 1's and -1's into the current row
			cur_row.putEntry(indices[i][1], -1);
			cur_row.putEntry(indices[t][0], -1);
			cur_row.putEntry(indices[t][1], 1);
			if (cur_row.nonEmpty())			//note that we don't have to check that relation is new because we keep relations in a set			
			{
				relations.add(cur_row);				
				count++;
			}							//if we had nonzero entries, store the row and go to the next one 
		}
		
	}
	
	
	/**
	 * Identifies the diagrams in the 4T relation and returns their indices
	 * in the keys list
	 * @param relation the 4T relation, String[3][2] array that stores a-b=x-y=c-d
	 * @return int[3][2] with indices of the srtings in relation
	 */
	public int[][] identify4Tdiagrams(String[][] relation)
	{
		int[][] indices = new int[3][2]; 		//array to store diagram indices
		//find out what diagams are in the relation
		for (int i=0;i<3;i++)
		{
			for (int j=0;j<2;j++)
			{
				diagram g = new diagram(relation[i][j]);
				//int t = keys.indexOf(g.diagID);					//FREAKIN' LONG REPLACE ME!!
				int t = Collections.binarySearch(keys, g.diagID);
				//if (t<0){System.out.println("-->"+g);}
				indices[i][j]=t;				
			}
		}
		
		return indices;
	}
	
	
	
	
	
	/**
	 * Generates 4T relations for the first pair of feet in the
	 * current rotation of a linik  
	 * @param g the diagram to generate 4T for
	 * @param link the index of the link
	 * @return array of string representations of diagrams in 4T
	 */
	public static String[][] generate4Ts(diagram g, int link)
	{
		if (g.ringsizes[link]>1)
		{
			String S = g.getStringRepresentation();
			int start=g.linkStringStart(link);	//start of the link in string representation
			char a = S.charAt(start);
			char b = S.charAt(start+1);
			if ((a!=b)&&(a!='|')&&(b!='|')&&(b!='}'))
			{				
				String[][] Result = new String[3][2];
				String REGEX = ""+a+"|"+b;
				String S1 = S.substring(0,start);
				String S2 = S.substring(start+2,S.length());
				String[] XY1 = S1.split(REGEX,-1);
				String[] XY2 = S2.split(REGEX,-1);
				String[] XY = JoinArrays(XY1, XY2);
				
				
				//System.out.println("}}-> "+S);
				//printArray(XY);
				String[] AB = new String[3];
			    int cnt = 0; 
			    char[] ab =new char[]{a,b}; 
			    for (int i=0;i<S1.length();i++)
			    {
			    	if(charMatch(S1, i, ab))
			    	{
			    		AB[cnt]=""+S1.charAt(i);
			    		cnt++;
			    	}
			    }			   
				AB[cnt]=""+a+b;
	    		cnt++;
			    
	    		for (int i=0;i<S2.length();i++)
			    {
			    	if(charMatch(S2, i, ab))
			    	{
			    		AB[cnt]=""+S2.charAt(i);
			    		cnt++;
			    	}
			    }	    
			    
			   for (int i=0;i<3;i++)
			   {
				   Result[i][0]=generateA(XY, AB, false);
				   Result[i][1]=generateA(XY, AB, true);
				   rotateStringArray(AB);
			   }
			
			   return Result;
			}
		}
	 return new String[0][0];	
	}
	
	
	
	/**
	 * Rotates string array clockwise
	 * @param a the string array to rotate
	 */
	public static void rotateStringArray(String[] a)
	{
		if(a.length>0)
		{
			String last = a[a.length-1];
			String t=a[0];			
			for (int i=1;i<a.length;i++)
			{
				String t2 = a[i];
				a[i]=t;
				t = t2;
			}
			a[0]=last;
		}
	}
	
	/**
	 * Takes a string and reverses characters in it 
	 * @param S the string to reverse
	 * @return S reversed. Ex: ABCD -> DCBA
	 */
	public static String reverseString(String S)
	{
		String t="";
		for(int i=S.length()-1;i>-1;i--)
		{
			t+=S.charAt(i);
		}
		return t;
	}
	
	
	/**
	 * Makes a string out of two array by the rule
	 * result=wAxByCz
	 * @param XY array containing w,x,y,z (in this order)
	 * @param AB array containing A,B,C (in this order)
	 * @param reverse if set true, each of A,B,C is reversed
	 * @return a string containing wAxByCz 
	 */
	public static String generateA(String[] XY, String[]AB, boolean reverse)
	{
		if (XY.length<4)
		{
			System.out.print("!!-> "); printArray(XY);
		}
		
		String t = XY[0];	
		for (int i=0;i<3;i++)
		{
			if (reverse)
			{
				t+=FourTGenerator.reverseString(AB[i]);
			}
			else
			{
				t+=AB[i];
			}
			t+=XY[i+1];
		}	
		//System.out.println("*-> "+t);
		return t;
		
	}
	
	

	public static void printArray(String[] a)
	{
		String S= "[ ";
		for (int i=0;i<a.length;i++)
		{
			S += "'"+a[i]+ "'"+" ";
		}
		S += "]";
		System.out.println(S);
	}

	/**
	 * Concatenates two string arrays
	 * @param A 
	 * @param B
	 * @return A+B
	 */
	public static String[] JoinArrays(String[] A, String[] B)
	{
		String[] res = new String[A.length + B.length];
		System.arraycopy(A, 0, res, 0, A.length);
		System.arraycopy(B, 0, res, A.length ,B.length);
		return res;
	}
	
	/**
	 * Tells whether a string contains a character belonging to a set at a position
	 * @param S
	 * @param index
	 * @param a
	 * @return
	 */
	public static boolean charMatch(String S, int index, char[]a)
	{
		for (int i=0;i<a.length;i++)
		{
			if (S.charAt(index)==a[i]) {return true;}
		}
		return false;
	}

	
	
	/**
	 * Generates a name of the file to save relations in.
	 * If the file with such a name is present, data is loaded from the file
	 * instead of being generated.
	 * @return name of the file with relations
	 */
	public String relationDataFname()
	{
		String fname = "relations_"+n+"-"+k+"_using_"+diag_gen.getFileName();
		return fname;
	}
	
	
	

	/**
	 * Draws a set of relations with a DDrawer
	 * @param DDrawer	an instance of the DiagramDrawer class to draw the relations
	 * @param relSet the set with relations to draw 
	 */
	public void DrawRelations(DiagramDrawer DDrawer, Set<Relation>relSet)
	{
		int i=0;
		for (Relation R:relSet)
		{
			DDrawer.DrawRelation(R, keys, diags, DDrawer.dspace, DDrawer.dspace);
			DDrawer.Save("relation_"+n+"-"+k+"_"+i);
			i++;
		}
	}
	
	/**
	 * Draws the set of 4T relations computed by this class with a DDRawer 
	 * @param DDrawer DiagramDrawer to draw the relations with
	 */
	public void DrawRelations(DiagramDrawer DDrawer)
	{
		DrawRelations(DDrawer,relations);
	}
	
	/**
	 * Tells whether a relation r is new to the list
	 * @param r relation to test
	 * @return	true, if r is not in the relations list
	 */
	public boolean isNew(Relation r)
	{
		return (!(relations.contains(r)));
	}
	
	
	
	/**
	 * Saves the relations  to the file
	 * The name of the file depends on n and k ans is returned by relationDataFname() function
	 * @return true if the relations were saved successfully; false otherwise
	 */
	public boolean saveRelations()
	{		
		return ConfigutaionHolder.saveObject(relations, relationDataFname());
	}


	
	/**
	 * Load the relation data from file. The filename is given by relationDataFname() function.
	 * @return true if the data was loaded successfully
	 */
	@SuppressWarnings("unchecked")
	public boolean loadRelations()
	{				
		String fname = relationDataFname(); 
		TreeSet<Relation> loaded = (TreeSet<Relation>)ConfigutaionHolder.loadObject(fname);
		if (loaded!=null) 
		{
			System.out.println("Relations loaded successfully from "+fname);
			relations=loaded;
			return true;
		}
		else
		{
			return false;
		}
	}
	
}



