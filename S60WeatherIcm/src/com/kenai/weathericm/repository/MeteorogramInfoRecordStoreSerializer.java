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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.rms.RecordStore;
//#mdebug
import net.sf.microlog.core.Logger;
import net.sf.microlog.core.LoggerFactory;
//#enddebug
import com.kenai.weathericm.domain.MeteorogramInfo;
import com.kenai.weathericm.domain.MeteorogramType;

/**
 * This is an implementation of {@link MeteorogramInfoSerializer} that is compatible to
 * {@link RecordStore}
 * @author Przemek Kryger
 */
public class MeteorogramInfoRecordStoreSerializer implements MeteorogramInfoSerializer {

//#mdebug
    /**
     * The class logger.
     */
    private final static Logger log = LoggerFactory.getLogger(MeteorogramInfoRecordStoreSerializer.class);
//#enddebug

    /**
     * Serializes given {@code info} to {@code byte[]} array. It uses
     * {@link DataOutputStream} to encode {@code info} into {@code byte[]}s array.
     * @param info {@link MeteorogramInfo} to be serialized.
     * @return {@code byte[]} array that represents {@code info}.
     * @see #resurect(int, byte[])
     */
    public byte[] serialize(MeteorogramInfo info) {
        byte[] infoBytes = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeUTF(info.getName());
            dos.writeInt(info.getX());
            dos.writeInt(info.getY());
            dos.writeInt(info.getType().getValue());
            infoBytes = baos.toByteArray();
        } catch (IOException ex) {
//#mdebug
            log.error("Cannot serialize info to bytes!", ex);
//#enddebug
        }
        return infoBytes;
    }

    /**
     * Resurects given {@code infoBytes} and {@code id} to {@link MeteorogramInfo}.
     * It uses {@link DataInputStream} to decode {@link MeteorogramInfo}
     * from {@code byte[]}s array.
     * @param id the {@code int} to set as {@link MeteorogramInfo} id.
     * @param infoBytes the {@code byte[]} to be deserialized
     * @return {@link MeteorogramInfo} that represents {@code infoBytes}
     * @see #serialize(weathericm.domain.MeteorogramInfo)
     */
    public MeteorogramInfo resurect(int id, byte[] infoBytes) {
        MeteorogramInfo info = new MeteorogramInfo();
        info.setId(new Integer(id));
        info.setTainted(true);
        ByteArrayInputStream bais = new ByteArrayInputStream(infoBytes);
        DataInputStream dis = new DataInputStream(bais);
        try {
            String name = dis.readUTF();
            info.setName(name);
            int x = dis.readInt();
            info.setX(x);
            int y = dis.readInt();
            info.setY(y);
            MeteorogramType type = MeteorogramType.getByValue(dis.readInt());
            info.setType(type);
            info.setTainted(false);
        } catch (IOException ex) {
//#mdebug
            log.error("Cannot deserialize info from bytes!", ex);
//#enddebug
        }
        return info;
    }
}
