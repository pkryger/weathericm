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

import net.sf.microlog.core.config.PropertyConfigurator;
import org.junit.BeforeClass;
import java.util.Enumeration;
import java.util.Vector;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests for {@link MeteorogramType}
 * @author Przemek Kryger
 */
public class MeteorogramTypeTest {

    @BeforeClass
    public static void setUpClass() {
        PropertyConfigurator.configure("/testMicrolog.properties");
    }

    @Test
    public void um() {
        assertThat(MeteorogramType.UM.getValue(), equalTo(0));
        assertThat(MeteorogramType.UM.getName(), equalTo("UM"));
        assertThat(MeteorogramType.UM.toString(), equalTo("UM"));
    }

    @Test
    public void coamps() {
        assertThat(MeteorogramType.COAMPS.getValue(), equalTo(1));
        assertThat(MeteorogramType.COAMPS.getName(), equalTo("COAMPS"));
        assertThat(MeteorogramType.COAMPS.toString(), equalTo("COAMPS"));
    }

    @Test
    public void getAllTypes() {
        Vector elements = MeteorogramType.getAllTypes();
        assertThat((MeteorogramType) elements.get(0), equalTo(MeteorogramType.UM));
        assertThat((MeteorogramType) elements.get(1), equalTo(MeteorogramType.COAMPS));
        assertThat(elements.size(), is(2));
    }

    @Test
    public void getByValueValid() {
        Vector all = MeteorogramType.getAllTypes();
        Enumeration e = all.elements();
        while (e.hasMoreElements()) {
            MeteorogramType expected = (MeteorogramType) e.nextElement();
            MeteorogramType actual = MeteorogramType.getByValue(expected.getValue());
            assertThat(actual, equalTo(expected));
        }
    }

    @Test
    public void getByValueInvalid() {
        MeteorogramType meteorogramType = MeteorogramType.getByValue(-1);
        assertThat(meteorogramType, is(nullValue()));
    }
}
