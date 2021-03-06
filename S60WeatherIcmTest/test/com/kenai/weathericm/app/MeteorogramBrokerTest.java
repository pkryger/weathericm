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

import com.kenai.weathericm.app.helpers.ForecastDataInMemoryDao;
import com.kenai.weathericm.domain.ForecastData;
import com.kenai.weathericm.app.helpers.MeteorogramInfoInMemoryDao;
import com.kenai.weathericm.domain.MeteorogramInfo;
import com.kenai.weathericm.repository.ForecastDataDao;
import com.kenai.weathericm.repository.ForecastDataSerializer;
import com.kenai.weathericm.repository.MeteorogramInfoDao;
import com.kenai.weathericm.repository.MeteorogramInfoSerializer;
import com.kenai.weathericm.util.AbstractStatusReporter;
import com.kenai.weathericm.util.Status;
import net.sf.microlog.core.config.PropertyConfigurator;
import org.junit.BeforeClass;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

/**
 * Tests for {@link MeteorogramBroker}.
 * @author Przemek Kryger
 */
public class MeteorogramBrokerTest {

    public final static String INFO_TO_TASK = "infoToDownloadTask";
    private MeteorogramBroker fixture = null;
    private DummyMeteorogramBrokerListener listener = null;
    private long timeout = 500;

    @BeforeClass
    public static void setUpClass() {
        PropertyConfigurator.configure("/testMicrolog.properties");
    }

    @Before
    public void setUp() {
        fixture = MeteorogramBroker.getInstance();
        Vector listenersVector = fixture.getListeners();
        if (listenersVector != null) {
            Enumeration listeners = listenersVector.elements();
            while (listeners.hasMoreElements()) {
                MeteorogramBrokerListener listener = (MeteorogramBrokerListener) listeners.nextElement();
                fixture.removeListener(listener);
            }
        }
        Whitebox.setInternalState(fixture, INFO_TO_TASK, new Hashtable());
        fixture.setMeteorogramInfoDao(null);
        listener = getDummyListener();
    }

    @Test
    public void singleton() {
        MeteorogramBroker other = MeteorogramBroker.getInstance();
        assertThat(other, is(not(nullValue())));
        assertThat(other, is(fixture));
    }

    @Test
    public void getListeners() {
        fixture.addListener(listener);
        Vector listeners1 = fixture.getListeners();
        Vector listeners2 = fixture.getListeners();
        assertThat(listeners1, is(not(nullValue())));
        assertThat(listeners2, is(not(nullValue())));
        assertThat(listeners1 == listeners2, is(false));
        assertThat(listeners1.size(), equalTo(listeners2.size()));
        for (int i = 0; i < listeners1.size(); i++) {
            Object listener1 = listeners1.get(i);
            Object listener2 = listeners2.get(i);
            assertThat(listener1, equalTo(listener2));
        }
    }

    @Test
    public void addListenerNull() {
        fixture.addListener(null);
        Vector actualListeners = fixture.getListeners();
        assertThat(actualListeners, is(not(nullValue())));
        assertThat(actualListeners.size(), equalTo(0));
    }

    @Test
    public void addListenerNotNull() {
        MeteorogramBrokerListener dummy1 = getDummyListener();
        MeteorogramBrokerListener dummy2 = getDummyListener();
        Vector actualListeners = null;

        fixture.addListener(dummy1);
        actualListeners = fixture.getListeners();
        assertThat(actualListeners, is(not(nullValue())));
        assertThat(actualListeners.size(), equalTo(1));
        assertThat(actualListeners.contains(dummy1), is(true));

        fixture.addListener(dummy1);
        actualListeners = fixture.getListeners();
        assertThat(actualListeners, is(not(nullValue())));
        assertThat(actualListeners.size(), equalTo(1));
        assertThat(actualListeners.contains(dummy1), is(true));

        fixture.addListener(dummy2);
        actualListeners = fixture.getListeners();
        assertThat(actualListeners, is(not(nullValue())));
        assertThat(actualListeners.size(), equalTo(2));
        assertThat(actualListeners.contains(dummy1), is(true));
        assertThat(actualListeners.contains(dummy2), is(true));
    }

    @Test
    public void removeListenerNull() {
        fixture.addListener(listener);
        fixture.removeListener(null);
        Vector listeners = fixture.getListeners();
        assertThat(listeners, is(not(nullValue())));
        assertThat(listeners.size(), equalTo(1));
        assertThat(listeners.contains(listener), is(true));
    }

