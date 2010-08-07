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

import com.kenai.weathericm.debug.MeteorogramInfoInMemoryDao;
import com.kenai.weathericm.domain.MeteorogramInfo;
import com.kenai.weathericm.repository.MeteorogramInfoDao;
import com.kenai.weathericm.repository.MeteorogramInfoSerializer;
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
        fixture.setDao(null);
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
        fixture.addListener(listener);
        MeteorogramInfoInMemoryDao dao = new MeteorogramInfoInMemoryDao();
        MeteorogramInfo info = new MeteorogramInfo();
        dao.create(info);
        fixture.setDao(dao);
        final MeteorogramBrokerListener lock = listener;
        synchronized (lock) {
            fixture.readAllMeteorogramInfos();
            lock.wait(timeout);
        }
        Vector actual = listener.readMeteorogramInfos;
        assertThat(actual.size(), equalTo(1));
        assertThat(actual.contains(info), is(true));
    }

    @Test
    public void createMeteorogramInfo() throws Exception {
        fixture.addListener(listener);
        MeteorogramInfoInMemoryDao dao = new MeteorogramInfoInMemoryDao();
        MeteorogramInfo info = new MeteorogramInfo();
        fixture.setDao(dao);
        final MeteorogramBrokerListener lock = listener;
        synchronized (lock) {
            fixture.createMeteorogramInfo(info);
            lock.wait(timeout);
        }
        MeteorogramInfo actual = listener.addedMeteorogramInfo;
        assertThat(actual, equalTo(info));
        Vector inDaoInfos = dao.readAll();
        assertThat(inDaoInfos.contains(info), is(true));
    }

    @Test
    public void updateMeteorogramInfo() throws Exception {
        fixture.addListener(listener);
        MeteorogramInfoInMemoryDao dao = new MeteorogramInfoInMemoryDao();
        MeteorogramInfo info = new MeteorogramInfo();
        dao.create(info);
        fixture.setDao(dao);
        final MeteorogramBrokerListener lock = listener;
        synchronized (lock) {
            fixture.updateMeteorogramInfo(info);
            lock.wait(timeout);
        }
        MeteorogramInfo actual = listener.updatedMeteorogramInfo;
        assertThat(actual, equalTo(info));
        Vector inDaoInfos = dao.readAll();
        assertThat(inDaoInfos.contains(info), is(true));
    }

    @Test
    public void deleteMeteorogramInfo() throws Exception {
        fixture.addListener(listener);
        MeteorogramInfoInMemoryDao dao = new MeteorogramInfoInMemoryDao();
        MeteorogramInfo info = new MeteorogramInfo();
        dao.create(info);
        fixture.setDao(dao);
        final MeteorogramBrokerListener lock = listener;
        synchronized (lock) {
            fixture.deleteMeteorogramInfo(info);
            lock.wait(timeout);
        }
        MeteorogramInfo actual = listener.deletedMeteorogramInfo;
        assertThat(actual, equalTo(info));
        Vector inDaoInfos = dao.readAll();
        assertThat(inDaoInfos.contains(info), is(false));
    }

    @Test
    public void getDownloadTaskNotExisting() {
        ForecastDownloadCancellableTask t1 =
                fixture.getDownloadTask(new MeteorogramInfo());
        ForecastDownloadCancellableTask t2 =
                fixture.getDownloadTask(new MeteorogramInfo());
        assertThat(t1, is(not(nullValue())));
        assertThat(t2, is(not(nullValue())));
        assertThat(t1, not(equalTo(t2)));
        Vector listeners = t1.getListeners();
        assertThat(listeners.contains(fixture), is(true));
        listeners = t2.getListeners();
        assertThat(listeners.contains(fixture), is(true));
    }

    @Test
    public void getDownloadTaskExisting() {
        MeteorogramInfo info = new MeteorogramInfo();
        ForecastDownloadCancellableTask t1 = fixture.getDownloadTask(info);
        ForecastDownloadCancellableTask t2 = fixture.getDownloadTask(info);
        assertThat(t1, is(not(nullValue())));
        assertThat(t2, is(not(nullValue())));
        assertThat(t1, equalTo(t2));
        Vector listeners = t1.getListeners();
        assertThat(listeners.contains(fixture), is(true));
    }

    @Test
    public void statusUpdateFinished() {
        fixture.addListener(listener);
        MeteorogramInfo info = new MeteorogramInfo();
        ForecastDownloadCancellableTask task = new ForecastDownloadCancellableTask(info);
        Hashtable infoToTask = (Hashtable) Whitebox.getInternalState(fixture, INFO_TO_TASK);
        infoToTask.put(info, task);
        fixture.statusUpdate(task, Status.FINISHED);
        infoToTask = (Hashtable) Whitebox.getInternalState(fixture, INFO_TO_TASK);
        assertThat(infoToTask.size(), equalTo(0));
        assertThat(listener.updatedMeteorogramInfo, is(notNullValue()));
        assertThat(listener.updatedMeteorogramInfo, equalTo(info));
    }

    @Test
    public void statusUpdateCanceled() {
        MeteorogramInfo info = new MeteorogramInfo();
        ForecastDownloadCancellableTask task = new ForecastDownloadCancellableTask(info);
        Hashtable infoToTask = (Hashtable) Whitebox.getInternalState(fixture, INFO_TO_TASK);
        infoToTask.put(info, task);
        fixture.statusUpdate(task, Status.CANCELLED);
        infoToTask = (Hashtable) Whitebox.getInternalState(fixture, INFO_TO_TASK);
        assertThat(infoToTask.size(), equalTo(0));
    }

    @Test
    public void statusUpdateNotFinishedNorCanceled() {
        MeteorogramInfo info = new MeteorogramInfo();
        ForecastDownloadCancellableTask task = new ForecastDownloadCancellableTask(info);
        Hashtable infoToTask = (Hashtable) Whitebox.getInternalState(fixture, INFO_TO_TASK);
        infoToTask.put(info, task);
        fixture.statusUpdate(task, new Status());
        infoToTask = (Hashtable) Whitebox.getInternalState(fixture, INFO_TO_TASK);
        assertThat(infoToTask.contains(task), is(true));
    }

    @Test(expected = NullPointerException.class)
    public void statusUpdateNullSource() {
        fixture.statusUpdate(null, Status.STARTED);
    }

    @Test(expected = NullPointerException.class)
    public void statusUpdateNullStatus() {
        fixture.statusUpdate(new ForecastDownloadCancellableTask(new MeteorogramInfo()),
                null);
    }

    @Test
    public void getSetDao() {
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
        fixture.setDao(null);
        assertThat(fixture.getDao(), is(nullValue()));
        fixture.setDao(expected);
        assertThat(fixture.getDao(), equalTo(expected));
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
}
