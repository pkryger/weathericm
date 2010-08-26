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
import com.kenai.weathericm.repository.MeteorogramInfoDao;
import com.kenai.weathericm.repository.MeteorogramInfoRecordStoreDao;
import com.kenai.weathericm.repository.MeteorogramInfoSerializer;

/**
 * This class is resposible for configuring all the application components to
 * to work together.
 * @author Przemek Kryger
 */
public class AppConfigurator {

    /**
     * Confiugres all the application components
     */
    public static void configure() {
//#mdebug
        PropertyConfigurator.configure();
//#enddebug
        MeteorogramBroker broker = MeteorogramBroker.getInstance();
        MeteorogramInfoDao dao = MeteorogramInfoRecordStoreDao.getInstance();
        broker.setMeteorogramInfoDao(dao);
        MeteorogramInfoSerializer serializationHelper = new MeteorogramInfoRecordStoreSerializer();
        dao.setMeteorogramInfoSerializer(serializationHelper);
    }
}
