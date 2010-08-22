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
package com.kenai.weathericm.domain;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import net.sf.microlog.core.config.PropertyConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for {@link ForecastData} class.
 * @author Przemek Kryger
 */
@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("javax.microedition.lcdui.Image")
public class ForecastDataTest {

    private ForecastData fixture;
    private Calendar calendar;
    private int year;
    private String sYear;
    private int month;
    private String sMonth;
    private int day;
    private String sDay;
    private int hour;
    private String sHour;

    @BeforeClass
    public static void setUpClass() {
        PropertyConfigurator.configure("/testMicrolog.properties");
    }

    @Before
    public void setUp() {
        calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        year = calendar.get(Calendar.YEAR);
        sYear = Integer.toString(year);
        month = calendar.get(Calendar.MONTH) + 1;
        sMonth = month > 9 ? Integer.toString(month) : "0" + Integer.toString(month);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        sDay = day > 9 ? Integer.toString(day) : "0" + Integer.toString(day);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        sHour = hour > 9 ? Integer.toString(hour) : "0" + Integer.toString(hour);
    }

    @Test
    public void createString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(sYear).append(sMonth).append(sDay).append(sHour);
        fixture = new ForecastData(buffer.toString());
        Date actual = fixture.getModelStart();
        assertThat(actual, equalTo(calendar.getTime()));
    }

    @Test(expected = NumberFormatException.class)
    public void createStringYyyyNaN() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("yyyy").append(sMonth).append(sDay).append(sHour);
        fixture = new ForecastData(buffer.toString());
    }

    @Test(expected = NumberFormatException.class)
    public void createStringMmNaN() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(sYear).append("mm").append(sDay).append(sHour);
        fixture = new ForecastData(buffer.toString());
        Date actual = fixture.getModelStart();
        assertThat(actual, equalTo(calendar.getTime()));
    }

    @Test(expected = NumberFormatException.class)
    public void createStringDdNaN() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(sYear).append(sMonth).append("dd").append(sHour);
        fixture = new ForecastData(buffer.toString());
        Date actual = fixture.getModelStart();
        assertThat(actual, equalTo(calendar.getTime()));
    }

    @Test(expected = NumberFormatException.class)
    public void createStringHhNaN() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(sYear).append(sMonth).append(sDay).append("hh");
        fixture = new ForecastData(buffer.toString());
        Date actual = fixture.getModelStart();
        assertThat(actual, equalTo(calendar.getTime()));
    }

    @Test
    public void createInt() {
        fixture = new ForecastData(year, month, day, hour);
        Date actual = fixture.getModelStart();
        assertThat(actual, equalTo(calendar.getTime()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createIntYearOutOfRangeToSmall() {
        fixture = new ForecastData(999, month, day, hour);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createIntYearOutOfRangeToBig() {
        fixture = new ForecastData(10000, month, day, hour);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createIntMonthOutOfRangeToSmall() {
        fixture = new ForecastData(year, 0, day, hour);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createIntMonthOutOfRangeToBig() {
        fixture = new ForecastData(year, 13, day, hour);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createIntDayOutOfRangeToSmall() {
        fixture = new ForecastData(year, month, 0, hour);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createIntDayOutOfRangeToBig() {
        fixture = new ForecastData(year, month, 32, hour);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createIntHourOutOfRangeToSmall() {
        fixture = new ForecastData(year, month, day, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createIntHourOutOfRangeToBig() {
        fixture = new ForecastData(year, month, day, 24);
    }

    @Test
    public void getSetModelResult() {
        byte[] data = new byte[] {1, 2, 3};
        fixture = new ForecastData(year, month, day, hour);
        fixture.setModelResult(data);
        byte[] actual = fixture.getModelResult();
        assertThat(actual, equalTo(data));
    }
}
