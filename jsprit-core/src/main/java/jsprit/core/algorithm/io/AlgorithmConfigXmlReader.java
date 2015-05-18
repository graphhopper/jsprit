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
package jsprit.core.algorithm.io;

import jsprit.core.util.Resource;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


public class AlgorithmConfigXmlReader {
	
	private static Logger log = LogManager.getLogger(AlgorithmConfigXmlReader.class.getName());
	
	private AlgorithmConfig algorithmConfig;
	
	private boolean schemaValidation = true;
	
	/**
	 * @param schemaValidation the schemaValidation to set
	 */
	public AlgorithmConfigXmlReader setSchemaValidation(boolean schemaValidation) {
		this.schemaValidation = schemaValidation;
		return this;
	}

	public AlgorithmConfigXmlReader(AlgorithmConfig algorithmConfig){
		this.algorithmConfig = algorithmConfig;
	}
	
	public void read(URL url){
		log.debug("read algorithm: " + url);
		algorithmConfig.getXMLConfiguration().setURL(url);
		algorithmConfig.getXMLConfiguration().setAttributeSplittingDisabled(true);
		algorithmConfig.getXMLConfiguration().setDelimiterParsingDisabled(true);
		
		if(schemaValidation){
			final InputStream resource = Resource.getAsInputStream("algorithm_schema.xsd");
			if(resource != null) {
				EntityResolver resolver = new EntityResolver() {

					@Override
					public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
						{
							InputSource is = new InputSource(resource);
							return is;
						}
					}
				};
				algorithmConfig.getXMLConfiguration().setEntityResolver(resolver);
				algorithmConfig.getXMLConfiguration().setSchemaValidation(true);
			}
			else{
				log.warn("cannot find schema-xsd file (algorithm_xml_schema.xsd). try to read xml without xml-file-validation.");
			}		
		}
		try {
			algorithmConfig.getXMLConfiguration().load();
		} catch (ConfigurationException e) {
			log.error(e);
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	
	public void read(String filename){
		log.debug("read algorithm-config from file " + filename);
		URL url = Resource.getAsURL(filename);
		read(url);
	}

}
