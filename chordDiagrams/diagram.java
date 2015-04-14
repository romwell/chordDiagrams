package chordDiagrams;
import java.io.Serializable;
/**
 * This class implements a chord diagram: stores it and has methods to 
 * compare it to other diagrams.
 * @author Romwell
 *
 */
public class diagram implements Serializable, Comparable<diagram> {

	/**
	 * Version number, needed for serialization
	 */
	static final long serialVersionUID = 002;
	
	/**
	 * data structure to hold chords in "skip" format
	 */
	int skipdiag[];
	
	
	/**
	 * data structure to store the chords in the "coloring" format
	 */
	int [] chords;
	
	/**
	 * data structure to store links configuration;
	 * stores indices of endpoints of links in the chords array
	 */
	int [] links;
	
	/**
	 * same as above; stores links by coloring chord endpoints
	 */
	int [] linkcolors;
	
	/**
	 * sort of same as links: stores the link configuration
	 * by storing the number of entries each ring occupies
	 */
	int [] ringsizes;
	
	/**
	 * number of chord endpoints
	 */
	int n; 
	
	/**
	 * number of links
	 */
	int k;
	
	
	/**
	 * stores connectivity graph between links
	 */
	boolean[][] connected;
	
	/**
	 * is the link graph connected ?
	 */
	boolean isConnected;
	
	/**
	 * Diagram's unique string ID obtained from its canonical form
	 */
	String diagID;

	/**
	 * Creates an instance of the diagram class
	 * @param dchords chords in the "skip" format
	 * @param dlinks links in regular format
	 */
	public diagram (int[] dchords, int[] dlinks, int[]dlinkcolors)
	{
		this.skipdiag= arrcopy(dchords);
		this.chords = SkipToColoring(skipdiag);
		this.links = arrcopy(dlinks);
		this.linkcolors = arrcopy(dlinkcolors);
		this.n = chords.length;
		this.k = links.length;
		this.ringsizes = new int [k];
		ringsizes[0] = links[0]+1;
		for (int i=1; i<k;i++)
		{
			ringsizes[i] = links[i]-links[i-1];
		}
		connected = new boolean[k][k];
		fillConnectivity();
		checkConnected();
		getCanonicalForm();
	}
	
	/**
	 * Initializes 
	 *
	 */
	public void initialize()
	{
		
	}
	
	/**
	 * Constructs an instance of the diagram from standard string representation
	 * @param S standard string representation of the diagram. Ex:
	 * a|bcd|ef|dec|bf
	 * Pipes ('|') denote link delimiters; any characters are OK as long as 
	 * distinct characters denote distinct colors.
	 */
	public diagram (String S)
	{
		this(colorsFromString(S),linksFromString(S));
	}
		
	
	/**
	 * Constructs an instance of the digram from chord colors description 
	 * and link delimiter descripion
	 * @param chordcolors chords in coloring format
	 * @param links links in link delimiter format
	 */
	public diagram (int[] chordcolors, int[]linksdel)
	{
		this(ColoringToSkip(chordcolors),linksdel,LinksToColors(linksdel));
	}
	
	/**
	 * Rotates a ring to the right; only affects the coloring format 
	 * @param ring The ring index
	 */
	public void rotatering(int ring)
	{
		int start; int end;
		if (ring == 0) {start = 0;} else {start = links[ring-1]+1;}
		end = links[ring];
		
		int t=chords[start]; int t2;
		for (int i=start+1;i<=end;i++)
		{
			t2=chords[i];
			chords[i]=t;
			t = t2;
		}
		chords[start]=t;
	}
	

	/**
	 * returns whether there exists a recoloring map from a to b
	 * @param a one diagram coloring
	 * @param b another diagram coloring
	 * @return true, if there is a recoloring
	 */
	public boolean recolorexists(int[]a, int[]b)
	{
		if (a.length != b.length) {return false;} 	//nogood =)
		int c = a.length/2;   						//number of colors
		int[] map = new int[c]; 					//to store the recoloring map
		for (int i=0;i<c;i++)						//initialize the map to blank
		{
			map[i]=-1;
		}
		for (int i=0; i<a.length; i++)
		{
			if (map[a[i]]!=b[i]) 
			{
				if (map[a[i]]!=-1) 
					{return false;} 
				else {map[a[i]]=b[i];}								
			}
			else {map[a[i]]=b[i];}
				
		}
		return true;
	}
	
	/**
	 * returns whether a and b are same by recoloring
	 * @param a
	 * @param b
	 * @return
	 */
	public boolean recolored (int[] a, int[]b)
	{
		return (recolorexists(a, b)&&recolorexists(b, a));
	}
	
	
	/**
	 * returns a copy of the integer array a
	 * @param a
	 */
	public static int[] arrcopy(int[] a)
	{
		int[]b = new int[a.length];
		for (int i=0;i<a.length;i++)
		{
			b[i]=a[i];
		}
		return b;
	}

