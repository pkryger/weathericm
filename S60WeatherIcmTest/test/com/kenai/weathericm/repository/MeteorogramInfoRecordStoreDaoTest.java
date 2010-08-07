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
import java.util.Vector;
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordComparator;
import javax.microedition.rms.RecordEnumeration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import net.sf.microlog.core.config.PropertyConfigurator;
import org.junit.BeforeClass;
import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.aryEq;
import static org.easymock.EasyMock.eq;

/**
 * Tests for {@link MeteorogramInfoRecordStoreDao}.
 * @author Przemek Kryger
 */
@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("javax.microedition.rms.RecordStore")
@PrepareForTest({RecordStore.class, RecordEnumeration.class})
public class MeteorogramInfoRecordStoreDaoTest {

    private RecordStore recordStoreMock;
    private RecordEnumeration recordEnumerationMock;
    private MeteorogramInfoRecordStoreDao fixture;
    private MeteorogramInfo info;
    private byte[] infoBytes;

    @BeforeClass
    public static void setUpClass() {
        PropertyConfigurator.configure("/testMicrolog.properties");
    }

    @Before
    public void setUp() {
        mockStatic(RecordStore.class);
        recordStoreMock = createMock(RecordStore.class);
        recordEnumerationMock = createMock(RecordEnumeration.class);
        fixture = MeteorogramInfoRecordStoreDao.getInstance();
        fixture.setMeteorogramInfoSerializer(new MeteorogramInfoSerializer() {

            public byte[] serialize(MeteorogramInfo info) {
                return new byte[] {(byte)info.getX(), (byte)info.getY()};
            }

            public MeteorogramInfo resurect(int id, byte[] infoBytes) {
                MeteorogramInfo info = new MeteorogramInfo();
                info.setId(id);
                if (infoBytes != null
                        && infoBytes.length == 2
                        && infoBytes[0] >= 0 && infoBytes[0] <= MeteorogramInfo.MAX_X
                        && infoBytes[1] >= 0 && infoBytes[1] <= MeteorogramInfo.MAX_Y) {
                    info.setX(infoBytes[0]);
                    info.setY(infoBytes[1]);
                    info.setTainted(false);
                } else {
                    info.setTainted(true);
                }
                return info;
            }

        });
        info = new MeteorogramInfo();
        info.setName("Kraków");
        info.setX(10);
        info.setY(20);
        info.setType(MeteorogramType.UM);
        info.setTainted(true);

        infoBytes = fixture.serialize(info);
    }

    @Test
    public void getInstnce() {
        replayAll();
        MeteorogramInfoRecordStoreDao otherFixture =
                MeteorogramInfoRecordStoreDao.getInstance();
        assertThat(otherFixture, equalTo(fixture));
        verifyAll();
    }

    @Test
    public void createNonExistingWithoutId() throws RecordStoreException {
        int expectedId = 23;
        expect(RecordStore.openRecordStore(
                MeteorogramInfoRecordStoreDao.STORE, true)).andReturn(recordStoreMock);
        expect(recordStoreMock.addRecord(
                aryEq(infoBytes), eq(0), eq(infoBytes.length))).andReturn(expectedId);
        recordStoreMock.closeRecordStore();
        replayAll();
        fixture.create(info);
        assertThat(info.isTainted(), is(false));
        assertThat(info.getId(), equalTo(expectedId));
        verifyAll();
    }

    @Test
    public void createNonExistingWithoutIdNoRoom() throws RecordStoreException {
        expect(RecordStore.openRecordStore(
                MeteorogramInfoRecordStoreDao.STORE, true)).andReturn(recordStoreMock);
        expect(recordStoreMock.addRecord(
                aryEq(infoBytes), eq(0), eq(infoBytes.length))).andThrow(new RecordStoreFullException());
        recordStoreMock.closeRecordStore();
        replayAll();
        fixture.create(info);
        assertThat(info.isTainted(), is(true));
        assertThat(info.getId(), is(nullValue()));
        verifyAll();
    }

