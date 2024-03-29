package utilities;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


/**
 * @author Anthony Jackson
 *
 *	CSVOutput writes out each players gps data file in csv format
 *
 */
public class CSVOutput {
	
		//public static String destinationFile;
		//public static int fileVersion = 1;
		//public static String fileExtension = ".csv";
	
	public CSVOutput(){		
	}
	
	public String writeOutFile(String filepath, String filename, List<String> datapoints){
		
		File file = new File(filepath + filename);
		// find the next available filename
		
		// create the new file
		if ( !file.exists() ){
			try {
				
				file.createNewFile();
			} catch (IOException e1) {
				// TODO return an error message to the user
				e1.printStackTrace();
			}
	      }	
		
		PrintWriter out = null;
		try {
			out = new PrintWriter(file, "UTF-8");
			

			for(String datapoint : datapoints){				
				out.println(datapoint);
			}
			
		} catch (Exception e) {
			// TODO return an error message to the user
			e.printStackTrace();
		}	finally {
			out.flush();
			out.close();
		}
		
		return filename;
	}
	
	
}

