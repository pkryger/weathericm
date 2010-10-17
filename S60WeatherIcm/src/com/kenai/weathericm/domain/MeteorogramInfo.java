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

import com.kenai.weathericm.app.ComparableForecastData;
import java.util.Date;
//#mdebug
import net.sf.microlog.core.Logger;
import net.sf.microlog.core.LoggerFactory;
//#enddebug

/**
 * The meteorogram info type. It becomes tainted whenever the instance needs to be
 * persisted in storage.
 * @author Przemek Kryger
 */
public class MeteorogramInfo {

    /**
     * The maximum value that can be set ot instance {@value x}.
     */
    public final static int MAX_X = 430;
    /**
     * The maximum value that can be set ot instance {@value y}.
     */
    public final static int MAX_Y = 598;
//#mdebug
    /**
     * The logger for class.
     */
    private final static Logger log = LoggerFactory.getLogger(MeteorogramInfo.class);
//#enddebug
    /**
     * Marks if instance needs to be persisted.
     */
    private boolean tainted = false;
    /**
     * Marks if instance is in storage.
     */
    private Integer id;
    /**
     * The instance name.
     */
    private String name = "";
    /**
     * The instance x.
     */
    private int x = 0;
    /**
     * The instance y.
     */
    private int y = 0;
    /**
     * The instance model type.
     */
    private MeteorogramType type = MeteorogramType.UM;
    /**
     * The {@link ForecastData} that contains forecast for this info.
     * Based on this the {@value #dataAvailability} is calculated.
     */
    private ForecastData forecastData = null;

    /**
     * If this is not-{@code null} then object is marked as stored.
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * It checks if {@code id} is {@code null} (for marking instance as not stored)
     * or if current (@value #id) is null (for marking instance as stored).
     * <p>
     * @param id the id to set
     */
    public void setId(Integer id) {
        if (id == null || this.id == null) {
//#mdebug
            log.debug("Setting a new id: new = " + id + ", old = " + this.id);
//#enddebug
            this.id = id;
        } else {
//#mdebug
            log.error("Attempt to change id from one value to another!");
//#enddebug
            throw new IllegalArgumentException("The Id is already set to "
                    + this.id + ". Cannot change it!");
        }
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * It checks if {@code name} is not {@code null}.
     * <p>
     * If the {@code name} is different than current {@value #name},
     * the obejct becomes tainted.
     * @param name the name to set
     */
    public void setName(String name) {
        if (name == null) {
//#mdebug
            log.error("Attempt to set name to null!");
//#enddebug
            throw new NullPointerException("The name cannot be null!");
        } else if (!name.equals(this.name)) {
            tainted = true;
            this.name = name;
        }
    }

    /**
     * @return the x
     */
    public int getX() {
        return x;
    }

    /**
     * It checks if {@code x} is greater or equal to 0 and lower or equal to
     * {@value #MAX_X}.
     * <p>
     * If the {@code x} is different than current {@value #x},
     * the obejct becomes tainted.
     * @param x the x to set
     */
    public void setX(int x) {
        if (x < 0) {
//#mdebug
            log.error("Attempt to set X lower than 0!");
//#enddebug
            throw new IllegalArgumentException("The X value cannot be lower than 0!");
        } else if (x > MAX_X) {
//#mdebug
            log.error("Attempt to set X greather than " + MAX_X + "!");
//#enddebug
            throw new IllegalArgumentException("The X value cannot be greater than "
                    + MAX_X + "!");
        } else if (x != this.x) {
            tainted = true;
            this.x = x;
            forecastData = null;
        }
    }

    /**
     * @return the y
     */
    public int getY() {
        return y;
    }

    /**
     * It checks if {@code y} is greater or equal to 0 and lower or equal to
     * {@value #MAX_Y}.
     * <p>
     * If the {@code y} is different than current {@value #y},
     * the obejct becomes tainted.
     * @param y the y to set
     */
    public void setY(int y) {
        if (y < 0) {
//#mdebug
            log.error("Attempt to set Y lower than 0!");
//#enddebug
            throw new IllegalArgumentException("The Y value cannot be lower than 0!");
        } else if (y > MAX_Y) {
//#mdebug
            log.error("Attempt to set Y greather than " + MAX_Y + "!");
//#enddebug
            throw new IllegalArgumentException("The Y value cannot be greater than "
                    + MAX_Y + "!");
        } else if (y != this.y) {
            tainted = true;
            this.y = y;
            forecastData = null;
        }

    }

    /**
     * If it is {@code true} then object need to be persisted.
     * @return the tainted
     */
    public boolean isTainted() {
        return tainted;
    }

    /**
     * @param tainted the tainted to set
     */
    public void setTainted(boolean tainted) {
        this.tainted = tainted;
//#mdebug
        log.debug(this + ": Now I'm" + (tainted ? " " : " not ") + "tainted");
//#enddebug
    }

    /**
     * @return the type
     */
    public MeteorogramType getType() {
        return type;
    }

    /**
     * If the {@code type} is different than current {@value #type},
     * the obejct becomes tainted.
     * @param type the type to set
     */
    public void setType(MeteorogramType type) {
        if (type == null) {
//#mdebug
            log.error("Attempt to set type to null!");
//#enddebug
            throw new NullPointerException("The type cannot be null!");
        } else if (!MeteorogramType.getAllTypes().contains(type)) {
//#mdebug
            log.error("Attempt to set type to unknown type: " + type);
//#enddebug
            throw new IllegalArgumentException("Cannot set type to: "
                    + type + "!");
        } else if (!type.equals(this.type)) {
            tainted = true;
            this.type = type;
            forecastData = null;
        }
    }

    /**
     * Gets the current forecast data for this info.
     * @return the {@link ForecastData} for this info.
     */
    public ForecastData getForecastData() {
        return forecastData;
    }

    /**
     * Sets the forecast data for this info.
     * @param data the {@link ForecastData} to be set.
     */
    public void setForecastData(ForecastData data) {
//#mdebug
        log.debug("Setting data to: " + data);
//#enddebug
        this.forecastData = data;
    }

    /**
     * Checks if this info has forecast data. If it has the data, then it's checked
     * if it's not to old.
     * @return {@code true} if this info has forecast data, {@code false} otherwise.
     */
    public Availability dataAvailability() {
        Availability retValue = null;
        if (forecastData != null) {
            ComparableForecastData myData = new ComparableForecastData(forecastData);
            Date now = new Date();
            if (myData.isOlderThan(now)) {
                retValue = Availability.AVAILABLE_OLD;
            } else {
                retValue = Availability.AVAILABLE;
            }
        } else {
            retValue = Availability.NOT_AVAILABLE;
        }
        return retValue;
    }

    /**
     * Converts this instance to a {@link String}.
     * @return the {@link String} representation.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer(getName());
        buffer.append(" [").append(getX()).append(", ").append(getY()).append("]: ").append(getType());
        return buffer.toString();
    }
}
