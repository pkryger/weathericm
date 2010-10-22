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

import com.kenai.weathericm.repository.MeteorogramInfoRecordStoreSerializer;
//#mdebug
import net.sf.microlog.core.config.PropertyConfigurator;
//#enddebug
import com.kenai.weathericm.app.MeteorogramBroker;
import com.kenai.weathericm.repository.ForecastDataDao;
import com.kenai.weathericm.repository.ForecastDataRecordStoreDao;
import com.kenai.weathericm.repository.ForecastDataRecordStoreSerializer;
import com.kenai.weathericm.repository.MeteorogramInfoDao;
import com.kenai.weathericm.repository.MeteorogramInfoRecordStoreDao;
import com.kenai.weathericm.repository.MeteorogramInfoSerializer;

/**
 * This class is responsible for configuring all the application components to
 * to work together.
 * @author Przemek Kryger
 */
public class AppConfigurator {

    /**
     * Configures all the application components
     */
    public static void configure() {
//#mdebug
        PropertyConfigurator.configure();
//#enddebug
        MeteorogramBroker broker = MeteorogramBroker.getInstance();
        MeteorogramInfoDao mid = MeteorogramInfoRecordStoreDao.getInstance();
        broker.setMeteorogramInfoDao(mid);
        MeteorogramInfoSerializer serializationHelper = new MeteorogramInfoRecordStoreSerializer();
        mid.setMeteorogramInfoSerializer(serializationHelper);
        ForecastDataDao fdd = ForecastDataRecordStoreDao.getInstance();
        broker.setForecastDataDao(fdd);
        fdd.setForecastDataSerializer(new ForecastDataRecordStoreSerializer());
    }
}
