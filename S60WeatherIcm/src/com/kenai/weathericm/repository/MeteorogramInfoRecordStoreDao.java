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

import java.util.Vector;
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordComparator;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStoreNotOpenException;
import com.kenai.weathericm.domain.MeteorogramInfo;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
//#mdebug
import net.sf.microlog.core.Logger;
import net.sf.microlog.core.LoggerFactory;
//#enddebug

/**
 * This is an implementation of {@link MeteorogramInfoDao} that is compatibile to
 * {@link RecordStore}. Since the record store access safety it implements
 * singleton pattern and new instances can be obtained by calling {@value #getInstance()).
 * @author Przemek Kryger
 * @see MeteorogramInfoRecordStoreDao#getInstance()
 */
public class MeteorogramInfoRecordStoreDao implements MeteorogramInfoDao {

    /**
     * The {@link RecordStore} store name
     */
    public final static String STORE = "MeteorogramInfo";
//#mdebug
    /**
     * The class logger.
     */
    private final static Logger log = LoggerFactory.getLogger(MeteorogramInfoRecordStoreDao.class);
//#enddebug
    /**
     * The {@link MeteorogramInfoSerializer} instance.
     */
    private MeteorogramInfoSerializer meteorogramInfoSerializer = null;

    /**
     * Default constructor is private for singleton safety.
     */
    private MeteorogramInfoRecordStoreDao() {
//#mdebug
        log.debug("Created MeteorogramInfoRecordStoreDao instance");
//#enddebug
    }
    /**
     * Singleton instance of {@link MeteorogramInfoRecordStoreDao}.
     */
    private static MeteorogramInfoRecordStoreDao instance;

    /**
     * Getter for singleton instance.
     * @return the {@link MeteorogramInfoRecordStoreDao} instance.
     */
    public static MeteorogramInfoRecordStoreDao getInstance() {
        if (instance == null) {
            instance = new MeteorogramInfoRecordStoreDao();
        }
        return instance;
    }

    /**
     * Creates given {@code info} in {@link RecordStore}. If something wen't wrong the {@code info}
     * is tainted.
     * @param info the {@link MeteorogramInfo} to be created in store.
     */
    public synchronized void create(MeteorogramInfo info) {
        if (info == null) {
//#mdebug
            log.error("Attempt to create info from null!");
//#enddebug
            throw new NullPointerException("Cannot create a record from null!");
        }
        if (info.getId() != null) {
//#mdebug
            log.warn("Attempt to create info that already exists in store! "
                    + "info = " + info);
//#enddebug
            throw new IllegalArgumentException("Record already exists!");
        }
//#mdebug
        log.debug("Creating info: " + info);
//#enddebug
        RecordStore store = null;
        try {
            store = RecordStore.openRecordStore(STORE, true);
            byte[] infoBytes = serialize(info);
            int id = store.addRecord(infoBytes, 0, infoBytes.length);
            info.setId(new Integer(id));
            info.setTainted(false);
//#mdebug
            log.info("Info created in store: " + info);
//#enddebug
        } catch (RecordStoreFullException ex) {
//#mdebug
            log.fatal("Info not created since RecordStore is full! "
                    + "info = " + info);
//#enddebug
            info.setId(null);
            info.setTainted(true);
        } catch (RecordStoreException ex) {
//#mdebug
            log.fatal("Info not created due to RecordStore problem! "
                    + "info = " + info, ex);
//#enddebug
            info.setId(null);
            info.setTainted(true);
        } finally {
            closeRecordStore(store);
        }
    }

    /**
     * Reads {@link MeteorogramInfo} with given {@code id} from {@link RecordStore}.
     * @param id the {@code int} of the entry in store to be read.
     * @return the {@link MeteorogramInfo} read from store or {@code null} if an
     * error occurred.
     */
    public synchronized MeteorogramInfo read(int id) {
//#mdebug
        log.debug("Reading info: " + id);
//#enddebug
        RecordStore store = null;
        MeteorogramInfo info = null;
        try {
            store = RecordStore.openRecordStore(STORE, true);
            byte[] infoBytes = store.getRecord(id);
            info = resurect(id, infoBytes);
            if (info.isTainted()) {
//#mdebug
                log.warn("Error while decoding info data from RecordStore bytes!");
//#enddebug
                delete(info);
                info = null;
            }
//#mdebug
            log.info("Read info from store: " + info);
//#enddebug
        } catch (RecordStoreException ex) {
//#mdebug
            log.fatal("Info not read due to RecordStore problem!", ex);
//#enddebug
        } finally {
            closeRecordStore(store);
        }
        return info;
    }

