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
package com.kenai.weathericm.repository;

import com.kenai.weathericm.domain.ForecastData;

/**
 * This is a helper interface for {@link ForecastDataDao} that is responsible for
 * serializing and resurecting {@link ForecastData} objects.
 * @author Przemek Kryger
 */
public interface ForecastDataSerializer {

    /**
     * Transforms given {@code forecastData} into array of {@code byte}s. The
     * implementation shall be reverse to the {@value #resurect(byte)}.
     * @param forecastData the {@link ForecastData} to be serialized. 
     * @return the array of {@code byte}s that represents the data.
     * @see #resurect(data)
     */
    byte[] serialize(ForecastData forecastData);

    /**
     * Creates the {@link ForecastData} from given array of {@code bytes}. The
     * implementation shall be reverse to the {@value
     * #serialize(com.kenai.weathericm.domain.ForecastData)}.
     * @param data the {@code byte} array to create {@link ForecastData} from.
     * @return the {@link ForecastData} created form {@code data}s
     */
    ForecastData resurect(byte[] data);
}
