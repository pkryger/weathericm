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

import com.kenai.weathericm.util.StatusListener;
import com.kenai.weathericm.util.AbstractStatusReporter;
import com.kenai.weathericm.util.Status;
import javax.microedition.lcdui.Image;
//#mdebug
import net.sf.microlog.core.Logger;
import net.sf.microlog.core.LoggerFactory;
//#enddebug
import com.kenai.weathericm.domain.ForecastData;
import com.kenai.weathericm.domain.MeteorogramInfo;
import com.kenai.weathericm.util.Properties;
import com.kenai.weathericm.util.PropertiesRepository;
import com.kenai.weathericm.util.StatusReporter;

/**
 * This one is responsible for downloading a forecast for a given {@link MeteorogramInfo}.
 * It tells it's status to all the registered {@link StatusListener}s.
 * The status is sent to all registered listeners using {@link Status}
 * objects.
 * Note: upon the registration of the listener this task status is sent to the listener.
 * @author Przemek Kryger
 */
public abstract class AbstractForecastDataDownloader extends AbstractStatusReporter
        implements ForecastDataDownloader, StatusListener {

    /**
     * The key used to obtaining the URL for model start data. This value 
     * shall be a full URL that points data that can be parsed by
     * {@code PARSE_}* keys below.
     */
    public final static String START_URL_KEY = "start.url";
    /**
     * The key used to obtaining the hour of model start. This
     * value shall be an integer in two digits form.
     */
    public final static String PARSE_HOUR_KEY = "parse.sst";
    /**
     * The key used to obtaining the day of the month of model start. This
     * value shall be an integer in two digits form.
     */
    public final static String PARSE_DAY_KEY = "parse.day";
    /**
     * The key used to obtaining the month of model start. This
     * value shall be an integer in two digits form.
     */
    public final static String PARSE_MONTH_KEY = "parse.month";
    /**
     * The key used to obtaining the year of model start. This
     * value shall be an integer in four digits form.
     */
    public final static String PARSE_YEAR_KEY = "parse.year";
    /**
     * The key used to obtaining the URL for forecast image.
     */
    public final static String IMAGE_URL_PREFIX_KEY = "image.url.prefix";
//#mdebug
    /**
     * The logger for the class.
     */
    private final static Logger log =
            LoggerFactory.getLogger(AbstractForecastDataDownloader.class);
//#enddebug
    /**
     * The {@link MeteorogramInfo} that this task will download forecast data.
     */
    private MeteorogramInfo info = null;
    /**
     * Indicates if the progress of this task. When it is set to -1,
     * this indicate that this thask has been already started and
     * shall not be started again.
     */
    private int progress = -1;
    /**
     * The actual {@link Thread} that executes this task. 
     */
    private Thread myThread = null;
    /**
     * Indicates the last status of this task.
     */
    protected Status lastStatus = null;
    /**
     * Indicates if this task has been cancelled.
     */
    private boolean cancelled = false;
    /**
     * This one will be used to get the start data for the {@link ForecastData}.
     */
    private StartDateDownloader startDateDownloader;
    /**
     * This will be used to get the model result for the {@link ForecastData}.
     */
    private ModelResultDownloader modelResultDownloader;
    /**
     * The total amount of chunk size that {@value #startDateDownloader}
     * or {@value #modelResultDownloader} will advance the progress.
     */
    private int chunkSize;
    /**
     * The progress the {@value #startDateDownloader} or {@value #modelResultDownloader}
     * has been actuatlly started.
     */
    private int chunkStart;

    /**
     * Adds a listener and afterwards, if taks is in progress notifies it about
     * the acutall progress.
     * @param listener
     */
    public void addListener(StatusListener listener) {
        super.addListener(listener);
        if (progress != -1) {
            Status status = new Status();
            status.setProgress(progress);
            listener.statusUpdate(this, status);
        }
    }

    /**
     * Notifies all the registered listeners on the {@code status}.
     * @param status the {@link Status} to be reported.
     */
    protected void fireStatusUpdate(Status status) {
        super.fireStatusUpdate(status);
        lastStatus = status;
    }

    /**
     * Advises to interrupt the run method and cancel it's task.
     *
     * @return true if the task was successfully cancelled, false otherwise
     */
    public synchronized boolean cancel() {
        if (myThread != null) {
//#mdebug
            log.info("Cancelling task! " + this);
//#enddebug
            cancelled = true;
            if (startDateDownloader != null) {
                startDateDownloader.cancel();
            } else {
//#mdebug
                log.error("Cannot cancel start date downloader - it is null!");
//#enddebug
            }
            if (modelResultDownloader != null) {
                modelResultDownloader.cancel();
            } else {
//#mdebug
                log.error("Cannot cancel model result downloader - it is null!");
//#enddebug
            }
            myThread.interrupt();
            return true;
        } else {
//#mdebug
            log.warn("Cancelling task that is not running now!" + this);
//#enddebug
            return false;
        }
    }

    public void run() {
        synchronized (this) {
            if (progress != -1) {
//#mdebug
                log.info("The task for info = " + info + " has been already "
                        + "started. Waiting for it to finish.");
//#enddebug
                if (myThread != null) {
                    try {
                        myThread.join();
                    } catch (InterruptedException ex) {
//#mdebug
                        log.debug("The other thread has been interrupted");
//#enddebug
                    }
                } else {
//#mdebug
                    log.debug("The other thread has already finished.");
//#enddebug
                }
                return;
            } else {
//#mdebug
                log.info("Starting the task for info = " + info);
//#enddebug
                progress = 0;
                myThread = Thread.currentThread();
                int priority = (Thread.NORM_PRIORITY - 1) > Thread.MIN_PRIORITY
                        ? Thread.NORM_PRIORITY - 1 : Thread.MIN_PRIORITY;
                try {
                    myThread.setPriority(priority);
                } catch (SecurityException ex) {
//#mdebug
                    log.warn("Cannot lower myThread priority!", ex);
//#enddebug
                }
                fireStatusUpdate(Status.STARTED);
            }
        }
        if (startDateDownloader == null || modelResultDownloader == null) {
//#mdebug
            log.fatal(this + "Either startDateDownloader or modelResultDownloader is null");
            log.debug("startdDateDownloader = " + startDateDownloader);
            log.debug("modelResultDownloader = " + modelResultDownloader);
//#enddebug
            throw new NullPointerException("Internal state is broken for downloader!");
        }
        try {
            if (cancelled) {
                throw new InterruptedException();
            }
            Properties typeProperties = loadTypeProperties();
            String startUrl = typeProperties.getProperty(START_URL_KEY);
            if (startUrl == null) {
//#mdebug
                log.error(this + ": Cannot load property for: " + START_URL_KEY);
//#enddebug
                throw new NullPointerException("Cannot load property for " + START_URL_KEY);
            } else if (startUrl.indexOf("http://") != 0) {
//#mdebug
                log.error(this + ": The start data URL doesn't start with http://! " + startUrl);
//#enddebug
                throw new IllegalArgumentException("URL for start data is invalid!");
            }
//#mdebug
            log.info(this + ": Reading start data...");
//#enddebug
            chunkStart = progress;
            chunkSize = 8 - progress;
            startDateDownloader.addListener(this);
            String dateBuffer = startDateDownloader.downloadStartDate(startUrl);
            startDateDownloader.removeListener(this);
//#mdebug
            log.info(this + ": Parsing received start data...");
//#enddebug
            String modelStartDate = parseStartDate(dateBuffer, typeProperties);
            dateBuffer = null;
            String imageUrl = createForecastDataUrl(modelStartDate, typeProperties);
            setProgress(9);
            if (cancelled) {
                throw new InterruptedException();
            }
            Thread.yield();
            chunkStart = progress;
            chunkSize = 99 - chunkStart;
            modelResultDownloader.addListener(this);
            byte[] modelResult = modelResultDownloader.downloadModelResult(imageUrl);
            modelResultDownloader.removeListener(this);
            if (modelResult == null) {
//#mdebug
                log.error("Cannot download image data from: " + imageUrl);
//#enddebug
                throw new NullPointerException("Image download failed!");
            }
            ForecastData forecastData = new ForecastData(modelStartDate);
            forecastData.setModelResult(modelResult);
            info.setData(forecastData);
            setProgress(progress = 100);
            modelResultDownloader.removeListener(this);
            Thread.yield();
            if (cancelled) {
                throw new InterruptedException();
            }
            fireStatusUpdate(Status.FINISHED);
        } catch (InterruptedException ex) {
//#mdebug
            log.info(this + " has been interrupted!");
//#enddebug
            fireStatusUpdate(Status.CANCELLED);
        } catch (RuntimeException ex) {
//#mdebug
            log.warn(this + " has an exception!", ex);
//#enddebug
            fireStatusUpdate(Status.CANCELLED);
            throw ex;
        } finally {
            startDateDownloader.removeListener(this);
            modelResultDownloader.removeListener(this);
            synchronized (this) {
                myThread = null;
            }
        }
    }

    /**
     * Sets the new progress and notifies all the listeners.
     * @param progress the {@code int} to set.
     */
    protected void setProgress(int progress) {
//#mdebug
        log.debug(this + ": Setting progress to: " + progress);
//#enddebug
        this.progress = progress;
        Status status = new Status();
        status.setProgress(progress);
        fireStatusUpdate(status);
    }

    /**
     * Loads properties from a file based on the {@value MeteorogramInfo#getType()}
     * for this instance's {@value @info}.
     */
    protected Properties loadTypeProperties() {
        StringBuffer fileNameBuffer = new StringBuffer("/");
        fileNameBuffer.append(info.getType().getName()).append(".properties");
//#mdebug
        log.debug("Loading the type properties from: " + fileNameBuffer.toString());
//#enddebug
        Properties properties = PropertiesRepository.getProperties(fileNameBuffer.toString());
        return properties;
    }

    /**
     * Parses {@code dataBuffer} into format acceptable for {@link ForecastData}
     * constructor (yyyymmddhh). It uses properties from {@code PARSE}* mambers.
     * @param dataBuffer the {@link String} to be parsed.
     * @param properties the {@link Properties} to look the parsing constants.
     * @return the {@link String} date to be used for {@link ForecastData}.
     * @see ForecastData#ForecastData(java.lang.String)
     */
    protected String parseStartDate(String dataBuffer, Properties properties) {
        if (dataBuffer == null || properties == null) {
//#mdebug
            log.error(this + ": Cannot extract start date from null!");
            log.debug("dataBuffer = " + dataBuffer + ", properties = " + properties);
//#enddebug
            throw new NullPointerException("Canot parse nulls!");
        }
        StringBuffer buffer = new StringBuffer(10);
//#mdebug
        log.info("Beginning of buffer is: " + dataBuffer.substring(0, dataBuffer.length() > 50 ? 50 : dataBuffer.length()));
//#enddebug
        String parseYear = properties.getProperty(PARSE_YEAR_KEY);
        if (parseYear == null) {
//#mdebug
            log.error(this + ": Cannot load property for " + PARSE_YEAR_KEY);
//#enddebug
            throw new NullPointerException("Cannot load property for " + PARSE_YEAR_KEY);
        }
        String parseMonth = properties.getProperty(PARSE_MONTH_KEY);
        if (parseMonth == null) {
//#mdebug
            log.error(this + ": Cannot load property for " + PARSE_MONTH_KEY);
//#enddebug
            throw new NullPointerException("Cannot load property for " + PARSE_MONTH_KEY);
        }
        String parseDay = properties.getProperty(PARSE_DAY_KEY);
        if (parseDay == null) {
//#mdebug
            log.error(this + ": Cannot load property for " + PARSE_DAY_KEY);
//#enddebug
            throw new NullPointerException("Cannot load property for " + PARSE_DAY_KEY);
        }
        String parseHour = properties.getProperty(PARSE_HOUR_KEY);
        if (parseHour == null) {
//#mdebug
            log.error(this + ": Cannot load property for " + PARSE_HOUR_KEY);
//#enddebug
            throw new NullPointerException("Cannot load property for " + PARSE_HOUR_KEY);
        }
        int idxYear = dataBuffer.indexOf(parseYear);
        if (idxYear == -1) {
//#mdebug
            log.error(this + ": Cannot find year in data");
//#enddebug
            throw new NullPointerException("Cannot find year in data");
        }
        int idxMonth = dataBuffer.indexOf(parseMonth);
        if (idxMonth == -1) {
//#mdebug
            log.error(this + ": Cannot find month in data");
//#enddebug
            throw new NullPointerException("Cannot find month in data");
        }
        int idxDay = dataBuffer.indexOf(parseDay);
        if (idxDay == -1) {
//#mdebug
            log.error(this + ": Cannot find day in data");
//#enddebug
            throw new NullPointerException("Cannot find day in data");
        }
        int idxHour = dataBuffer.indexOf(parseHour);
        if (idxHour == -1) {
//#mdebug
            log.error(this + ": Cannot find hour in data");
//#enddebug
            throw new NullPointerException("Cannot find hour in data");
        }
        String extracted;
        idxYear += parseYear.length();
        extracted = dataBuffer.substring(idxYear, idxYear + 4);
        buffer.append(extracted);
        idxMonth += parseMonth.length();
        extracted = dataBuffer.substring(idxMonth, idxMonth + 2);
        buffer.append(extracted);
        idxDay += parseDay.length();
        extracted = dataBuffer.substring(idxDay, idxDay + 2);
        buffer.append(extracted);
        idxHour += parseHour.length();
        extracted = dataBuffer.substring(idxHour, idxHour + 2);
        buffer.append(extracted);
//#mdebug
        log.debug("Extracted start date = " + buffer.toString());
//#enddebug
        return buffer.toString();
    }

    /**
     * Creates the forecast data URL that can be used to download the image that
     * contains the forecast for the {@value #info}.
     * @param startData the {@link String} that indicates the model start.
     * @param properties the {@link Properties} that are used to get
     *                   {@value #IMAGE_URL_PREFIX_KEY} value.
     * @return the {@link String} with forecast data image URL.
     */
    protected String createForecastDataUrl(String startData, Properties properties) {
        if (startData == null || properties == null) {
//#mdebug
            log.error(this + ": Cannot create url from null!");
            log.debug("startData = " + startData + ", properties = " + properties);
//#enddebug
            throw new NullPointerException("Cannot create URL from nulls!");
        }
        String urlPrefix = properties.getProperty(IMAGE_URL_PREFIX_KEY);
        if (urlPrefix == null) {
//#mdebug
            log.error(this + ": Cannot load property for " + IMAGE_URL_PREFIX_KEY);
//#enddebug
            throw new NullPointerException("Cannot load property for " + IMAGE_URL_PREFIX_KEY);
        } else if (urlPrefix.indexOf("http://") != 0) {
//#mdebug
            log.error(this + ": The start data URL doesn't start with http://! " + urlPrefix);
//#enddebug
            throw new IllegalArgumentException("URL for forecast data is invalid!");
        }
        StringBuffer urlBuffer = new StringBuffer(urlPrefix);
        urlBuffer.append(startData);
        urlBuffer.append("&row=").append(info.getY());
        urlBuffer.append("&col=").append(info.getX());
        urlBuffer.append("&lang=pl");
//#mdebug
        log.debug("Created image URL = " + urlBuffer.toString());
//#enddebug
        return urlBuffer.toString();
    }

    /**
     * Converts this instance into {@link String}.
     * @return the {@link String} representation of this instance.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer("Downloader for info = {");
        buffer.append(info).append("}, progress = ").append(progress);
        return buffer.toString();
    }

    /**
     * Sets the {@code info} that the {@link ForecastData} shall be downloaded for.
     * @param info the {@link MeteorogramInfo} to download forecast data.
     * @throws NullPointerException if the {@code info} is null.
     */
    public void setMeteorogramInfo(MeteorogramInfo info) {
        if (info == null) {
//#mdebug
            log.error("Cannot set the info to null!");
//#enddebug
            throw new NullPointerException("Cannot create new instance with null info!");
        }
        this.info = info;
    }

    /**
     * @return the {@link MeteorogramInfo} that this task downloads forecast data.
     */
    public MeteorogramInfo getMeteorogramInfo() {
        return info;
    }

    /**
     * Sets the implemantation of {@link StartDateDownloader} to be used to get
     * {@link ForecastData} start date.
     * @param startDateDownloader the instance to be used to get start date.
     * @throws NullPointerException if the {@code startDateDownloader} is {@code null}.
     */
    public void setStartDateDownloader(StartDateDownloader startDateDownloader) {
        if (startDateDownloader == null) {
//#mdebug
            log.error("Trying to set null as start date downloader!");
//#enddebug
            throw new NullPointerException("Cannot set null as start date downloader!");
        }
        this.startDateDownloader = startDateDownloader;
    }

    /**
     * Sets the implemantation of {@link ModelResultDownloader} to be used to get
     * {@link ForecastData} model result.
     * @param modelResultDownloader  the instance to be used to get start date.
     * @throws NullPointerException if the {@code modelResultDownloader} is {@code null}.
     */
    public void setModelResultDownloader(ModelResultDownloader modelResultDownloader) {
        if (modelResultDownloader == null) {
//#mdebug
            log.error("Trying to set null as model result downloader!");
//#enddebug
            throw new NullPointerException("Cannot set null as model result downloader!");
        }
        this.modelResultDownloader = modelResultDownloader;
    }

    /**
     * Handles the status updates from either {@value #startDateDownloader} or
     * {@value #modelResultDownloader}. It uses current settings of
     * @param source
     * @param status
     * @throws NullPointerException if either {@code source} or {@code status}
     *         is null
     * @throws IllegalArgumentException if source is niether {@value #startDateDownloader}
     *         nor {@value #modelResultDownloader}.
     */
    public void statusUpdate(StatusReporter source, Status status) {
        if (source == null || status == null) {
//#mdebug
            log.error(this + "Cannot update status! source = " + source
                    + ", status = " + status);
//#enddebug
            throw new NullPointerException("Cannot update if either source or"
                    + "status is null!");
        }
        if (source != startDateDownloader && source != modelResultDownloader) {
//#mdebug
            log.error("Cannot update status with unknown source = " + source);
//#enddebug
            throw new IllegalArgumentException("Cannot update status with unknown source!");
        }
        int currentProgress = (int) ((double) chunkSize / 100d
                * (double) status.getProgress()) + chunkStart;
        if (currentProgress > progress) {
            setProgress(currentProgress);
        }
        if (status.equals(Status.CANCELLED) || status.equals(Status.FINISHED)) {
            source.removeListener(this);
        }
    }
}
