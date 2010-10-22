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

import com.kenai.weathericm.domain.Availability;
import com.kenai.weathericm.domain.ForecastData;
import com.kenai.weathericm.util.StatusListener;
import com.kenai.weathericm.util.StatusReporter;
import com.kenai.weathericm.util.Status;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
//#mdebug
import net.sf.microlog.core.Logger;
import net.sf.microlog.core.LoggerFactory;
//#enddebug
import com.kenai.weathericm.domain.MeteorogramInfo;
import com.kenai.weathericm.repository.ForecastDataDao;
import com.kenai.weathericm.repository.MeteorogramInfoDao;

/**
 * This is responsible for the controlling the application flow for {@link MeteorogramInfo}
 * persistence, and gathering the meteorogram info data.
 * @author Przemek Kryger
 */
public class MeteorogramBroker implements StatusListener {

//#mdebug
    /**
     * The logger for class.
     */
    private final static Logger log = LoggerFactory.getLogger(MeteorogramBroker.class);
//#enddebug
    /**
     * The {@link MeteorogramBroker} singleton instance.
     */
    private static MeteorogramBroker instance = null;
    /**
     * The {@link MeteorogramInfoDao} instance.
     */
    private MeteorogramInfoDao meteorogramInfoDao = null;
    /**
     * The {@link ForecastDataDao} instance.
     */
    private ForecastDataDao forecastDataDao = null;
    /**
     * All the registered {@link MeteorogramBrokerListener}s instances.
     */
    private final Vector listeners = new Vector();
    /**
     * The map that contains {@link MeteorogramInfo} to a corresponding
     * {@link ForecastDownloader}s mapping.
     */
    private Hashtable infoToDownloadTask = new Hashtable();

    /**
     * Private constructor for singleton safety.
     */
    private MeteorogramBroker() {
//#mdebug
        log.info("Created MeteorogramBroker instance");
//#enddebug
    }

    /**
     * Getter for a singleton instance.
     * @return the {@link MeteorogramBroker} instance.
     */
    public static MeteorogramBroker getInstance() {
        if (instance == null) {
            instance = new MeteorogramBroker();
        }
        return instance;
    }

    /**
     * @return the meteorogramInfoDao
     */
    public MeteorogramInfoDao getMeteorogramInfoDao() {
        return meteorogramInfoDao;
    }

    /**
     * @param meteorogramInfoDao the meteorogramInfoDao to set
     */
    public void setMeteorogramInfoDao(MeteorogramInfoDao meteorogramInfoDao) {
        this.meteorogramInfoDao = meteorogramInfoDao;
    }

    /**
     * Gets all the registered {@link MeteorogramBrokerListener}s.
     * @return the {@link Vector} that contains all registered listeners.
     */
    protected Vector getListeners() {
        Vector retValue = null;
        synchronized (listeners) {
            retValue = new Vector(listeners.size());
            Enumeration listenersEnum = listeners.elements();
            while (listenersEnum.hasMoreElements()) {
                Object listener = listenersEnum.nextElement();
                retValue.addElement(listener);
            }
        }
        return retValue;
    }

    /**
     * Registers the new {@code listener}.
     * @param listener the {@link MeteorogramBrokerListener} to register.
     */
    public void addListener(MeteorogramBrokerListener listener) {
        if (listener == null) {
            return;
        }
        synchronized (listeners) {
            if (!listeners.contains(listener)) {
                listeners.addElement(listener);
            }
        }
//#mdebug
        log.debug("Listener added");
//#enddebug
    }

    /**
     * Removes the {@code listener}.
     * @param listener the {@link MeteorogramBrokerListener} to remove.
     */
    public void removeListener(MeteorogramBrokerListener listener) {
        if (listener != null) {
            synchronized (listeners) {
                if (listeners.contains(listener)) {
                    listeners.removeElement(listener);
                }
            }
        }
//#mdebug
        log.debug("Listener removed");
//#enddebug
    }

    /**
     * Notifies all the {@link MeteorogramBrokerListener}s in {@value #listeners}
     * that {@code newCachedMeteorogramInfos} has been read from DAO.
     * @param newCachedMeteorogramInfos the {@link Vector} that contains read 
     *                                  {@link MeteorogramInfo}s.
     */
    protected void fireReadMeteorogramInfo(Vector newCachedMeteorogramInfos) {
//#mdebug
        log.info("Notifying: infos have been read from store!");
//#enddebug
        synchronized (listeners) {
            Enumeration e = getListeners().elements();
            while (e.hasMoreElements()) {
                MeteorogramBrokerListener listener = (MeteorogramBrokerListener) e.nextElement();
                listener.readMeteorogramInfo(newCachedMeteorogramInfos);
            }
        }
    }

