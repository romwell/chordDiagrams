package chordDiagrams;

/**
 * Generates all distinct chord diagrams for the framed case,
 * modulo link rotation and recoloring.
 * Automatically saves the results to file, and loads the file
 * instead of recomputing (if the file is found).
 * @author Romwell
 *
 */
public class FrameGen2 extends UnframedDiagramGenerator 
{

	/**
	 * Constructs an instance of the generator
	 * @param n number of chords
	 * @param k number of links
	 */
	public FrameGen2(int n, int k)
	{		
		super(n, k);
	}
	
	
	/**
	 * All skips allowed in the framed case
	 * @param cur starting poisition
	 * @param skip skip number
	 * @param links link config in regular format
	 * @param linkcolors link config in coloring format
	 * @return
	 */
	public boolean isLegitSkip(int cur, int skip, int[] links, int[]linkcolors)
	{
		return true;
	}
	
	/**
	 * Filename that the diagrams will be stored to/loaded from : "frame2_n-k.data"
	 */
	public String getFileName()
	{
		return  "frame2_"+n+"-"+k+".data";
	}

	
}
