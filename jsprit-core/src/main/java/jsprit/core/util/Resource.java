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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * 
 * @author stefan schroeder, pierre-david belanger
 *
 */
public class Resource {
	
	private static Logger log = LogManager.getLogger(Resource.class);
	
	public final static URL getAsURL(final String filename) {
		URL url = Resource.class.getClassLoader().getResource(filename);
		if (url != null) {
			return url;
		}
		log.debug("resource: " + filename + " is unreachable by the current class loader, try the filesystem");
		File file = new File(filename);
		if (!file.exists()) {
			log.debug("resource: " + filename + " do not exists on the filesystem");
			return null;
		}
		try {
			return file.toURI().toURL();
		} catch (MalformedURLException e) {
			log.debug("resource: " + filename + " exists on the filesystem, but its URL is invalid: " + e.getMessage());
			return null;
		}
	}

	public final static InputStream getAsInputStream(final String filename) {
		InputStream stream = Resource.class.getClassLoader().getResourceAsStream(filename);
		if (stream != null) {
			return stream;
		}
		log.debug("resource: " + filename + " is unreachable by the current class loader, try the filesystem");
		File file = new File(filename);
		if (!file.exists()) {
			log.debug("resource: " + filename + " do not exists on the filesystem");
			return null;
		}
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			log.debug("resource: " + filename + " exists on the filesystem, but its URL is invalid: " + e.getMessage());
			return null;
		}
	}
	
}
