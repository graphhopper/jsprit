/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package jsprit.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

/**
 * 
 * @author stefan schroeder
 *
 */
public class Resource {
	
	private static Logger log = Logger.getLogger(Resource.class);
	
	public final static URL getAsURL(final String filename) {
		File file = new File(filename);
		if (file.exists()) {
			try {
				return file.toURI().toURL();
			} catch (MalformedURLException e) {
				log.warn("Even resource exists, could not return its URL.", e);				
			}
		}
		URL url = Resource.class.getResource("/" + filename);
		if (url == null) {
			log.warn("Could not find resource '" + filename + "'!");
		}
		return url;
	}

	public final static InputStream getAsInputStream(final String filename) {
		try {
			return new FileInputStream("/" + filename);
		} catch (FileNotFoundException e) {
			log.info("Could not find '" + filename + "'!.");
		}
		InputStream stream = Resource.class.getResourceAsStream("/" + filename);
		if (stream == null) {
			log.warn("Could not find resource '" + filename + "'!");
		}
		return stream;
	}
	
}
