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

/**
 * This is a simple enumertion for {@link MeteorogramInfo} to easy report it's
 * {@link ForecastData} availability.
 * @author Przemek Kryger
 */
public class Availability {

    public final static Availability NOT_AVALIABLE = new Availability("Not Available");
    public final static Availability AVALIABLE = new Availability("Available");
    public final static Availability AVALIABLE_OLD = new Availability("Available Old");

    /**
     * Creates a new {@link Availability} with {@code displayString}
     * as a presentation name.
     * @param displayString the {@link String} to be used as a presentation name.
     */
    private Availability(String displayString) {
        this.displayString = displayString;
    }
    /**
     * This availability diplay string used to present it by {@value #toString()}
     */
    private String displayString;

    /**
     * Gives this {@link Availability} display string.
     * @return the {@link String} to be used as a presentation name.
     */
    public String toString() {
        return displayString;
    }
}
