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

import com.kenai.weathericm.util.AbstractStatusReporter;
import com.kenai.weathericm.util.Status;
import java.io.DataInputStream;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
//#mdebug
import net.sf.microlog.core.Logger;
import net.sf.microlog.core.LoggerFactory;
//#enddebug

/**
 * This downloader uses {@link Connector} and {@link HttpConnection} stuff to get
 * start date from the given URL.
 * @author Przemek Kryger
 */
public class ConnectorStartDateDownloader extends AbstractStatusReporter implements StartDateDownloader {

//#mdebug
    /**
     * The logger for the class
     */
    private final static Logger log = LoggerFactory.getLogger(ConnectorStartDateDownloader.class);
//#enddebug
    /**
     * Indicates if the download shall be canceled.
     */
    private boolean cancelled = false;

    /**
     * Downloads the data from the given {@code url}.
     * @param url the {@link String} with URL to download data from.
     * @return the downloaded data
     */
    public String downloadStartDate(String url) {
        HttpConnection connection = null;
        DataInputStream dis = null;
        String retValue = null;
        try {
            if (cancelled) {
                throw new InterruptedException();
            }
            fireStatusUpdate(Status.STARTED);
            connection = (HttpConnection) Connector.open(url);
            dis = connection.openDataInputStream();
            if (cancelled) {
                throw new InterruptedException();
            }
            // Start data is 2170(UM) or 2310(COAMPS) bytes (by default)
            long totalBytes = connection.getLength();
            if (totalBytes == -1) {
                totalBytes = 2310L;
            }
//#mdebug
            log.debug("Setting totalBytes to default = " + totalBytes);
//#enddebug
            // Let's leave one percent for parsing.
            long percentChunk = totalBytes / 100L;
            long chunkCounter = 0;
            int readByte;
            int percent = 0;
            StringBuffer startDateBuffer = new StringBuffer((int) totalBytes);
            while ((readByte = dis.read()) != -1) {
                startDateBuffer.append((char) readByte);
                if (++chunkCounter >= percentChunk && percent < 100) {
                    chunkCounter = 0;
                    reportProgress(++percent);
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
            retValue = startDateBuffer.toString();
            fireStatusUpdate(Status.FINISHED);
        } catch (InterruptedException ex) {
//#mdebug
            log.info(this + " has been interrupted!");
//#enddebug
            fireStatusUpdate(Status.CANCELLED);
        } catch (IOException ex) {
//#mdebug
            log.warn(this + " has an exception!", ex);
//#enddebug
            fireStatusUpdate(Status.CANCELLED);
        } catch (IllegalArgumentException ex) {
//#mdebug
            log.warn(this + " has an exception!", ex);
//#enddebug
            fireStatusUpdate(Status.CANCELLED);
        } catch (SecurityException ex) {
//#mdebug
            log.warn(this + " has an exception!", ex);
//#enddebug
            fireStatusUpdate(Status.CANCELLED);
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
        }
        return retValue;
    }

    /**
     * Aborts the download operation.
     * @return {@code true} if canceled succeed, {@code false} otherwise.
     */
    public synchronized boolean cancel() {
        cancelled = true;
        return true;
    }

    /**
     * Reports the passed in {@code progress} to all listeners.
     * @param progress the {@code int} to be reported.
     */
    protected void reportProgress(int progress) {
        Status status = new Status();
        status.setProgress(progress);
        fireStatusUpdate(status);
    }
}