    /**
     * Updates given {@code info} in {@link RecordStore}. If something wen't wrong the {@code info}
     * is tainted.
     * @param info the {@link MeteorogramInfo} to be updated in store.
     */
    public synchronized void update(MeteorogramInfo info) {
        if (info == null) {
//#mdebug
            log.error("Attempt to update info with null!");
//#enddebug
            throw new NullPointerException("Cannot update record with null!");
        }
        if (info.getId() == null) {
//#mdebug
            log.warn("Attempt to update info that hasn't been already created! "
                    + "info = " + info);
//#enddebug
            throw new IllegalArgumentException("Cannot update record without Id!");
        }
        if (!info.isTainted()) {
//#mdebug
            log.info("Info not updated since it is not tainted! "
                    + "info = " + info);
//#enddebug
            return;
        }
//#mdebug
        log.debug("Updating info in store: " + info);
//#enddebug
        RecordStore store = null;
        try {
            store = RecordStore.openRecordStore(STORE, true);
            int id = info.getId().intValue();
            byte[] infoBytes = serialize(info);
            store.setRecord(id, infoBytes, 0, infoBytes.length);
            info.setTainted(false);
//#mdebug
            log.info("Info updated in store: " + info);
//#enddebug
        } catch (InvalidRecordIDException ex) {
//#mdebug
            log.error("Info not updated since it doesn't exist in RecordStore! "
                    + "info = " + info);
//#enddebug
            info.setId(null);
        } catch (RecordStoreException ex) {
//#mdebug
            log.fatal("Info not updated due to recordStore problem! "
                    + "info = " + info, ex);
//#enddebug
        } finally {
            closeRecordStore(store);
        }
    }

    /**
     * Deletes given {@code info} from {@link RecordStore}.
     * @param info the {@link MeteorogramInfo} to be deleted from store.
     */
    public synchronized void delete(MeteorogramInfo info) {
        if (info == null) {
//#mdebug
            log.error("Attempt to delete null info!");
//#enddebug
            throw new NullPointerException("Cannot delete null!");
        }
        if (info.getId() == null) {
//#mdebug
            log.error("Attempt to delete info that desn't exist in store! "
                    + "info = " + info);
//#enddebug
            throw new IllegalArgumentException("Cannot delete record with null id!");
        }
        int id = info.getId().intValue();
        RecordStore store = null;
//#mdebug
        log.debug("Deleting info from store: " + info);
//#enddebug
        try {
            store = RecordStore.openRecordStore(STORE, true);
            store.deleteRecord(id);
            info.setId(null);
//#mdebug
            log.info("Info deleted from store: " + info);
//#enddebug
        } catch (InvalidRecordIDException ex) {
//#mdebug
            log.error("Info not deleted since it doesn't exist in store! "
                    + "info = " + info);
//#enddebug
            info.setId(null);
        } catch (RecordStoreException ex) {
//#mdebug
            log.fatal("Info not deleted due to RecordStore problem! "
                    + "info = " + info, ex);
//#enddebug
        } finally {
            closeRecordStore(store);
        }
    }

    /**
     * Convenience method to persists {@code info} in {@link RecordStore}. It creates an entry
     * if one dosn't exist in store or update it if it already exists in store.
     * @param info the {@link MeteorogramInfo} to be persisted.
     */
    public synchronized void createOrUpdate(MeteorogramInfo info) {
        if (info == null) {
//#mdebug
            log.error("Attempt to create/uptdate info with null!");
//#enddebug
            throw new NullPointerException("Cannot create nor update null!");
        }
        if (info.getId() == null) {
//#mdebug
            log.info("Info seems to not exist in store, let's create it!");
//#enddebug
            create(info);
        } else {
//#mdebug
            log.info("Info already exists in store, let's update it!");
//#enddebug
            update(info);
            if (info.getId() == null) {
//#mdebug
                log.warn("Something went wrong while updating info in store. "
                        + "Let's try to create it!");
//#enddebug
                create(info);
            }
        }
    }

