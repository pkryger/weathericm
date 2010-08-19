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
import com.kenai.weathericm.util.StatusReporter;

/**
 * This is the interface for task used to download {@link ForecastData} based on
 * a given {@link MeteorogramInfo}.
 * Note: it shall be instantiaded by a {@link ForecastDataDownloaderFactory}.
 * @author Przemek Kryger
 */
public interface ForecastDataDownloader extends Runnable, StatusReporter {

    /**
     * Aborts the download operation.
     * @return {@code true} if cancelation was successful, {@code false} otherwise.
     */
    boolean cancel();

    /**
     * Sets the meteorogram info the {@link ForecastData} shall be downloaded for.
     * It shall be called before the download operation begins.
     * @param info the {@link MeteorogramInfo} that shall have the
     *             {@link ForecastData} be downloaded.
     */
    void setMeteorogramInfo(MeteorogramInfo info);

    /**
     * Returns the meteorogram info this downloader will download {@link ForecastData}.
     * @return the {@link MeteorogramInfo}
     */
    MeteorogramInfo getMeteorogramInfo();

    /**
     * Sets the downloader to obtain start data for {@link ForecastData}.
     * @param startDateDownloader the {@link StartDateDownloader} to be used.
     */
    void setStartDateDownloader(StartDateDownloader startDateDownloader);

    /**
     * Sets the downloader to obtain the model result for {@link ForecastData}.
     * @param modelResultDownloader the {@link ModelResultDownloader} to be used.
     */
    void setModelResultDownloader(ModelResultDownloader modelResultDownloader);
}
