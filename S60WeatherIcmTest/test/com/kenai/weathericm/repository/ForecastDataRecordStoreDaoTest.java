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

import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;
import com.kenai.weathericm.domain.ForecastData;
import java.util.Random;
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;
import net.sf.microlog.core.config.PropertyConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.createPartialMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.startsWith;
import static org.easymock.EasyMock.aryEq;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.anyInt;

/**
 * Tests for {@link ForecastDataRecordStoreDao} class.
 * @author Przemek Kryger
 */
@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("javax.microedition.rms.RecordStore")
@PrepareForTest({RecordStore.class, RecordEnumeration.class, ForecastDataRecordStoreDao.class})
public class ForecastDataRecordStoreDaoTest {

    @BeforeClass
    public static void setUpClass() {
        PropertyConfigurator.configure("/testMicrolog.properties");
    }
    private RecordStore recordStoreMock = null;
    private RecordEnumeration recordEnumerationMock = null;
    private ForecastDataRecordStoreDao fixture = null;
    ForecastData data = null;
    ForecastDataSerializer serializer = null;

    @Before
    public void setUp() {
        mockStatic(RecordStore.class);
        recordStoreMock = createMock(RecordStore.class);
        recordEnumerationMock = createMock(RecordEnumeration.class);
        fixture = ForecastDataRecordStoreDao.getInstance();
        final String date = "2010091012";
        data = new ForecastData(date);
        data.setModelResult(new byte[ForecastDataRecordStoreDao.MIN_STORE_SPACE * 10]);
        serializer = new ForecastDataSerializer() {

            @Override
            public byte[] serialize(ForecastData forecastData) {
                return forecastData.getModelResult();
            }

            @Override
            public ForecastData resurect(byte[] data) {
                ForecastData forecastData = new ForecastData(date);
                forecastData.setModelResult(data);
                return forecastData;
            }
        };
        fixture.setForecastDataSerializer(serializer);
    }

    @Test
    public void getInstance() {
        replayAll();
        ForecastDataRecordStoreDao otherInstance = ForecastDataRecordStoreDao.getInstance();
        assertThat(otherInstance, is(fixture));
        verifyAll();
    }

    @Test
    public void convertIdToBaseName() {
        replayAll();
        Integer id = 77;
        String expected = ForecastDataRecordStoreDao.STORE_PREFIX
                + id + ForecastDataRecordStoreDao.STORE_INFIX;
        String actual = fixture.convertIdToBaseName(id);
        assertThat(actual, equalTo(expected));
        verifyAll();
    }

    @Test(expected = NullPointerException.class)
    public void convertIdToBaseNameNull() {
        replayAll();
        fixture.convertIdToBaseName(null);
        verifyAll();
    }

    @Test
    public void existsNoRecordStore() {
        Integer id = 3;
        expect(RecordStore.listRecordStores()).andReturn(null);
        replayAll();
        boolean actual = fixture.exists(id);
        assertThat(actual, is(false));
    }

    @Test
    public void existsExist() {
        Integer id = 98;
        String[] recordStores = new String[]{
            fixture.convertIdToBaseName(id - 1) + "1",
            fixture.convertIdToBaseName(id) + "1",
            fixture.convertIdToBaseName(id + 1) + "1",};
        expect(RecordStore.listRecordStores()).andReturn(recordStores);
        replayAll();
        boolean actual = fixture.exists(id);
        assertThat(actual, is(true));
        verifyAll();
    }

    @Test
    public void existsNotExists() {
        Integer id = 24;
        String[] recordStores = new String[]{
            fixture.convertIdToBaseName(id - 1) + "1",
            fixture.convertIdToBaseName(id + 1) + "1",};
        expect(RecordStore.listRecordStores()).andReturn(recordStores);
        replayAll();
        boolean actual = fixture.exists(id);
        assertThat(actual, is(false));
        verifyAll();
    }

