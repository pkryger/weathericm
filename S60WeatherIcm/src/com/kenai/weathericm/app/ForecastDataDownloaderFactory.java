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

import com.kenai.weathericm.util.Properties;
import com.kenai.weathericm.util.PropertiesRepository;
//#mdebug
import net.sf.microlog.core.Logger;
import net.sf.microlog.core.LoggerFactory;
//#enddebug

/**
 * This is the factory used to get instances of {@link ForecastDataDownloader}s.
 * It will obtain {@link StartDataDownloader} and {@link ModelResultDownloader}
 * implementations from {@code ForecastDataDownloaderFactory.properties}.
 * @author Przemek Kryger
 */
public class ForecastDataDownloaderFactory {

    /**
     * The key for implementation of {@link ForecastDataDownloader}.
     */
    public static final String FORECAST_DATA_DOWNLOADER_KEY = "forecast.data.downloader";
    /**
     * The key for implementation of {@link StartDateDownloader}.
     */
    public static final String START_DATE_DOWNLOADER_KEY = "start.date.downloader";
    /**
     * The key for implementation of {@link ModelResultDownloader}.
     */
    public static final String MODEL_RESULT_DOWNLOADER_KEY = "model.result.downloader";
    /**
     * The key for implementation of {@link ModelDownloadChecker} that is to be used
     * for 'checked' {@link ForecastDataDownloader}.
     */
    public static final String CHECKED_MODEL_DOWNLOAD_CHECKER_KEY = "model.checker.checked";
    /**
     * The key for implementation of {@link ModelDownloadChecker} that is to be used
     * for 'forced' {@link ForecastDataDownloader}.
     */
    public static final String FORCED_MODEL_DOWNLOAD_CHECKER_KEY = "model.checker.forced";
//#mdebug
    /**
     * The logger for the class.
     */
    private static final Logger log = LoggerFactory.getLogger(ForecastDataDownloader.class);
//#enddebug
    /**
     * The holder for properties.
     */
    private static Properties properties = null;

    /**
     * Gets the new instance of {@link ForecastDataDownloader} with propery configured
     * {@link StartDataDownloader}, {@link ModelResultDownloader} and {@link
     * ModelDownloadChecker} to perform forced download operation.
     * @return the new {@link ForecastDataDownloader}.
     */
    private static ForecastDataDownloader getDownloader(String modelDownloadCheckerKey) {
        if (properties == null) {
//#mdebug
            log.info("Instantiating properties");
//#enddebug
            properties = PropertiesRepository.getProperties("/ForecastDataDownloaderFactory.properties");
        }
        ForecastDataDownloader forecastDataDownloader = null;
        String implementation = properties.getProperty(FORECAST_DATA_DOWNLOADER_KEY);
        try {
            forecastDataDownloader =
                    (ForecastDataDownloader) Class.forName(implementation).newInstance();
            implementation =
                    properties.getProperty(START_DATE_DOWNLOADER_KEY);
            StartDateDownloader startDateDownloader =
                    (StartDateDownloader) Class.forName(implementation).newInstance();
            implementation =
                    properties.getProperty(MODEL_RESULT_DOWNLOADER_KEY);
            ModelResultDownloader modelResultDownloader =
                    (ModelResultDownloader) Class.forName(implementation).newInstance();
            implementation =
                    properties.getProperty(modelDownloadCheckerKey);
            ModelDownloadChecker modelDownloadChecker =
                    (ModelDownloadChecker) Class.forName(implementation).newInstance();
            forecastDataDownloader.setStartDateDownloader(startDateDownloader);
            forecastDataDownloader.setModelResultDownloader(modelResultDownloader);
            forecastDataDownloader.setModelResultDownloadChecker(modelDownloadChecker);
        } catch (ClassNotFoundException ex) {
//#mdebug
            log.fatal("Cannot find a class for " + implementation, ex);
//#enddebug
            forecastDataDownloader = null;
        } catch (IllegalAccessException ex) {
//#mdebug
            log.fatal("Cannot acces class for " + implementation, ex);
//#enddebug
            forecastDataDownloader = null;
        } catch (InstantiationException ex) {
//#mdebug
            log.fatal("Cannot instantiate " + implementation, ex);
//#enddebug
            forecastDataDownloader = null;
        }
        return forecastDataDownloader;
    }

    /**
     * Gets the new instance of {@link ForecastDataDownloader} with propery configured
     * {@link StartDataDownloader}, {@link ModelResultDownloader} and {@link
     * ModelDownloadChecker} to perform forced download operation.
     * @return the new {@link ForecastDataDownloader}.
     */
    public static ForecastDataDownloader getForcedDownloader() {
        return getDownloader(FORCED_MODEL_DOWNLOAD_CHECKER_KEY);
    }

    /**
     * Gets the new instance of {@link ForecastDataDownloader} with propery configured
     * {@link StartDataDownloader}, {@link ModelResultDownloader} and {@link
     * ModelDownloadChecker} to perform forced download operation.
     * @return the new {@link ForecastDataDownloader}.
     */
    public static ForecastDataDownloader getCheckedDownloader() {
        return getDownloader(CHECKED_MODEL_DOWNLOAD_CHECKER_KEY);
    }
}
