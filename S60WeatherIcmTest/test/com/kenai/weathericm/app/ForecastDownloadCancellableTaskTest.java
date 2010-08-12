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
import com.kenai.weathericm.util.Properties;
import com.kenai.weathericm.util.Status;
import com.kenai.weathericm.util.StatusListener;
import com.kenai.weathericm.util.StatusReporter;
import org.junit.Before;
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
 * Tests for {@link ForecastDownloadCancellableTask}
 * @author Przemek Kryger
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ForecastDownloadCancellableTask.class)
public class ForecastDownloadCancellableTaskTest {

    private MeteorogramInfo info;
    private ForecastDownloadCancellableTask fixture;
    private DummyListener listener;
    private Thread threadMock;
    private Properties properties;
    private final static String PROGRESS = "progress";
    private final static String MY_THREAD = "myThread";
    private final static String INFO = "info";
    private final static String CANCELLED = "cancelled";

    @Before
    public void setUp() {
        info = new MeteorogramInfo();
        fixture = new ForecastDownloadCancellableTask(info);
        listener = new DummyListener();
        threadMock = createMock(Thread.class);
        properties = new Properties();
        properties.setProperty(ForecastDownloadCancellableTask.IMAGE_URL_PREFIX_KEY, "img.url");
        properties.setProperty(ForecastDownloadCancellableTask.PARSE_DAY_KEY, "day.key");
        properties.setProperty(ForecastDownloadCancellableTask.PARSE_HOUR_KEY, "hour.key");
        properties.setProperty(ForecastDownloadCancellableTask.PARSE_MONTH_KEY, "month.key");
        properties.setProperty(ForecastDownloadCancellableTask.PARSE_YEAR_KEY, "year.key");
        mockStatic(Thread.class);
    }

    @Test(expected = NullPointerException.class)
    public void createWithNull() {
        fixture = new ForecastDownloadCancellableTask(null);
    }

    @Test
    public void cancelSuccess() {
        int progress = 5;
        Whitebox.setInternalState(fixture, MY_THREAD, threadMock);
        Whitebox.setInternalState(fixture, PROGRESS, progress);
        threadMock.interrupt();
        replayAll();
        boolean cancelled = fixture.cancel();
        boolean failed = fixture.hasFailed();
        assertThat(cancelled, is(true));
        assertThat(failed, is(true));
        Boolean internalCancelled = Whitebox.getInternalState(fixture, CANCELLED);
        assertThat(internalCancelled, equalTo(Boolean.TRUE));
        verifyAll();
    }

    @Test
    public void cancelFail() {
        int progress = -1;
        Whitebox.setInternalState(fixture, MY_THREAD, (String) null);
        Whitebox.setInternalState(fixture, PROGRESS, progress);
        replayAll();
        boolean cancelled = fixture.cancel();
        boolean failed = fixture.hasFailed();
        assertThat(cancelled, is(false));
        assertThat(failed, is(true));
        Boolean internalCancelled = Whitebox.getInternalState(fixture, CANCELLED);
        assertThat(internalCancelled, equalTo(Boolean.FALSE));
        verifyAll();
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
        assertThat(actual.getProperty(ForecastDownloadCancellableTask.IMAGE_URL_PREFIX_KEY),
                is(not(nullValue())));
        assertThat(actual.getProperty(ForecastDownloadCancellableTask.PARSE_DAY_KEY),
                is(not(nullValue())));
        assertThat(actual.getProperty(ForecastDownloadCancellableTask.PARSE_MONTH_KEY),
                is(not(nullValue())));
        assertThat(actual.getProperty(ForecastDownloadCancellableTask.PARSE_HOUR_KEY),
                is(not(nullValue())));
        assertThat(actual.getProperty(ForecastDownloadCancellableTask.PARSE_YEAR_KEY),
                is(not(nullValue())));
    }

