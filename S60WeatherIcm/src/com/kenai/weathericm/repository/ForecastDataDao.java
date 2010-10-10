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
 * The implementation will take care of the CRUD operations of given 
 * {@link ForecastData}  based on it's IDs.
 * @author Przemek Kryger
 */
public interface ForecastDataDao {

    /**
     * Adds the given {@code forecastData} to the persistent storage,
     * to be read later by given {@code id}. It's caller resposibility to make
     * make sure no {@link ForecastData} has been already stored with given
     * {@code id}.
     * @param id the {@code Integer} that will be used to identify {@code forecastData}
     * @param forecastData the {@link ForecastData} to be persisted.
     * @return {@code true} if the {@link ForecastData} has been succesfully
     *         persisted, {@code false} otherwise.
     * @see #exists(Integer)
     */
    boolean create(Integer id, ForecastData forecastData);

    /**
     * Reads the {@link ForecastData} from the persistend storage.
     * @param id the {@code Integer} to be used to identify persisted {@link ForecastData}.
     * @return the {@link ForecastData} read from persistent sotrage,
     *         or {@code null} in case of any error.
     */
    ForecastData read(Integer id);

    /**
     * Updates the given {@code forecastData} in the persistent storage.
     * @param id the {@code Integer} to be used to identify {@link ForecastData} to update.
     * @param forecastData the {@link ForecastData} to update in storage.
     * @return {@code true} if the update has been successfull, {@code flase} otherwise.
     */
    boolean update(Integer id, ForecastData forecastData);

    /**
     * Removes the persisted {@link ForecastData} that is identified by a given
     * {@code id} from the persistend storage.
     * @param id the {@code Integer} that is used to identify the {@link ForecastData}
     *        to remove
     * @return {@code true} if the deletion has been successfull, {@code false} otherwise.
     */
    boolean delete(Integer id);

    /**
     * Checks if there is {@link ForecastData} already persisted under given
     * {@code id}.
     * @param id the {@code Integer} to be used for checking.
     * @return {@code true} it there is {@link ForecastData} already persisted,
     *         {@code false} otherwise.
     */
    boolean exists(Integer id);

    /**
     * Creates or updates the {@code forecastData} in the persistent storage. The
     * {@code forecastData} will be created in storage if no other {@link ForecastData}
     * already exisits in the storage that is identifiec by {@code id} or the {@code
     * forecastData} will update the {@link ForecastData} if it already exist in the
     * storage.
     * @param id the {@code Integer} to identify {@code forecastData} in storage.
     * @param forecastData the {@link ForecastData} to be created or updated.
     * @return {@link true} if the operation has been successfull, {@code false} otherwise.
     */
    boolean createOrUpdate(Integer id, ForecastData forecastData);

    /**
     * Sets the serializer for this implementation. It will be used to transform
     * {@link ForecastData} into {@code byte} arrays and resurect {@link ForecastData}s
     * from {@code byte} arrays.
     * @param serializer the serializer to be used.
     */
    void setForecastDataSerializer(ForecastDataSerializer serializer);
}
