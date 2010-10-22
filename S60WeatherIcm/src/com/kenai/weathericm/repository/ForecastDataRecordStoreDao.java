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
import java.util.Vector;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;
import net.sf.microlog.core.Logger;
import net.sf.microlog.core.LoggerFactory;

/**
 * This uses {@link RecordStore} to persist {@link ForecastData} objects. This class
 * manages multiple {@link RecordStore}s that contains actual data. Each of these
 * stores contains at most one {@link ForecastData}'s data, however one {@link
 * ForecastData} may be persisted in multiple stores.
 * @author Przemek Kryger
 */
public class ForecastDataRecordStoreDao implements ForecastDataDao {

//#mdebug
    /**
     * The logger for the class.
     */
    private final static Logger log = LoggerFactory.getLogger(ForecastDataRecordStoreDao.class);
//#enddebug
    /**
     * The prefix to be used for all the {@link RecordStore}s managed by this DAO.
     */
    public static final String STORE_PREFIX = "ForecastData";
    /**
     * The infix to be used for all the {@link RecordStore}s managed by this DAO.
     */
    public static final String STORE_INFIX = "_";
    /**
     * The minimum RecordStore space that is required before the record is going
     * to be persisted. 
     */
    public static final int MIN_STORE_SPACE = 64;
    /**
     * Singleton instance.
     */
    private static ForecastDataRecordStoreDao instance = null;
    /**
     * The serializer to be used to for {@link ForecastData} to/from {@code byte}
     * array conversions.
     */
    private ForecastDataSerializer serializer = null;

    /**
     * Private constructor for singleton.
     */
    private ForecastDataRecordStoreDao() {
//#mdebug
        log.info("Creating new instance.");
//#enddebug
    }

    /**
     * Getter for the {@link ForecastDataRecordStoreDao} singleton instance.
     * @return the singleton instance.
     */
    public static ForecastDataRecordStoreDao getInstance() {
        if (instance == null) {
            instance = new ForecastDataRecordStoreDao();
        }
        return instance;
    }

    public boolean create(Integer id, ForecastData forecastData) {
        if (id == null || forecastData == null) {
//#mdebug
            log.error("Cannot create record store(s) with id = " + id
                    + " and data = " + forecastData);
//#enddebug
            throw new NullPointerException("Cannot persit forecast data!");
        }
        if (exists(id)) {
//#mdebug
            log.error("Cannot create record store(s) for id = " + id
                    + " since it already exists!");
//#enddebug
            return false;
        }
//#mdebug
        log.trace("Creating record store(s) for id = " + id + " and data = " + forecastData);
//#enddebug
        boolean stored = false;
        RecordStore store = null;
        int rsNumber = 1;
        String rsPrefix = convertIdToBaseName(id);
        byte[] toWrite = serializer.serialize(forecastData);
        int bytesWritten = 0;
        try {
            while (bytesWritten < toWrite.length) {
                store = RecordStore.openRecordStore(rsPrefix + rsNumber, true);
                int chunkSize = (int) (store.getSizeAvailable() * 0.95);
                int leftSize = toWrite.length - bytesWritten;
                if (chunkSize < MIN_STORE_SPACE && chunkSize < leftSize) {
                    break;
                }
                store.addRecord(toWrite, bytesWritten, chunkSize > leftSize ? leftSize : chunkSize);
                bytesWritten += chunkSize;
                store.closeRecordStore();
                store = null;
                rsNumber++;
            }
            stored = true;
        } catch (RecordStoreException ex) {
//#mdebug
            log.error("Creating record store(s) has failed!", ex);
//#enddebug
        } catch (IllegalArgumentException ex) {
//#mdebug
            log.error("Creating record store(s) has failed!", ex);
//#enddebug
        } catch (SecurityException ex) {
//#mdebug
            log.error("Creating record store(s) has failed!", ex);
//#enddebug
        } finally {
            if (store != null) {
//#mdebug
                log.info("Closing record store");
//#enddebug
                try {
                    store.closeRecordStore();
                } catch (RecordStoreNotOpenException ex) {
//#mdebug
                    log.error("Attempt to close RecordStore that hasn't been already open!");
//#enddebug
                } catch (RecordStoreException ex) {
//#mdebug
                    log.fatal("Error while closing store due to RecordStore problem!", ex);
//#enddebug
                }
            }
        }
        if (!stored) {
//#mdebug
            log.warn("Creating record store(s) for id = " + id + " and data = " + forecastData
                    + "has failed - cleaning up");
//#enddebug
            delete(id);
        }
        return stored;
    }