    @Test
    public void createSingleRecordStore() throws Exception {
        Integer id = 56;
        String[] recordStores = new String[]{
            fixture.convertIdToBaseName(id - 1) + "1",
            fixture.convertIdToBaseName(id + 1) + "1",};
        expect(RecordStore.listRecordStores()).andReturn(recordStores);
        String storeName = fixture.convertIdToBaseName(id);
        expect(RecordStore.openRecordStore(startsWith(storeName), eq(true))).andReturn(recordStoreMock);
        byte[] serializedData = serializer.serialize(data);
        expect(recordStoreMock.getSizeAvailable()).andReturn(2 * serializedData.length
                + ForecastDataRecordStoreDao.MIN_STORE_SPACE);
        expect(recordStoreMock.addRecord(aryEq(serializedData), eq(0), eq(serializedData.length))).andReturn(1);
        recordStoreMock.closeRecordStore();
        replayAll();
        boolean actual = fixture.create(id, data);
        assertThat(actual, is(true));
        verifyAll();
    }

    @Test
    public void createMultipleRecordStores() throws Exception {
        Integer id = 59;
        String[] recordStores = new String[]{
            fixture.convertIdToBaseName(id - 1) + "1",
            fixture.convertIdToBaseName(id + 1) + "1",};
        expect(RecordStore.listRecordStores()).andReturn(recordStores);
        String storeName = fixture.convertIdToBaseName(id);
        expect(RecordStore.openRecordStore(startsWith(storeName), eq(true))).andReturn(recordStoreMock).atLeastOnce();
        byte[] serializedData = serializer.serialize(data);
        expect(recordStoreMock.getSizeAvailable()).andReturn(serializedData.length / 3
                + ForecastDataRecordStoreDao.MIN_STORE_SPACE).atLeastOnce();
        expect(recordStoreMock.addRecord(aryEq(serializedData), anyInt(), anyInt())).andReturn(1).atLeastOnce();
        recordStoreMock.closeRecordStore();
        expectLastCall().atLeastOnce();
        replayAll();
        boolean actual = fixture.create(id, data);
        assertThat(actual, is(true));
        verifyAll();
    }

    @Test(expected = NullPointerException.class)
    public void createNullId() {
        replayAll();
        fixture.create(null, data);
        verifyAll();
    }

    @Test(expected = NullPointerException.class)
    public void createNullForecastData() {
        replayAll();
        fixture.create(77, null);
        verifyAll();
    }

    @Test
    public void createFirstRecordStoreCreationFailureRecordStoreException() throws Exception {
        createFirstRecordStoreCreationFailure(new RecordStoreException());
    }

    @Test
    public void createFirstRecordStoreCreationFailureRecordStoreNotFoundException() throws Exception {
        createFirstRecordStoreCreationFailure(new RecordStoreNotFoundException());
    }

    @Test
    public void createFirstRecordStoreCreationFailureRecordStoreFullException() throws Exception {
        createFirstRecordStoreCreationFailure(new RecordStoreFullException());
    }

    @Test
    public void createFirstRecordStoreCreationFailureIllegalArgumentException() throws Exception {
        createFirstRecordStoreCreationFailure(new IllegalArgumentException());
    }

    private void createFirstRecordStoreCreationFailure(Exception failure) throws Exception {
        Integer id = 53;
        String[] recordStores = new String[]{
            fixture.convertIdToBaseName(id - 1) + "1",
            fixture.convertIdToBaseName(id + 1) + "1",};
        expect(RecordStore.listRecordStores()).andReturn(recordStores).anyTimes();
        String storeName = fixture.convertIdToBaseName(id);
        expect(RecordStore.openRecordStore(startsWith(storeName), eq(true))).andThrow(failure);
        RecordStore.deleteRecordStore(startsWith(storeName));
        expectLastCall().anyTimes();
        replayAll();
        boolean actual = fixture.create(id, data);
        assertThat(actual, is(false));
        verifyAll();
    }

    @Test
    public void createFollowingRecordStoreCreationFailureRecordStoreException() throws Exception {
        createFollowingRecordStoreCreationFailure(new RecordStoreException());
    }