    @Test(expected = NullPointerException.class)
    public void createNull() {
        replayAll();
        fixture.create(null);
        verifyAll();
    }

    @Test(expected = IllegalArgumentException.class)
    public void createExistingWithId() {
        replayAll();
        info.setId(10);
        fixture.create(info);
        verifyAll();
    }

    @Test
    public void updateValidWithIdTainted() throws RecordStoreException {
        int id = 200;
        info.setId(id);
        expect(RecordStore.openRecordStore(
                MeteorogramInfoRecordStoreDao.STORE, true)).andReturn(recordStoreMock);
        recordStoreMock.setRecord(eq(id), aryEq(infoBytes), eq(0), eq(infoBytes.length));
        recordStoreMock.closeRecordStore();
        replayAll();
        fixture.update(info);
        assertThat(info.isTainted(), is(false));
        verifyAll();
    }

    @Test
    public void updateValidWithIdTaintedNoRoom() throws RecordStoreException {
        int id = 120;
        info.setId(id);
        expect(RecordStore.openRecordStore(
                MeteorogramInfoRecordStoreDao.STORE, true)).andReturn(recordStoreMock);
        recordStoreMock.setRecord(eq(id), aryEq(infoBytes), eq(0), eq(infoBytes.length));
        expectLastCall().andThrow(new RecordStoreFullException());
        recordStoreMock.closeRecordStore();
        replayAll();
        fixture.update(info);
        assertThat(info.isTainted(), is(true));
        verifyAll();
    }

    @Test
    public void updateValidWithIdNotTainted() {
        int id = 145;
        info.setId(id);
        info.setTainted(false);
        replayAll();
        fixture.update(info);
        assertThat(info.isTainted(), is(false));
        verifyAll();
    }