    public ForecastData read(Integer id) {
        if (id == null) {
//#mdebug
            log.error("Cannot read forecast data with null id!");
//#enddebug
            throw new NullPointerException("Cannot read forecast data!");
        }
//#mdebug
        log.trace("Reading forecast data with id = " + id);
//#enddebug
        ForecastData forecastData = null;
        String[] recordStores = RecordStore.listRecordStores();
        if (recordStores != null) {
            String prefix = convertIdToBaseName(id);
            Vector stores = new Vector();
            for (int i = 0; i < recordStores.length; i++) {
                if (recordStores[i].startsWith(prefix)) {
                    stores.addElement(recordStores[i]);
                }
            }
            if (!stores.isEmpty()) {
                Vector dataVector = new Vector();
                boolean allRead = true;
                int storeNumber = 1;
                while (!stores.isEmpty()) {
                    String storeName = prefix + storeNumber;
                    if (!stores.removeElement(storeName)) {
//#mdebug
                        log.warn("Cannot find record store with name = " + storeName);
//#enddebug
                        allRead = false;
                        break;
                    }
                    if (!readRecordStoreToVector(storeName, dataVector)) {
//#mdebug
                        log.warn("Cannot read record store with name = " + storeName);
//#enddebug
                        allRead = false;
                        break;
                    }
                    storeNumber++;
                }
                if (allRead == true) {
                    int dataSize = 0;
                    for (int i = 0; i < dataVector.size(); i++) {
                        dataSize += ((byte[])dataVector.elementAt(i)).length;
                    }
                    byte[] rawData = new byte[dataSize];
                    int i = 0;
                    while (!dataVector.isEmpty()) {
                        byte[] bytes = (byte[])dataVector.elementAt(0);
                        for (int j = 0; j < bytes.length; j++) {
                            rawData[i] = bytes[j];
                            i++;
                        }
                        dataVector.removeElement(bytes);
                    }
                    forecastData = serializer.resurect(rawData);
                }
            } else {
//#mdebug
                log.info("Cannot find any record store for id = " + id);
//#enddebug
            }
        }
        return forecastData;
    }

    /**
     * Convenience method to read a single record from record store with given
     * {@codestoreName} and put it into a {@codedataVector} as a {@codebyte[]} array.
     * @param storeName the {@link String} with @{link{RecordStore} name to read data from.
     * @param dataVector the {@link Vector} to store the read {@code byte[]} array.
     * @return
     */
    private boolean readRecordStoreToVector(String storeName, Vector dataVector) {
        boolean read = false;
        RecordStore store = null;
        try {
            store = RecordStore.openRecordStore(storeName, false);
            RecordEnumeration records = store.enumerateRecords(null, null, false);
            dataVector.addElement(records.nextRecord());
            read = true;
        } catch (RecordStoreException ex) {
//#mdebug
            log.error("Reading from record store failed!", ex);
//#enddebug
        } catch (IllegalArgumentException ex) {
//#mdebug
            log.error("Reading from record store failed!", ex);
//#enddebug
        } finally {
            if (store != null) {
//#mdebug
                log.info("Closing record store");
//#enddebug
                try {
                    store.closeRecordStore();
                } catch (RecordStoreNotOpenException ex) {
//#mdebug
                    log.error("Attempt to close RecordStore that hasn't been already open!");
//#enddebug
                } catch (RecordStoreException ex) {
//#mdebug
                    log.fatal("Error while closing store due to RecordStore problem!", ex);
//#enddebug
                }
            }
        }
        return read;
    }
    
