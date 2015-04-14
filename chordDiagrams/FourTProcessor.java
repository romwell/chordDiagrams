package chordDiagrams;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import com.wolfram.jlink.KernelLink;
import com.wolfram.jlink.MathLinkException;
import com.wolfram.jlink.MathLinkFactory;

/**
 * The class processes a list of 4T relations to leave out only
 * independent ones in row-reduced echelon form. Mathematica is 
 * used to generate this form.
 * @author Romwell
 *
 */
public class FourTProcessor extends FourTGenerator
{

	
	/**
	 * isInBasis[i] is true if diagram referenced by keys.get(i) is in the basis
	 * <br>Not initialized until evaluateRelations() is called
	 */
	boolean [] isInBasis;
	
	/**
	 * Data structire to hold the independent relations 
	 */
	public TreeSet<Relation> indRelations;
	
	
	
	/**
	 * Kernel Link object to interface with Mathematica
	 */
	public KernelLink ml = null;
	
	/**
	 * Maximum size of the matrix (rows*cols) that will be sent to Mathematica.
	 * If the matrix exceeds this size, it will be broken into submatrices of smaller sizes.
	 */
	int MAX;
	
	
	
	
	/**
	 * Creates a new instance of the class 
	 * @param n	number of chords
	 * @param k	number of links
	 * @param MAXnum maximum size of the matrix that Mathematica will handle
	 * <br>NOTE: if MAX is not big enough(i.e., big enough to fit the row-reduced form
	 * in its full [not sparse] form), the program will run FOREVER. Be careful.
	 * @param gen Diagram generator to use
	 */
	public FourTProcessor(int n, int k, int MAXnum, UnframedDiagramGenerator gen)
	{
		super(n,k,gen);
		generateAll4Ts();		
		indRelations = new TreeSet<Relation>();
		this.MAX = MAXnum;
	}
	
	

	/**
	 * Sends a certain number of relations to Mathematica to row-reduce, no more than a MAX at a time,
	 * and puts the result into the resultSet
	 * @param iter Iterator to get the relations from
	 * @param num	number of relations to send
	 * @param resultSet the set to put resuling (independent)relations into
	 * @param MAX maximum number of relations to send at a time
	 */
	public void processRelations(Iterator<Relation> iter, Set<Relation>resultSet, int num, int MAX)
	{
		if (num>MAX)	//recurse here
		{
			Set<Relation> tempSet = new HashSet<Relation>();
			processRelations(iter, tempSet, num/2, MAX);
			processRelations(iter, tempSet, num-num/2, MAX);
			//now combine the results from two previous steps and put them into resultSet
			processRelations(tempSet.iterator(), resultSet, tempSet.size(), MAX);						
		}
		else			//stop recursion and send stuff to Mathematica here
		{
			evaluateRelations(iter, resultSet, num);		
		}
	}
	
	
	/**
	 * Row-reduces the relation matrix using Mathematica (via J/Link API)
	 * and puts result into indRelation using the recursive processRelation() method
	 * @return Sorted set with independent relations
	 */
	public SortedSet<Relation> evaluateRelations()
	{
		if (!loadIndRelations())
		{
			int MAXrow = MAX/diags.keySet().size();
			processRelations(relations.iterator(), indRelations, relations.size(),MAXrow);
			saveIndRelations();			
		}
		int bcount = extractBasis();
		System.out.println("Number of independent relations: "+indRelations.size());
		System.out.println("Basis size: "+bcount);
		return indRelations;
	}
	
	
	