	/**
	 * Prints array to the console
	 * @param a the array to print
	 */
	public static void PrintArray(int[] a)
	{
		System.out.print("[ ");
		for (int i=0;i<a.length;i++)
		{
			System.out.print(a[i] + " ");
		}
		System.out.println(" ]");
	}
	
	/**
	 * Provides a string representations of the diagram in a human-readable way
	 */
	public String toString()
	{
		return getStringRepresentation();
	}
	
	/**
	 * Converts a diagram into a string form. Example:
	 * <br> {A|ABCD|BC|D}
	 * <br> { -> start of diagram
	 * <br> } -> end of diagram
	 * <br> A -> color symbol (every symbol must occur exactly two times!)
	 * <br> | -> link sepearator (diagram must not start or end with |, and || must not occcur)
	 * This function is separate from toString() so that the toString()
	 * method could provide additional extra info; this form, however,
	 * is fixed so that a constructor could be called with it.
	 * @return a standard string representation of the diagram
	 */
	public String getStringRepresentation()
	{
		int t=0;			
		String S = "";
		for (int i=0;i<n;i++) 
		{
			S += (char)('0'+chords[i]);
			if ((i==links[t])&&(i<n-1)) {S+="|";t++;}
		}
		S="{"+S+"}";
		return S;		
	}
	
	
	
	/**
	 *Prints the diagram' string form
	 */
	public void print()
	{
		System.out.println(this);
	}
	
	/**
	 * Recursive function to check whether this diagram is equivalent to some other diagram g.
	 * @param g 
	 * The other diagram
	 * @param link link number to check all rotations
	 * @return returns true if the diagrams are equivalent
	 */
	private boolean EqualsTo(diagram g, int link)
	{
		if (link == k)
		{
			if (recolored (this.chords, g.chords)) {return true;}			
		}
		else 
		{
			for (int i=0;i<ringsizes[link];i++)
			{
				rotatering(link);
				if (EqualsTo(g, link+1)) {return true;}
			}
		}
		
		return false;
	}
	

	/**
	 * returns whether two diagrams are equivalent under link rotation
	 * Obsolete with introduction of canonical form
	 * @param g
	 * @return
	 */
	public boolean equals_ex (diagram g)
	{
		if (same(links, g.links))
		{
			return EqualsTo(g, 0);
		}
		else
		{
			return false;
		}
	}
	
	
	/**
	 * Returns whether this diagram is equivalent to another one.
	 * Both diagrams must have their diagID set properly.
	 * @param g	other diagram
	 * @return	this == g 
	 */
	public boolean eqials(diagram g)
	{
		return (this.diagID.equals(g.diagID));
	}
	
	/**
	 * returns whether two arrays have the same data
	 * @param a
	 * @param b
	 * @return
	 */
	public boolean same(int[] a, int[] b)
	{
		if (a.length==b.length)
		{
			for (int i=0;i<a.length;i++)
			{
			 if (a[i]!=b[i]){return false;}
			}		
			return true;
		}
		else return false;
	}
	
	

	/**
	 * Converts a chord diagram description from skip format to coloring
	 * @param a the chord diagram description in skip format
	 * @return diagram description in coloring format 
	 */
	public static int[] SkipToColoring(int [] a)
	{
	int[] c = new int[a.length];	
	int t=0;
	for (int i=0;i<a.length;i++)
	{
		if (a[i]>0)
			{
				c[i]=t;
				c[i+a[i]]=t; 
				t++;
			}
	}
	return c;
	}
	
	/**
	 * Converts a chord diagram description from coloring to skip
	 * @param a the chord diagram description in coloring format
	 * @return diagram description in skip format 
	 */
	public static int[] ColoringToSkip(int[] chords)
	{
		/*length of array: 2*n */
		int len = chords.length; 
		/*diagram in skip format*/
		int [] skips = new int[len];  
		/* stores indices of first and last occurence of a color */ 
		int [][] intervals = new int [len/2][2];
		/*initialize intervals array*/
		for (int i=0;i<len/2;i++) 
		{
			for (int j=0;j<2;j++)
			{
				intervals[i][j]=-1;
			}
		}
		
		/*fill intervals and skips array*/
		for (int i=0; i<len;i++)
		{
			int col = chords[i];
			if (intervals[col][0]>-1) 
			{
				intervals[col][1]=i;
				skips[i]=-1;
				skips[intervals[col][0]]=intervals[col][1]-intervals[col][0];
			} 
			else 
			{
				intervals[col][0]=i;
			}
		}
		return skips;
	}
	/**
	 * Fills the connectivity graph 
	 */
	public void fillConnectivity()
	{
		for (int i=0;i<skipdiag.length;i++)
		{
			if (skipdiag[i]>0)
			{
				int col1 = linkcolors[i];
				int col2 = linkcolors[i+skipdiag[i]];
				connected[col1][col2]=true;
				connected[col2][col1]=true;
			}
		
		}
	}
	