    @Test
    public void removeListenerNotInList() {
        fixture.addListener(listener);
        fixture.removeListener(getDummyListener());
        Vector listeners = fixture.getListeners();
        assertThat(listeners, is(not(nullValue())));
        assertThat(listeners.size(), equalTo(1));
        assertThat(listeners.contains(listener), is(true));
    }

    @Test
    public void removeListenerInList() {
        fixture.addListener(listener);
        fixture.removeListener(listener);
        Vector listeners = fixture.getListeners();
        assertThat(listeners, is(not(nullValue())));
        assertThat(listeners.size(), equalTo(0));
        assertThat(listeners.contains(listener), is(false));
    }

    @Test
    public void fireReadMeteorogramInfo() {
        Vector testInfos = new Vector();
        fixture.addListener(listener);
        fixture.fireReadMeteorogramInfo(testInfos);
        assertThat(listener.readMeteorogramInfos, is(testInfos));
    }

    @Test
    public void fireAddedMeteorogramInfo() {
        MeteorogramInfo testInfo = new MeteorogramInfo();
        fixture.addListener(listener);
        fixture.fireAddedMeteorogramInfo(testInfo);
        assertThat(listener.addedMeteorogramInfo, equalTo(testInfo));
    }

    @Test
    public void fireDeletedMeteorogramInfo() {
        MeteorogramInfo testInfo = new MeteorogramInfo();
        fixture.addListener(listener);
        fixture.fireDeletedMeteorogramInfo(testInfo);
        assertThat(listener.deletedMeteorogramInfo, equalTo(testInfo));
    }

    @Test
    public void fireUpdateMeteorogramInfo() {
        MeteorogramInfo testInfo = new MeteorogramInfo();
        fixture.addListener(listener);
        fixture.fireUpdatedMeteorogramInfo(testInfo);
        assertThat(listener.updatedMeteorogramInfo, equalTo(testInfo));
    }

    @Test
    public void readAllMeteorogramInfos() throws Exception {
        ForecastDataDao forecastDataDao = new ForecastDataInMemoryDao();
        fixture.setForecastDataDao(forecastDataDao);
        fixture.addListener(listener);
        MeteorogramInfoInMemoryDao meteorogramInfoInMemoryDao = new MeteorogramInfoInMemoryDao();
        MeteorogramInfo infoWithData = new MeteorogramInfo();
        MeteorogramInfo infoWithoutData = new MeteorogramInfo();
        meteorogramInfoInMemoryDao.create(infoWithData);
        meteorogramInfoInMemoryDao.create(infoWithoutData);
        forecastDataDao.create(infoWithData.getId(), new ForecastData("1994090800"));
        fixture.setMeteorogramInfoDao(meteorogramInfoInMemoryDao);
        final MeteorogramBrokerListener lock = listener;
        synchronized (lock) {
            fixture.readAllMeteorogramInfos();
            lock.wait(timeout);
        }
        Vector actual = listener.readMeteorogramInfos;
        assertThat(actual.size(), equalTo(2));
        assertThat(actual.contains(infoWithData), is(true));
        assertThat(actual.contains(infoWithoutData), is(true));
        assertThat(infoWithoutData.getForecastData(), is(nullValue()));
        assertThat(infoWithData.getForecastData(), is(not(nullValue())));
    }

    @Test
    public void createMeteorogramInfo() throws Exception {
        ForecastDataDao forecastDataDao = new ForecastDataInMemoryDao();
        fixture.setForecastDataDao(forecastDataDao);
        fixture.addListener(listener);
        MeteorogramInfoInMemoryDao meteorogramInfoInMemoryDao = new MeteorogramInfoInMemoryDao();
        MeteorogramInfo info = new MeteorogramInfo();
        int id = (Integer) Whitebox.getInternalState(meteorogramInfoInMemoryDao, "lastElement");
        forecastDataDao.create(id, new ForecastData("1999080701"));
        fixture.setMeteorogramInfoDao(meteorogramInfoInMemoryDao);
        final MeteorogramBrokerListener lock = listener;
        synchronized (lock) {
            fixture.createMeteorogramInfo(info);
            lock.wait(timeout);
        }
        MeteorogramInfo actual = listener.addedMeteorogramInfo;
        assertThat(actual, equalTo(info));
        Vector inDaoInfos = meteorogramInfoInMemoryDao.readAll();
        assertThat(inDaoInfos.contains(info), is(true));
        assertThat(forecastDataDao.exists(id), is(false));
    }

