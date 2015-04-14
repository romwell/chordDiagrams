package chordDiagrams;
import java.util.Scanner;


/**
 * The driver class to do the work.
 * @author Romwell
 *
 */

public class Driver {
	
	/**
	 * Scanner to read from the input
	 */
	Scanner sc = new Scanner(System.in);
	
	/**
	 * number of chords
	 */
	int n;
	
	/**
	 * number of links  
	 */
	int k;
	
	/**
	 * User option
	 */
	int choice=-1;
	
	/**
	 * Gets input from user and initializes variables	
	 */
	public boolean GetData()
	{		
		if (choice <0)
		{
			System.out.println("Enter your choice: ");
			PrintHelp();
			choice = sc.nextInt();
		}
		if ((choice<1)||(choice>11)) 
		{
			System.out.println("Invalid choice.");
			return false;
		}
		else
		{
			System.out.println("Enter the number of chords and links (or 0 to exit): ");		
			n = sc.nextInt();
			if (n==0) {return false;}
			k = sc.nextInt();
			return true;
		}	
	}

	
	
	
	/**
	 * Prints info about choices
	 */
	public void PrintHelp()
	{
		System.out.println("0: Exit");
		System.out.println("1: Evaluate 4T relations");
		System.out.println("2: Draw diagram equations");
		System.out.println("3: Draw basis");
		System.out.println("4: Calculate relations only");
		System.out.println("5: Generate orbits");
		System.out.println("6: Draw diagrams");
		System.out.println("7: Draw orbits");
		System.out.println("8: Evaluate 4T relations / find basis for Disconnected");
		System.out.println("9: Draw diagram equations for Disconnected");
		System.out.println("10: Draw basis for Disconnected");
		System.out.println("11: Draw diags for Disconnected");
	}
	
	
	/**
	 * Method that does  work
	 */
	public void DoStuff()
	{		
		int MAX = 23455424;
				  	
		
		switch(choice)
		{
			case 1:	 
			{
				FrameGen2 dgen = new FrameGen2(n,k);
				FourTProcessor gen = new FourTProcessor(n,k,MAX,dgen);
				gen.evaluateRelations();
				System.out.println("Conjecture is : "+gen.TestMegaConjecture());
				break;
			}
			case 2: 
			{
				FrameGen2 dgen = new FrameGen2(n,k);
				FourTProcessor gen = new FourTProcessor(n,k,MAX,dgen);
				gen.evaluateRelations();
				DiagramDrawer DDrawer = new DiagramDrawer(50,20,1,1,10);
				gen.DrawRelations(DDrawer);
				break;
			}
			case 3: 
			{
				FrameGen2 dgen = new FrameGen2(n,k);
				FourTProcessor gen = new FourTProcessor(n,k,MAX,dgen);
				gen.evaluateRelations();
				DiagramDrawer DDrawer = new DiagramDrawer(50,20,2,4,10);
				gen.DrawBasis(DDrawer);
				break;
			}
			case 4:
			{
				FrameGen2 dgen = new FrameGen2(n,k);
				FourTGenerator gen = new FourTGenerator(n,k,dgen);
				gen.generateAll4Ts();
				break;
			}
			case 5:
			{
				FrameGen2 dgen = new FrameGen2(n,k);
				OrbitObserver O = new OrbitObserver(n,k,MAX,dgen);
				O.generateAllOrbits();
				O.printOrbits();
				System.out.println("---------------------------------");
				O.generateOrbitLabels();
				O.printOrbitLabels();
				break;
			}
			case 6:
			{
				FrameGen2 dgen = new FrameGen2(n,k);
				FourTGenerator gen = new FourTGenerator(n,k,dgen);
				DiagramDrawer DDrawer = new DiagramDrawer(50,20,1,1,10);
				DDrawer.DrawAndSave(gen.diags, gen.keys, "diags");
				break;
			}
			case 7:
			{
				FrameGen2 dgen = new FrameGen2(n,k);
				DiagramDrawer DDrawer = new DiagramDrawer(50,20,1,1,10);
				OrbitObserver O = new OrbitObserver(n,k,MAX,dgen);
				O.generateAllOrbits();
				O.drawOrbits(DDrawer);
				break;
			}
			case 8:
			{
				FrameGenDisconnected dgen = new FrameGenDisconnected(n,k);
				FourTProcessor gen = new FourTProcessor(n,k,MAX,dgen);
				gen.evaluateRelations();
				break;
			}
			case 9: 
			{
				FrameGenDisconnected dgen = new FrameGenDisconnected(n,k);
				FourTProcessor gen = new FourTProcessor(n,k,MAX,dgen);
				gen.evaluateRelations();
				DiagramDrawer DDrawer = new DiagramDrawer(50,20,1,1,10);
				gen.DrawRelations(DDrawer);
				break;
			}
			case 10: 
			{
				FrameGenDisconnected dgen = new FrameGenDisconnected(n,k);
				FourTProcessor gen = new FourTProcessor(n,k,MAX,dgen);
				gen.evaluateRelations();
				DiagramDrawer DDrawer = new DiagramDrawer(50,20,2,4,10);
				gen.DrawBasis(DDrawer);
				break;	
			}
			case 11:
			{
				FrameGenDisconnected dgen = new FrameGenDisconnected(n,k);
				FourTGenerator gen = new FourTGenerator(n,k,dgen);
				DiagramDrawer DDrawer = new DiagramDrawer(50,20,1,1,10);
				DDrawer.DrawAndSave(gen.diags, gen.keys, "diags");
				break;
			}
		}
		System.out.println("Job complete.");		
	}
	
	public static void main (String[] args)
	{
		Driver d = new Driver();
		while (d.GetData())
		{
			d.DoStuff();
		}
		System.out.println("Good-bye.");
	}
	
	
}
