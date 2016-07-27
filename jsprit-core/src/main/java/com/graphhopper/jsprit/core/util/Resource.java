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
package com.graphhopper.jsprit.core.util;




import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * @author stefan schroeder, pierre-david belanger
 */
public class Resource {

    private static Logger log = LoggerFactory.getLogger(Resource.class);

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