    @Test
    public void updateMeteorogramInfoForecastDataLost() throws Exception {
        ForecastDataDao forecastDataDao = new ForecastDataInMemoryDao();
        fixture.setForecastDataDao(forecastDataDao);
        fixture.addListener(listener);
        MeteorogramInfoInMemoryDao meteorogramInfoInMemoryDao = new MeteorogramInfoInMemoryDao();
        MeteorogramInfo info = new MeteorogramInfo();
        ForecastData forecastData = new ForecastData("1954101018");
        meteorogramInfoInMemoryDao.create(info);
        int id = info.getId();
        forecastDataDao.create(id, forecastData);
        fixture.setMeteorogramInfoDao(meteorogramInfoInMemoryDao);
        final MeteorogramBrokerListener lock = listener;
        synchronized (lock) {
            fixture.updateMeteorogramInfo(info);
            lock.wait(timeout);
        }
        MeteorogramInfo actual = listener.updatedMeteorogramInfo;
        assertThat(actual, equalTo(info));
        Vector inDaoInfos = meteorogramInfoInMemoryDao.readAll();
        assertThat(inDaoInfos.contains(info), is(true));
        assertThat(forecastDataDao.exists(id), is(false));
    }

    @Test
    public void updateMeteorogramInfoForecastDataNotLost() throws Exception {
        ForecastDataDao forecastDataDao = new ForecastDataInMemoryDao();
        fixture.setForecastDataDao(forecastDataDao);
        fixture.addListener(listener);
        MeteorogramInfoInMemoryDao meteorogramInfoInMemoryDao = new MeteorogramInfoInMemoryDao();
        MeteorogramInfo info = new MeteorogramInfo();
        ForecastData forecastData = new ForecastData("1954101018");
        info.setForecastData(forecastData);
        meteorogramInfoInMemoryDao.create(info);
        int id = info.getId();
        forecastDataDao.create(id, forecastData);
        fixture.setMeteorogramInfoDao(meteorogramInfoInMemoryDao);
        final MeteorogramBrokerListener lock = listener;
        synchronized (lock) {
            fixture.updateMeteorogramInfo(info);
            lock.wait(timeout);
        }
        MeteorogramInfo actual = listener.updatedMeteorogramInfo;
        assertThat(actual, equalTo(info));
        Vector inDaoInfos = meteorogramInfoInMemoryDao.readAll();
        assertThat(inDaoInfos.contains(info), is(true));
        assertThat(forecastDataDao.exists(id), is(true));
    }

    @Test
    public void deleteMeteorogramInfo() throws Exception {
        ForecastDataDao forecastDataDao = new ForecastDataInMemoryDao();
        fixture.setForecastDataDao(forecastDataDao);
        fixture.addListener(listener);
        MeteorogramInfoInMemoryDao meteorogramInfoInMemoryDao = new MeteorogramInfoInMemoryDao();
        MeteorogramInfo info = new MeteorogramInfo();
        meteorogramInfoInMemoryDao.create(info);
        int id = info.getId();
        forecastDataDao.create(id, new ForecastData("1999080701"));
        fixture.setMeteorogramInfoDao(meteorogramInfoInMemoryDao);
        final MeteorogramBrokerListener lock = listener;
        synchronized (lock) {
            fixture.deleteMeteorogramInfo(info);
            lock.wait(timeout);
        }
        MeteorogramInfo actual = listener.deletedMeteorogramInfo;
        assertThat(actual, equalTo(info));
        Vector inDaoInfos = meteorogramInfoInMemoryDao.readAll();
        assertThat(inDaoInfos.contains(info), is(false));
        assertThat(forecastDataDao.exists(id), is(false));
    }

    @Test
    public void getForcedDownloadTaskNotExisting() {
        ForecastDataDownloader t1 =
                fixture.getForcedDownloadTask(new MeteorogramInfo());
        ForecastDataDownloader t2 =
                fixture.getForcedDownloadTask(new MeteorogramInfo());
        assertThat(t1, is(not(nullValue())));
        assertThat(t2, is(not(nullValue())));
        assertThat(t1, not(equalTo(t2)));
        Vector listeners = t1.getListeners();
        assertThat(listeners.contains(fixture), is(true));
        listeners = t2.getListeners();
        assertThat(listeners.contains(fixture), is(true));
    }

    @Test
    public void getForcedDownloadTaskExisting() {
        MeteorogramInfo info = new MeteorogramInfo();
        ForecastDataDownloader t1 = fixture.getForcedDownloadTask(info);
        ForecastDataDownloader t2 = fixture.getForcedDownloadTask(info);
        assertThat(t1, is(not(nullValue())));
        assertThat(t2, is(not(nullValue())));
        assertThat(t1, equalTo(t2));
        Vector listeners = t1.getListeners();
        assertThat(listeners.contains(fixture), is(true));
    }