	/**
	 * Row-reduces the relation matrix using Mathematica (via J/Link API)
	 * and puts result into resultSet
	 * @param iter Iterator to get the relations from
	 * @param num	number of relations to send
	 * @param resultSet the set to put resuling (independent)relations into

	 */
	public void evaluateRelations(Iterator<Relation> iter, Set<Relation>resultSet, int num)
	{
		if (initKernelLink())
		{
			try
			{							
				ml.newPacket();
				ml.putFunction("EvaluatePacket", 1);				
				  ml.putFunction("N", 1);	
				  	ml.putFunction("RowReduce", 1);
				  		putRelationsToMathematica(iter, num);	
				ml.endPacket();
				ml.waitForAnswer();
				double [][] result = ml.getDoubleArray2();
				processMathematicaOutput(result, resultSet);
			}
			catch (MathLinkException e)
			{
				System.out.println("Oops! Mathematica error ocurred: "+e.getMessage());	
			}			
			finally
			{
				ml.abandonEvaluation();
				ml.clearError();
				ml.terminateKernel();
				ml.close();
			}
		}
	}
	
	
	/**
	 * Initialize Mathematica KernelLink
	 * @return true if the kernel was loaded successfully
	 */
	public boolean initKernelLink()
	{
		try
		{
			ml = MathLinkFactory.createKernelLink("-linkmode launch -linkname 'D:\\Soft\\Mathematica\\MathKernel.exe'");
			ml.enableObjectReferences();
		} 
		catch (MathLinkException e) 
		{
			System.out.println("Link could not be created: " + e.getMessage());
			return false;
		}

		try {
			ml.connect(10000); // Wait at most 10 seconds
		} catch (MathLinkException e) {
			// If the timeout expires, a MathLinkException will be thrown.
			System.out.println("Failure to connect link: " + e.getMessage());
			ml.close();
			return false; 
		}

		return true;
	}
	
	
	/**
	 * Finalizes Kernel Link Mathematica connection
	 *
	 */
	public void closeKernelLink()
	{
		ml.close();
	}
	
	/**
	 * Outputs matrix to console
	 * @param A matrix to print
	 */
	public void printMatrix(int [][] A)
	{
		for (int i=0;i<A.length;i++)
		{
			for (int j=0;j<A[i].length;j++)
			{
				System.out.print(A[i][j]+" ");
			}
			System.out.println();
		}
	}

	
	/**
	 * Outputs matrix to console
	 * @param A matrix to print
	 */
	public static void printMatrix(double [][] A)
	{
		for (int i=0;i<A.length;i++)
		{
			for (int j=0;j<A[i].length;j++)
			{
				System.out.print(A[i][j]+" ");
			}
			System.out.println();
		}
	}
	
	



	/**
	 * Sends the relations to mathematica in the sparse array form
	 * The sparse array is asigned to variable "matrix". 
	 * <br> The relations are obtained from the provided iterator;
	 * the method attempts to get num relations, and stops
	 * when the iterator has no next element. Note that this method advances the 
	 * iterator by calling the setFromIterator method.
	 * @param iter	Iterator to get the relations from
	 * @param num	Number of relations to get from the Iterator
	 * @return the number of relations sent
	 * @throws MathLinkException and exception is thrown when Mathematica cries
	 */
	public int putRelationsToMathematica(Iterator<Relation> iter, int num) throws MathLinkException
	{					
		//prepare a set for the putRelationRulesToMathematica method
		HashSet<Relation> chunk = new HashSet<Relation>();
		setFromIterator(iter, chunk, num);
		int count = chunk.size();
		ml.putFunction("SparseArray", 2);
			putRelationRulesToMathematica(chunk);
			int [] size = {count, diags.keySet().size()};
			ml.put(size);				
		//System.out.println("Attempted to send :"+num+"; sent: "+count);
		return count;
	}
	
	
	/**
	 * Sends the relations matrix "rules" to Mathematica ("rules" for sparse array notation)
	 * The relations are obtained from the provided set. 
	 * @param relSet the set to take relations from
	 * @throws MathLinkException
	 */
	public void putRelationRulesToMathematica(Set<Relation> relSet) throws MathLinkException
	{
		ml.putFunction("List", nnz(relSet));
		int row = 0;
		for(Relation R:relSet)
		{
			row++;							//Note that Mathematica rows and columns start with 1 
			for (Integer col:R.relation.keySet())
			{					
				double val = R.relation.get(col);
				ml.putFunction("Rule", 2);
				int [] pos = {row,col+1};
				ml.put(pos);
				ml.put(val);	
			}
		}
	}

	
	
	/**
	 * Takes and iterator and tries to take a specified number of Relations 
	 * from the iterator. The result is added to a set provided to the method.
	 * <br>Note that the iterator is being advanced n times (or till the end of a
	 * a collection, whichever occurs first)
	 * <br> The method returns the number of relations obtained from the Iterator. 
	 * @param iter the Iterator to take the Relations from
	 * @param n number of elements to take (mwthod will stop once the Iterator is depleted, if this occurs sooner)
	 * @param resultSet the set to put the relations into
	 * @return the number of relations obtained from the iterator
	 */
	public int setFromIterator(Iterator<Relation> iter, Set<Relation> resultSet, int n)
	{
		int count = 0;
		for (int i=0;i<n;i++)
		{
			if (iter.hasNext())
			{
				Relation r = iter.next(); 
				resultSet.add(r);
				count++;
			}
		}
		return count;
	}
	
	
	/**
	 * Returns the number of non-zero entries in a relation set
	 * @param relSet the set with relations
	 * @return number of nonzero entries in the set
	 */
	public int nnz(Set<Relation> relSet)
	{
		int count = 0;
		for (Relation r : relSet)
		{
			count += r.size();
		}
		return count;
	}
	
	
	
	

