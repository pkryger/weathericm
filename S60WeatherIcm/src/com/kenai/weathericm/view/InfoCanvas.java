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
package com.kenai.weathericm.view;

import com.kenai.weathericm.domain.Availability;
import com.kenai.weathericm.domain.MeteorogramInfo;
//#mdebug
import javax.microedition.lcdui.Image;
import net.sf.microlog.core.Logger;
import net.sf.microlog.core.LoggerFactory;
//#enddebug

/**
 * This is a convenience class that allows to display {@link MeteorogramInfo}'s
 * forecast data in a {@link ScrollableCanvas}.
 * @author Przemek Kryger
 */
public class InfoCanvas extends ScrollableCanvas {
//#mdebug

    /**
     * The logger for the class.
     */
    private final static Logger log = LoggerFactory.getLogger(InfoCanvas.class);
//#enddebug

    /**
     * Sets the forecast data as the {@value ScrollableCanvas#image} and the
     * info's name as the {@value ScrollableCanvas#title}.
     * @param info the {@link MeteorogramInfo} to use as data.
     * @throws NullPointerException when the {@code info} is null.
     */
    public void setInfo(MeteorogramInfo info) {
        if (info != null) {
            if (info.dataAvailability() != Availability.NOT_AVAILABLE) {
                byte[] forecastData = info.getForecastData().getModelResult();
                Image forecast = null;
                if (forecastData != null) {
                    forecast = Image.createImage(forecastData, 0, forecastData.length);
                } else {
//#mdebug
                    log.warn("The " + info + " has broken forecast data!");
//#enddebug
                }
                setImage(forecast);
            } else {
//#mdebug
                log.warn("Info has no data avaliable. Setting null image.");
//#enddebug
                setImage(null);
            }
            setTitle(info.getName());
        } else {
//#mdebug
            log.error("Cannot set null info!");
//#enddebug
            throw new NullPointerException("Cannot set null info!");
        }
    }
}