    @Test
    public void getCheckedDownloadTaskNotExisting() {
        ForecastDataDownloader t1 =
                fixture.getCheckedDownloadTask(new MeteorogramInfo());
        ForecastDataDownloader t2 =
                fixture.getCheckedDownloadTask(new MeteorogramInfo());
        assertThat(t1, is(not(nullValue())));
        assertThat(t2, is(not(nullValue())));
        assertThat(t1, not(equalTo(t2)));
        Vector listeners = t1.getListeners();
        assertThat(listeners.contains(fixture), is(true));
        listeners = t2.getListeners();
        assertThat(listeners.contains(fixture), is(true));
    }

    @Test
    public void getCheckedDownloadTaskExisting() {
        MeteorogramInfo info = new MeteorogramInfo();
        ForecastDataDownloader t1 = fixture.getCheckedDownloadTask(info);
        ForecastDataDownloader t2 = fixture.getCheckedDownloadTask(info);
        assertThat(t1, is(not(nullValue())));
        assertThat(t2, is(not(nullValue())));
        assertThat(t1, equalTo(t2));
        Vector listeners = t1.getListeners();
        assertThat(listeners.contains(fixture), is(true));
    }

    @Test
    public void getCheckedForcedDownloadTaskExisting() {
        MeteorogramInfo info = new MeteorogramInfo();
        ForecastDataDownloader t1 = fixture.getCheckedDownloadTask(info);
        ForecastDataDownloader t2 = fixture.getForcedDownloadTask(info);
        assertThat(t1, is(not(nullValue())));
        assertThat(t2, is(not(nullValue())));
        assertThat(t1, equalTo(t2));
        Vector listeners = t1.getListeners();
        assertThat(listeners.contains(fixture), is(true));
    }

    @Test
    public void getForcedCheckedDownloadTaskExisting() {
        MeteorogramInfo info = new MeteorogramInfo();
        ForecastDataDownloader t1 = fixture.getForcedDownloadTask(info);
        ForecastDataDownloader t2 = fixture.getCheckedDownloadTask(info);
        assertThat(t1, is(not(nullValue())));
        assertThat(t2, is(not(nullValue())));
        assertThat(t1, equalTo(t2));
        Vector listeners = t1.getListeners();
        assertThat(listeners.contains(fixture), is(true));
    }

    @Test
    public void statusUpdateFinished() {
        fixture.addListener(listener);
        ForecastDataDao forecastDataDao = new ForecastDataInMemoryDao();
        fixture.setForecastDataDao(forecastDataDao);
        MeteorogramInfo info = new MeteorogramInfo();
        int id = 7;
        info.setId(id);
        ForecastData data = new ForecastData("2009090900");
        info.setForecastData(data);
        ForecastDataDownloader task = new DummyForecastDataDownloader();
        task.setMeteorogramInfo(info);
        Hashtable infoToTask = (Hashtable) Whitebox.getInternalState(fixture, INFO_TO_TASK);
        infoToTask.put(info, task);
        fixture.statusUpdate(task, Status.FINISHED);
        infoToTask = (Hashtable) Whitebox.getInternalState(fixture, INFO_TO_TASK);
        assertThat(infoToTask.size(), equalTo(0));
        assertThat(listener.updatedMeteorogramInfo, is(notNullValue()));
        assertThat(listener.updatedMeteorogramInfo, equalTo(info));
        Vector taskListeners = task.getListeners();
        assertThat(taskListeners.contains(fixture), is(false));
        assertThat(forecastDataDao.exists(id), is(true));
    }

    @Test
    public void statusUpdateCanceled() {
        ForecastDataDao forecastDataDao = new ForecastDataInMemoryDao();
        fixture.setForecastDataDao(forecastDataDao);
        MeteorogramInfo info = new MeteorogramInfo();
        int id = 5;
        info.setId(id);
        ForecastData data = new ForecastData("2009080900");
        info.setForecastData(data);
        ForecastDataDownloader task = new DummyForecastDataDownloader();
        task.setMeteorogramInfo(info);
        Hashtable infoToTask = (Hashtable) Whitebox.getInternalState(fixture, INFO_TO_TASK);
        infoToTask.put(info, task);
        fixture.statusUpdate(task, Status.CANCELLED);
        infoToTask = (Hashtable) Whitebox.getInternalState(fixture, INFO_TO_TASK);
        assertThat(infoToTask.size(), equalTo(0));
        Vector taskListeners = task.getListeners();
        assertThat(taskListeners.contains(fixture), is(false));
        assertThat(forecastDataDao.exists(id), is(false));
    }

