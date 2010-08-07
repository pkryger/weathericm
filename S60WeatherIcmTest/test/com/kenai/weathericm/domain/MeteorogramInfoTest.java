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
import java.util.Hashtable;
import java.util.TimeZone;
import net.sf.microlog.core.config.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests for {@link MeteorogramInfo}
 * @author Przemek Kryger
 */
public class MeteorogramInfoTest {

    MeteorogramInfo fixture;

    @BeforeClass
    public static void setUpClass() {
        PropertyConfigurator.configure("/testMicrolog.properties");
    }

    @Before
    public void setUp() {
        fixture = new MeteorogramInfo();
    }

    @Test
    public void getSetFirstValidId() {
        assertThat(fixture.getId(), is(nullValue()));
        Integer expected = new Integer(1000);
        fixture.setId(expected);
        assertThat(fixture.getId(), equalTo(expected));
        assertThat(fixture.isTainted(), is(false));
    }

    @Test
    public void setNullId() {
        fixture.setId(33);
        fixture.setId(null);
        assertThat(fixture.isTainted(), is(false));
        assertThat(fixture.getId(), is(nullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setChangeId() {
        fixture.setId(1);
        fixture.setId(2);
    }

    @Test
    public void getSetValidName() {
        assertThat(fixture.getName(), equalTo(""));
        String expected = "Kraków";
        fixture.setData(new ForecastData("2010050607"));
        fixture.setName(expected);
        assertThat(fixture.getName(), equalTo(expected));
        assertThat(fixture.isTainted(), is(true));
        assertThat(fixture.getData(), is(notNullValue()));
        fixture.setTainted(false);
        fixture.setName(expected);
        assertThat(fixture.isTainted(), is(false));
        assertThat(fixture.getData(), is(notNullValue()));
    }

    @Test(expected = NullPointerException.class)
    public void setNullName() {
        fixture.setName(null);
    }

    @Test
    public void getSetValidX() {
        assertThat(fixture.getX(), equalTo(0));
        int expected = MeteorogramInfo.MAX_X / 2;
        fixture.setData(new ForecastData("2010050607"));
        fixture.setX(expected);
        assertThat(fixture.getX(), equalTo(expected));
        assertThat(fixture.isTainted(), is(true));
        assertThat(fixture.getData(), is(nullValue()));
        fixture.setTainted(false);
        fixture.setData(new ForecastData("2010050607"));
        fixture.setX(expected);
        assertThat(fixture.isTainted(), is(false));
        assertThat(fixture.getData(), is(notNullValue()));
    }

    @Test
    public void getSetMaxX() {
        fixture.setX(MeteorogramInfo.MAX_X);
        assertThat(fixture.getX(), equalTo(MeteorogramInfo.MAX_X));
        assertThat(fixture.isTainted(), is(true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setNegativeX() {
        fixture.setX(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setOverMaxX() {
        fixture.setX(MeteorogramInfo.MAX_X + 1);
    }

    @Test
    public void getSetValidY() {
        assertThat(fixture.getY(), equalTo(0));
        int expected = MeteorogramInfo.MAX_Y / 2;
        fixture.setData(new ForecastData("2010050607"));
        fixture.setY(expected);
        assertThat(fixture.getY(), equalTo(expected));
        assertThat(fixture.isTainted(), is(true));
        assertThat(fixture.getData(), is(nullValue()));
        fixture.setTainted(false);
        fixture.setData(new ForecastData("2010050607"));
        fixture.setY(expected);
        assertThat(fixture.isTainted(), is(false));
        assertThat(fixture.getData(), is(notNullValue()));
    }

    @Test
    public void getSetMaxY() {
        fixture.setY(MeteorogramInfo.MAX_Y);
        assertThat(fixture.getY(), equalTo(MeteorogramInfo.MAX_Y));
        assertThat(fixture.isTainted(), is(true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setNegativeY() {
        fixture.setY(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setOverMaxY() {
        fixture.setY(MeteorogramInfo.MAX_Y + 1);
    }

    @Test
    public void getSetValidType() {
        assertThat(fixture.getType(), equalTo(MeteorogramType.UM));
        fixture.setData(new ForecastData("2010050607"));
        fixture.setType(MeteorogramType.COAMPS);
        assertThat(fixture.getType(), equalTo(MeteorogramType.COAMPS));
        assertThat(fixture.isTainted(), is(true));
        assertThat(fixture.getData(), is(nullValue()));
    }

    @Test(expected = NullPointerException.class)
    public void setNullType() {
        fixture.setType(null);
    }

    private static class MyType extends MeteorogramType {

        private MyType() {
            super(1, "");
        }
        public final static MeteorogramType MY_VALUE = new MeteorogramType(3, "MY_VALUE");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setInvalidType() {
        fixture.setType(MyType.MY_VALUE);
    }

    @Test
    public void getSetTainted() {
        assertThat(fixture.isTainted(), is(false));
        fixture.setTainted(true);
        assertThat(fixture.isTainted(), is(true));
    }

    @Test
    public void getSetData() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        ForecastData data = new ForecastData(cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.HOUR_OF_DAY));
        assertThat(fixture.isDataAvaliable(), is(false));
        fixture.setData(data);
        assertThat(fixture.isTainted(), is(false));
        ForecastData actual = fixture.getData();
        assertThat(actual, is(not(nullValue())));
        assertThat(actual, equalTo(data));
        assertThat(fixture.isDataAvaliable(), is(true));
    }

    @Test
    public void hashtable() {
        Hashtable hash = new Hashtable();
        Object value = new Object();
        hash.put(fixture, value);
        assertThat(hash.size(), equalTo(1));
        assertThat(hash.containsKey(fixture), is(true));
        Object actual = hash.get(fixture);
        assertThat(actual, is(value));
        actual = hash.remove(fixture);
        assertThat(hash.size(), equalTo(0));
        assertThat(hash.containsKey(fixture), is(false));
        assertThat(actual, is(value));
        hash.put(fixture, value);
        hash.put(fixture, new Object());
        assertThat(hash.containsKey(fixture), is(true));
        actual = hash.get(fixture);
        assertThat(actual, is(not(value)));
    }
}
