/*******************************************************************************
 * Copyright (c) 2013 Stefan Schroeder.
 * eMail: stefan.schroeder@kit.edu
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
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
		File file = new File(filename);
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

	/**
	 * @param filename relative path from within the resource directory to a file to be loaded
	 * @return a Stream to the requested resource file, or <code>null</code> if no such file exists.
	 */
	public final static InputStream getAsInputStream(final String filename) {
		// look for the file locally
		try {
			return new FileInputStream("/" + filename);
		} catch (FileNotFoundException e) {
			log.info("Resource '" + filename + "' not found locally. May not be fatal.");
			// just continue, maybe we have more luck in the classpath
		}
		// maybe we find the file in the classpath, possibly inside a jar-file
		InputStream stream = Resource.class.getResourceAsStream("/" + filename);
		if (stream == null) {
			log.warn("Resource '" + filename + "' not found!");
		}
		return stream;
	}
	
}