    @Test
    public void createFollowingRecordStoreCreationFailureRecordStoreNotFoundException() throws Exception {
        createFollowingRecordStoreCreationFailure(new RecordStoreNotFoundException());
    }

    @Test
    public void createFollowingRecordStoreCreationFailureRecordStoreFullException() throws Exception {
        createFollowingRecordStoreCreationFailure(new RecordStoreFullException());
    }

    @Test
    public void createFollowingRecordStoreCreationFailureIllegalArgumentException() throws Exception {
        createFollowingRecordStoreCreationFailure(new IllegalArgumentException());
    }

    private void createFollowingRecordStoreCreationFailure(Exception failure) throws Exception {
        Integer id = 53;
        String[] recordStores = new String[]{
            fixture.convertIdToBaseName(id - 1) + "1",
            fixture.convertIdToBaseName(id + 1) + "1",};
        expect(RecordStore.listRecordStores()).andReturn(recordStores);
        String storeName = fixture.convertIdToBaseName(id);
        expect(RecordStore.openRecordStore(startsWith(storeName), eq(true))).andReturn(recordStoreMock);
        byte[] serializedData = serializer.serialize(data);
        expect(recordStoreMock.getSizeAvailable()).andReturn(serializedData.length / 3
                + ForecastDataRecordStoreDao.MIN_STORE_SPACE);
        expect(recordStoreMock.addRecord(aryEq(serializedData), eq(0), anyInt())).andReturn(1);
        recordStoreMock.closeRecordStore();
        expect(RecordStore.openRecordStore(startsWith(storeName), eq(true))).andThrow(failure);
        String[] newRecordStores = new String[]{
            fixture.convertIdToBaseName(id - 1) + "1",
            fixture.convertIdToBaseName(id) + "1",
            fixture.convertIdToBaseName(id + 1) + "1",};
        expect(RecordStore.listRecordStores()).andReturn(newRecordStores);
        RecordStore.deleteRecordStore(startsWith(storeName));
        replayAll();
        boolean actual = fixture.create(id, data);
        assertThat(actual, is(false));
        verifyAll();
    }

    @Test
    public void createRecordStoreFirstAdditionFailureRecordStoreException() throws Exception {
        createRecordStoreFirstAdditionFailure(new RecordStoreException());
    }

    @Test
    public void createRecordStoreFirstAdditionFailureRecordStoreNotOpenException() throws Exception {
        createRecordStoreFirstAdditionFailure(new RecordStoreNotOpenException());
    }

    @Test
    public void createRecordStoreFirstAdditionFailureRecordStoreFullException() throws Exception {
        createRecordStoreFirstAdditionFailure(new RecordStoreFullException());
    }

    @Test
    public void createRecordStoreFirstAdditionFailureSecurityException() throws Exception {
        createRecordStoreFirstAdditionFailure(new SecurityException());
    }

    private void createRecordStoreFirstAdditionFailure(Exception failure) throws Exception {
        Integer id = 53;
        String[] recordStores = new String[]{
            fixture.convertIdToBaseName(id - 1) + "1",
            fixture.convertIdToBaseName(id + 1) + "1",};
        expect(RecordStore.listRecordStores()).andReturn(recordStores);
        String storeName = fixture.convertIdToBaseName(id);
        expect(RecordStore.openRecordStore(startsWith(storeName), eq(true))).andReturn(recordStoreMock);
        byte[] serializedData = serializer.serialize(data);
        expect(recordStoreMock.getSizeAvailable()).andReturn(serializedData.length / 3
                + ForecastDataRecordStoreDao.MIN_STORE_SPACE);
        expect(recordStoreMock.addRecord(aryEq(serializedData), eq(0), anyInt())).andThrow(failure);
        recordStoreMock.closeRecordStore();
        String[] newRecordStores = new String[]{
            fixture.convertIdToBaseName(id - 1) + "1",
            fixture.convertIdToBaseName(id) + "1",
            fixture.convertIdToBaseName(id + 1) + "1",};
        expect(RecordStore.listRecordStores()).andReturn(newRecordStores);
        RecordStore.deleteRecordStore(startsWith(storeName));
        replayAll();
        boolean actual = fixture.create(id, data);
        assertThat(actual, is(false));
        verifyAll();
    }

