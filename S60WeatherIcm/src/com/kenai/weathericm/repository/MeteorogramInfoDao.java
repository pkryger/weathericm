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

import java.util.Vector;
import com.kenai.weathericm.domain.MeteorogramInfo;

/**
 * This is a contract for CRUD operations for {@link MeteorogramInfo} objects 
 * in store. In addition implementations needs to implement two convenience
 * methods for reading all elements in store and for 'persists' given
 * {@link MeteorogramInfo} regardless it exists or not in store. Implementations
 * will relay on the {@link MeteorogramInfoSerializer} for transforming
 * {@link MeteorogramInfo}s into/from store format data.
 * @author Przemek Kryger
 * @see MeteorogramInfoSerializer
 */
public interface MeteorogramInfoDao {

    /**
     * Creates given {@code info} in store. If something went wrong the {@code info}
     * is tainted.
     * @param info the {@link MeteorogramInfo} to be created in store.
     */
    void create(MeteorogramInfo info);

    /**
     * Updates given {@code info} in store. If something wen't wrong the {@code info}
     * is tainted.
     * @param info the {@link MeteorogramInfo} to be updated in store.
     */
    void update(MeteorogramInfo info);

    /**
     * Deletes given {@code info} from store.
     * @param info the {@link MeteorogramInfo} to be deleted from store.
     */
    void delete(MeteorogramInfo info);

    /**
     * Convenience method to persists {@code info} in store. It creates an entry
     * if one doesn't exist in store or update it if it already exists in store.
     * @param info the {@link MeteorogramInfo} to be persisted.
     */
    void createOrUpdate(MeteorogramInfo info);

    /**
     * Reads {@link MeteorogramInfo} with given {@code id} from store.
     * @param id the {@code int} of the entry in store to be read.
     * @return the {@link MeteorogramInfo} read from store or {@code null} if an
     * error occurred.
     */
    MeteorogramInfo read(int id);

    /**
     * Convenience method to read all {@link MeteorogramInfo} entries from store.
     * The order of read elements depends on given implementation.
     * @return the {@link Vector} with all {@link MeteorogramInfo}s in store.
     */
    Vector readAll();

    /**
     * Getter for serialized used to encode/decode data for store.
     * @return the {@link MeteorogramInfoSerializer}.
     */
    MeteorogramInfoSerializer getMeteorogramInfoSerializer();

    /**
     * Setter for serializer used to encode/decode data for store.
     * @param meteorogramInfoSerializer the {@link MeteorogramInfoSerializer} to set.
     */
    void setMeteorogramInfoSerializer(MeteorogramInfoSerializer meteorogramInfoSerializer);
}
