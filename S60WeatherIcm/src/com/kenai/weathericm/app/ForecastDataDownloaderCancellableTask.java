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

import com.kenai.weathericm.util.Status;
import org.netbeans.microedition.util.CancellableTask;

/**
 * This is the implementation of {@link AbstractForecastDataDownloader} that implements
 * {@link CancellableTask}.
 * @author Przemek Kryger
 */
public class ForecastDataDownloaderCancellableTask extends AbstractForecastDataDownloader
        implements CancellableTask {

    /**
     * Informs whether the task run was not successful. Since the {@value #succeed}
     * is set only after the task successfully finishes, so until finish this returns
     * {@code true}.
     * @return {@code true} if the task did not finish correctly.
     *         {@code false} if everything was OK.
     */
    public boolean hasFailed() {
        return !(lastStatus != null && lastStatus.equals(Status.FINISHED));
    }

    /**
     * Gets the reason for the failure. In the case there was not any failure, this method should return null.
     * @return A descriptive message of the failure or null if there was no failure.
     */
    public String getFailureMessage() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