    @Test
    public void createRecordStoreFollowingAdditionFailureRecordStoreException() throws Exception {
        createRecordStoreFollowingAdditionFailure(new RecordStoreException());
    }

    @Test
    public void createRecordStoreFollowingAdditionFailureRecordStoreNotOpenException() throws Exception {
        createRecordStoreFollowingAdditionFailure(new RecordStoreNotOpenException());
    }

    @Test
    public void createRecordStoreFollowingAdditionFailureRecordStoreFullException() throws Exception {
        createRecordStoreFollowingAdditionFailure(new RecordStoreFullException());
    }

    @Test
    public void createRecordStoreFollowingAdditionFailureSecurityException() throws Exception {
        createRecordStoreFollowingAdditionFailure(new SecurityException());
    }

    private void createRecordStoreFollowingAdditionFailure(Exception failure) throws Exception {
        Integer id = 53;
        String[] recordStores = new String[]{
            fixture.convertIdToBaseName(id - 1) + "1",
            fixture.convertIdToBaseName(id + 1) + "1",};
        expect(RecordStore.listRecordStores()).andReturn(recordStores);
        String storeName = fixture.convertIdToBaseName(id);
        expect(RecordStore.openRecordStore(startsWith(storeName), eq(true))).andReturn(recordStoreMock).times(2);
        byte[] serializedData = serializer.serialize(data);
        expect(recordStoreMock.getSizeAvailable()).andReturn(serializedData.length / 3
                + ForecastDataRecordStoreDao.MIN_STORE_SPACE).times(2);
        expect(recordStoreMock.addRecord(aryEq(serializedData), eq(0), anyInt())).andReturn(1);
        expect(recordStoreMock.addRecord(aryEq(serializedData), anyInt(), anyInt())).andThrow(failure);
        recordStoreMock.closeRecordStore();
        expectLastCall().times(2);
        String[] newRecordStores = new String[]{
            fixture.convertIdToBaseName(id - 1) + "1",
            fixture.convertIdToBaseName(id) + "1",
            fixture.convertIdToBaseName(id) + "2",
            fixture.convertIdToBaseName(id + 1) + "1",};
        expect(RecordStore.listRecordStores()).andReturn(newRecordStores);
        RecordStore.deleteRecordStore(startsWith(storeName));
        expectLastCall().times(2);
        replayAll();
        boolean actual = fixture.create(id, data);
        assertThat(actual, is(false));
        verifyAll();
    }

    @Test
    public void createRecordStoreAlreadyExists() {
        Integer id = 56;
        String[] recordStores = new String[]{
            fixture.convertIdToBaseName(id - 1) + "1",
            fixture.convertIdToBaseName(id) + "1",
            fixture.convertIdToBaseName(id + 1) + "1",};
        expect(RecordStore.listRecordStores()).andReturn(recordStores);
        replayAll();
        boolean actual = fixture.create(id, data);
        assertThat(actual, is(false));
        verifyAll();
    }

    @Test(expected = NullPointerException.class)
    public void deleteNullId() {
        fixture.delete(null);
    }

    @Test
    public void deleteNotExisting() {
        Integer id = 57;
        String[] recordStores = new String[]{
            fixture.convertIdToBaseName(id - 1) + "1",
            fixture.convertIdToBaseName(id + 1) + "1",};
        expect(RecordStore.listRecordStores()).andReturn(recordStores);
        replayAll();
        boolean actual = fixture.delete(id);
        assertThat(actual, is(false));
        verifyAll();
    }

