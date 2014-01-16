package jsprit.util;

import java.io.File;

public class Examples {
	
	public static void createOutputFolder(){
		File dir = new File("output");
		// if the directory does not exist, create it
		if (!dir.exists()){
			System.out.println("creating directory ./output");
			boolean result = dir.mkdir();  
			if(result) System.out.println("./output created");  
		}
	}

}
