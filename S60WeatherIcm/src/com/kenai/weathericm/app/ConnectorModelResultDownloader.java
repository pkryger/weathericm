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
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
//#mdebug
import net.sf.microlog.core.Logger;
import net.sf.microlog.core.LoggerFactory;
//#enddebug

/**
 * This downloader uses {@link Connector} and {@link HttpConnection} stuff to get
 * model result from the given URL.
 * @author Przemek Kryger
 */
public class ConnectorModelResultDownloader extends AbstractStatusReporter implements ModelResultDownloader {

//#mdebug
    /**
     * The logger for the class
     */
    private final static Logger log = LoggerFactory.getLogger(ConnectorModelResultDownloader.class);
//#enddebug
    /**
     * Indicates if the downlad shall be cancelled.
     */
    private boolean cancelled = false;

    /**
     * Downloads the data from the gived {@code url}.
     * @param url the {@link String} with url to download data from.
     * @return the downloaded data
     */
    public byte[] downloadModelResult(String url) {
        HttpConnection connection = null;
        DataInputStream dis = null;
        byte[] retValue = null;
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
            // Image is 19800(UM) or 23500(COAMPS) by default
            long totalBytes = connection.getLength();
            if (totalBytes == -1) {
                totalBytes = 23500L;
//#mdebug
                log.debug("Setting totalBytes to default = " + totalBytes);
//#enddebug
            }
            long percentChunk = totalBytes / 100L;
            long chunkCounter = 0;
            int percent = 0;
            int readByte;
            Vector imageBuffer = new Vector((int) totalBytes);
            while ((readByte = dis.read()) != -1) {
                imageBuffer.addElement(new Byte((byte) readByte));
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
            retValue = new byte[imageBuffer.size()];
//#mdebug
            log.info(this + ": Parsing received image data...");
//#enddebug
            for (int i = 0; i < retValue.length; i++) {
                retValue[i] = ((Byte) imageBuffer.elementAt(i)).byteValue();
            }
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
     * Aborts the download opertaion.
     * @return {@code true} if cancelled succed, {@code false} otherwise.
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