    @Test
    public void deleteSingleRecordStore() throws Exception {
        Integer id = 58;
        String[] recordStores = new String[]{
            fixture.convertIdToBaseName(id - 1) + "1",
            fixture.convertIdToBaseName(id) + "1",
            fixture.convertIdToBaseName(id + 1) + "1",};
        expect(RecordStore.listRecordStores()).andReturn(recordStores);
        RecordStore.deleteRecordStore(startsWith(fixture.convertIdToBaseName(id)));
        replayAll();
        boolean actual = fixture.delete(id);
        assertThat(actual, is(true));
        verifyAll();
    }

    @Test
    public void deleteMultipleRecordStores() throws Exception {
        Integer id = 59;
        String[] recordStores = new String[]{
            fixture.convertIdToBaseName(id - 1) + "1",
            fixture.convertIdToBaseName(id) + "1",
            fixture.convertIdToBaseName(id) + "2",
            fixture.convertIdToBaseName(id + 1) + "1",};
        expect(RecordStore.listRecordStores()).andReturn(recordStores);
        RecordStore.deleteRecordStore(startsWith(fixture.convertIdToBaseName(id)));
        expectLastCall().times(2);
        replayAll();
        boolean actual = fixture.delete(id);
        assertThat(actual, is(true));
        verifyAll();
    }

    private void deleteSingleRecordStoreFailure(Exception failure) throws Exception {
        Integer id = 60;
        String[] recordStores = new String[]{
            fixture.convertIdToBaseName(id - 1) + "1",
            fixture.convertIdToBaseName(id) + "1",
            fixture.convertIdToBaseName(id + 1) + "1",};
        expect(RecordStore.listRecordStores()).andReturn(recordStores);
        RecordStore.deleteRecordStore(startsWith(fixture.convertIdToBaseName(id)));
        expectLastCall().andThrow(failure);
        replayAll();
        boolean actual = fixture.delete(id);
        assertThat(actual, is(false));
        verifyAll();
    }

    @Test
    public void deleteSingleRecordStoreRecordStoreException() throws Exception {
        deleteSingleRecordStoreFailure(new RecordStoreException());
    }

    @Test
    public void deleteSingleRecordStoreRecordStoreNotFoundException() throws Exception {
        deleteSingleRecordStoreFailure(new RecordStoreNotFoundException());
    }

    private void deleteMultipleRecordStoresOneFailure(Exception failure) throws Exception {
        Integer id = 61;
        String[] recordStores = new String[]{
            fixture.convertIdToBaseName(id - 1) + "1",
            fixture.convertIdToBaseName(id) + "1",
            fixture.convertIdToBaseName(id) + "2",
            fixture.convertIdToBaseName(id + 1) + "1",};
        expect(RecordStore.listRecordStores()).andReturn(recordStores);
        if (new Random().nextBoolean()) {
            RecordStore.deleteRecordStore(startsWith(fixture.convertIdToBaseName(id)));
            expectLastCall().andThrow(failure);
            RecordStore.deleteRecordStore(startsWith(fixture.convertIdToBaseName(id)));
        } else {
            RecordStore.deleteRecordStore(startsWith(fixture.convertIdToBaseName(id)));
            RecordStore.deleteRecordStore(startsWith(fixture.convertIdToBaseName(id)));
            expectLastCall().andThrow(failure);
        }
        replayAll();
        boolean actual = fixture.delete(id);
        assertThat(actual, is(false));
        verifyAll();
    }

    @Test
    public void deleteMultipleRecordStoresOneRecordStoreException() throws Exception {
        deleteMultipleRecordStoresOneFailure(new RecordStoreException());
    }

    @Test
    public void deleteMultipleRecordStoresOneRecordStoreNotFoundException() throws Exception {
        deleteMultipleRecordStoresOneFailure(new RecordStoreNotFoundException());
    }

    private void deleteMultipleRecordStoresMultipleFailure(Exception failure) throws Exception {
        Integer id = 62;
        String[] recordStores = new String[]{
            fixture.convertIdToBaseName(id - 1) + "1",
            fixture.convertIdToBaseName(id) + "1",
            fixture.convertIdToBaseName(id) + "2",
            fixture.convertIdToBaseName(id + 1) + "1",};
        expect(RecordStore.listRecordStores()).andReturn(recordStores);
        RecordStore.deleteRecordStore(startsWith(fixture.convertIdToBaseName(id)));
        expectLastCall().andThrow(failure).times(2);
        replayAll();
        boolean actual = fixture.delete(id);
        assertThat(actual, is(false));
        verifyAll();
    }

