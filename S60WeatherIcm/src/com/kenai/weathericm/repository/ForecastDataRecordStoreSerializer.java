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

import com.kenai.weathericm.domain.ForecastData;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
//#mdebug
import net.sf.microlog.core.Logger;
import net.sf.microlog.core.LoggerFactory;
//#enddebug

/**
 * This one serializes/resurects the {@link ForecastData} to/from a form convinint
 * to be persisted in record store.
 * @author Przemek Kryger
 */
public class ForecastDataRecordStoreSerializer implements ForecastDataSerializer {

//#mdebug
    /**
     * The class logger.
     */
    private final static Logger log = LoggerFactory.getLogger(ForecastDataRecordStoreSerializer.class);
//#enddebug

    /**
     * The serialized header size. It's the time of model start (long) + the size of a model result (int)
     */
    private final static int HEADER_LENGTH = 8 + 4;
    
    /**
     * Transforms given {@code forecastData} into array of {@code byte}s. The
     * implementation shall be symetric to the {@value #resurect(byte)}.
     * @param forecastData the {@link ForecastData} to be serialized.
     * @return the array of {@code byte}s that represents the data.
     * @see #resurect(data)
     * @throws NullPointerException in case the {@code forecastData} is {@code null}.
     */
    public byte[] serialize(ForecastData forecastData) {
        if (forecastData == null) {
//#mdebug
            log.error("Cannot serialize null forecast data!");
//#enddebug
            throw new NullPointerException("Cannot serialize forecast data!");
        }
        if (forecastData.getModelResult() == null || forecastData.getModelStart() == null) {
//#mdebug
            log.error("Cannot serialize forecast data with null fields! "
                    + forecastData.getModelResult() == null ? " model is null " : ""
                    + forecastData.getModelStart() == null ? " model start is null " : "");
//#enddebug
            throw new NullPointerException("Cannot serialize forecast data with null!");
        }
//#mdebug
        log.trace("Serializing forecast data: " + forecastData);
//#enddebug
        byte[] serialized = null;
        byte[] modelResult = forecastData.getModelResult();
        int bufferSize = HEADER_LENGTH + modelResult.length;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(bufferSize);
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeLong(forecastData.getModelStart().getTime());
            dos.writeInt(modelResult.length);
            dos.write(modelResult, 0, modelResult.length);
            dos.flush();
            serialized = baos.toByteArray();
        } catch (IOException ex) {
//#mdebug
            log.warn("Cannot serialize forecast data! " + forecastData, ex);
//#enddebug
        }
        return serialized;
    }

    /**
     * Creates the {@link ForecastData} from given array of {@code bytes}. The
     * implementation shall be symetric to the {@value
     * #serialize(com.kenai.weathericm.domain.ForecastData)}.
     * @param data the {@code byte} array to create {@link ForecastData} from.
     * @return the {@link ForecastData} created form {@code data}s
     * @throws NullPointerException in case the {@code data} is {@code null}.
     */
    public ForecastData resurect(byte[] data) {
        if (data == null) {
            log.error("Cannoct resurect forecast data with null data!");
//#mdebug
            throw new NullPointerException("Cannot resurect forecast data!");
//#enddebug
        }
//#mdebug
        log.trace("Resurecting forecast data");
//#enddebug
        ForecastData resurected = null;
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);
        try {
            long modelStartTime = dis.readLong();
            int modelResultLength = dis.readInt();
            if (modelStartTime > 0 && modelResultLength > 0
                    && (HEADER_LENGTH + modelResultLength) == data.length) {
                byte[] modelResult = new byte[modelResultLength];
                dis.readFully(modelResult);
                Date date = new Date(modelStartTime);
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                calendar.setTime(date);
                resurected = new ForecastData(calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH),
                        calendar.get(Calendar.HOUR_OF_DAY));
                resurected.setModelResult(modelResult);
            }
        } catch (IOException ex) {
//#mdebug
            log.warn("Cannot deserialize forecast data!", ex);
//#enddebug
        } catch (IllegalArgumentException ex) {
//#mdebug
            log.warn("Cannod deserialize forecast data!", ex);
//#enddebug
        }
        return resurected;
    }

}