    @Test
    public void statusUpdateNotFinishedNorCanceled() {
        ForecastDataDao forecastDataDao = new ForecastDataInMemoryDao();
        fixture.setForecastDataDao(forecastDataDao);
        MeteorogramInfo info = new MeteorogramInfo();
        int id = 9;
        info.setId(id);
        ForecastData data = new ForecastData("2009090800");
        info.setForecastData(data);
        ForecastDataDownloader task = new DummyForecastDataDownloader();
        Hashtable infoToTask = (Hashtable) Whitebox.getInternalState(fixture, INFO_TO_TASK);
        infoToTask.put(info, task);
        fixture.statusUpdate(task, new Status());
        infoToTask = (Hashtable) Whitebox.getInternalState(fixture, INFO_TO_TASK);
        assertThat(infoToTask.contains(task), is(true));
        assertThat(forecastDataDao.exists(id), is(false));
    }

    @Test(expected = NullPointerException.class)
    public void statusUpdateNullSource() {
        fixture.statusUpdate(null, Status.STARTED);
    }

    @Test(expected = NullPointerException.class)
    public void statusUpdateNullStatus() {
        fixture.statusUpdate(new DummyForecastDataDownloader(), null);
    }

    @Test
    public void getSetMeteorogramInfoDao() {
        MeteorogramInfoDao expected = new MeteorogramInfoDao() {

            public void create(MeteorogramInfo info) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void update(MeteorogramInfo info) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void delete(MeteorogramInfo info) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void createOrUpdate(MeteorogramInfo info) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public MeteorogramInfo read(int id) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public Vector readAll() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public MeteorogramInfoSerializer getMeteorogramInfoSerializer() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void setMeteorogramInfoSerializer(MeteorogramInfoSerializer meteorogramInfoSerializer) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        fixture.setMeteorogramInfoDao(null);
        assertThat(fixture.getMeteorogramInfoDao(), is(nullValue()));
        fixture.setMeteorogramInfoDao(expected);
        assertThat(fixture.getMeteorogramInfoDao(), is(expected));
    }

    @Test
    public void getSetForecastDataDao() {
        ForecastDataDao expected = new ForecastDataDao() {

            @Override
            public boolean create(Integer id, ForecastData forecastData) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public ForecastData read(Integer id) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean update(Integer id, ForecastData forecastData) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean delete(Integer id) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean exists(Integer id) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean createOrUpdate(Integer id, ForecastData forecastData) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setForecastDataSerializer(ForecastDataSerializer serializer) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        fixture.setForecastDataDao(expected);
        assertThat(fixture.getForecastDataDao(), is(expected));
        fixture.setForecastDataDao(null);
        assertThat(fixture.getForecastDataDao(), is(nullValue()));
    }

    private DummyMeteorogramBrokerListener getDummyListener() {
        return new DummyMeteorogramBrokerListener();
    }

    private class DummyMeteorogramBrokerListener implements MeteorogramBrokerListener {

        public Vector readMeteorogramInfos = null;

        public void readMeteorogramInfo(Vector readMeteorogramInfos) {
            this.readMeteorogramInfos = readMeteorogramInfos;
            synchronized (this) {
                this.notify();
            }
        }
        public MeteorogramInfo addedMeteorogramInfo = null;

        public void addedMeteorogramInfo(MeteorogramInfo addedMeteorigramInfo) {
            this.addedMeteorogramInfo = addedMeteorigramInfo;
            synchronized (this) {
                this.notify();
            }
        }
        public MeteorogramInfo deletedMeteorogramInfo = null;

        public void deletedMeteorogramInfo(MeteorogramInfo deletedMeteorogramInfo) {
            this.deletedMeteorogramInfo = deletedMeteorogramInfo;
            synchronized (this) {
                this.notify();
            }
        }
        public MeteorogramInfo updatedMeteorogramInfo = null;

        public void updatedMeteorogramInfo(MeteorogramInfo updatedMeteorogramInfo) {
            this.updatedMeteorogramInfo = updatedMeteorogramInfo;
            synchronized (this) {
                this.notify();
            }
        }
    }

    private class DummyForecastDataDownloader extends AbstractStatusReporter implements ForecastDataDownloader {

        MeteorogramInfo info;

        @Override
        public boolean cancel() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setMeteorogramInfo(MeteorogramInfo info) {
            this.info = info;
        }

        @Override
        public MeteorogramInfo getMeteorogramInfo() {
            return info;
        }

        @Override
        public void setStartDateDownloader(StartDateDownloader startDateDownloader) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setModelResultDownloader(ModelResultDownloader modelResultDownloader) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void run() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setModelResultDownloadChecker(ModelDownloadChecker modelDownloadChecker) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
