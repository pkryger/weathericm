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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests for {@link ForecastDataRecordStoreSerializer} class.
 * @author Przemek Kryger
 */
public class ForecastDataRecordStoreSerializerTest {

    ForecastDataRecordStoreSerializer fixture = null;

    @Before
    public void setUp() {
        fixture = new ForecastDataRecordStoreSerializer();
    }

    @Test(expected = NullPointerException.class)
    public void serializeNullForecastData() {
        fixture.serialize(null);
    }

    @Test(expected = NullPointerException.class)
    public void serializeNullModelResult() {
        ForecastData forecastData = new ForecastData("2010102000");
        fixture.serialize(forecastData);
    }

    @Test(expected = NullPointerException.class)
    public void serializeNullModelStart() {
        ForecastData forecastData = new ForecastData("2010102001");
        Whitebox.setInternalState(forecastData, "modelStart", (Date)null);
        forecastData.setModelResult(new byte[]{});
        fixture.serialize(forecastData);
    }

    @Test
    public void serialize() {
        byte[] modelResult = new byte[] {1, 2, 3};
        int year = 2010;
        int month = 10;
        int day = 20;
        int hour = 02;
        ForecastData forecastData = new ForecastData(year, month, day, hour);
        forecastData.setModelResult(modelResult);
        byte[] actualData = fixture.serialize(forecastData);
        assertThat(actualData, is(notNullValue()));
        int actualYear = bytesToInt(actualData, 0);
        assertThat(actualYear, equalTo(year));
        int actualMonth = bytesToInt(actualData, 4);
        assertThat(actualMonth, equalTo(month - 1));
        int actualDay = bytesToInt(actualData, 8);
        assertThat(actualDay, equalTo(day));
        int actualHour = bytesToInt(actualData, 12);
        assertThat(actualHour, equalTo(hour));
        int actualLength = bytesToInt(actualData, 16);
        assertThat(actualLength, equalTo(modelResult.length));
        byte[] actualModelResult = Arrays.copyOfRange(actualData, 20, 22);
        assertThat(actualModelResult, equalTo(modelResult));
    }

    private int bytesToInt(byte[] bytes, int offset) {
        int retValue = 0;
        for (int i = 0; i < 4; i++) {
            retValue <<= 8;
            retValue += bytes[offset + i];
        }
        return retValue;
    }

    @Test(expected = NullPointerException.class)
    public void resurectNull() {
        fixture.resurect(null);
    }

    @Test
    public void resurectBadStartDate() {
        byte[] data = new byte[] {
            0, 0, 0, -1,
            0, 0, 0, -1,
            0, 0, 0, -1,
            0, 0, 0, -1,
            0, 0, 0, 4,
            1, 1, 1, 1,
        };
        ForecastData actual = fixture.resurect(data);
        assertThat(actual, is(nullValue()));
    }

    @Test
    public void resurectZeroLength() {
       byte[] data = new byte[] {
            0, 0, 7, -91,
            0, 0, 0, 9,
            0, 0, 0, 20,
            0, 0, 0, 03,
            0, 0, 0, 0,
        };
        ForecastData actual = fixture.resurect(data);
        assertThat(actual, is(nullValue()));
    }

    @Test
    public void resurectNoModelResult() {
       byte[] data = new byte[] {
            0, 0, 7, -91,
            0, 0, 0, 9,
            0, 0, 0, 20,
            0, 0, 0, 04,
            0, 0, 0, 4,
        };
        ForecastData actual = fixture.resurect(data);
        assertThat(actual, is(nullValue()));
    }

    @Test
    public void resurectWrongLength() {
        byte[] data = new byte[] {
            0, 0, 7, -91,
            0, 0, 0, 9,
            0, 0, 0, 20,
            0, 0, 0, 05,
            0, 0, 0, 2,
            1, 1, 1, 1,
        };
        ForecastData actual = fixture.resurect(data);
        assertThat(actual, is(nullValue()));
    }

    @Test
    public void resurect() {
        byte[] data = new byte[] {
            0, 0, 7, -91,
            0, 0, 0, 9,
            0, 0, 0, 20,
            0, 0, 0, 05,
            0, 0, 0, 4,
            1, 1, 1, 1,
        };
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.set(Calendar.YEAR, 2010);
        calendar.set(Calendar.MONTH, 10);
        calendar.set(Calendar.DAY_OF_MONTH, 20);
        calendar.set(Calendar.HOUR_OF_DAY, 4);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date date = calendar.getTime();
        ForecastData actual = fixture.resurect(data);
        assertThat(actual, is(notNullValue()));
        Date actualDate = actual.getModelStart();
        assertThat(actualDate.compareTo(date), equalTo(0));
        byte[] actualModelResult = actual.getModelResult();
        assertThat(actualModelResult, equalTo(Arrays.copyOfRange(data, 20, 23)));
    }
}