    /**
     * Notifies all the {@link MeteorogramBrokerListener}s in {@value #listeners}
     * that {@code addedMeteorogramInfo} has been added to DAO.
     * @param addedMeteorogramInfo the {@link MeteorogramInfo} that has been added.
     */
    protected void fireAddedMeteorogramInfo(MeteorogramInfo addedMeteorogramInfo) {
//#mdebug
        log.info("Notifying: info has been added to store");
//#enddebug
        synchronized (listeners) {
            Enumeration e = getListeners().elements();
            while (e.hasMoreElements()) {
                MeteorogramBrokerListener listener = (MeteorogramBrokerListener) e.nextElement();
                listener.addedMeteorogramInfo(addedMeteorogramInfo);
            }
        }
    }

    /**
     * Notifies all the {@link MeteorogramBrokerListener}s in {@value #listeners}
     * that {@code deletedMeteorogramInfo} has been deleted from DAO.
     * @param deletedMeteorogramInfo the {@link MeteorogramInfo} that has been deleted.
     */
    protected void fireDeletedMeteorogramInfo(MeteorogramInfo deletedMeteorogramInfo) {
//#mdebug
        log.info("Notifying: info has been deleted from store");
//#enddebug
        synchronized (listeners) {
            Enumeration e = getListeners().elements();
            while (e.hasMoreElements()) {
                MeteorogramBrokerListener listener = (MeteorogramBrokerListener) e.nextElement();
                listener.deletedMeteorogramInfo(deletedMeteorogramInfo);
            }
        }
    }

    /**
     * Notifies all the {@link MeteorogramBrokerListener}s in {@value #listeners}
     * that {@code addedMeteorogramInfo} has been updated in DAO.
     * @param updatedMeteorogramInfo the {@link MeteorogramInfo} that has been updated.
     */
    protected void fireUpdatedMeteorogramInfo(MeteorogramInfo updatedMeteorogramInfo) {
//#mdebug
        log.info("Notifying: info has been updated in store");
//#enddebug
        synchronized (listeners) {
            Enumeration e = getListeners().elements();
            while (e.hasMoreElements()) {
                MeteorogramBrokerListener listener = (MeteorogramBrokerListener) e.nextElement();
                listener.updatedMeteorogramInfo(updatedMeteorogramInfo);
            }
        }
    }

    /**
     * Request for this broker to read all {@link MeteorogramInfo}s from DAO.
     */
    public void readAllMeteorogramInfos() {
        Thread reader = new Thread() {

            public void run() {
//#mdebug
                log.info("Reading all infos from DAO");
//#enddebug
                Vector meteorogramInfos = meteorogramInfoDao.readAll();
                Enumeration e = meteorogramInfos.elements();
                while (e.hasMoreElements()) {
                    MeteorogramInfo info = (MeteorogramInfo) e.nextElement();
                    if (forecastDataDao.exists(info.getId())) {
                        ForecastData forecastData = forecastDataDao.read(info.getId());
                        info.setForecastData(forecastData);
                    }
                }
                fireReadMeteorogramInfo(meteorogramInfos);
            }
        };
        reader.start();
    }

    /**
     * Creates the given {@code info} in DAO.
     * @param info the {@link MeteorogramInfo} to be created.
     */
    public void createMeteorogramInfo(final MeteorogramInfo info) {
        Thread creator = new Thread() {

            public void run() {
//#mdebug
                log.info("Creating info in DAO: " + info);
//#enddebug
                meteorogramInfoDao.create(info);
                if (forecastDataDao.exists(info.getId())) {
//#mdebug
                    log.warn("The ForecastData already exists for a brand new info, deleting it!");
//#enddebug
                    forecastDataDao.delete(info.getId());
                }
                fireAddedMeteorogramInfo(info);
            }
        };
        creator.start();
    }

    /**
     * Updates the given {@code info} in DAO.
     * @param info the {@link MeteorogramInfo} to be updated.
     */
    public void updateMeteorogramInfo(final MeteorogramInfo info) {
        Thread updater = new Thread() {

            public void run() {
//#mdebug
                log.info("Updating info in DAO: " + info);
//#enddebug
                meteorogramInfoDao.update(info);
                if (info.dataAvailability().equals(Availability.NOT_AVAILABLE)
                        && forecastDataDao.exists(info.getId())) {
//#mdebug
                    log.debug("The info has no ForecsastData any longer, let's delete it!");
//#enddebug
                    forecastDataDao.delete(info.getId());
                }
                fireUpdatedMeteorogramInfo(info);
            }
        };
        updater.start();
    }

