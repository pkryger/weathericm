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

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import javax.microedition.lcdui.Image;
//#mdebug
import net.sf.microlog.core.Logger;
import net.sf.microlog.core.LoggerFactory;
//#enddebug

/**
 * This is a wrapper for the forecast data to display. It consists of two members:
 * {@value ForecastData#modelStart} that indicates the date when the forecast data was
 * generated and {@value ForecastData#modelResult} that contains the {@link Image} data
 * to display as a forecast.
 * @author Przemek Kryger
 */
public class ForecastData {

//#mdebug
    /**
     * The logger for the class.
     */
    private final static Logger log = LoggerFactory.getLogger(ForecastData.class);
//#enddebug
    /**
     * The time when the model for forecast has been started.
     */
    private Date modelStart;
    /**
     * The model results in a form of {@code byte[]} array that can be transformed
     * into image and displayed as forecast.
     */
    private byte[] modelResult;

    /**
     * Creates a new instance using a passed in {@link String} to set up
     * {@value #modelStart}. The {@code yyyymmddhh} shall adhere to the folowing
     * rules:
     * <ol>
     * <li> yyyy - a year in four digits format.</li>
     * <li> mm - month in two digits format (1-based, range: 1..12).</li>
     * <li> dd - day of the month in two digits format (1-based, range: 1..31).</li>
     * <li> hh - hour of the day in two digits format (24H format, range: 0..23).</li>
     * </ol>
     * @param yyyymmddhh the {@link String} with
     * @throws NullPointerException if {@code yyyymmddhh} is {@code null}.
     * @throws IllegalArgumentException if any part of the parameter is out of range.
     * @throws NumberFormatException if any part of the parameter cannot be converted
     *                               to a number.
     */
    public ForecastData(String yyyymmddhh) {
        if (yyyymmddhh == null) {
//#mdebug
            log.error("Cannot create ForecastData with null date String!");
//#enddebug
            throw new NullPointerException("Cannot create ForecastData with null date String!");
        }
        int year = Integer.parseInt(yyyymmddhh.substring(0, 4));
        int month = Integer.parseInt(yyyymmddhh.substring(4, 6));
        int day = Integer.parseInt(yyyymmddhh.substring(6, 8));
        int hour = Integer.parseInt(yyyymmddhh.substring(8, 10));
        setModelStart(year, month, day, hour);
    }

    /**
     * Creates a new instance using passed in {@code int}s to set up
     * {@value #modelStart}.
     * @param year an {@code int} for a year .
     * @param month an {@code int} for a month (1-based, range: 1..12).
     * @param day an {@code int} for a day of the month (1-based, range: 1..31).
     * @param hour an {@code int} for a hour of the day (range: 0..23).
     * @throws IllegalArgumentException if any of the parameters is out of range.
     */
    public ForecastData(int year, int month, int day, int hour) {
        setModelStart(year, month, day, hour);
    }

    /**
     * Creates a new instance using passed in {@code other} {@link ForecastData}
     * to set up {@value #modelStart).
     * @param other the {@link ForecastData} to take the {@value #modelStart} from
     *        to init this instance.
     * @throws NullPointerException if the {@code other} is {@code null} or
     *                              {@value other#modelStart} is {@code null}.
     */
    protected ForecastData(ForecastData other) {
        if (other == null) {
//#mdebug
            log.error("Cannot create ForecastData with null other!");
//#enddebug
            throw new NullPointerException("Cannot create ForecastData with null other!");
        }
        this.modelStart = other.getModelStart();
        if (this.modelStart == null) {
//#mdebug
            log.error("Cannot create ForecastData with null other's startDate: " + other);
//#enddebug
            throw new NullPointerException("Cannot create ForecastData with not valid other!");
        }

    }

    /**
     * Check if passed values are in range. If not, {@link IllegalArgumentException}
     * is thrown.
     * @param year range 1000..9999
     * @param month range 1..12
     * @param day range 1..31
     * @param hour range 0..23
     * @throws IllegalArgumentException if any one of passed parameters is out of range.
     */
    private void setModelStart(int year, int month, int day, int hour) {
        if (year < 1000 || year > 9999) {
//#mdebug
            log.error("Cannot create an instance with year = " + year);
//#enddebug
            throw new IllegalArgumentException("The year must be within 1000..9999 range");
        }
        if (month < 1 || month > 12) {
//#mdebug
            log.error("Cannot create an instance with a month = " + month);
//#enddebug
            throw new IllegalArgumentException("The month must be within 1..12 range");
        }
        if (day < 1 | day > 31) {
//#mdebug
            log.error("Cannot create an instance with a day = " + day);
//#enddebug
            throw new IllegalArgumentException("The day must be within 1..31 range");
        }
        if (hour < 0 || hour > 23) {
//#mdebug
            log.error("Cannot create an instance with a hour = " + hour);
//#enddebug
            throw new IllegalArgumentException("The hour must be within 0..23 range");
        }
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        modelStart = calendar.getTime();
    }

    /**
     * @return the {@link Date} when the model has been staretd.
     */
    public Date getModelStart() {
        return modelStart;
    }

    /**
     * @return the {@link Image} that contains forecast model result.
     */
    public byte[] getModelResult() {
        return modelResult;
    }

    /**
     * @param modelResult the {@code byte[]} array for forecast model result to set.
     */
    public void setModelResult(byte[] modelResult) {
        this.modelResult = modelResult;
    }

    /**
     * Converts this instance into {@link String}.
     * @return the {@link String} representing this instance.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer("[");
        buffer.append(modelStart.toString()).append("] ").append(modelResult == null ? "w/o data" : "w/ data");
        return buffer.toString();
    }
}
