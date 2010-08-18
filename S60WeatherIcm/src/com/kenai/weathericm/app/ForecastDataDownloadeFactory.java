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

/**
 * This is the factory used to get instances of {@link ForecastDataDownloader}s.
 * @author Przemek Kryger
 */
public class ForecastDataDownloadeFactory {

    /**
     * Gets the new instance of {@link ForecastDataDownloader} with propery configured
     * {@link StartDataDownloader} and {@link ModelResultDownloader}.
     * @return the new {@link ForecastDataDownloader}.
     */
    public static ForecastDataDownloader getDownloader() {
        return null;
    }

    /**
     * Sets the implemenation of {@link StartDataDownloader} to be used for new
     * {@link ForecastDataDownloader}s.
     * @param implementation the {@link Class} that implements {@link StartDataDownloader}.
     * @throws IllegalArgumentException if the the {@code implementation} cannot be instaniated
     *         or it doesn't implement {@link StartDataDownloader}.
     */
    public static void setStartDataDownloaderImplementation(Class implementation) {
    }

    /**
     * Sets the implemenation of {@link ModelResultDownloader} to be used for new
     * {@link ForecastDataDownloader}s.
     * @param implementation the {@link Class} that implements {@link ModelResultDownloader}.
     * @throws IllegalArgumentException if the the {@code implementation} cannot be instaniated
     *         or it doesn't implement {@link ModelResultDownloader}.
     */
    public static void setModelResultDownloaderImplementation(Class implementation) {
    }
}
