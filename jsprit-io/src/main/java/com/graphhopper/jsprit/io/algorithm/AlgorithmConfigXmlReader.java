/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.graphhopper.jsprit.io.algorithm;

import com.graphhopper.jsprit.core.util.Resource;
import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


public class AlgorithmConfigXmlReader {

    private static Logger log = LoggerFactory.getLogger(AlgorithmConfigXmlReader.class.getName());

    private AlgorithmConfig algorithmConfig;

    private boolean schemaValidation = true;

    /**
     * @param schemaValidation the schemaValidation to set
     */
    public AlgorithmConfigXmlReader setSchemaValidation(boolean schemaValidation) {
        this.schemaValidation = schemaValidation;
        return this;
    }

    public AlgorithmConfigXmlReader(AlgorithmConfig algorithmConfig) {
        this.algorithmConfig = algorithmConfig;
    }

    public void read(URL url) {
        log.debug("read algorithm: " + url);
        algorithmConfig.getXMLConfiguration().setURL(url);
        algorithmConfig.getXMLConfiguration().setAttributeSplittingDisabled(true);
        algorithmConfig.getXMLConfiguration().setDelimiterParsingDisabled(true);

        if (schemaValidation) {
            final InputStream resource = Resource.getAsInputStream("algorithm_schema.xsd");
            if (resource != null) {
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
            } else {
                log.warn("cannot find schema-xsd file (algorithm_xml_schema.xsd). try to read xml without xml-file-validation.");
            }
        }
        try {
            algorithmConfig.getXMLConfiguration().load();
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }


    public void read(String filename) {
        log.debug("read algorithm-config from file " + filename);
        URL url = Resource.getAsURL(filename);
        read(url);
    }

}