    @Test
    public void deleteMultipleRecordStoresMultipleRecordStoreException() throws Exception {
        deleteMultipleRecordStoresMultipleFailure(new RecordStoreException());
    }

    @Test
    public void deleteMultipleRecordStoresMultipleRecordStoreNotFoundException() throws Exception {
        deleteMultipleRecordStoresMultipleFailure(new RecordStoreNotFoundException());
    }

    @Test(expected = NullPointerException.class)
    public void updateNullId() {
        fixture.update(null, data);
    }

    @Test(expected = NullPointerException.class)
    public void updateNullData() {
        Integer id = 63;
        fixture.update(id, null);
    }

    @Test
    public void updateDeletionOfExistingRecordFailure() {
        Integer id = 64;
        fixture = createPartialMock(ForecastDataRecordStoreDao.class, "delete");
        expect(fixture.delete(id)).andReturn(Boolean.FALSE);
        replayAll();
        boolean actual = fixture.update(id, data);
        assertThat(actual, is(false));
        verifyAll();
    }

    @Test
    public void updateCreationFailure() {
        Integer id = 65;
        fixture = createPartialMock(ForecastDataRecordStoreDao.class, "delete", "create");
        expect(fixture.delete(id)).andReturn(Boolean.TRUE);
        expect(fixture.create(id, data)).andReturn(Boolean.FALSE);
        replayAll();
        boolean actual = fixture.update(id, data);
        assertThat(actual, is(false));
        verifyAll();
    }

    @Test
    public void update() {
        Integer id = 66;
        fixture = createPartialMock(ForecastDataRecordStoreDao.class, "delete", "create");
        expect(fixture.delete(id)).andReturn(Boolean.TRUE);
        expect(fixture.create(id, data)).andReturn(Boolean.TRUE);
        replayAll();
        boolean actual = fixture.update(id, data);
        assertThat(actual, is(true));
        verifyAll();
    }

    @Test(expected = NullPointerException.class)
    public void createOrUpdateNullId() {
        fixture.createOrUpdate(null, data);
    }

    @Test(expected = NullPointerException.class)
    public void createOrUpdateNullData() {
        Integer id = 67;
        fixture.createOrUpdate(id, null);
    }

    @Test
    public void createOrUpdateExistingUpdateFailure() {
        Integer id = 68;
        fixture = createPartialMock(ForecastDataRecordStoreDao.class, "exists", "update");
        expect(fixture.exists(id)).andReturn(Boolean.TRUE).atLeastOnce();
        expect(fixture.update(id, data)).andReturn(Boolean.FALSE);
        replayAll();
        boolean actual = fixture.createOrUpdate(id, data);
        assertThat(actual, is(false));
        verifyAll();
    }

    @Test
    public void createOrUpdateExisting() {
        Integer id = 69;
        fixture = createPartialMock(ForecastDataRecordStoreDao.class, "exists", "update");
        expect(fixture.exists(id)).andReturn(Boolean.TRUE).atLeastOnce();
        expect(fixture.update(id, data)).andReturn(Boolean.TRUE);
        replayAll();
        boolean actual = fixture.createOrUpdate(id, data);
        assertThat(actual, is(true));
        verifyAll();
    }

    @Test
    public void createOrUpdateNotExistingCreationFailure() {
        Integer id = 70;
        fixture = createPartialMock(ForecastDataRecordStoreDao.class, "exists", "create");
        expect(fixture.exists(id)).andReturn(Boolean.FALSE).atLeastOnce();
        expect(fixture.create(id, data)).andReturn(Boolean.FALSE);
        replayAll();
        boolean actual = fixture.createOrUpdate(id, data);
        assertThat(actual, is(false));
        verifyAll();
    }

