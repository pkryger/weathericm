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

import com.kenai.weathericm.domain.ForecastData;
import com.kenai.weathericm.repository.ForecastDataDao;
import java.util.HashMap;
import java.util.Map;
import net.sf.microlog.core.Logger;
import net.sf.microlog.core.LoggerFactory;

/**
 * Simple in-memory implementation of {@link ForecastDataDao}
 * @author Przemek Kryger
 */
public class ForecastDataInMemoryDao implements ForecastDataDao {

    /**
     * The logger for the class
     */
    private final static Logger log = LoggerFactory.getLogger(ForecastDataInMemoryDao.class);
    /**
     * The in-RAM store.
     */
    private Map<Integer, ForecastData> store = new HashMap<Integer, ForecastData>();

    @Override
    public boolean create(Integer id, ForecastData forecastData) {
        log.info("In create...");
        if (store.containsKey(id)) {
            return false;
        }
        store.put(id, forecastData);
        return true;
    }

    @Override
    public ForecastData read(Integer id) {
        log.info("In read...");
        return store.get(id);
    }

    @Override
    public boolean update(Integer id, ForecastData forecastData) {
        log.info("In update...");
        store.put(id, forecastData);
        return true;
    }

    @Override
    public boolean delete(Integer id) {
        log.info("In delete...");
        if (store.containsKey(id)) {
            store.remove(id);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean exits(Integer id) {
        log.info("In exists...");
        return store.containsKey(id);
    }

    @Override
    public boolean createOrUpdate(Integer id, ForecastData forecastData) {
        log.info("In createOrUpdate...");
        if (store.containsKey(id)) {
            return update(id, forecastData);
        } else {
            return create(id, forecastData);
        }
    }
}
