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
package com.kenai.weathericm.util;

//#mdebug
import net.sf.microlog.core.Logger;
import net.sf.microlog.core.LoggerFactory;
//#enddebug

/**
 * The holder for a {@link ForecastDownloadCancellableTask}'s status.
 * @author Przemek Kryger
 */
public class Status {
//#mdebug

    /**
     * The class logger.
     */
    private final static Logger log = LoggerFactory.getLogger(Status.class);
//#enddebug
    /**
     * Predefined status to be sent when {@link ForecastDownloadCancellableTask}
     * has been started.
     */
    public final static Status STARTED = new Status();
    /**
     * Predefined status to be sent when {@link ForecastDownloadCancellableTask}
     * has been canceled.
     */
    public final static Status CANCELLED = new Status();
    /**
     * Predefined status to be sent when {@link ForecastDownloadCancellableTask}
     * has finished.
     */
    public final static Status FINISHED = new Status();

    static {
        STARTED.setProgress(0);
        CANCELLED.setProgress(100);
        FINISHED.setProgress(100);
    }
    /**
     * The {@link ForecastDownloadCancellableTask} progress (in percent) to report.
     */
    private int progress = 0;

    /**
     * Gives the current {@value #progress}. It's a value in range from 0 to 100.
     * @return the progress
     */
    public int getProgress() {
        return progress;
    }

    /**
     * Sets current progress. It checks the value if it is in range from 0 to 100.
     * @param progress the progress to set
     * @throws {@link IllegalArgumentException} if {@code progress} is not in range.
     */
    public void setProgress(int progress) {
        if (progress >= 0 && progress <= 100) {
            this.progress = progress;
        } else {
//#mdebug
            log.error("Attempt to set a progress to: " + progress);
//#enddebug
            throw new IllegalArgumentException("Cannot set progress to: " + progress);
        }
    }

    /**
     * Converts this instance to {@link String}.
     * @return the {@link String} representation of this instance.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer("Status[progress = ");
        buffer.append(progress).append("]");
        return buffer.toString();
    }
}