    /**
     * Deletes the given {@code info} from DAO.
     * @param info the {@link MeteorogramInfo} to be delted.
     */
    public void deleteMeteorogramInfo(final MeteorogramInfo info) {
        Thread deleter = new Thread() {

            public void run() {
//#mdebug
                log.info("Deleting info from DAO: " + info);
//#enddebug
                forecastDataDao.delete(info.getId());
                meteorogramInfoDao.delete(info);
                fireDeletedMeteorogramInfo(info);
            }
        };
        deleter.start();
    }

    /**
     * Gets the the task that downloads forecast for a given info. In case there
     * already is a task that downloads forecast for a given info, it is returned.
     * Otherwise a new task is created.
     * The returned task will force downloading the forecastData for the info. However,
     * in case there is already a task for the given info, it's returned. It's the caller
     * responsibility with dealing with this.
     * @param info the {@link MeteorogramInfo} that needs a task to perform a download.
     * @return the {@link ForecastDataDownloader} that performs a download.
     */
    public ForecastDataDownloader getForcedDownloadTask(MeteorogramInfo info) {
//#mdebug
        log.info("Getting a forced downaload task for info: " + info);
//#enddebug
        ForecastDataDownloader task =
                (ForecastDataDownloader) infoToDownloadTask.get(info);
        if (task == null) {
//#mdebug
            log.debug("Creating a new forced download task for info: " + info);
//#enddebug
            task = ForecastDataDownloaderFactory.getForcedDownloader();
            task.setMeteorogramInfo(info);
            infoToDownloadTask.put(info, task);
        }
        task.addListener(this);
        return task;
    }

    /**
     * Gets the the task that downloads forecast for a given info. In case there
     * already is a task that downloads forecast for a given info, it is returned.
     * Otherwise a new task is created.
     * The returned task will check if downloading the forecastData for the info
     * is really needed. However, in case there is already a task for the given info,
     * it's returned. It's the caller responsibility with dealing with this.
     * @param info the {@link MeteorogramInfo} that needs a task to perform a download.
     * @return the {@link ForecastDataDownloader} that performs a download.
     */
    public ForecastDataDownloader getCheckedDownloadTask(MeteorogramInfo info) {
//#mdebug
        log.info("Getting a checked downaload task for info: " + info);
//#enddebug
        ForecastDataDownloader task =
                (ForecastDataDownloader) infoToDownloadTask.get(info);
        if (task == null) {
//#mdebug
            log.debug("Creating a new checked download task for info: " + info);
//#enddebug
            task = ForecastDataDownloaderFactory.getCheckedDownloader();
            task.setMeteorogramInfo(info);
            infoToDownloadTask.put(info, task);
        }
        task.addListener(this);
        return task;
    }

    /**
     * When one of the {@link ForecastDataDownloader}s finishes it's downloading
     * it's removed from mapping. It's done whenever {@code status} is one of:
     * {@value Status#CANCELLED} or {@value Status#FINISHED}.
     * @param source the {@link StatusReporter} that triggered the event.
     * @param status the {@link Status} that describes the status.
     * @throws NullPointerException when either {@code source} or {@code status} is {@code null}.
     */
    public void statusUpdate(StatusReporter source, Status status) {
        if (status == null || source == null) {
//#mdebug
            log.fatal("Status and/or source is null: status = " + status
                    + ", source = " + source);
//#enddebug
            throw new NullPointerException("Cannot update status for task if one of them is null!");
        } else if (!(source instanceof ForecastDataDownloader)) {
//#mdebug
            log.error("Someone strange reports to broker: " + source);
//#enddebug
        } else if (status == Status.CANCELLED
                || status == Status.FINISHED) {
            ForecastDataDownloader task = (ForecastDataDownloader) source;
            MeteorogramInfo info = task.getMeteorogramInfo();
            if (status == Status.FINISHED) {
                if (!info.dataAvailability().equals(Availability.NOT_AVAILABLE)) {
                    forecastDataDao.createOrUpdate(info.getId(), info.getForecastData());
                } else {
//#mdebug
                    log.warn("The download task has finished, but there is no ForecastData! " + source);
//#enddebug
                    forecastDataDao.delete(info.getId());
                }
                fireUpdatedMeteorogramInfo(info);
            }
            infoToDownloadTask.remove(info);
            task.removeListener(this);
        }
    }

    /**
     * @return the forecastDataDao
     */
    public ForecastDataDao getForecastDataDao() {
        return forecastDataDao;
    }

    /**
     * @param forecastDataDao the forecastDataDao to set
     */
    public void setForecastDataDao(ForecastDataDao forecastDataDao) {
        this.forecastDataDao = forecastDataDao;
    }
}