    @Test(expected = NullPointerException.class)
    public void updateNull() {
        replayAll();
        fixture.update(null);
        verifyAll();
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateNotExistingWithoutId() {
        replayAll();
        fixture.update(info);
        fail("IllegalArgumentException expected");
        verifyAll();
    }

    @Test
    public void updateNotExistingNotInStore() throws RecordStoreException {
        int id = 5;
        info.setId(id);
        expect(RecordStore.openRecordStore(
                MeteorogramInfoRecordStoreDao.STORE, true)).andReturn(recordStoreMock);
        recordStoreMock.setRecord(eq(id), aryEq(infoBytes), eq(0), eq(infoBytes.length));
        expectLastCall().andThrow(new InvalidRecordIDException());
        recordStoreMock.closeRecordStore();
        replayAll();
        fixture.update(info);
        assertThat(info.getId(), is(nullValue()));
        assertThat(info.isTainted(), is(true));
        verifyAll();
    }

    @Test
    public void createOrUpdateValidNotExistingWithoutId() throws RecordStoreException {
        int expectedId = 3;
        expect(RecordStore.openRecordStore(
                MeteorogramInfoRecordStoreDao.STORE, true)).andReturn(recordStoreMock);
        expect(recordStoreMock.addRecord(
                aryEq(infoBytes), eq(0), eq(infoBytes.length))).andReturn(expectedId);
        recordStoreMock.closeRecordStore();
        replayAll();
        fixture.createOrUpdate(info);
        assertThat(info.getId(), equalTo(expectedId));
        assertThat(info.isTainted(), is(false));
        verifyAll();
    }

    @Test
    public void createOrUpdateValidExistingWithIdTainted() throws RecordStoreException {
        int id = 127;
        info.setId(id);
        expect(RecordStore.openRecordStore(
                MeteorogramInfoRecordStoreDao.STORE, true)).andReturn(recordStoreMock);
        recordStoreMock.setRecord(eq(id), aryEq(infoBytes), eq(0), eq(infoBytes.length));
        recordStoreMock.closeRecordStore();
        replayAll();
        fixture.createOrUpdate(info);
        assertThat(info.isTainted(), is(false));
        verifyAll();
    }

    @Test
    public void createOrUpdateValidExistingWithIdNotTainted() {
        int id = 67;
        info.setId(id);
        info.setTainted(false);
        replayAll();
        fixture.createOrUpdate(info);
        assertThat(info.isTainted(), is(false));
        verifyAll();
    }

    @Test
    public void createOrUpdateValidNotExistingWithIdTainted() throws RecordStoreException {
        int id = 70;
        int newId = 89;
        info.setId(id);
        expect(RecordStore.openRecordStore(
                MeteorogramInfoRecordStoreDao.STORE, true)).andReturn(recordStoreMock).times(2);
        recordStoreMock.setRecord(eq(id), aryEq(infoBytes), eq(0), eq(infoBytes.length));
        expectLastCall().andThrow(new InvalidRecordIDException());
        expect(recordStoreMock.addRecord(
                aryEq(infoBytes), eq(0), eq(infoBytes.length))).andReturn(newId);
        recordStoreMock.closeRecordStore();
        expectLastCall().times(2);
        replayAll();
        fixture.createOrUpdate(info);
        assertThat(info.getId(), equalTo(newId));
        assertThat(info.isTainted(), is(false));
        verifyAll();
    }

    @Test(expected = NullPointerException.class)
    public void createOrUpdateNull() {
        replayAll();
        fixture.createOrUpdate(null);
        verifyAll();
    }

    @Test
    public void deleteExisting() throws RecordStoreException {
        int id = 123;
        info.setId(id);
        expect(RecordStore.openRecordStore(
                MeteorogramInfoRecordStoreDao.STORE, true)).andReturn(recordStoreMock);
        recordStoreMock.deleteRecord(id);
        recordStoreMock.closeRecordStore();
        replayAll();
        fixture.delete(info);
        assertThat(info.getId(), is(nullValue()));
        verifyAll();

    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteNonExistingWithoutId() {
        replayAll();
        fixture.delete(info);
        verifyAll();
    }

    @Test
    public void deleteNonExistingNotInStore() throws RecordStoreException {
        int id = 234;
        info.setId(id);
        expect(RecordStore.openRecordStore(
                MeteorogramInfoRecordStoreDao.STORE, true)).andReturn(recordStoreMock);
        recordStoreMock.deleteRecord(id);
        expectLastCall().andThrow(new InvalidRecordIDException());
        recordStoreMock.closeRecordStore();
        replayAll();
        fixture.delete(info);
        assertThat(info.getId(), is(nullValue()));
        verifyAll();
    }

    @Test(expected = NullPointerException.class)
    public void deleteNull() {
        replayAll();
        fixture.delete(null);
        verifyAll();
    }

    @Test
    public void readValidData() throws RecordStoreException {
        int id = 53;
        expect(RecordStore.openRecordStore(
                MeteorogramInfoRecordStoreDao.STORE, true)).andReturn(recordStoreMock);
        expect(recordStoreMock.getRecord(id)).andReturn(infoBytes);
        recordStoreMock.closeRecordStore();
        replayAll();
        MeteorogramInfo actualInfo = fixture.read(id);
        assertThat(actualInfo.getId(), equalTo(id));
        assertThat(actualInfo.getX(), equalTo(info.getX()));
        assertThat(actualInfo.getY(), equalTo(info.getY()));
        assertThat(actualInfo.isTainted(), is(false));
        verifyAll();
    }

    @Test
    public void readInvalidData() throws RecordStoreException {
        infoBytes = new byte[]{10};
        int id = 34;
        expect(RecordStore.openRecordStore(
                MeteorogramInfoRecordStoreDao.STORE, true)).andReturn(recordStoreMock).times(2);
        expect(recordStoreMock.getRecord(id)).andReturn(infoBytes);
        recordStoreMock.deleteRecord(id);
        recordStoreMock.closeRecordStore();
        expectLastCall().times(2);
        replayAll();
        MeteorogramInfo actualInfo = fixture.read(id);
        assertThat(actualInfo, is(nullValue()));
        verifyAll();
    }

    @Test
    public void readNotInStore() throws RecordStoreException {
        int id = 45;
        expect(RecordStore.openRecordStore(
                MeteorogramInfoRecordStoreDao.STORE, true)).andReturn(recordStoreMock);
        expect(recordStoreMock.getRecord(id)).andThrow(new InvalidRecordIDException());
        recordStoreMock.closeRecordStore();
        replayAll();
        MeteorogramInfo actualInfo = fixture.read(id);
        assertThat(actualInfo, is(nullValue()));
        verifyAll();
    }

    @Test
    public void readAllValid() throws RecordStoreException {
        int id1 = 90;
        int id2 = 190;
        expect(RecordStore.openRecordStore(
                MeteorogramInfoRecordStoreDao.STORE, true)).andReturn(recordStoreMock);
        expect(recordStoreMock.enumerateRecords(
                null, fixture.getRecordComparator(), false)).andReturn(recordEnumerationMock);
        recordEnumerationMock.keepUpdated(true);
        expect(recordEnumerationMock.numRecords()).andReturn(2);
        expect(recordEnumerationMock.hasNextElement()).andReturn(Boolean.TRUE).times(2);
        expect(recordEnumerationMock.hasNextElement()).andReturn(Boolean.FALSE);
        expect(recordEnumerationMock.nextRecordId()).andReturn(id1);
        expect(recordEnumerationMock.nextRecordId()).andReturn(id2);
        expect(recordStoreMock.getRecord(id1)).andReturn(infoBytes);
        expect(recordStoreMock.getRecord(id2)).andReturn(infoBytes);
        recordEnumerationMock.destroy();
        recordStoreMock.closeRecordStore();
        replayAll();
        Vector actuals = fixture.readAll();
        assertThat(actuals.size(), equalTo(2));
        MeteorogramInfo actualInfo = (MeteorogramInfo) actuals.get(0);
        assertThat(actualInfo, is(not(nullValue())));
        assertThat(actualInfo.getId(), equalTo(id1));
        actualInfo = (MeteorogramInfo) actuals.get(1);
        assertThat(actualInfo, is(not(nullValue())));
        assertThat(actualInfo.getId(), equalTo(id2));
        verifyAll();
    }

    @Test
    public void readAllInvalid() throws RecordStoreException {
        int id = 17;
        expect(RecordStore.openRecordStore(
                MeteorogramInfoRecordStoreDao.STORE, true)).andReturn(recordStoreMock).times(2);
        expect(recordStoreMock.enumerateRecords(
                null, fixture.getRecordComparator(), false)).andReturn(recordEnumerationMock);
        recordEnumerationMock.keepUpdated(true);
        expect(recordEnumerationMock.numRecords()).andReturn(1);
        expect(recordEnumerationMock.hasNextElement()).andReturn(Boolean.TRUE);
        expect(recordEnumerationMock.hasNextElement()).andReturn(Boolean.FALSE);
        expect(recordEnumerationMock.nextRecordId()).andReturn(id);
        expect(recordStoreMock.getRecord(id)).andReturn(new byte[]{10});
        recordStoreMock.deleteRecord(id);
        recordEnumerationMock.destroy();
        recordStoreMock.closeRecordStore();
        expectLastCall().times(2);
        replayAll();
        Vector infos = fixture.readAll();
        assertThat(infos.size(), equalTo(0));
        verifyAll();
    }

    @Test
    public void getRecordComparator() {
        RecordComparator actual = fixture.getRecordComparator();
        assertThat(actual, is(nullValue()));
    }
}