    /**
     * Convenience method to read all {@link MeteorogramInfo} entries from {@link RecordStore}.
     * The order of read elements depends on given implementation.
     * @return the {@link Vector} with all {@link MeteorogramInfo}s in store.
     */
    public synchronized Vector readAll() {
        RecordStore store = null;
        Vector infos = new Vector(0);
        RecordEnumeration records = null;
        try {
            store = RecordStore.openRecordStore(STORE, true);
            records = store.enumerateRecords(
                    null, getRecordComparator(), false);
            records.keepUpdated(true);
//#mdebug
            int i = 1;
            log.info("There are " + records.numRecords() + " infos in store");
//#enddebug
            while (records.hasNextElement()) {
//#mdebug
                log.debug("Deserializeing info, no = " + i++);
//#enddebug
                int id = records.nextRecordId();
                byte[] infoBytes = store.getRecord(id);
                MeteorogramInfo info = resurect(id, infoBytes);
                if (info.isTainted()) {
//#mdebug
                    log.debug("Info is broken... deleting it: " + info);
//#enddebug
                    delete(info);
                } else {
//#mdebug
                    log.debug("Successfully deserialized info: " + info);
//#enddebug
                    infos.addElement(info);
                }
            }
        } catch (RecordStoreException ex) {
//#mdebug
            log.fatal("Infos not read due to RecordStore problem!", ex);
//#enddebug
        } finally {
            if (records != null) {
                records.destroy();
            }
            closeRecordStore(store);
        }
        return infos;
    }

    /**
     * Gets the {@link RecordComparator} implementation to make sure objects are
     * always read in the same order.
     * @return for now {@code null}
     */
    protected RecordComparator getRecordComparator() {
        //@todo design method to compare records?
        return null;
    }

    /**
     * Helper method to communicate with {@value #meteorogramInfoSerializer} to
     * encode {@code info} into {@link RecordStore} format.
     * @param info the {@link MeteorogramInfo} to be encoded.
     * @return the {@code byte[]} array with encoded data.
     * @see MeteorogramInfoSerializer#serialize(MeteorogramInfo)
     */
    protected byte[] serialize(MeteorogramInfo info) {
        return getMeteorogramInfoSerializer().serialize(info);
    }

    /**
     * Helper method to communicate with (@value #meteorogramInfoSerializer} to
     * decode {@code infoBytes} from {@link RecordStore} format.
     * @param id the {@code int} id of the resurected {@link MeteorogramInfo}
     * @param infoBytes the {@code byte[]} array to be decoded.
     * @return the {@link MeteorogramInfo} from {@code infoBytes}.
     * @see MeteorogramInfoSerializer#resurect(int, byte[])
     */
    protected MeteorogramInfo resurect(int id, byte[] infoBytes) {
        return getMeteorogramInfoSerializer().resurect(id, infoBytes);
    }

    /**
     * Convenience method to close {@code store}.
     * @param store the {@link RecordStore} to be closed.
     */
    protected void closeRecordStore(RecordStore store) {
//#mdebug
        log.info("Closing record store: " + STORE);
//#enddebug
        if (store != null) {
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

    /**
     * Getter for serialized used to encode/decode data for store.
     * @return the {@link MeteorogramInfoSerializer}.
     */
    public MeteorogramInfoSerializer getMeteorogramInfoSerializer() {
        return meteorogramInfoSerializer;
    }

    /**
     * Setter for serializer used to encode/decode data for store.
     * @param meteorogramInfoSerializer the {@link MeteorogramInfoSerializer} to set.
     */
    public void setMeteorogramInfoSerializer(MeteorogramInfoSerializer meteorogramInfoSerializer) {
        this.meteorogramInfoSerializer = meteorogramInfoSerializer;
    }
}
