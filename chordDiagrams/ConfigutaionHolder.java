package chordDiagrams;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

/**
 * This class is to provide an easy way to access lists of diagrams.
 * This allows to save computation time by not-recomputing known lists.
 * @author Romwell
 *
 */
public abstract class ConfigutaionHolder {

	/**
	 * File with diagrams
	 */
	public  HashMap<String, diagram> configs;
	
	/**
	 * Saves the daigrams (i.e., the configs arraylist) to the file
	 * The filename is given by getFileName() function and is determined by n and k.
	 * @return true if the diagram data was saved successfully
	 */
	public boolean saveConfigs()
	{		
		return saveObject(configs, getFileName());
	}

	/**
	 * Load the diagram data from file. The filename is given by getFileName() function and is determined by n and k.
	 * @return true if the diagrams were loaded successfully
	 */
	@SuppressWarnings("unchecked")
	public boolean loadConfigs()
	{				
		String fname = getFileName();
		HashMap<String, diagram> loaded = (HashMap<String, diagram>)loadObject(fname);
		if (loaded!=null)
		{
			configs = loaded;
			System.out.println("The diagram data was successfully loaded from "+fname);
			return true;
		}
		else
		{
			return false;
		}
	}

	
	
	/**
	 *checks whether a given configuration is new, i.e. not in the list
	 *@param a the diagram to check for presence in the list of diagrams 
	 */
	public boolean isNew(diagram a)
	{
		return (!(configs.containsKey(a.diagID)));
	}
		
	
	/**
	 * Generates a filename to store the diagram list into.
	 * Must be overriden in subsequent implementations to provide
	 * different file names for different type of lists.
	 * @return digram list filename
	 */
	public abstract String getFileName();
	
	
	/**
	 * Attempts to load an Object from file. NOTE: It is your responsibility
	 * to make sure that the Object implements Serializable, that you are
	 * loading the correct class, etc.
	 * @param fname	Filename to load the object from
	 * @return the object, if it was loaded successfully; null otherwise
	 */
	public static Object loadObject(String fname)
	{
		Object obj;
		File f = new File(fname);
		if (f.exists()) 
		{
		FileInputStream fis;
		ObjectInputStream ois;
			try
			{
				fis = new FileInputStream(fname);
				ois = new ObjectInputStream(fis);			
				obj = ois.readObject();
				ois.close();
				return obj;
			}
			catch(IOException ex)
			{
				System.out.println("Aaa! File I/O Exception: "+ex);
				return null;
			}
			catch(ClassNotFoundException ex)
			{
				System.out.println("Aaa! Class Exception: "+ex);
				return null;
			}
		}
		else {return null;}
	}
	
	/**
	 * Saves an object to file to a file with a given filename
	 * @param obj 	The object to save to a file
	 * @param fname The filename to save to
	 * @return true, if the object was saved successfully; false otherwise
	 */
	public static boolean saveObject(Object obj, String fname)
	{		
	FileOutputStream fos;
	ObjectOutputStream oos;
	try
		{
			fos = new FileOutputStream(fname);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(obj);
			oos.close();
		}
		catch(IOException ex)
		{
			System.out.println("Cannot save diagram data to disk!");
			return false;
		}
		return true;
	}
	
}