    @Test
    public void createOrUpdateNotExisting() {
        Integer id = 70;
        fixture = createPartialMock(ForecastDataRecordStoreDao.class, "exists", "create");
        expect(fixture.exists(id)).andReturn(Boolean.FALSE).atLeastOnce();
        expect(fixture.create(id, data)).andReturn(Boolean.TRUE);
        replayAll();
        boolean actual = fixture.createOrUpdate(id, data);
        assertThat(actual, is(true));
        verifyAll();
    }

    @Test(expected = NullPointerException.class)
    public void readNullId() {
        fixture.read(null);
    }

    @Test
    public void readNotExisting() {
        Integer id = 71;
        String[] recordStores = new String[]{
            fixture.convertIdToBaseName(id - 1) + "1",
            fixture.convertIdToBaseName(id + 1) + "1",
        };
        expect(RecordStore.listRecordStores()).andReturn(recordStores);
        replayAll();
        ForecastData actual = fixture.read(id);
        assertThat(actual, is(nullValue()));
        verifyAll();
    }

    private void readOpenStoreFailure(Exception failure) throws Exception {
        Integer id = 72;
        String storeName = fixture.convertIdToBaseName(id) + "1";
        String[] recordStores = new String[]{
            fixture.convertIdToBaseName(id - 1) + "1",
            storeName,
            fixture.convertIdToBaseName(id + 1) + "1",
        };
        expect(RecordStore.listRecordStores()).andReturn(recordStores);
        expect(RecordStore.openRecordStore(storeName, false)).andThrow(failure);
        replayAll();
        ForecastData actual = fixture.read(id);
        assertThat(actual, is(nullValue()));
        verifyAll();
    }

    @Test
    public void readOpenStoreRecordStoreException() throws Exception {
        readOpenStoreFailure(new RecordStoreException());
    }

    @Test
    public void readOpenStoreRecordStoreNotFoundException() throws Exception {
        readOpenStoreFailure(new RecordStoreNotFoundException());
    }

    @Test
    public void readOpenStoreRecordStoreFullException() throws Exception {
        readOpenStoreFailure(new RecordStoreFullException());
    }

    @Test
    public void readOpenStoreIllegalArgumentException() throws Exception {
        readOpenStoreFailure(new IllegalArgumentException());
    }

    private void readNextRecordFailure(Exception failure) throws Exception {
        Integer id = 73;
        String storeName = fixture.convertIdToBaseName(id) + "1";
        String[] recordStores = new String[]{
            fixture.convertIdToBaseName(id - 1) + "1",
            storeName,
            fixture.convertIdToBaseName(id + 1) + "1",
        };
        expect(RecordStore.listRecordStores()).andReturn(recordStores);
        expect(RecordStore.openRecordStore(storeName, false)).andReturn(recordStoreMock);
        expect(recordStoreMock.enumerateRecords(null, null, false)).andReturn(recordEnumerationMock);
        expect(recordEnumerationMock.nextRecord()).andThrow(failure);
        recordStoreMock.closeRecordStore();
        replayAll();
        ForecastData actual = fixture.read(id);
        assertThat(actual, is(nullValue()));
        verifyAll();
    }

    @Test
    public void readNextRecordInvalidRecordIDException() throws Exception {
        readNextRecordFailure(new InvalidRecordIDException());
    }

    @Test
    public void readNextRecordRecordStoreException() throws Exception {
        readNextRecordFailure(new RecordStoreException());
    }