	/**
	 * recursive function to check connectivity of the link graph
	 * vertices = links
	 * edges = "connectivity" matrix
	 * @param visited array of booleans to mark visited vertices
	 * @param t current vertex
	 */
	private void checkConnected(boolean[] visited, int t)
	{
		visited[t]=true;
		for (int i=0;i<k;i++)
		{
			if ((connected[t][i])&&(!visited[i]))
			{
				checkConnected(visited, i);
			}
		}
	}
	
	
	/**
	 * calls the corresponding recursive method to set the
	 * isConnected field.
	 */
	private void checkConnected()
	{
		boolean[] visited = new boolean[k];
		checkConnected(visited, 0);
		isConnected = true;
		for (int i=0;i<k;i++)
		{
			if (!visited[i]) {isConnected = false; break;}
		}
	}
	
	
	/**
	 * Returns the position of the endpoint in its ring
	 * Used for diagram drawing
	 * @param t the index of the endpoint
	 * @return index of the endpoint in its ring
	 */
	public int posInRing(int t)
	{
		if (t==0) {return 0;} else {
		int p = 0;
		int i = t-1;
		while ((i>=0)&&(linkcolors[i]==linkcolors[t])) {i--;p++;}
		return p;
		}
	}
	
	/**
	 * Returns a new diagram with a kink inserted into link referenced by linknumber
	 * @param linknumber the index of the link to insert the kink into
	 * @return a new diagram with a kink
	 */
	public  diagram insertKinkAt(int linknumber)
	{
	int [] newchords = new int [n+2]; 	//will store new chord arrangement
	int lend = links[linknumber];    	//end of the link
	int kcol = 0; 						//color for the kink
	for (int i=0;i<=lend;i++) 
		{
			newchords[i]=chords[i];
			if (chords[i]>kcol) {kcol=chords[i];}  //kcol will store largest color after this loop
		}
	kcol++;
	newchords[lend+1]=kcol;
	newchords[lend+2]=kcol;
	for (int i=lend+3;i<n+2;i++)
	{
		if (chords[i-2]>=kcol)
		{
			newchords[i]=chords[i-2]+1;
		}
		else
		{
			newchords[i]=chords[i-2];
		}
	}	 
	int [] newlinks = arrcopy(links);
	/*increase the length of succesive links by 2*/
	for (int i=linknumber;i<k;i++)
	{
		newlinks[i]+=2;
	}
	return new diagram(newchords, newlinks);
	}
	
	/**
	 * Inserts kinks into the diagram according to the prescription in presc:
	 * presc[# of link] = # of kinks to insert into that link
	 * @param presc kink insertion prescription
	 * @return a new diagram with kinks
	 */
	public diagram insertKinksAt(int [] presc)
	{
		diagram t = new diagram(this.chords,this.links);
		for (int i=0;i<presc.length;i++)
		{
			for(int j=0;j<presc[i];j++)
			{
				t = t.insertKinkAt(i);
			}
		}
		
		return t;
	}
	
	
	
	
	/**
	 * Converts links config from delimiter index to coloring format
	 * @param linksdel links in link delimiter format
	 * @return  link in coloring format
	 */
	public static int[] LinksToColors (int [] linksdel)
	{
		int [] col = new int[linksdel[linksdel.length-1]+1];
		int i=0; int t=0;
		while (t<linksdel.length)
		{
			while (i<=linksdel[t])
			{
				col[i]=t;
				i++;
			}
		t++;
		}
		return col;
	}
		
	
	
	/**
	 * Converts the diagram into its canonical form
	 * by rotating and recoloring links in the chords 
	 * array
	 */
	public void getCanonicalForm()
	{	
		int[] best = arrcopy(chords);
		getCanonicalForm(best, 0);
		copyArray(best, chords);
		diagID = getStringRepresentation();
	}
	
	
	
	public void getCanonicalForm(int[] best, int link)
	{
		if (link == k)
		{			
			int [] temp = recolorCanonically(chords);	
			if (getString(best).compareTo(getString(temp))>0) 
			{
				copyArray(temp, best);
			}
		}
		else 
		{
			for (int i=0;i<ringsizes[link];i++)
			{
				rotatering(link);
				getCanonicalForm(best, link+1);
			}
		}
	}

