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
package com.kenai.weathericm.app;

import java.util.Vector;
import com.kenai.weathericm.domain.MeteorogramInfo;

/**
 * Contract for listenening for {@link MeteorogramBroker} events.
 * @author Przemek Kryger
 */
public interface MeteorogramBrokerListener {

    /**
     * Event fired whenever all the infos has been read from DAO.
     * @param newMeteorogramInfos the {@link Vector} that contains all the read
     *                            {@link MeteorogramInfo}s
     */
    void readMeteorogramInfo(Vector newMeteorogramInfos);

    /**
     * Event fired whenever {@code addedMeteorogramInfo} has been added to DAO.
     * @param addedMeteorogramInfo the {@link MeteorogramInfo} added to DAO.
     */
    void addedMeteorogramInfo(MeteorogramInfo addedMeteorogramInfo);

    /**
     * Event fired whenever {@code deletedMeteorogramInfo} has been deleted from DAO.
     * @param deletedMeteorogramInfo the {@link MeteorogramInfo} deleted from DAO.
     */
    void deletedMeteorogramInfo(MeteorogramInfo deletedMeteorogramInfo);

    /**
     * Event fired whenever {@code updatedMeteorogramInfo} has been updated in DAO.
     * @param updatedMeteorogramInfo the {@link MeteorogramInfo} updated in DAO.
     */
    void updatedMeteorogramInfo(MeteorogramInfo updatedMeteorogramInfo);
}
