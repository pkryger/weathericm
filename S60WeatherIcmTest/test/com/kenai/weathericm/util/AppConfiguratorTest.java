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

import com.kenai.weathericm.app.MeteorogramBroker;
import com.kenai.weathericm.repository.MeteorogramInfoDao;
import com.kenai.weathericm.repository.MeteorogramInfoRecordStoreDao;
import com.kenai.weathericm.repository.MeteorogramInfoRecordStoreSerializer;
import com.kenai.weathericm.repository.MeteorogramInfoSerializer;
import net.sf.microlog.core.config.PropertyConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.powermock.api.easymock.PowerMock.suppress;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for {@link AppConfigurator}.
 * @author Przemek Kryger
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(PropertyConfigurator.class)
public class AppConfiguratorTest {

    @BeforeClass
    public static void setUpClass() {
        PropertyConfigurator.configure("/testMicrolog.properties");
    }

    @Before
    public void setUp() throws Exception {
        suppress(PropertyConfigurator.class.getDeclaredMethod("configure"));
        replay(PropertyConfigurator.class);
    }

    @Test
    public void configure() {
        AppConfigurator.configure();
        replayAll();
        MeteorogramBroker broker = MeteorogramBroker.getInstance();
        assertThat(broker, is(not(nullValue())));
        MeteorogramInfoDao dao = broker.getDao();
        assertThat(dao, is(not(nullValue())));
        assertThat(dao, is((MeteorogramInfoDao)MeteorogramInfoRecordStoreDao.getInstance()));
        MeteorogramInfoSerializer serializer = dao.getMeteorogramInfoSerializer();
        assertThat(serializer, is(not(nullValue())));
        assertThat(serializer, instanceOf(MeteorogramInfoRecordStoreSerializer.class));
        verifyAll();
    }
}
