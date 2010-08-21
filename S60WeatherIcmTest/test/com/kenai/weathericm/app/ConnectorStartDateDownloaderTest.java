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

import com.kenai.weathericm.util.Status;
import com.kenai.weathericm.util.StatusListener;
import com.kenai.weathericm.util.StatusReporter;
import java.io.DataInputStream;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import net.sf.microlog.core.config.PropertyConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

/**
 * Tests for {@link ConnectorStartDateDownloader}.
 * 
 * @author Przemek Kryger
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ConnectorStartDateDownloader.class, Connector.class})
@SuppressStaticInitializationFor("javax.microedition.io.Connector")
public class ConnectorStartDateDownloaderTest {

    @BeforeClass
    public static void setUpClass() {
        PropertyConfigurator.configure("/testMicrolog.properties");
    }
    private ConnectorStartDateDownloader fixture;
    private DummyListener listener;
    private HttpConnection connectionMock;
    private final static String CANCELLED = "cancelled";

    @Before
    public void setUp() {
        fixture = new ConnectorStartDateDownloader();
        listener = new DummyListener();
        connectionMock = createMock(HttpConnection.class);
        mockStatic(Connector.class);
    }

    @Test
    public void downloadStartDateOpenConnectionExeption() throws Exception {
        String url = "myUrl";
        expect(Connector.open(url)).andThrow(new IOException());
        replayAll();
        fixture.downloadStartDate(url);
        verifyAll();
    }

    @Test
    public void downloadStartDateConnectionExeption() throws Exception {
        String url = "myUrl";
        expect(Connector.open(url)).andReturn(connectionMock);
        expect(connectionMock.openDataInputStream()).andThrow(new IOException());
        connectionMock.close();
        replayAll();
        fixture.downloadStartDate(url);
        verifyAll();
    }

    @Test
    public void downloadStartDateStreamException() throws Exception {
        DataInputStream disMock = createMock(DataInputStream.class);
        String url = "myUrl";
        expect(Connector.open(url)).andReturn(connectionMock);
        expect(connectionMock.openDataInputStream()).andReturn(disMock);
        expect(connectionMock.getLength()).andReturn(-1L);
        expect(disMock.read()).andThrow(new IOException());
        disMock.close();
        connectionMock.close();
        replayAll();
        fixture.downloadStartDate(url);
        verifyAll();
    }

    @Test
    public void downloadStartDate() throws Exception {
        String data = "abcd";
        DataInputStream disMock = createMock(DataInputStream.class);
        String url = "myUrl";
        expect(Connector.open(url)).andReturn(connectionMock);
        expect(connectionMock.openDataInputStream()).andReturn(disMock);
        expect(connectionMock.getLength()).andReturn(-1L);
        for (int i = 0; i < data.length(); i++) {
            int ch = data.charAt(i);
            expect(disMock.read()).andReturn(ch);
        }
        expect(disMock.read()).andReturn(-1);
        disMock.close();
        connectionMock.close();
        replayAll();
        String actual = fixture.downloadStartDate(url);
        assertThat(actual, equalTo(data));
        verifyAll();
    }

    @Test
    public void cancel() {
        boolean result = fixture.cancel();
        assertThat(result, is(true));
        Boolean cancelled = Whitebox.getInternalState(fixture, CANCELLED);
        assertThat(cancelled, is(Boolean.TRUE));
    }

    @Test
    public void reportProgress() {
        int progress = 23;
        fixture.addListener(listener);
        fixture.reportProgress(progress);
        assertThat(listener.source, is((StatusReporter) fixture));
        assertThat(listener.status, is(notNullValue()));
        assertThat(listener.status.getProgress(), equalTo(progress));
    }

    private class DummyListener implements StatusListener {

        public StatusReporter source;
        public Status status;

        @Override
        public void statusUpdate(StatusReporter source, Status status) {
            this.source = source;
            this.status = status;
        }
    }
}
