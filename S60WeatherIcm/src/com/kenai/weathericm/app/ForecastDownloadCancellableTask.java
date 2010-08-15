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
import com.kenai.weathericm.util.StatusReporter;
import com.kenai.weathericm.util.Status;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Image;
//#mdebug
import net.sf.microlog.core.Logger;
import net.sf.microlog.core.LoggerFactory;
//#enddebug
import org.netbeans.microedition.util.CancellableTask;
import com.kenai.weathericm.domain.ForecastData;
import com.kenai.weathericm.domain.MeteorogramInfo;
import com.kenai.weathericm.domain.MeteorogramType;
import com.kenai.weathericm.util.Properties;
import com.kenai.weathericm.util.PropertiesRepository;

/**
 * This one is responsible for downloading a forecast for a given {@link MeteorogramInfo}.
 * It tells it's status to all the registered {@link StatusListener}s.
 * The status is sent to all registered listeners using {@link Status}
 * objects.
 * Note: upon the registration of the listener this task status is sent to the listener.
 * @author Przemek Kryger
 */
public class ForecastDownloadCancellableTask extends StatusReporter implements CancellableTask {

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
            LoggerFactory.getLogger(ForecastDownloadCancellableTask.class);
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
     * Indicates if this task has succeeded.
     */
    private boolean succeed = false;
    /**
     * Indicates if this task has been cancelled.
     */
    private boolean cancelled = false;

    /**
     * Creates a new instance of {@link ForecastDownloadCancellableTask} wich
     * will download forecast data for given {@code info}.
     * @param info the {@link MeteorogramInfo} that needs data to be downloaded
     */
    public ForecastDownloadCancellableTask(MeteorogramInfo info) {
        if (info == null) {
//#mdebug
            log.error("Cannot create new instance with null info!");
//#enddebug
            throw new NullPointerException("Cannot create new instance with null info!");
        }
        this.info = info;
    }

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
     * Advises to interrupt the run method and cancel it's task.
     *
     * @return true if the task was successfully cancelled, false otherwise
     */
    public synchronized boolean cancel() {
        if (myThread != null) {
//#mdebug
            log.info("Cancelling task! " + this);
//#enddebug
            myThread.interrupt();
            cancelled = true;
            return true;
        } else {
//#mdebug
            log.warn("Cancelling task that is not running now!" + this);
//#enddebug
            return false;
        }
    }

    /**
     * Informs whether the task run was not successfull. Since the {@value #succeed}
     * is set only after the task succesfully finishes, so until finish this returns
     * {@code true}.
     * @return {@code true} if the task did not finish correctly.
     *         {@code false} if everything was ok.
     */
    public boolean hasFailed() {
        return !succeed;
    }

