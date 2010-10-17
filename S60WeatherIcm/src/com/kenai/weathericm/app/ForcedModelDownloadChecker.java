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

import com.kenai.weathericm.domain.ForecastData;
import com.kenai.weathericm.domain.MeteorogramInfo;

/**
 *
 * @author Przemek Kryger
 */
public class ForcedModelDownloadChecker implements ModelDownloadChecker {

    /**
     * Since this is a 'forced' implementation this method will always return {@code true}.
     * @param info
     * @param newData
     * @return {@code true} since this is a 'forced' implementation.
     * @see ModelDownloadChecker#isDownloadNeeded(com.kenai.weathericm.domain.MeteorogramInfo, com.kenai.weathericm.domain.ForecastData) 
     */
    public boolean isDownloadNeeded(MeteorogramInfo info, ForecastData newData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