    public boolean update(Integer id, ForecastData forecastData) {
        if (id == null) {
//#mdebug
            log.error("Cannot update forecast data with null id!");
//#enddebug
            throw new NullPointerException("Cannot update forecast data!");
        }
        if (forecastData == null) {
//#mdebug
            log.error("Cannot update forecast data id = " + id + " with null data!");
//#enddebug
            throw new NullPointerException("Cannot update forecast data!");
        }
//#mdebug
        log.trace("Updating forecast data with id = " + id + " and data = " + forecastData);
//#enddebug
        boolean status = delete(id);
        if (status == true) {
            status = create(id, forecastData);
            if (status == false) {
//#mdebug
                log.warn("Cannot update forecast data with id = " + id + ": creation failed!");
//#enddebug
            }
        } else {
//#mdebug
            log.warn("Cannot update forecast data with id = " + id + ": deletion failed!");
//#enddebug
        }
        return status;
    }

    public boolean delete(Integer id) {
        if (id == null) {
//#mdebug
            log.error("Cannot delete forecast data with null id!");
//#enddebug
            throw new NullPointerException("Cannot delete forecast data!");
        }
        boolean deleted = false;
        boolean noFailures = true;
        String match = convertIdToBaseName(id);
        String[] recordStores = RecordStore.listRecordStores();
        for (int i = 0; i < recordStores.length; i++) {
            try {
                if (recordStores[i].startsWith(match)) {
//#mdebug
                    log.trace("Deleteing record store: " + recordStores[i]);
//#enddebug
                    RecordStore.deleteRecordStore(recordStores[i]);
                    deleted = true;
                }
            } catch (RecordStoreException ex) {
//#mdebug
                log.error("Cannot delete record store: " + ex);
//#enddebug
                noFailures = false;
            }
        }
//#mdebug
        if (!deleted) {
            log.warn("No record stores deleted for id = " + id);
        }
//#enddebug
        return deleted && noFailures;
    }

    public boolean exists(Integer id) {
//#mdebug
        log.trace("Checking if forecast data exist for id = " + id);
//#enddebug
        String[] recordStores = RecordStore.listRecordStores();
        if (recordStores != null) {
            String match = convertIdToBaseName(id);
            for (int i = 0; i < recordStores.length; i++) {
                if (recordStores[i].startsWith(match)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean createOrUpdate(Integer id, ForecastData forecastData) {
        if (id == null) {
//#mdebug
            log.error("Cannot create nor update forecast data with null id!");
//#enddebug
            throw new NullPointerException("Cannot create nor update forecast data!");
        }
        if (forecastData == null) {
//#mdebug
            log.error("Cannot create nor update forecast data id = " + id + " with null data!");
//#enddebug
            throw new NullPointerException("Cannot create nor update forecast data!");
        }
//#mdebug
        log.trace("Creating or updating forecast data with id = " + id + " and data = " + forecastData);
//#enddebug
        boolean status = false;
        if (exists(id)) {
            status = update(id, forecastData);
        } else {
            status = create(id, forecastData);
        }
        return status;
    }

    /**
     * Converts given {@code id} to base name of a record store to be used.
     * @param id the {@link Integer} to be used to create base name.
     * @return the {@link String} to be used as a store base name.
     * @throws NullPointerException if {@code id} is {@code null}.
     */
    protected String convertIdToBaseName(Integer id) {
        if (id == null) {
//#mdebug
            log.error("Cannot create basename for record store with null!");
//#enddebug
            throw new NullPointerException("Cannot create basename for forecast data!");
        }
        StringBuffer buffer = new StringBuffer(STORE_PREFIX);
        buffer.append(id.toString());
        buffer.append(STORE_INFIX);
//#mdebug
        log.trace("Base name created from id = " + id.toString() + " is: " + buffer.toString());
//#enddebug
        return buffer.toString();
    }

    /**
     * Sets the serializer to be used to convert {@link ForecastData}s to/from
     * {@code byte} arrays. It needs to be set before the object can do its work.
     * @param serializer the {@link ForecastDataSerializer} to be used.
     */
    public void setForecastDataSerializer(ForecastDataSerializer serializer) {
//#mdebug
        log.trace("Setting serialzer to: " + serializer);
//#enddebug
        this.serializer = serializer;
    }
}