    /**
     * Gets the reason for the failure. In the case there was not any failure, this method should return null.
     * @return A descriptive message of the failuire or null if there was no failure.
     */
    public String getFailureMessage() {
        throw new UnsupportedOperationException("Not supported yet.");
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

        HttpConnection connection = null;
        DataInputStream dis = null;
        try {
            if (cancelled) {
                throw new InterruptedException();
            }
            Properties typeProperties = loadTypeProperties();
            int percent = progress;
            long totalBytes = 0;
            long percentChunk, chunkCounter;
            int readByte;
//#mdebug
            log.info(this + ": Reading start data...");
//#enddebug
            //@todo the start data downloading and parsing shall be extracted to
            //      a separate class. - start here
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
            connection = (HttpConnection) Connector.open(startUrl);
            dis = connection.openDataInputStream();
            setProgress(++percent);
            if (cancelled) {
                throw new InterruptedException();
            }
            // Start data is 2170(UM) or 2310(COAMPS) bytes (by default)
            totalBytes = connection.getLength();
            if (totalBytes == -1) {
                if (info.getType().equals(MeteorogramType.UM)) {
                    totalBytes = 2170L;
                } else {
                    totalBytes = 2310L;
                }
//#mdebug
                log.debug("Setting totalBytes to default = " + totalBytes);
//#enddebug
            } else {
//#mdebug
                log.debug("Got totalBytes = " + totalBytes);
//#enddebug
            }
            // Start data is around 9% of total data to download and parse.
            // Let's leave one percent for parsing.
            percentChunk = (totalBytes / ((long) (7 - percent))) + 1L;
            chunkCounter = 0;
            StringBuffer startDateBuffer = new StringBuffer((int) totalBytes);
            while ((readByte = dis.read()) != -1) {
                startDateBuffer.append((char) readByte);
                if (++chunkCounter >= percentChunk && percent < 7) {
                    chunkCounter = 0;
                    setProgress(++percent);
                }
                if (cancelled) {
                    throw new InterruptedException();
                }
            }
//#mdebug
            if (chunkCounter >= percentChunk) {
                log.warn("Last chunkCounter was bigger than percentChunk!"
                        + " chunkCounter = " + chunkCounter
                        + ", percentChunk = " + percentChunk);
            }
//#enddebug
            setProgress(++percent); //8%
            dis.close();
            connection.close();
//#mdebug
            log.info(this + ": Parsing received start data...");
//#enddebug
            String modelStartDate = parseStartDate(startDateBuffer, typeProperties);
            startDateBuffer = null;
            setProgress(percent = 9);
            if (cancelled) {
                throw new InterruptedException();
            }
            Thread.yield();
            //@todo the image data downloading shall be extracted to
            //      a separate class.
            String imageUrl = createForecastDataUrl(modelStartDate, typeProperties);
            connection = (HttpConnection) Connector.open(imageUrl);
            dis = connection.openDataInputStream();
            setProgress(++percent); //10%
            if (cancelled) {
                throw new InterruptedException();
            }
            // Image is 19800(UM) or 23500(COAMPS) by default
            totalBytes = connection.getLength();
            if (totalBytes == -1) {
                if (info.getType().equals(MeteorogramType.UM)) {
                    totalBytes = 19800L;
                } else {
                    totalBytes = 23500L;
                }
//#mdebug
                log.debug("Setting totalBytes to default = " + totalBytes);
//#enddebug
            } else {
//#mdebug
                log.debug("Got totalBytes = " + totalBytes);
//#enddebug
            }
            // IM data is around 91% of total data to download and parse.
            // Let's leave two percents for parsing.
            percentChunk = (totalBytes / ((long) (97 - percent))) + 1L;
            chunkCounter = 0;
            Vector imageBuffer = new Vector((int) totalBytes);
            while ((readByte = dis.read()) != -1) {
                imageBuffer.addElement(new Byte((byte) readByte));
                if (++chunkCounter >= percentChunk && percent < 97) {
                    chunkCounter = 0;
                    setProgress(++percent);
                }
                if (cancelled) {
                    throw new InterruptedException();
                }
            }
//#mdebug
            if (chunkCounter >= percentChunk) {
                log.warn("Last chunkCounter was bigger than percentChunk!"
                        + " chunkCounter = " + chunkCounter
                        + ", percentChunk = " + percentChunk);
            }
//#enddebug
            setProgress(++percent); //98%
            dis.close();
            connection.close();
//#mdebug
            log.info(this + ": Parsing received image data...");
//#enddebug
            byte[] imageData = new byte[imageBuffer.size()];
            for (int i = 0; i < imageData.length; i++) {
                imageData[i] = ((Byte) imageBuffer.elementAt(i)).byteValue();
            }
            setProgress(++percent); //99%
            Image modelResult = Image.createImage(imageData, 0, imageData.length);
            ForecastData forecastData = new ForecastData(modelStartDate);
            forecastData.setModelResult(modelResult);
            info.setData(forecastData);
            setProgress(progress = 100);
            Thread.yield();
            if (cancelled) {
                throw new InterruptedException();
            }
            succeed = true;
            fireStatusUpdate(Status.FINISHED);
        } catch (InterruptedException ex) {
//#mdebug
            log.info(this + " has been interrupted!");
//#enddebug
            fireStatusUpdate(Status.CANCELLED);
        } catch (Exception ex) {
//#mdebug
            log.warn(this + " has an exception!", ex);
//#enddebug
            fireStatusUpdate(Status.FINISHED);
        } finally {
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException ex) {
//#mdebug
                    log.debug("Error occurred while closing stream: ", ex);
//#enddebug
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (IOException ex) {
//#mdebug
                    log.debug("Error occurred while chlosing connection: ", ex);
//#enddebug
                }
            }
            synchronized (this) {
                myThread = null;
            }
        }
    }

    /**
     * @return the {@link MeteorogramInfo} that this task downloads forecast data
     */
    public MeteorogramInfo getInfo() {
        return info;
    }

    /**
     * Sets the new progress and notifies all the listeners.
     * @param progress the {@code int} to set.
     */
    public void setProgress(int progress) {
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
     * @param dataBuffer the {@link StringBuffer} to be parsed.
     * @param properties the {@link Properties} to look the parsing constants.
     * @return the {@link String} date to be used for {@link ForecastData}.
     * @see ForecastData#ForecastData(java.lang.String)
     */
    protected String parseStartDate(StringBuffer dataBuffer, Properties properties) {
        if (dataBuffer == null || properties == null) {
//#mdebug
            log.error(this + ": Cannot extract start date from null!");
            log.debug("dataBuffer = " + dataBuffer + ", properties = " + properties);
//#enddebug
            throw new NullPointerException("Canot parse nulls!");
        }
        StringBuffer buffer = new StringBuffer(10);
        String data = dataBuffer.toString();
//#mdebug
        log.info("Beginning of buffer is: " + data.substring(0, data.length() > 50 ? 50 : data.length()));
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
        int idxYear = data.indexOf(parseYear);
        if (idxYear == -1) {
//#mdebug
            log.error(this + ": Cannot find year in data");
//#enddebug
            throw new NullPointerException("Cannot find year in data");
        }
        int idxMonth = data.indexOf(parseMonth);
        if (idxMonth == -1) {
//#mdebug
            log.error(this + ": Cannot find month in data");
//#enddebug
            throw new NullPointerException("Cannot find month in data");
        }
        int idxDay = data.indexOf(parseDay);
        if (idxDay == -1) {
//#mdebug
            log.error(this + ": Cannot find day in data");
//#enddebug
            throw new NullPointerException("Cannot find day in data");
        }
        int idxHour = data.indexOf(parseHour);
        if (idxHour == -1) {
//#mdebug
            log.error(this + ": Cannot find hour in data");
//#enddebug
            throw new NullPointerException("Cannot find hour in data");
        }
        String extracted;
        idxYear += parseYear.length();
        extracted = data.substring(idxYear, idxYear + 4);
        buffer.append(extracted);
        idxMonth += parseMonth.length();
        extracted = data.substring(idxMonth, idxMonth + 2);
        buffer.append(extracted);
        idxDay += parseDay.length();
        extracted = data.substring(idxDay, idxDay + 2);
        buffer.append(extracted);
        idxHour += parseHour.length();
        extracted = data.substring(idxHour, idxHour + 2);
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
}