	/**
	 * Takes a relation matrix (usually this is a result of Mathematica computation)
	 * and puts it into a relation set 
	 * @param data	relation matrix
	 * @param relations	data structure (Set) to store relations in
	 */
	public void processMathematicaOutput(double[][] data, Set<Relation> relations)
	{
		boolean hasnonzero=true;
		int row = 0;
		while (hasnonzero)
		{
			hasnonzero = false;				
			Relation R = new Relation();
			for (int col=0;col<data[row].length;col++)
			{
				if (!Relation.isZero(data[row][col]))
				{
					hasnonzero=true;
					R.putEntry(col, data[row][col]);
				}					
			}
			if (R.size()>0) {relations.add(R);}
			row++;
		}
	}
	
	
	/**
	 * Extracts the basis from the list of independent relations,
	 * once it has been computed. The isInBasis array is filled
	 * with booleans; isinabsis[i]=true means diagram i is in the basis.
	 * @return number of elements in the basis
	 */
	public int extractBasis()
	{
		isInBasis = new boolean[diags.keySet().size()];
		int count = isInBasis.length;
		Arrays.fill(isInBasis, true);
		for (Relation R:indRelations)
		{
			int pivot = R.relation.firstKey();
			isInBasis[pivot]=false;
			count--;
		}
		return count;
	}
	

	
	/**
	 * Once the basis has been computed (after Mathematica computations were run),
	 * this method draws the diagrams in the basis and saves them into files.
	 * @param DDrawer diagram drawer to draw basis with 
	 */
	public void DrawBasis(DiagramDrawer DDrawer)
	{
		String fname="basis_"+n+"-"+k+"_";		
		ArrayList<String> basiskeys = new ArrayList<String>();

		//construct a list of keys for the basis
		for (int i=0;i<isInBasis.length;i++)
		{
			if (isInBasis[i])
			{
				basiskeys.add(keys.get(i));
			}
		}		
		DDrawer.DrawAndSave(diags, basiskeys, fname);
	}

	
	
	/**
	 * Draws the set of 4T relations computed by this class with a DDRawer 
	 * @param DDrawer DiagramDrawer to draw the relations with
	 */
	public void DrawRelations(DiagramDrawer DDrawer)
	{
		DrawRelations(DDrawer,indRelations);
	}
		
	
	/**
	 * Saves the independent relations  to the file
	 * The name of the file depends on n and k ans is returned by indRelationDataFname() function
	 * @return true if the relations were saved successfully; false otherwise
	 */
	public boolean saveIndRelations()
	{		
		return ConfigutaionHolder.saveObject(indRelations, indRelationDataFname());
	}


	
	/**
	 * Load the independent relation data from file. The filename is given by indRelationDataFname() function.
	 * @return true if the data was loaded successfully
	 */
	@SuppressWarnings("unchecked")
	public boolean loadIndRelations()
	{				
		String fname = indRelationDataFname(); 
		TreeSet<Relation> loaded = (TreeSet<Relation>)ConfigutaionHolder.loadObject(fname);
		if (loaded!=null) 
		{
			System.out.println("Independent Relations loaded successfully from "+fname);
			indRelations=loaded;
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Generates a name of the file to save relations in.
	 * If the file with such a name is present, data is loaded from the file
	 * instead of being generated.
	 * @return name of the file with relations
	 */
	public String indRelationDataFname()
	{
		String fname = "ind_"+this.relationDataFname();
		return fname;
	}
	
	
	
	/**
	 * Tests the mega conjecture that all relations have size 2.
	 * @return true if the conjecture is true
	 */
	public boolean TestMegaConjecture()
	{
		for (Relation R:indRelations)
		{
			if(R.size()!=2)
			{
				return false;
			}
		}
		return true;
	}
	
	
}
