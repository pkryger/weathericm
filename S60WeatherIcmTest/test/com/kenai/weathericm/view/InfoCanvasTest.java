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
package com.kenai.weathericm.view;

import com.kenai.weathericm.domain.ForecastData;
import com.kenai.weathericm.domain.MeteorogramInfo;
import javax.microedition.lcdui.Image;
import net.sf.microlog.core.config.PropertyConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.aryEq;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.createPartialMock;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;
import static org.powermock.api.easymock.PowerMock.mockStatic;

/**
 * Tests for {@link InfoCanvas} class.
 * @author Przemek Kryger
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({InfoCanvas.class, Image.class})
@SuppressStaticInitializationFor({"javax.microedition.lcdui.Displayable",
    "javax.microedition.lcdui.Canvas",
    "javax.microedition.lcdui.Graphics",
    "javax.microedition.lcdui.Image"})
public class InfoCanvasTest {

    @BeforeClass
    public static void setUpClass() {
        PropertyConfigurator.configure("/testMicrolog.properties");
    }
    private InfoCanvas fixture;
    private Image imageMock;
    private MeteorogramInfo info;

    @Before
    public void setUp() {
        fixture = createPartialMock(InfoCanvas.class,
                "setImage", "setTitle");
        imageMock = createMock(Image.class);
        info = new MeteorogramInfo();
    }

    @Test
    public void setInfoWithData() {
        String title = "df";
        byte[] modelResult = new byte[]{1, 2, 3};
        ForecastData data = new ForecastData(2010, 8, 10, 0);
        data.setModelResult(modelResult);
        info.setForecastData(data);
        info.setName(title);
        mockStatic(Image.class);
        expect(Image.createImage(aryEq(modelResult), eq(0), eq(modelResult.length))).andReturn(imageMock);
        fixture.setImage(imageMock);
        fixture.setTitle(title);
        replayAll();
        fixture.setInfo(info);
        verifyAll();
    }

    @Test
    public void setInfoWithBrokenData() {
        String title = "xy";
        info.setName(title);
        ForecastData data = new ForecastData(2010, 8, 10, 0);
        info.setForecastData(data);
        fixture.setTitle(title);
        fixture.setImage(null);
        replayAll();
        fixture.setInfo(info);
        verifyAll();
    }

    @Test
    public void setInfoWithoutData() {
        String title = "xy";
        info.setName(title);
        fixture.setTitle(title);
        fixture.setImage(null);
        replayAll();
        fixture.setInfo(info);
        verifyAll();
    }

    @Test(expected = NullPointerException.class)
    public void setInfoNull() {
        replayAll();
        fixture.setInfo(null);
        verifyAll();
    }
}
