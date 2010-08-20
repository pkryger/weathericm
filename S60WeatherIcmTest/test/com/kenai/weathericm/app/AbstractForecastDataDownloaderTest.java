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

import com.kenai.weathericm.domain.MeteorogramInfo;
import com.kenai.weathericm.domain.MeteorogramType;
import com.kenai.weathericm.util.AbstractStatusReporter;
import com.kenai.weathericm.util.Properties;
import com.kenai.weathericm.util.Status;
import com.kenai.weathericm.util.StatusListener;
import com.kenai.weathericm.util.StatusReporter;
import net.sf.microlog.core.config.PropertyConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

/**
 * Tests for {@link AbstractForecastDataDownloader}
 * @author Przemek Kryger
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(AbstractForecastDataDownloader.class)
public class AbstractForecastDataDownloaderTest {

    @BeforeClass
    public static void setUpClass() {
        PropertyConfigurator.configure("/testMicrolog.properties");
    }
    private MeteorogramInfo info;
    private AbstractForecastDataDownloader fixture;
    private StartDateDownloader startDateDownloader;
    private ModelResultDownloader modelResultDownloader;
    private DummyListener listener;
    private Thread threadMock;
    private Properties properties;
    private final static String PROGRESS = "progress";
    private final static String MY_THREAD = "myThread";
    private final static String CANCELLED = "cancelled";
    private final static String START_DATE_DOWNLOADER = "startDateDownloader";
    private final static String MODEL_RESULT_DOWNLOADER = "modelResultDownloader";

    @Before
    public void setUp() {
        info = new MeteorogramInfo();
        startDateDownloader = new DummyStartDateDownloader();
        modelResultDownloader = new DummyModelResultDownloader();
        fixture = new AbstractForecastDataDownloader() {
        };
        fixture.setStartDateDownloader(startDateDownloader);
        fixture.setModelResultDownloader(modelResultDownloader);
        fixture.setMeteorogramInfo(info);
        listener = new DummyListener();
        threadMock = createMock(Thread.class);
        properties = new Properties();
        properties.setProperty(AbstractForecastDataDownloader.IMAGE_URL_PREFIX_KEY, "img.url");
        properties.setProperty(AbstractForecastDataDownloader.PARSE_DAY_KEY, "day.key");
        properties.setProperty(AbstractForecastDataDownloader.PARSE_HOUR_KEY, "hour.key");
        properties.setProperty(AbstractForecastDataDownloader.PARSE_MONTH_KEY, "month.key");
        properties.setProperty(AbstractForecastDataDownloader.PARSE_YEAR_KEY, "year.key");
        mockStatic(Thread.class);
    }

    @Test
    public void cancelSuccess() {
        int progress = 5;
        Whitebox.setInternalState(fixture, MY_THREAD, threadMock);
        Whitebox.setInternalState(fixture, PROGRESS, progress);
        threadMock.interrupt();
        replayAll();
        boolean cancelled = fixture.cancel();
        assertThat(cancelled, is(true));
        Boolean internalCancelled = Whitebox.getInternalState(fixture, CANCELLED);
        assertThat(internalCancelled, equalTo(Boolean.TRUE));
        assertThat(startDateDownloader.getListeners().contains(fixture), is(false));
        assertThat(modelResultDownloader.getListeners().contains(fixture), is(false));
        verifyAll();
    }

    @Test
    public void cancelFail() {
        int progress = -1;
        Whitebox.setInternalState(fixture, MY_THREAD, (String) null);
        Whitebox.setInternalState(fixture, PROGRESS, progress);
        replayAll();
        boolean cancelled = fixture.cancel();
        assertThat(cancelled, is(false));
        Boolean internalCancelled = Whitebox.getInternalState(fixture, CANCELLED);
        assertThat(internalCancelled, equalTo(Boolean.FALSE));
        verifyAll();
        assertThat(startDateDownloader.getListeners().contains(fixture), is(true));
        assertThat(modelResultDownloader.getListeners().contains(fixture), is(true));
    }

    @Test
    public void runAlreadyStarted() throws Exception {
        int progress = 5;
        Whitebox.setInternalState(fixture, MY_THREAD, threadMock);
        Whitebox.setInternalState(fixture, PROGRESS, progress);
        threadMock.join();
        replayAll();
        fixture.run();
        verifyAll();
    }

    @Test
    public void loadTypePropertiesUm() {
        info.setType(MeteorogramType.UM);
        Properties actual = fixture.loadTypeProperties();
        assertThat(actual, is(not(nullValue())));
        assertThat(actual.getProperty(AbstractForecastDataDownloader.IMAGE_URL_PREFIX_KEY),
                is(not(nullValue())));
        assertThat(actual.getProperty(AbstractForecastDataDownloader.PARSE_DAY_KEY),
                is(not(nullValue())));
        assertThat(actual.getProperty(AbstractForecastDataDownloader.PARSE_MONTH_KEY),
                is(not(nullValue())));
        assertThat(actual.getProperty(AbstractForecastDataDownloader.PARSE_HOUR_KEY),
                is(not(nullValue())));
        assertThat(actual.getProperty(AbstractForecastDataDownloader.PARSE_YEAR_KEY),
                is(not(nullValue())));
    }

    @Test
    public void loadTypePropertiesCoamps() {
        info.setType(MeteorogramType.COAMPS);
        Properties actual = fixture.loadTypeProperties();
        assertThat(actual, is(not(nullValue())));
        assertThat(actual.getProperty(AbstractForecastDataDownloader.IMAGE_URL_PREFIX_KEY),
                is(not(nullValue())));
        assertThat(actual.getProperty(AbstractForecastDataDownloader.PARSE_DAY_KEY),
                is(not(nullValue())));
        assertThat(actual.getProperty(AbstractForecastDataDownloader.PARSE_MONTH_KEY),
                is(not(nullValue())));
        assertThat(actual.getProperty(AbstractForecastDataDownloader.PARSE_HOUR_KEY),
                is(not(nullValue())));
        assertThat(actual.getProperty(AbstractForecastDataDownloader.PARSE_YEAR_KEY),
                is(not(nullValue())));
    }

    @Test
    public void parseStartDate() {
        String day = "12";
        String month = "13";
        String year = "1999";
        String hour = "18";
        String expected = year + month + day + hour;
        StringBuffer buffer = prepareDateStringBuffer(year, month, day, hour);
        String actual = fixture.parseStartDate(buffer, properties);
        assertThat(actual, equalTo(expected));
    }

    @Test(expected = NullPointerException.class)
    public void parseStartDateNullBuffer() {
        fixture.parseStartDate(null, properties);
    }

    @Test(expected = NullPointerException.class)
    public void parseStartDateNullProperties() {
        fixture.parseStartDate(new StringBuffer(), null);
    }

    @Test(expected = NullPointerException.class)
    public void parseStartDateNoYearKey() {
        properties.remove(AbstractForecastDataDownloader.PARSE_YEAR_KEY);
        String day = "12";
        String month = "13";
        String year = "1999";
        String hour = "18";
        StringBuffer buffer = prepareDateStringBuffer(year, month, day, hour);
        fixture.parseStartDate(buffer, properties);
    }

    @Test(expected = NullPointerException.class)
    public void parseStartDateNoMonthKey() {
        properties.remove(AbstractForecastDataDownloader.PARSE_MONTH_KEY);
        String day = "12";
        String month = "13";
        String year = "1999";
        String hour = "18";
        StringBuffer buffer = prepareDateStringBuffer(year, month, day, hour);
        fixture.parseStartDate(buffer, properties);
    }

    @Test(expected = NullPointerException.class)
    public void parseStartDateNoDayKey() {
        properties.remove(AbstractForecastDataDownloader.PARSE_DAY_KEY);
        String day = "12";
        String month = "13";
        String year = "1999";
        String hour = "18";
        StringBuffer buffer = prepareDateStringBuffer(year, month, day, hour);
        fixture.parseStartDate(buffer, properties);
    }

    @Test(expected = NullPointerException.class)
    public void parseStartDateNoHourKey() {
        properties.remove(AbstractForecastDataDownloader.PARSE_HOUR_KEY);
        String day = "12";
        String month = "13";
        String year = "1999";
        String hour = "18";
        StringBuffer buffer = prepareDateStringBuffer(year, month, day, hour);
        fixture.parseStartDate(buffer, properties);
    }

    @Test(expected = NullPointerException.class)
    public void parseStartDateNoYear() {
        String day = "12";
        String month = "13";
        String hour = "18";
        StringBuffer buffer = prepareDateStringBuffer(null, month, day, hour);
        fixture.parseStartDate(buffer, properties);
    }

    @Test(expected = NullPointerException.class)
    public void parseStartDateNoMonth() {
        String day = "12";
        String year = "2000";
        String hour = "18";
        StringBuffer buffer = prepareDateStringBuffer(year, null, day, hour);
        fixture.parseStartDate(buffer, properties);
    }

    @Test(expected = NullPointerException.class)
    public void parseStartDateNoDay() {
        String year = "2012";
        String month = "13";
        String hour = "18";
        StringBuffer buffer = prepareDateStringBuffer(year, month, null, hour);
        fixture.parseStartDate(buffer, properties);
    }

    @Test(expected = NullPointerException.class)
    public void parseStartDateNoHour() {
        String day = "12";
        String month = "13";
        String year = "1918";
        StringBuffer buffer = prepareDateStringBuffer(year, month, day, null);
        fixture.parseStartDate(buffer, properties);
    }

    @Test
    public void createForecastDataUrl() {
        String prefix = "http://a.com/sd=";
        String startData = "2010031400";
        info.setX(9);
        info.setY(7);
        String expected = prefix + startData
                + "&row=" + info.getY() + "&col=" + info.getX() + "&lang=pl";
        properties.setProperty(AbstractForecastDataDownloader.IMAGE_URL_PREFIX_KEY, prefix);
        String actual = fixture.createForecastDataUrl(startData, properties);
        assertThat(actual, is(not(nullValue())));
        assertThat(actual, equalTo(expected));
    }

    @Test(expected = NullPointerException.class)
    public void createForecastDataUrlStartDataNull() {
        String prefix = "http://a.com/sd=";
        info.setX(9);
        info.setY(7);
        properties.setProperty(AbstractForecastDataDownloader.IMAGE_URL_PREFIX_KEY, prefix);
        String actual = fixture.createForecastDataUrl(null, properties);
    }

    @Test(expected = NullPointerException.class)
    public void createForecastDataUrlPropertiesNull() {
        String startData = "2010031400";
        info.setX(9);
        info.setY(7);
        String actual = fixture.createForecastDataUrl(startData, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createForecastDataUrlStartDataInvalid() {
        String prefix = "hxxp://a.com/sd=";
        String startData = "2010031400";
        info.setX(9);
        info.setY(7);
        properties.setProperty(AbstractForecastDataDownloader.IMAGE_URL_PREFIX_KEY, prefix);
        String actual = fixture.createForecastDataUrl(startData, properties);
    }

    @Test(expected = NullPointerException.class)
    public void createForecastDataUrlNoPrefixKey() {
        String startData = "2010031400";
        info.setX(9);
        info.setY(7);
        properties.remove(AbstractForecastDataDownloader.IMAGE_URL_PREFIX_KEY);
        String actual = fixture.createForecastDataUrl(startData, properties);
    }

    private StringBuffer prepareDateStringBuffer(String year, String month, String day, String hour) {
        StringBuffer buffer = new StringBuffer();
        if (year != null) {
            buffer.append(properties.getProperty(AbstractForecastDataDownloader.PARSE_YEAR_KEY));
            buffer.append(year);
        }
        if (month != null) {
            buffer.append(properties.getProperty(AbstractForecastDataDownloader.PARSE_MONTH_KEY));
            buffer.append(month);
        }
        if (day != null) {
            buffer.append(properties.getProperty(AbstractForecastDataDownloader.PARSE_DAY_KEY));
            buffer.append(day);
        }
        if (hour != null) {
            buffer.append(properties.getProperty(AbstractForecastDataDownloader.PARSE_HOUR_KEY));
            buffer.append(hour);
        }
        return buffer;
    }

    @Test
    public void addListenerInProgress() {
        Whitebox.setInternalState(fixture, PROGRESS, 2);
        fixture.addListener(listener);
        assertThat(listener.status.getProgress(), equalTo(2));
    }

    @Test
    public void fireStatusUpdate() {
        Status status = Status.CANCELLED;
        fixture.fireStatusUpdate(status);
        assertThat(fixture.lastStatus, equalTo(status));
    }

    @Test
    public void getSetStartDateDownloader() {
        fixture = new AbstractForecastDataDownloader() {
        };
        fixture.setStartDateDownloader(startDateDownloader);
        StartDateDownloader actual = Whitebox.getInternalState(fixture, START_DATE_DOWNLOADER);
        assertThat(actual, equalTo(startDateDownloader));
        assertThat(startDateDownloader.getListeners().contains(fixture), is(true));
    }

    @Test(expected = NullPointerException.class)
    public void setStartDateDownloaderNull() {
        fixture.setStartDateDownloader(null);
    }

    @Test
    public void getSetModelResultDownloader() {
        fixture = new AbstractForecastDataDownloader() {
        };
        fixture.setModelResultDownloader(modelResultDownloader);
        ModelResultDownloader actual = Whitebox.getInternalState(fixture, MODEL_RESULT_DOWNLOADER);
        assertThat(actual, equalTo(modelResultDownloader));
        assertThat(modelResultDownloader.getListeners().contains(fixture), is(true));
    }

    @Test(expected = NullPointerException.class)
    public void setModelResultDownloaderNull() {
        fixture.setModelResultDownloader(null);
    }

    @Test
    public void getSetMeteorogramInfo() {
        fixture.setMeteorogramInfo(info);
        MeteorogramInfo actual = fixture.getMeteorogramInfo();
        assertThat(actual, equalTo(info));
    }

    @Test(expected = NullPointerException.class)
    public void setMeteorogramInfoNull() {
        fixture.setMeteorogramInfo(null);
    }

    private class DummyStartDateDownloader extends AbstractStatusReporter
            implements StartDateDownloader {

        public String startDate = "2010082000";
        public boolean cancelSuccess = true;
        public boolean cancelled = false;
        public String url = null;

        @Override
        public String downloadStartDate(String url) {
            this.url = url;
            return startDate;
        }

        @Override
        public boolean cancel() {
            this.cancelled = true;
            return cancelSuccess;
        }
    }

    private class DummyModelResultDownloader extends AbstractStatusReporter
            implements ModelResultDownloader {

        public byte[] modelResult = null;
        public boolean cancelSuccess = true;
        public boolean cancelled = false;
        public String url = null;

        @Override
        public byte[] downloadModelResult(String url) {
            this.url = url;
            return modelResult;
        }

        @Override
        public boolean cancel() {
            this.cancelled = true;
            return cancelSuccess;
        }
    }

    private class DummyListener implements StatusListener {

        public StatusReporter source = null;
        public Status status = null;

        @Override
        public void statusUpdate(StatusReporter source, Status status) {
            this.source = source;
            this.status = status;
        }
    }
}