	/**
	 * Recolors the chord diagram canonically
	 * (colors in the recoloring start from 0 and go  to [number of colors]-1 in the 
	 * order of occurence)
	 * @param chorcol the color diagram to recolor (the colors can be any numbers in range 0..255)
	 * @return the recoloring
	 */
	public static int[] recolorCanonically(int[]chordcol)
	{
		int [] colormap = new int [256];				//to store the color map
		int [] recol = new int [chordcol.length];
		for (int i=0;i<colormap.length;i++) {colormap[i]=-1;}		//initialize array to -1's
		int col = 0;								//next unused color		
		for (int i=0;i<chordcol.length;i++)				
		{
			if (colormap[chordcol[i]]==-1) 			//if the color wasn't encountered before
			{
				colormap[chordcol[i]]=col;				
				col++;
			}
			recol[i]=colormap[chordcol[i]];
		}
		return recol;
	}
	
	/**
	 * Takes the numbers in the subarray from start to end and puts
	 * them into comma-separated string 
	 * @param a the array
	 * @param start index of the beginning of  the subarray
	 * @param end index of the end of the subarray
	 * @return the string of the form "x_1,x_2,...,x_n"
	 */
	public static String getSubstring(int[] a, int start, int end)
	{
		String S="";
		for (int i=start;i<=end;i++)
		{
			S+=a[i];
			if (i<end){S+=",";}
		}
		return S;
	}
	
	/**
	 * Converts an array into string
	 * @param a array 
	 * @return string representation
	 */
	public static String getString(int[]a)
	{
		return getSubstring(a, 0, a.length-1);
	}
	
	
	/**
	 * Compares this diagram to another one by comparing the diagID strings
	 * @param g the diagram to compare to
	 * @return 0, if equal; 1, if this one is greater than g; -1 - otherwise. 
	 */
	public int compareTo(diagram g)
	{
		return this.diagID.compareTo(g.diagID);
	}
	
	/**
	 * Copies the content of array a into array b
	 * @param a array to copy FROM
	 * @param b array to copy TO
	 */
	public void copyArray(int[]a, int[]b)
	{
		for (int i=0;i<a.length;i++)
		{
			b[i]=a[i];
		}
	}
	
	
	/**
	 * Takes a standard string representation of a string S and returns
	 * the links delimiter array 
	 * @param S the standard string representation of S
	 * @return the links delimiter array that can be passed to the constructor
	 * Ex: called with string AB|BC|CD
	 * the color string is ABBCCD
	 * links arrays is {1,3,5}
	 */
	public static int[] linksFromString (String Str)
	{
		String S = Str.substring(1, Str.length()-1);
		int[]links;   		//to store link arrangement
		int linkcount = 0;
		//count the number of links
		for (int i=0;i<S.length();i++)
		{
			if (S.charAt(i)=='|') {linkcount++;}
		}
		linkcount++;								//avoiding fencepost error: # of links is 
													//1+#of separators
		links = new int[linkcount];
	
		int L=0; //links carret
		int t=S.indexOf('|');
		while (t>-1)
		{
			links[L]=t-1;
			L++;
			S=S.substring(0,t)+S.substring(t+1,S.length());
			t=S.indexOf('|');
		}
		links[links.length-1]=S.length()-1;
		return links;
	}
	
	/**
	 * Returns a color band for the diagram (chord endpoint colors array)
	 * that can be passed to the contructor. Basically, it removes all the
	 * link delimiters (pipes, '|') from the string, and converts the
	 * rest into colors in canonical coloring. 
	 * @param S the standard string representation of S
	 * @return the color band
	 * Ex: called with string ABC|DE|CD|ABE, the color band is ABCDECDABE
	 */
	public static int[] colorsFromString (String Str)
	{
		String S = Str.substring(1, Str.length()-1); //getting rid of '{' and '}'
		S = removeChar(S, '|');
		int[]chords = new int[S.length()]; 		//to store color band
		//fill chords array
		for (int i=0;i<S.length();i++)
		{
			chords[i]=(int)S.charAt(i);
		}		
		chords = recolorCanonically(chords);
		return chords;
	}
	
	/**
	 * Removes all occurrences of a character from a string
	 * @param S the string to remove the character from
	 * @param c the character to be removed from a string
	 * @return the string S without occurrences of character c 
	 */
	public static String removeChar(String S, char c) 
	{
		String t = "";
		for (int i=0;i<S.length();i++) 
		{
			if (!(S.charAt(i)==c)) t += S.charAt(i);
		}
		return t;
	}
	

	/**
	 * Returns the position of the link start in the string representation of the diagram
	 * NOTE: the implementation must be consistent with getStringRepresentation() method.
	 * @param link the index of the link
	 * @return the position of the first symbol in the link in the string representation of the 
	 * diagram
	 */
	public int linkStringStart(int link)
	{
		int start;
		if (link==0) 
		{
			start=1;		//{*...., the position of * is 1
		} 
		else 
		{
			start = links[link-1]+link+2; 	//{..|..|..|*, position of * is 
											//1+#of symbols before + (link-1)separators
		}
		return start;
	}
	
	
	
}