    @Test
    public void loadTypePropertiesCoamps() {
        info.setType(MeteorogramType.COAMPS);
        Properties actual = fixture.loadTypeProperties();
        assertThat(actual, is(not(nullValue())));
        assertThat(actual.getProperty(ForecastDownloadCancellableTask.IMAGE_URL_PREFIX_KEY),
                is(not(nullValue())));
        assertThat(actual.getProperty(ForecastDownloadCancellableTask.PARSE_DAY_KEY),
                is(not(nullValue())));
        assertThat(actual.getProperty(ForecastDownloadCancellableTask.PARSE_MONTH_KEY),
                is(not(nullValue())));
        assertThat(actual.getProperty(ForecastDownloadCancellableTask.PARSE_HOUR_KEY),
                is(not(nullValue())));
        assertThat(actual.getProperty(ForecastDownloadCancellableTask.PARSE_YEAR_KEY),
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
        properties.remove(ForecastDownloadCancellableTask.PARSE_YEAR_KEY);
        String day = "12";
        String month = "13";
        String year = "1999";
        String hour = "18";
        StringBuffer buffer = prepareDateStringBuffer(year, month, day, hour);
        fixture.parseStartDate(buffer, properties);
    }

    @Test(expected = NullPointerException.class)
    public void parseStartDateNoMonthKey() {
        properties.remove(ForecastDownloadCancellableTask.PARSE_MONTH_KEY);
        String day = "12";
        String month = "13";
        String year = "1999";
        String hour = "18";
        StringBuffer buffer = prepareDateStringBuffer(year, month, day, hour);
        fixture.parseStartDate(buffer, properties);
    }

    @Test(expected = NullPointerException.class)
    public void parseStartDateNoDayKey() {
        properties.remove(ForecastDownloadCancellableTask.PARSE_DAY_KEY);
        String day = "12";
        String month = "13";
        String year = "1999";
        String hour = "18";
        StringBuffer buffer = prepareDateStringBuffer(year, month, day, hour);
        fixture.parseStartDate(buffer, properties);
    }

    @Test(expected = NullPointerException.class)
    public void parseStartDateNoHourKey() {
        properties.remove(ForecastDownloadCancellableTask.PARSE_HOUR_KEY);
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
        properties.setProperty(ForecastDownloadCancellableTask.IMAGE_URL_PREFIX_KEY, prefix);
        String actual = fixture.createForecastDataUrl(startData, properties);
        assertThat(actual, is(not(nullValue())));
        assertThat(actual, equalTo(expected));
    }

    @Test(expected = NullPointerException.class)
    public void createForecastDataUrlStartDataNull() {
        String prefix = "http://a.com/sd=";
        info.setX(9);
        info.setY(7);
        properties.setProperty(ForecastDownloadCancellableTask.IMAGE_URL_PREFIX_KEY, prefix);
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
        properties.setProperty(ForecastDownloadCancellableTask.IMAGE_URL_PREFIX_KEY, prefix);
        String actual = fixture.createForecastDataUrl(startData, properties);
    }

    @Test(expected = NullPointerException.class)
    public void createForecastDataUrlNoPrefixKey() {
        String startData = "2010031400";
        info.setX(9);
        info.setY(7);
        properties.remove(ForecastDownloadCancellableTask.IMAGE_URL_PREFIX_KEY);
        String actual = fixture.createForecastDataUrl(startData, properties);
    }

    private StringBuffer prepareDateStringBuffer(String year, String month, String day, String hour) {
        StringBuffer buffer = new StringBuffer();
        if (year != null) {
            buffer.append(properties.getProperty(ForecastDownloadCancellableTask.PARSE_YEAR_KEY));
            buffer.append(year);
        }
        if (month != null) {
            buffer.append(properties.getProperty(ForecastDownloadCancellableTask.PARSE_MONTH_KEY));
            buffer.append(month);
        }
        if (day != null) {
            buffer.append(properties.getProperty(ForecastDownloadCancellableTask.PARSE_DAY_KEY));
            buffer.append(day);
        }
        if (hour != null) {
            buffer.append(properties.getProperty(ForecastDownloadCancellableTask.PARSE_HOUR_KEY));
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

    private class DummyListener implements StatusListener {

        public StatusReporter source = null;
        public Status status = null;

        public void statusUpdate(StatusReporter source, Status status) {
            this.source = source;
            this.status = status;
        }
    }
}
