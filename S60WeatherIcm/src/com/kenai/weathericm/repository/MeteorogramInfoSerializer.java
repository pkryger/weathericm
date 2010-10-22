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
package com.kenai.weathericm.repository;

import com.kenai.weathericm.domain.MeteorogramInfo;

/**
 * This is a contract for serialization and deserialization of
 * {@link MeteorogramInfo}s to/from {@code byte[]} arrays.
 * @author Przemek Kryger
 */
public interface MeteorogramInfoSerializer {

    /**
     * Serializes given {@code info} to {@code byte[]} array. This shall be
     * reverse operation to {@value #resurect(int, byte[])}.
     * @param info {@link MeteorogramInfo} to be serialized.
     * @return {@code byte[]} array that represents {@code info}.
     * @see #resurect(int, byte[])
     */
    byte[] serialize(MeteorogramInfo info);

    /**
     * resurects given {@code infoBytes} and {@code id} to {@link MeteorogramInfo}.
     * This shall be reverse operation to
     * {@value #serialize(weathericm.domain.MeteorogramInfo)}
     * @param id the {@code int} to set as {@link MeteorogramInfo} id.
     * @param infoBytes the {@code byte[]} to be deserialized
     * @return {@link MeteorogramInfo} that represents {@code infoBytes}
     * @see #serialize(weathericm.domain.MeteorogramInfo) 
     */
    MeteorogramInfo resurect(int id, byte[] infoBytes);
}
