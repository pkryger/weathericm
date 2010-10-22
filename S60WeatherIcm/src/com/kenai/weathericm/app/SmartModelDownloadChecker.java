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
//#mdebug
import net.sf.microlog.core.Logger;
import net.sf.microlog.core.LoggerFactory;
//#enddebug

/**
 * This download checker uses {@link ComparableForecastData} to determine if the
 * download shall be continued.
 * @author Przemek Kryger
 */
public class SmartModelDownloadChecker implements ModelDownloadChecker {

//#mdebug
    /**
     * The class logger.
     */
    private final static Logger log = LoggerFactory.getLogger(SmartModelDownloadChecker.class);
//#enddebug

    /**
     * Will perform a checking using {@link ComparableForecastData}. In case the
     * {@code newData} is newer than the {@code info}'s one, or the {@code info}
     * doesn't have the forecast data, then it will return {@code true}.
     * @param info the {@link MeteorogramInfo} to be checked.
     * @param newData the {@link ForecastData} to be checked.
     * @return {@code true} if download is needed, {@code false} otherwise.
     * @throws NullPointerException in case either {@code info} or {@code newData} is null.
     */
    public boolean isDownloadNeeded(MeteorogramInfo info, ForecastData newData) {
        if (info == null) {
//#mdebug
            log.error("Cannot perform checking for null info!");
//#enddebug
            throw new NullPointerException("Not checking for null info!");
        }
        if (newData == null) {
//#mdebug
            log.error("Cannot perform checking for null info!");
//#enddebug
            throw new NullPointerException("Not checking for null info!");
        }
        ForecastData infoData = info.getForecastData();
        if (infoData == null) {
            return true;
        }
        ComparableForecastData checker = new ComparableForecastData(newData);
        return checker.isNewerThan(infoData);
    }
}
