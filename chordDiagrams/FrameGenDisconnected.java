package chordDiagrams;

/**
 * Generates all distinct chord diagrams for the framed case,
 * modulo link rotation and recoloring, allowing 
 * DISCONNECTED diagrams.
 * Automatically saves the results to file, and loads the file
 * instead of recomputing (if the file is found).
 * @author Romwell
 *
 */
public class FrameGenDisconnected extends FrameGen2 {

	
	/**
	 * Constructs an instance of the generator
	 * @param n number of chords
	 * @param k number of links
	 */
	public FrameGenDisconnected(int n, int k)
	{		
		super(n, k);
	}
		
	/**
	 * Filename that the diagrams will be stored to/loaded from : "frame2_n-k.data"
	 */
	public String getFileName()
	{
		return  "frameDisconnected_"+n+"-"+k+".data";
	}

	
	/**
	 * Overriden so that the generator thinks that every diagram is connected,
	 * and thus allows all, even disconnected diagrams to be in the list.
	 * @param d diagram whose connectivity is to be checked
	 * @return true always.
	 */
	public boolean isConnected (diagram d)
	{
		System.out.println("Me called!");
		return true;		
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
		return true;
		//if (linkcolors[cur] != linkcolors[cur+skip] ) {return true;} else {return false;}
	}
	

	
	
}
