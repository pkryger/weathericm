/*
 *  Copyright (C) 2010 Przemek Kryger
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.kenai.weathericm.util;

import java.io.InputStream;
import java.util.Hashtable;
//#mdebug
import net.sf.microlog.core.Logger;
import net.sf.microlog.core.LoggerFactory;
//#enddebug

/**
 * This defines the repository for properties. In general this shall be used
 * to obtain instances of {@link Properties} within this application.
 * @author Przemek Kryger
 */
public class PropertiesRepository {
//#mdebug

    /**
     * The logger for the class.
     */
    private final static Logger log = LoggerFactory.getLogger(PropertiesRepository.class);
//#enddebug
    /**
     * The instance of {@link PropertiesRepository} for singleton.
     */
    private final static PropertiesRepository instance = new PropertiesRepository();
    /**
     * The storage for read properties.
     */
    private Hashtable cachedProperties = new Hashtable(2);

    /**
     * Private constructor for singleton.
     */
    private PropertiesRepository() {
//#mdebug
        log.info("Created repository for properties");
//#enddebug
    }

    /**
     * Obtains {@link Properties} instance for a given {@code fileName}. Note, thet
     * the instance will be cached, hence succesive invocations will return the same
     * object.
     * @param fileName the {@link String} to the resource that represents properties.
     * @return the {@link Properties} read from {@code fileName}.
     */
    public static Properties getProperties(String fileName) {
        Properties properties = (Properties)instance.cachedProperties.get(fileName);
        if (properties == null) {
            properties = new Properties();
            InputStream is = PropertiesRepository.class.getResourceAsStream(fileName);
            properties.load(is);
            instance.cachedProperties.put(fileName, properties);
        }
        return properties;
    }
}
