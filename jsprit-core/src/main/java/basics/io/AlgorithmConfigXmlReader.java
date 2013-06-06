/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package basics.io;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import util.Resource;

public class AlgorithmConfigXmlReader {
	
	private static Logger log = Logger.getLogger(AlgorithmConfigXmlReader.class);
	
	private AlgorithmConfig algorithmConfig;
	
	private boolean schemaValidation = true;
	
	/**
	 * @param schemaValidation the schemaValidation to set
	 */
	public void setSchemaValidation(boolean schemaValidation) {
		this.schemaValidation = schemaValidation;
	}

	public AlgorithmConfigXmlReader(AlgorithmConfig algorithmConfig){
		this.algorithmConfig = algorithmConfig;
	}
	
	public void read(String filename){
		log.info("read algorithm-config from file " + filename);
		algorithmConfig.getXMLConfiguration().setFileName(filename);
		algorithmConfig.getXMLConfiguration().setAttributeSplittingDisabled(true);
		algorithmConfig.getXMLConfiguration().setDelimiterParsingDisabled(true);
		
		if(schemaValidation){
			final URL resource = Resource.getAsURL("algorithm_schema.xsd");
			if(resource != null) {
				EntityResolver resolver = new EntityResolver() {

					@Override
					public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
						{
							InputSource is = new InputSource(resource.getFile());
							return is;
						}
					}
				};
				algorithmConfig.getXMLConfiguration().setEntityResolver(resolver);
				algorithmConfig.getXMLConfiguration().setSchemaValidation(true);
				log.info("validating " + filename + " with xsd-schema");
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

}