    @Test
    public void readCloseRecordStoreFailure() throws Exception {
        Integer id = 74;
        byte[] rawData = new byte[] {1, 2, 3};
        String storeName = fixture.convertIdToBaseName(id) + "1";
        String[] recordStores = new String[]{
            fixture.convertIdToBaseName(id - 1) + "1",
            storeName,
            fixture.convertIdToBaseName(id + 1) + "1",
        };
        expect(RecordStore.listRecordStores()).andReturn(recordStores);
        expect(RecordStore.openRecordStore(storeName, false)).andReturn(recordStoreMock);
        expect(recordStoreMock.enumerateRecords(null, null, false)).andReturn(recordEnumerationMock);
        expect(recordEnumerationMock.nextRecord()).andReturn(rawData);
        recordStoreMock.closeRecordStore();
        expectLastCall().andThrow(new RecordStoreException());
        replayAll();
        ForecastData actual = fixture.read(id);
        assertThat(actual, is(notNullValue()));
        assertThat(actual.getModelResult(), equalTo(rawData));
        verifyAll();
    }

    @Test
    public void readSingleRecordStore() throws Exception {
        Integer id = 75;
        byte[] rawData = new byte[] {1, 2, 3};
        String storeName = fixture.convertIdToBaseName(id) + "1";
        String[] recordStores = new String[]{
            fixture.convertIdToBaseName(id - 1) + "1",
            storeName,
            fixture.convertIdToBaseName(id + 1) + "1",
        };
        expect(RecordStore.listRecordStores()).andReturn(recordStores);
        expect(RecordStore.openRecordStore(storeName, false)).andReturn(recordStoreMock);
        expect(recordStoreMock.enumerateRecords(null, null, false)).andReturn(recordEnumerationMock);
        expect(recordEnumerationMock.nextRecord()).andReturn(rawData);
        recordStoreMock.closeRecordStore();
        replayAll();
        ForecastData actual = fixture.read(id);
        assertThat(actual, is(notNullValue()));
        assertThat(actual.getModelResult(), equalTo(rawData));
        verifyAll();
    }

    @Test
    public void readMultipleRecordStores() throws Exception {
        Integer id = 76;
        byte[] rawData1 = new byte[] {1, 2, 3};
        byte[] rawData2 = new byte[] {4, 5, 6};
        byte[] rawData = new byte[] {1, 2, 3, 4, 5, 6};
        String storeNamePrefix = fixture.convertIdToBaseName(id);
        String[] recordStores = new String[]{
            fixture.convertIdToBaseName(id - 1) + "1",
            storeNamePrefix + "1",
            storeNamePrefix + "2",
            fixture.convertIdToBaseName(id + 1) + "1",
        };
        expect(RecordStore.listRecordStores()).andReturn(recordStores);
        expect(RecordStore.openRecordStore(startsWith(storeNamePrefix), eq(false))).andReturn(recordStoreMock).times(2);
        expect(recordStoreMock.enumerateRecords(null, null, false)).andReturn(recordEnumerationMock).times(2);
        expect(recordEnumerationMock.nextRecord()).andReturn(rawData1);
        expect(recordEnumerationMock.nextRecord()).andReturn(rawData2);
        recordStoreMock.closeRecordStore();
        expectLastCall().times(2);
        replayAll();
        ForecastData actual = fixture.read(id);
        assertThat(actual, is(notNullValue()));
        assertThat(actual.getModelResult(), equalTo(rawData));
        verifyAll();
    }

    @Test
    public void readMultiplerecordStoresMissingOneFailure() throws Exception {
        Integer id = 77;
        byte[] rawData = new byte[] {1, 2, 3};
        String storeNamePrefix = fixture.convertIdToBaseName(id);
        String[] recordStores = new String[]{
            fixture.convertIdToBaseName(id - 1) + "1",
            storeNamePrefix + "1",
            storeNamePrefix + "3",
            fixture.convertIdToBaseName(id + 1) + "1",
        };
        expect(RecordStore.listRecordStores()).andReturn(recordStores);
        expect(RecordStore.openRecordStore(startsWith(storeNamePrefix), eq(false))).andReturn(recordStoreMock);
        expect(recordStoreMock.enumerateRecords(null, null, false)).andReturn(recordEnumerationMock);
        expect(recordEnumerationMock.nextRecord()).andReturn(rawData);
        recordStoreMock.closeRecordStore();
        replayAll();
        ForecastData actual = fixture.read(id);
        assertThat(actual, is(nullValue()));
        verifyAll();
    }
}
