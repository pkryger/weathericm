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
package com.kenai.weathericm.domain;

import java.util.Enumeration;
import java.util.Vector;
//#mdebug
import net.sf.microlog.core.Logger;
import net.sf.microlog.core.LoggerFactory;
//#enddebug

/**
 * This is a type definition for meteorogram. It works similar to enum.
 * @author Przemek Kryger
 */
public class MeteorogramType {

    /**
     * The UM instance to be used.
     */
    public final static MeteorogramType UM = new MeteorogramType(0, "UM");
    /**
     * The COAMPS instance to be used.
     */
    public final static MeteorogramType COAMPS = new MeteorogramType(1, "COAMPS");
//#mdebug
    /**
     * Thle logger for the class
     */
    private final static Logger log = LoggerFactory.getLogger(MeteorogramType.class);
//#enddebug
    /**
     * The instance value.
     */
    private int value;
    /**
     * The instance name.
     */
    private String name;

    /**
     * Creates a new type with given value and name.
     * @param value the {@code int} value for this instance
     * @param name the {@link String} name for this instance
     */
    protected MeteorogramType(int value, String name) {
        this.value = value;
        this.name = name;
    }

    /**
     * Gets the instance value
     * @return the {@code int} value
     */
    public int getValue() {
        return value;
    }

    /**
     * Gets the instance name
     * @return the {@link String} name
     */
    public String getName() {
        return name;
    }

    /**
     * Converts this instance to {@link String}
     * @return the {@link String} name
     */
    public String toString() {
        return getName();
    }

    /**
     * Convenience method to get the {@link MeteorogramType} instance by given value
     * @param value the {@code int} of the instance
     * @return the instance
     */
    public static MeteorogramType getByValue(int value) {
        MeteorogramType meteorogramType = null;
        Vector all = getAllTypes();
        Enumeration e = all.elements();
        while (e.hasMoreElements()) {
            MeteorogramType mt = (MeteorogramType) e.nextElement();
            if (mt.getValue() == value) {
                meteorogramType = mt;
                break;
            }
        }
//#mdebug
        if (meteorogramType == null) {
            log.warn("Cannot find metoeorogram type for value = " + value);
        } else {
            log.debug("Found type " + meteorogramType + " for value = " + value);
        }
//#enddebug
        return meteorogramType;
    }

    /**
     * Convenience method to get all defined types.
     * @return a {@link Vector} that contains all the defined {@link MeteorogramType}
     *         instances.
     */
    public static Vector getAllTypes() {
        Vector types = new Vector(2);
        types.addElement(UM);
        types.addElement(COAMPS);
        return types;
    }
}
