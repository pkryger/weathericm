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

import com.kenai.weathericm.domain.ForecastData;
import java.util.Date;
import net.sf.microlog.core.config.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.aryEq;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

/**
 * Tests for {@link ComparableForecastData} class.
 * @author Przemek Kryger
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ComparableForecastData.class)
public class ComparableForecastDataTest {

    @BeforeClass
    public static void setUpClass() {
        PropertyConfigurator.configure("/testMicrolog.properties");
    }
    private ComparableForecastData fixture;
    private ForecastData decoratedMock;
    private Date decoratedModelStart;
    private final static String DECORATED = "decorated";
    private long offset;

    @Before
    public void setUp() {
        decoratedMock = createMock(ForecastData.class);
        decoratedModelStart = new Date();
        offset = ComparableForecastData.getOffset();
    }
    
    @After
    public void tearDown() {
        ComparableForecastData.setOffset(offset);
    }

    @Test(expected = NullPointerException.class)
    public void createWithNullOther() {
        fixture = new ComparableForecastData(null);
    }

    @Test(expected = NullPointerException.class)
    public void createWithOtherNullDate() {
        expect(decoratedMock.getModelStart()).andReturn((Date) null);
        replayAll();
        fixture = new ComparableForecastData(decoratedMock);
        verifyAll();
    }

    @Test
    public void create() {
        expect(decoratedMock.getModelStart()).andReturn(decoratedModelStart);
        replayAll();
        fixture = new ComparableForecastData(decoratedMock);
        ForecastData other = Whitebox.getInternalState(fixture, DECORATED);
        assertThat(other, is(decoratedMock));
        verifyAll();
    }

    @Test
    public void decorate() {
        expect(decoratedMock.getModelStart()).andReturn(decoratedModelStart);
        byte[] data = new byte[]{0, 1, 2};
        expect(decoratedMock.getModelResult()).andReturn(data);
        decoratedMock.setModelResult(aryEq(data));
        Date date = new Date();
        expect(decoratedMock.getModelStart()).andReturn(date);
        replayAll();
        fixture = new ComparableForecastData(decoratedMock);
        byte[] actualData = fixture.getModelResult();
        assertThat(actualData, equalTo(data));
        fixture.setModelResult(data);
        Date actualDate = fixture.getModelStart();
        assertThat(actualDate, equalTo(date));
        verifyAll();
    }

    @Test
    public void getSetOffset() {
        long defaultOffset = 7 * 3600 * 1000;
        long actual = ComparableForecastData.getOffset();
        assertThat(actual, equalTo(defaultOffset));
        long expected = 3;
        ComparableForecastData.setOffset(expected);
        actual = ComparableForecastData.getOffset();
        assertThat(actual, equalTo(expected));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setOffsetInvalid() {
        ComparableForecastData.setOffset(-1L);
    }
    
    @Test
    public void isSameAsOtherIsSame() {
        long now = System.currentTimeMillis();
        Date decoratedDate = new Date(now);
        Date otherDate = new Date(now);
        expect(decoratedMock.getModelStart()).andReturn(decoratedDate).times(2);
        ForecastData otherMock = createMock(ForecastData.class);
        expect(otherMock.getModelStart()).andReturn(otherDate);
        replayAll();
        fixture = new ComparableForecastData(decoratedMock);
        boolean same = fixture.isSameAs(otherMock);
        assertThat(same, is(true));
        verifyAll();
    }

    @Test
    public void isSameAsOtherIsNotSame() {
        long now = System.currentTimeMillis();
        Date decoratedDate = new Date(now);
        Date otherDate = new Date(now + 1000);
        expect(decoratedMock.getModelStart()).andReturn(decoratedDate).times(2);
        ForecastData otherMock = createMock(ForecastData.class);
        expect(otherMock.getModelStart()).andReturn(otherDate);
        replayAll();
        fixture = new ComparableForecastData(decoratedMock);
        boolean same = fixture.isSameAs(otherMock);
        assertThat(same, is(false));
        verifyAll();
    }

    @Test
    public void isOlderThanOtherIsNewer() {
        long now = System.currentTimeMillis();
        Date decoratedDate = new Date(now);
        Date otherDate = new Date(now + ComparableForecastData.getOffset() + 1);
        expect(decoratedMock.getModelStart()).andReturn(decoratedDate).times(2);
        ForecastData otherMock = createMock(ForecastData.class);
        expect(otherMock.getModelStart()).andReturn(otherDate);
        replayAll();
        fixture = new ComparableForecastData(decoratedMock);
        boolean older = fixture.isOlderThan(otherMock);
        assertThat(older, is(true));
        verifyAll();
    }

    @Test
    public void isOlderThanOtherIsNotNewer() {
        long now = System.currentTimeMillis();
        Date decoratedDate = new Date(now);
        Date otherDate = new Date(now + ComparableForecastData.getOffset());
        expect(decoratedMock.getModelStart()).andReturn(decoratedDate).times(2);
        ForecastData otherMock = createMock(ForecastData.class);
        expect(otherMock.getModelStart()).andReturn(otherDate);
        replayAll();
        fixture = new ComparableForecastData(decoratedMock);
        boolean older = fixture.isOlderThan(otherMock);
        assertThat(older, is(false));
        verifyAll();
    }

    @Test
    public void isNewerThanOtherIsOlder() {
        long now = System.currentTimeMillis();
        Date decoratedDate = new Date(now + ComparableForecastData.getOffset() + 1);
        Date otherDate = new Date(now);
        expect(decoratedMock.getModelStart()).andReturn(decoratedDate).times(2);
        ForecastData otherMock = createMock(ForecastData.class);
        expect(otherMock.getModelStart()).andReturn(otherDate);
        replayAll();
        fixture = new ComparableForecastData(decoratedMock);
        boolean newer = fixture.isNewerThan(otherMock);
        assertThat(newer, is(true));
        verifyAll();
    }

    @Test
    public void isNewerThanOtherIsNotOlder() {
        long now = System.currentTimeMillis();
        Date decoratedDate = new Date(now + ComparableForecastData.getOffset());
        Date otherDate = new Date(now);
        expect(decoratedMock.getModelStart()).andReturn(decoratedDate).times(2);
        ForecastData otherMock = createMock(ForecastData.class);
        expect(otherMock.getModelStart()).andReturn(otherDate);
        replayAll();
        fixture = new ComparableForecastData(decoratedMock);
        boolean newer = fixture.isNewerThan(otherMock);
        assertThat(newer, is(false));
        verifyAll();
    }

    @Test
    public void isSameAsOtherIsSameDate() {
        long now = System.currentTimeMillis();
        Date decoratedDate = new Date(now);
        Date otherDate = new Date(now);
        expect(decoratedMock.getModelStart()).andReturn(decoratedDate).times(2);
        replayAll();
        fixture = new ComparableForecastData(decoratedMock);
        boolean same = fixture.isSameAs(otherDate);
        assertThat(same, is(true));
        verifyAll();
    }

    @Test
    public void isSameAsOtherIsNotSameDate() {
        long now = System.currentTimeMillis();
        Date decoratedDate = new Date(now);
        Date otherDate = new Date(now + 1);
        expect(decoratedMock.getModelStart()).andReturn(decoratedDate).times(2);
        replayAll();
        fixture = new ComparableForecastData(decoratedMock);
        boolean same = fixture.isSameAs(otherDate);
        assertThat(same, is(false));
        verifyAll();
    }

    @Test
    public void isOlderThanOtherIsNewerDate() {
        long now = System.currentTimeMillis();
        Date decoratedDate = new Date(now);
        Date otherDate = new Date(now + ComparableForecastData.getOffset() + 1);
        expect(decoratedMock.getModelStart()).andReturn(decoratedDate).times(2);
        replayAll();
        fixture = new ComparableForecastData(decoratedMock);
        boolean older = fixture.isOlderThan(otherDate);
        assertThat(older, is(true));
        verifyAll();
    }

    @Test
    public void isOlderThanOtherIsNotNewerDate() {
        long now = System.currentTimeMillis();
        Date decoratedDate = new Date(now);
        Date otherDate = new Date(now + ComparableForecastData.getOffset());
        expect(decoratedMock.getModelStart()).andReturn(decoratedDate).times(2);
        replayAll();
        fixture = new ComparableForecastData(decoratedMock);
        boolean older = fixture.isOlderThan(otherDate);
        assertThat(older, is(false));
        verifyAll();
    }

    @Test
    public void isNewerThanOtherIsOlderDate() {
        long now = System.currentTimeMillis();
        Date decoratedDate = new Date(now + ComparableForecastData.getOffset() + 1);
        Date otherDate = new Date(now);
        expect(decoratedMock.getModelStart()).andReturn(decoratedDate).times(2);
        replayAll();
        fixture = new ComparableForecastData(decoratedMock);
        boolean newer = fixture.isNewerThan(otherDate);
        assertThat(newer, is(true));
        verifyAll();
    }

    @Test
    public void isNewerThanOtherIsNotOlderDate() {
        long now = System.currentTimeMillis();
        Date decoratedDate = new Date(now + ComparableForecastData.getOffset());
        Date otherDate = new Date(now);
        expect(decoratedMock.getModelStart()).andReturn(decoratedDate).times(2);
        replayAll();
        fixture = new ComparableForecastData(decoratedMock);
        boolean newer = fixture.isNewerThan(otherDate);
        assertThat(newer, is(false));
        verifyAll();
    }
}
