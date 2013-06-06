package util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import algorithms.SchrimpfFactory;

/**
 * This is a copy of MatsimResource.java (see matsim.org).
 * 
 * It makes sure that resources can also be located within jar files, since it looks for resources in several places.
 * 
 * @author stefan schroeder
 *
 */
public class Resource {
	
	private static Logger log = Logger.getLogger(Resource.class);
	
	/**
	 * Returns URL from the relative path of a resource.
	 * 
	 * @param filename
	 * @return
	 */
	public final static URL getAsURL(final String filename) {
		// look for the file locally
		File file = new File("/" + filename);
		if (file.exists()) {
			try {
				return file.toURI().toURL();
			} catch (MalformedURLException e) {
				log.warn("Found resource-file, but could not return URL for it.", e);				// just continue, maybe we have more luck in the classpath
			}
		}
		// maybe we find the file in the classpath, possibly inside a jar-file
		URL url = SchrimpfFactory.class.getResource("/" + filename);
		if (url == null) {
			log.warn("Resource '" + filename + "' not found!");
		}
		return url;
	}

}
