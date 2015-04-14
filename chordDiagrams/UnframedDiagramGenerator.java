package chordDiagrams;
import java.util.HashMap;

/**
 * Generates all distinct chord diagrams for unframed singular knots.
 * Automatically saves the results to file, and loads the file
 * instead of recomputing (if the file is found).
 *
 */
public class UnframedDiagramGenerator extends ConfigutaionHolder{
	
	/**
	 * Number of chords
	 */
	int n;
	
	/**
	 * Number of links
	 */
	int k;
	
	/**
	 *Array to store current configuration
	 *0 -> not occupied
	 *>0 -> skip number
	 *-1 -> occupied 
	 */
	int[] conf;

	/**
	 * Stores configuration count;
	 */
	int count;
	

	
		
	/**
	 * Outputs diagram configuration (used to print diagrams in skip format)
	 * (prints every entry in the array that's great than -1)
	 * @param a the diagram in skip format
	 */
	public void OutDiag(int[] a)
	{
		System.out.print("[ ");
		for (int i=0; i<a.length; i++)
		{
			if (a[i]>-1)
			{System.out.print(a[i]+" ");}						
		}
		System.out.println(" ]");
	}


	
	/** 
	 * returns the size of a link
	 * @param links link configuration
	 * @param t link number
	 * @return
	 */
	public int linkSize(int [] links, int t)
	{
		if (t==0) {return links[0]+1;}
		else {return links[t]-links[t-1]; }	
	}
	
	
	/**
	 * Tells whether a certain skip is allowed. Goal: get rid of kinks.
	 * @param cur starting poisition
	 * @param skip skip number
	 * @param links link config in regular format
	 * @param linkcolors link config in coloring format
	 * @return
	 */
	public boolean isLegitSkip(int cur, int skip, int[] links, int[]linkcolors)
	{
		
		if (linkcolors[cur] != linkcolors[cur+skip] ) {return true;}
		else 
		{
			int num = linkcolors[cur];
			int size = linkSize(links, num); 
			if ((skip==1)||(skip==size-1)) {return false;} else {return true;}			
		}
	}
	

	
	
	/**
	 * Criteria used to enumerate connectors between 2 (or more?) circles
	 * @param cur starting poisition
	 * @param skip skip number
	 * @param links link config in regular format
	 * @param linkcolors link config in coloring format
	 * @return
	 */
	public boolean isLegitSkip_v2(int cur, int skip, int[] links, int[]linkcolors)
	{
		
		if (linkcolors[cur] != linkcolors[cur+skip] ) {return true;} else {return false;}
	}
	

	
	
	
	/**
	 * Goes through all skip confugrations to create the list of all diagrams
	 * @param arr array to store the configuration
	 * @param p candidate position
	 * @param links stores the link structure by storing ring delimiter indices
	 * @param linkcolors same as links, but stores link colors in an array
	 */
	public void backtrack (int[] arr, int p, int[]links, int [] linkcolors)
	{
		//if the position to fill has valid index and is not the last position
		if (p<2*n-1)
		{
			//if the position is onoccupied...
			if (arr[p]==0)
			{				
				for (int i=1;i<2*n;i++)
				{
					//if the attempt is valid
					if ((p+i<2*n) && (arr[p+i]==0))
					{
						if (this.isLegitSkip(p, i, links, linkcolors))
						//if this skip won't result in a floater (aka kink)
						{
							arr[p]=i;  		//SET
							arr[p+i]=-1;
						
							backtrack(arr, p+1, links, linkcolors);	//RECURSE
						
							arr[p]=0;
							arr[p+i]=0;		//BACKTRACK!										
						
						}
					}
				}
			}
			else
			//if the position is already occupied..	
			{
				backtrack(arr, p+1, links, linkcolors);
			}
		}
		else if (p==2*n-1)
		//if we came to the last index, out diagram configuration
		{
			diagram d = new diagram (arr ,links, linkcolors);
			if ((isConnected(d))&&(isNew(d)))
			{
			configs.put(d.diagID, d);
			count++;
			//d.print();
			}
		}
		
	}
	
	/**
	 * This is the diagram connectivity test used in generating the list of diagrams.
	 * Can be overriden in descendants to allow for disconnected diagrams.
	 * Returns d.isConnected
	 * @param d diagram whose connectivity is to be checked
	 * @return true if d is connected
	 */
	public boolean isConnected (diagram d)
	{
		return d.isConnected;		
	}
	
	/**
	 * Generates all diagrams by generating all link configs for n chords, 
	 * and then generating all colorings by calling the corresponding function.
	 * The method thus fills the configs ArrayList.
	 * @param links the array to store the config
	 * @param n number of chords
	 * @param t current link 
	 */
	public void ProduceLinkConfigs (int[] links, int n, int t)
	{
		int k = links.length;
		if (t==k-1)
		{
			links[t]=2*n-1;			
			//call coloring recursion here
			
			conf = new int[2*n];
			backtrack(conf, 0, links, diagram.LinksToColors(links));

			
		}
		else
		{
			int start;
			if (t==0) {start=0;} else {start=links[t-1]+1;}
				for (int i=start;i<=2*n-(k-t);i++)
				{
					links [t]=i;  						//SET..
					ProduceLinkConfigs(links, n, t+1);  //..AND RECURSE
				}
				
		}
	}
	
	
	/**
	 * Generates all the diagrams with n chords and k links
	 * the diagrams have no kinks entries containing
	 * (xx in coloring format, 1-1 in skip format)
	 * If the diagram list has already been generated (i.e., if the file
	 * with the list exists), the data from the list will be used instead.
	 * @return the list that contains all the generated diagrams
	 */
	public HashMap<String, diagram> GenerateDiagrams()
	{
		if (!loadConfigs())
		{
			int [] links =  new int [k];
			ProduceLinkConfigs(links, n, 0);
			saveConfigs();
		}
		return configs;
	}
	
	
	/**
	 * Initializes fields
	 *
	 */
	public void init()
	{
		conf = new int[2*n];
		count = 0;
		configs = new HashMap<String, diagram>();
	}
	
	
	/**
	 * Creates an instance of the class that generates all diagrams with n chords and k links
	 * for the unframed case
	 */
	public UnframedDiagramGenerator(int n, int k)
	{		
		this.n = n;
		this.k = k;
		init();		
	}


	/**
	 * Filename that the diagrams will be stored to/loaded from : "unframed_n-k.zdata"
	 */
	public String getFileName()
	{
		return  "unframed_diagrams_"+n+"-"+k+".data";
	}
	
}
