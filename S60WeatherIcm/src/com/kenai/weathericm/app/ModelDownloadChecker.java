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
 * This checks the if downloading a {@link ForecastData} for a given {@link MeteorogramInfo}.
 * @author Przemek Kryger
 */
public interface ModelDownloadChecker {
    /**
     * Given the {@code info} checks if it will make sense to download the {@link
     * ForecastData} for it, based on the {@code newData}.
     * @param info the {@link MeteorogramInfo} to perform the check for.
     * @param newData the {@link ForecastData}
     * @return {@code true} if downloading model result is needed, {@code false} otherwise.
     */
    boolean isDownloadNeeded(MeteorogramInfo info, ForecastData newData);
}
