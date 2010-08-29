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

import com.kenai.weathericm.domain.ForecastData;
import java.util.Date;
//#mdebug
import net.sf.microlog.core.Logger;
import net.sf.microlog.core.LoggerFactory;
//#enddebug

/**
 * This is a decorator for {@link ForecastData} that adds posibility to comare
 * {@link ForecastData} instances.
 * @author Przemek Kryger
 */
public class ComparableForecastData extends ForecastData {
//#mdebug

    /**
     * The logger for the class.
     */
    private final static Logger log = LoggerFactory.getLogger(ComparableForecastData.class);
//#enddebug
    /**
     * The instance this class decorates.
     */
    private ForecastData decorated;
    /**
     * The offset used to calculate {@value #isNewerThan()} and {@value #isOlderThan()}.
     * The default is 7h.
     */
    private static long offset = 7 * 3600 * 1000;

    /**
     * Creates a new instance that decorates {@code other}.
     * @param decorated the {@link ForecastData} instance to decorate.
     * @throws NullPointerException if the {@code decorated} is {@code null} or
     *                              {@value decorated#modelStart} is null.
     */
    public ComparableForecastData(ForecastData decorated) {
        super(decorated);
        this.decorated = decorated;
    }

    /**
     * @return the {@link Date} when the model has been staretd.
     */
    public Date getModelStart() {
        return decorated.getModelStart();
    }

    /**
     * @return the {@link Image} that contains forecast model result.
     */
    public byte[] getModelResult() {
        return decorated.getModelResult();
    }

    /**
     * @param modelResult the {@code byte[]} array for forecast model result to set.
     */
    public void setModelResult(byte[] modelResult) {
        decorated.setModelResult(modelResult);
    }

    /**
     * Converts this instance into {@link String}.
     * @return the {@link String} representing this instance.
     */
    public String toString() {
        return decorated.toString();
    }

    /**
     * @return the offset used to calculate {@value #isNewerThan()} and {@value #isOlderThan()}.
     */
    public static long getOffset() {
        return offset;
    }

    /**
     * @param offset the offset used to calculate {@value #isNewerThan()} and {@value #isOlderThan()}.
     */
    public static void setOffset(long offset) {
        if (offset < 0L) {
//#mdebug
            log.error("Cannot set offsset to: " + offset);
//#enddebug
            throw new IllegalArgumentException("Offset must be greater or equal to 0!");
        }
        ComparableForecastData.offset = offset;
    }

    /**
     * Checks if this instance is the same as the {@code other} one.
     * @param other the {@link ForecastData} to comapre this instance with.
     * @return {@code true} if the {@code other} and this instance has equal
     *         {@value #getModelStart()} responses, {@code false} otherwise.
     * @throws NullPointerException if the {@code other} is {@code null}.
     */
    public boolean isSameAs(ForecastData other) {
        if (other == null) {
            log.error("Cannot compare if it is the same with null!");
//#mdebug
            throw new NullPointerException("Cannot compare with null!");
//#enddebug
        }
        return isSameAs(other.getModelStart());
    }

    /**
     * Checks if this instance's {@value #getModelResult()} is the same as the {@code other} date.
     * @param other the {@link Date} to comapre this instance with.
     * @return {@code true} if the {@code other} is equal to this instance
     *         {@value #getModelStart()} response, {@code false} otherwise.
     * @throws NullPointerException if the {@code other} is {@code null}.
     */
    public boolean isSameAs(Date other) {
        if (other == null) {
            log.error("Cannot compare if it is the same with null!");
//#mdebug
            throw new NullPointerException("Cannot compare with null!");
//#enddebug
        }
        return getModelStart().equals(other);
    }

    /**
     * Checks if this instance is the older than {@code other} one.
     * @param other the {@link ForecastData} to comapre this instance with.
     * @return {@code true} if the {@value #getModelStart()} + {@value #offset} is
     *         lower (earlier in time) that the {@value other#getModelStart()},
     *         {@code false} otherwise.
     * @throws NullPointerException if the {@code other} is {@code null}.
     */
    public boolean isOlderThan(ForecastData other) {
        if (other == null) {
            log.error("Cannot compare if it is older with null!");
//#mdebug
            throw new NullPointerException("Cannot compare with null!");
//#enddebug
        }
        return isOlderThan(other.getModelStart());
    }

    /**
     * Checks if this instance is the older than {@code other} date.
     * @param other the {@link ForecastData} to comapre this instance with.
     * @return {@code true} if the {@value #getModelStart()} + {@value #offset} is
     *         lower (earlier in time) that the {@code other},
     *         {@code false} otherwise.
     * @throws NullPointerException if the {@code other} is {@code null}.
     */
    public boolean isOlderThan(Date other) {
        if (other == null) {
            log.error("Cannot compare if it is older with null!");
//#mdebug
            throw new NullPointerException("Cannot compare with null!");
//#enddebug
        }
        return (getModelStart().getTime() + offset)
                < other.getTime();
    }

    /**
     * Checks if this instance is the newer than {@code other} one.
     * @param other the {@link ForecastData} to comapre this instance with.
     * @return {@code true} if the {@value #getModelStart()} is greater (later in time)
     *         than the {@value other#getModelStart()} + {@value #offset},
     *         {@code false} otherwise.
     * @throws NullPointerException if the {@code other} is {@code null}.
     */
    public boolean isNewerThan(ForecastData other) {
        if (other == null) {
            log.error("Cannot compare if it is newer with null!");
//#mdebug
            throw new NullPointerException("Cannot compare with null!");
//#enddebug
        }
        return isNewerThan(other.getModelStart());
    }
        /**
     * Checks if this instance is the newer than {@code other} date.
     * @param other the {@link ForecastData} to comapre this instance with.
     * @return {@code true} if the {@value #getModelStart()} is greater (later in time)
     *         than the {@code other} + {@value #offset},
     *         {@code false} otherwise.
     * @throws NullPointerException if the {@code other} is {@code null}.
     */
    public boolean isNewerThan(Date other) {
        if (other == null) {
            log.error("Cannot compare if it is newer with null!");
//#mdebug
            throw new NullPointerException("Cannot compare with null!");
//#enddebug
        }
        return getModelStart().getTime()
                > (other.getTime() + offset);
    }
}
