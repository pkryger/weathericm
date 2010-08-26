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
package com.kenai.weathericm.app.helpers;

import java.util.Vector;
import net.sf.microlog.core.Logger;
import net.sf.microlog.core.LoggerFactory;
import com.kenai.weathericm.domain.MeteorogramInfo;
import com.kenai.weathericm.repository.MeteorogramInfoDao;
import com.kenai.weathericm.repository.MeteorogramInfoSerializer;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple implementation of {@link MeteorogramInfoDao} that uses RAM for storage.
 * Shall be used for debug purposes only.
 * @author Przemek Kryger
 */
public class MeteorogramInfoInMemoryDao implements MeteorogramInfoDao {

    /**
     * The logger for class.
     */
    private final static Logger log = LoggerFactory.getLogger(MeteorogramInfoInMemoryDao.class);
    /**
     * The in-RAM storage. It's simple {@link HashMap}. It maps
     * {@link MeteorogramInfo}'s id to actual object.
     */
    private Map<Integer, MeteorogramInfo> store = new HashMap<Integer, MeteorogramInfo>();
    /**
     * Counter for the last
     */
    private int lastElement = 0;

    /**
     * Creates given {@code info} in store. 
     * @param info the {@link MeteorogramInfo} to be created in store.
     */
    @Override
    public void create(MeteorogramInfo info) {
        log.info("In create...");
        if (info.getId() == null) {
            log.info("Creating object");
            info.setId(new Integer(lastElement++));
            store.put(info.getId(), info);
        } else {
            log.error("Object already exists!");
        }

    }

    /**
     * Updates given {@code info} in store.
     * @param info the {@link MeteorogramInfo} to be updated in store.
     */
    @Override
    public void update(MeteorogramInfo info) {
        log.info("In update...");
        if (info.getId() != null) {
            log.info("Updating object");
            store.put(info.getId(), info);
        } else {
            log.error("Object deosn't exist!");
        }
    }

    /**
     * Deletes given {@code info} from store.
     * @param info the {@link MeteorogramInfo} to be deleted from store.
     */
    @Override
    public void delete(MeteorogramInfo info) {
        log.info("In delete...");
        if (info.getId() != null) {
            log.info("Deleting object");
            store.remove(info.getId());
        } else {
            log.error("Object doesn't exit!");
        }
    }

    /**
     * Convenience method to persists {@code info} in store. It creates an entry
     * if one dosn't exist in store or update it if it already exists in store.
     * @param info the {@link MeteorogramInfo} to be persisted.
     */
    @Override
    public void createOrUpdate(MeteorogramInfo info) {
        log.info("In create or update...");
        if (info.getId() == null) {
            log.info("Going to create");
            create(info);
        } else {
            log.info("Going to update");
            update(info);
        }
    }

    /**
     * Reads {@link MeteorogramInfo} with given {@code id} from store.
     * @param id the {@code int} of the entry in store to be read.
     * @return the {@link MeteorogramInfo} read from store or {@code null} if an
     * error occurred.
     */
    @Override
    public MeteorogramInfo read(int id) {
        log.info("In read...");
        MeteorogramInfo retValue = store.get(new Integer(id));
        if (null == retValue) {
            log.error("Object doesn't exist in store for key = " + id);
        } else {
            log.info("Read object from store");
        }
        return retValue;
    }

    /**
     * Convenience method to read all {@link MeteorogramInfo} entries from store.
     * The order of read elements depends on given implementation.
     * @return the {@link Vector} with all {@link MeteorogramInfo}s in store.
     */
    @Override
    public Vector readAll() {
        log.info("In read all...");
        Vector infos = new Vector(store.values());
        return infos;
    }

    /**
     * Since this implementation doesn't need to encode nor decode data, the
     * {@link MeteorogramInfoSerializer} is not needed.
     * @return the {@link MeteorogramInfoSerializer}.
     * @throws UnsupportedOperationException
     */
    @Override
    public MeteorogramInfoSerializer getMeteorogramInfoSerializer() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Since this implementation doesn't need to encode nor decode data, the
     * {@link MeteorogramInfoSerializer} is not needed.
     * @param meteorogramInfoSerializer the {@link MeteorogramInfoSerializer} to set.
     * @throws UnsupportedOperationException
     */
    @Override
    public void setMeteorogramInfoSerializer(MeteorogramInfoSerializer meteorogramInfoSerializer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
