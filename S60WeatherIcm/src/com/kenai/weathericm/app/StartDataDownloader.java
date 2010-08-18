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

import com.kenai.weathericm.domain.MeteorogramType;

/**
 * The interface for the task that will get the start data from the network.
 * @author Przemek Kryger
 */
public interface StartDataDownloader {

    /**
     * Downloads the start data for the given {@code type}.
     * @param type the {@link MeteorogramType the data shall be downloaded.
     * @return the {@link String} that contains start data.
     */
    String downloadStartData(MeteorogramType type);
}
