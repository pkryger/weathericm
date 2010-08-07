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

import com.kenai.weathericm.domain.MeteorogramInfo;
import com.kenai.weathericm.domain.MeteorogramType;
import net.sf.microlog.core.config.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for {@link MeteorogramInfoRecordStoreSerializer}
 * @author Przemek Kryger
 */
public class MeteorogramInfoRecordStoreSerializerTest {

    MeteorogramInfoRecordStoreSerializer fixture;

    @BeforeClass
    public static void setUpClass() {
        PropertyConfigurator.configure("/testMicrolog.properties");
    }

    @Before
    public void setUp() {
        fixture = new MeteorogramInfoRecordStoreSerializer();
    }

    @Test
    public void resurectValid() {
        String name = "a";
        int x = 1;
        int y = 2;
        MeteorogramType type = MeteorogramType.COAMPS;
        byte[] data = new byte[]{
            0, (byte) name.length(), name.getBytes()[0],
            0, 0, 0, (byte) x,
            0, 0, 0, (byte) y,
            0, 0, 0, (byte) type.getValue(),};
        int id = 30;
        MeteorogramInfo resurected = fixture.resurect(id, data);
        assertThat(resurected, is(not(nullValue())));
        assertThat(resurected.getId(), equalTo(id));
        assertThat(resurected.getName(), equalTo(name));
        assertThat(resurected.getX(), equalTo(x));
        assertThat(resurected.getY(), equalTo(y));
        assertThat(resurected.getType(), equalTo(type));
        assertThat(resurected.isTainted(), is(false));
    }

    @Test
    public void resurectInvalid() {
        byte[] data = new byte[]{8};
        int id = 20;
        MeteorogramInfo resurected = fixture.resurect(id, data);
        assertThat(resurected, is(not(nullValue())));
        assertThat(resurected.getId(), equalTo(id));
        assertThat(resurected.isTainted(), is(true));
    }

    @Test
    public void serialize() {
        String name = "a";
        int x = 1;
        int y = 2;
        MeteorogramType type = MeteorogramType.COAMPS;
        byte[] expected = new byte[]{
            0, (byte) name.length(), name.getBytes()[0],
            0, 0, 0, (byte) x,
            0, 0, 0, (byte) y,
            0, 0, 0, (byte) type.getValue(),};
        MeteorogramInfo info = new MeteorogramInfo();
        info.setName(name);
        info.setX(x);
        info.setY(y);
        info.setType(type);
        byte[] serialized = fixture.serialize(info);
        assertThat(serialized, equalTo(expected));
    }
}
