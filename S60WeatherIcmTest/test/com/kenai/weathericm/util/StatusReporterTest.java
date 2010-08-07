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
package com.kenai.weathericm.util;

import java.util.Vector;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Przemek Kryger
 */
public class StatusReporterTest {

    private StatusReporter fixture = null;
    private DummyListener listener = null;

    @Before
    public void setUp() {
        fixture = new StatusReporter() {};
        listener = new DummyListener();
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
        StatusListener dummy1 = new DummyListener();
        StatusListener dummy2 = new DummyListener();
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
        fixture.removeListener(new DummyListener());
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
    public void fireStatusUpdate() {
        fixture.addListener(listener);
        Status status = new Status();
        fixture.fireStatusUpdate(status);
        assertThat(listener.source, is(fixture));
        assertThat(listener.status, is(status));
    }

    private class DummyListener implements StatusListener {

        public StatusReporter source = null;
        public Status status = null;

        public void statusUpdate(StatusReporter source, Status status) {
            this.source = source;
            this.status = status;
        }
    }
}
