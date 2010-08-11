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

import com.kenai.weathericm.app.MeteorogramBroker;
import com.kenai.weathericm.domain.MeteorogramInfo;
import com.kenai.weathericm.domain.MeteorogramType;
import com.kenai.weathericm.util.Status;
import com.kenai.weathericm.util.StatusReporter;
import com.kenai.weathericm.view.validation.MeteorogramInfoDataValidator;
import com.kenai.weathericm.view.validation.NewEditMeteorogramInfoFormData;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDlet;
import net.sf.microlog.core.config.PropertyConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.netbeans.microedition.lcdui.WaitScreen;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.eq;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;
import static org.powermock.api.easymock.PowerMock.constructor;
import static org.powermock.api.easymock.PowerMock.suppress;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.createPartialMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.expectPrivate;

/**
 * Tests for the {@link ViewController}.
 * @author Przemek Kryger
 */
@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor({"javax.microedition.lcdui.List",
    "javax.microedition.lcdui.Displayable",
    "javax.microedition.lcdui.Form",
    "javax.microedition.lcdui.TextField",
    "javax.microedition.lcdui.Item",
    "javax.microedition.lcdui.ChoiceGroup",
    "javax.microedition.lcdui.Image",
    "javax.microedition.lcdui.Canvas",
    "javax.microedition.lcdui.Font",
    "org.netbeans.microedition.lcdui.AbstractInfoScreen",
    "org.netbeans.microedition.lcdui.WaitScreen",
    "javax.microedition.lcdui.Command"})
@PrepareForTest(ViewController.class)
public class ViewControllerTest {

    @BeforeClass
    public static void setUpClass() {
        PropertyConfigurator.configure("/testMicrolog.properties");
    }
    private ViewController fixture = null;
    private List mainListMock = null;
    private Form newEditFormMock = null;
    private TextField nameTextFieldMock = null;
    private TextField xTextFieldMock = null;
    private TextField yTextFieldMock = null;
    private ChoiceGroup modelChoiceGroupMock = null;
    private WaitScreen waitScreenMock = null;
    private Image avaliableImage = null;
    private Image notAvaliableImage = null;
    private Command showCommandMock = null;
    private Command editCommandMock = null;
    private Command deleteCommandMock = null;
    MeteorogramBroker brokerMock = null;
    MeteorogramInfoDataValidator validatorMock = null;
    private final static String INFO_TO_INDEX_NAME = "infoToMainListIndex";
    private final static String NEW_MODE_NAME = "newMode";
    private final static String PROCESSED_INFO_NAME = "processedInfo";
    private final static String BROKER_NAME = "broker";
    private final static String VALIDATION_ERROR_NAME = "validationError";
    private final static String MAIN_LIST = "mainList";
    private final static String WAIT_SCREEN = "downloadWaitScreen";

    @Before
    public void setUp() {
        suppress(constructor(MIDlet.class));
        fixture = createPartialMock(ViewController.class,
                "getNewEditForm", "getNameTextField",
                "getXTextField", "getYTextField", "getModelChoiceGroup",
                "getForecastAvaliableImage", "getForecastNotAvaliableImage",
                "isDownloadWaitScreenVisible",
                "getShowCommand", "getEditCommand", "getDeleteCommand");
        mainListMock = createMock(List.class);
        newEditFormMock = createMock(Form.class);
        nameTextFieldMock = createMock(TextField.class);
        xTextFieldMock = createMock(TextField.class);
        yTextFieldMock = createMock(TextField.class);
        modelChoiceGroupMock = createMock(ChoiceGroup.class);
        avaliableImage = createMock(Image.class);
        notAvaliableImage = createMock(Image.class);
        waitScreenMock = createMock(WaitScreen.class);
        showCommandMock = createMock(Command.class);
        editCommandMock = createMock(Command.class);
        deleteCommandMock = createMock(Command.class);
        mockStatic(MeteorogramBroker.class);
        brokerMock = createMock(MeteorogramBroker.class);
        validatorMock = createMock(MeteorogramInfoDataValidator.class);
        Whitebox.setInternalState(fixture, INFO_TO_INDEX_NAME, new Hashtable(0));
        Whitebox.setInternalState(fixture, BROKER_NAME, brokerMock);
        Whitebox.setInternalState(fixture, MAIN_LIST, mainListMock);
        Whitebox.setInternalState(fixture, WAIT_SCREEN, waitScreenMock);
    }

    @Test
    public void readMeteorogramInfo() {
        int position0 = 0;
        int position1 = 1;
        String name0 = "b";
        String name1 = "a";
        mainListMock.deleteAll();
        expect(fixture.getShowCommand()).andReturn(showCommandMock).times(2);
        mainListMock.removeCommand(showCommandMock);
        mainListMock.addCommand(showCommandMock);
        expect(fixture.getEditCommand()).andReturn(editCommandMock).times(2);
        mainListMock.removeCommand(editCommandMock);
        mainListMock.addCommand(editCommandMock);
        expect(fixture.getDeleteCommand()).andReturn(deleteCommandMock).times(2);
        mainListMock.removeCommand(deleteCommandMock);
        mainListMock.addCommand(deleteCommandMock);
        expect(fixture.getForecastNotAvaliableImage()).andReturn(notAvaliableImage).times(2);
        expect(mainListMock.append(name1, notAvaliableImage)).andReturn(position1);
        expect(mainListMock.append(name0, notAvaliableImage)).andReturn(position0);
        Vector data = new Vector(2);
        MeteorogramInfo info1 = new MeteorogramInfo();
        info1.setName(name1);
        data.addElement(info1);
        MeteorogramInfo info0 = new MeteorogramInfo();
        info0.setName(name0);
        data.addElement(info0);
        replayAll();
        fixture.readMeteorogramInfo(data);
        Hashtable internalInfoToIndexactualInfos = Whitebox.getInternalState(fixture, INFO_TO_INDEX_NAME);
        assertThat(internalInfoToIndexactualInfos.size(), equalTo(2));
        assertThat((Integer) internalInfoToIndexactualInfos.get(info0), equalTo(position0));
        assertThat((Integer) internalInfoToIndexactualInfos.get(info1), equalTo(position1));
        verifyAll();
    }

    @Test
    public void readMeteorogramInfoEmptyCollection() {
        mainListMock.deleteAll();
        expect(fixture.getShowCommand()).andReturn(showCommandMock);
        mainListMock.removeCommand(showCommandMock);
        expect(fixture.getEditCommand()).andReturn(editCommandMock);
        mainListMock.removeCommand(editCommandMock);
        expect(fixture.getDeleteCommand()).andReturn(deleteCommandMock);
        mainListMock.removeCommand(deleteCommandMock);
        Vector data = new Vector(0);
        replayAll();
        fixture.readMeteorogramInfo(data);
        Hashtable internalInfoToIndexactualInfos = Whitebox.getInternalState(fixture, INFO_TO_INDEX_NAME);
        assertThat(internalInfoToIndexactualInfos.size(), equalTo(0));
        verifyAll();
    }

    @Test
    public void readMeteorogramInfoNull() {
        replayAll();
        fixture.readMeteorogramInfo(null);
        Hashtable internalInfoToIndex = Whitebox.getInternalState(fixture, INFO_TO_INDEX_NAME);
        assertThat(internalInfoToIndex.size(), equalTo(0));
        verifyAll();
    }

    @Test
    public void addedMeteorogramInfoNotInListFirst() {
        int position = 7;
        String name = "c";
        expect(fixture.getForecastNotAvaliableImage()).andReturn(notAvaliableImage);
        expect(mainListMock.append(name, notAvaliableImage)).andReturn(position);
        expect(fixture.getShowCommand()).andReturn(showCommandMock);
        mainListMock.addCommand(showCommandMock);
        expect(fixture.getEditCommand()).andReturn(editCommandMock);
        mainListMock.addCommand(editCommandMock);
        expect(fixture.getDeleteCommand()).andReturn(deleteCommandMock);
        mainListMock.addCommand(deleteCommandMock);
        MeteorogramInfo info = new MeteorogramInfo();
        info.setName(name);
        replayAll();
        fixture.addedMeteorogramInfo(info);
        Hashtable internalInfoToIndex = Whitebox.getInternalState(fixture, INFO_TO_INDEX_NAME);
        assertThat((Integer) internalInfoToIndex.get(info), equalTo(position));
        verifyAll();
    }

    @Test
    public void addedMeteorogramInfoNotInListNotFirst() {
        int position = 7;
        String name = "c";
        expect(fixture.getForecastNotAvaliableImage()).andReturn(notAvaliableImage);
        expect(mainListMock.append(name, notAvaliableImage)).andReturn(position);
        Hashtable internalInfoToIndex = new Hashtable(1);
        MeteorogramInfo info = new MeteorogramInfo();
        internalInfoToIndex.put(info, new Integer(3));
        Whitebox.setInternalState(fixture, INFO_TO_INDEX_NAME, internalInfoToIndex);
        MeteorogramInfo newInfo = new MeteorogramInfo();
        newInfo.setName(name);
        replayAll();
        fixture.addedMeteorogramInfo(newInfo);
        internalInfoToIndex = Whitebox.getInternalState(fixture, INFO_TO_INDEX_NAME);
        assertThat((Integer) internalInfoToIndex.get(newInfo), equalTo(position));
        verifyAll();
    }

    @Test
    public void addedMeteorogramInfoInList() {
        Hashtable internalInfoToIndex = new Hashtable(1);
        MeteorogramInfo info = new MeteorogramInfo();
        int position = 33;
        internalInfoToIndex.put(info, new Integer(position));
        Whitebox.setInternalState(fixture, INFO_TO_INDEX_NAME, internalInfoToIndex);
        replayAll();
        fixture.addedMeteorogramInfo(info);
        verifyAll();
    }

    @Test
    public void addedMeteorogramInfoNull() {
        replayAll();
        fixture.addedMeteorogramInfo(null);
        verifyAll();
    }

    @Test
    public void deletedMeteorogramInfoOnlyOne() {
        Hashtable internalInfoToIndex = new Hashtable(1);
        MeteorogramInfo info = new MeteorogramInfo();
        int position = 0;
        internalInfoToIndex.put(info, new Integer(position));
        Whitebox.setInternalState(fixture, INFO_TO_INDEX_NAME, internalInfoToIndex);
        mainListMock.delete(position);
        expect(fixture.getShowCommand()).andReturn(showCommandMock);
        mainListMock.removeCommand(showCommandMock);
        expect(fixture.getEditCommand()).andReturn(editCommandMock);
        mainListMock.removeCommand(editCommandMock);
        expect(fixture.getDeleteCommand()).andReturn(deleteCommandMock);
        mainListMock.removeCommand(deleteCommandMock);
        replayAll();
        fixture.deletedMeteorogramInfo(info);
        internalInfoToIndex = Whitebox.getInternalState(fixture, INFO_TO_INDEX_NAME);
        assertThat(internalInfoToIndex.size(), equalTo(0));
        verifyAll();
    }

    @Test
    public void deletedMeteorogramInfoFisrstInList() {
        Hashtable internalInfoToIndex = new Hashtable(2);
        MeteorogramInfo info0 = new MeteorogramInfo();
        int position0 = 0;
        internalInfoToIndex.put(info0, new Integer(position0));
        MeteorogramInfo info1 = new MeteorogramInfo();
        int position1 = 1;
        internalInfoToIndex.put(info1, new Integer(position1));
        Whitebox.setInternalState(fixture, INFO_TO_INDEX_NAME, internalInfoToIndex);
        mainListMock.delete(position0);
        replayAll();
        fixture.deletedMeteorogramInfo(info0);
        internalInfoToIndex = Whitebox.getInternalState(fixture, INFO_TO_INDEX_NAME);
        assertThat(internalInfoToIndex.size(), equalTo(1));
        assertThat(internalInfoToIndex.get(info0), is(nullValue()));
        assertThat((Integer) internalInfoToIndex.get(info1), equalTo(--position1));
        verifyAll();
    }

    @Test
    public void deletedMeteorogramInfoLastInList() {
        Hashtable internalInfoToIndex = new Hashtable(2);
        MeteorogramInfo info0 = new MeteorogramInfo();
        int position0 = 0;
        internalInfoToIndex.put(info0, new Integer(position0));
        MeteorogramInfo info1 = new MeteorogramInfo();
        int position1 = 1;
        internalInfoToIndex.put(info1, new Integer(position1));
        Whitebox.setInternalState(fixture, INFO_TO_INDEX_NAME, internalInfoToIndex);
        mainListMock.delete(position1);
        replayAll();
        fixture.deletedMeteorogramInfo(info1);
        internalInfoToIndex = Whitebox.getInternalState(fixture, INFO_TO_INDEX_NAME);
        assertThat(internalInfoToIndex.size(), equalTo(1));
        assertThat(internalInfoToIndex.get(info1), is(nullValue()));
        assertThat((Integer) internalInfoToIndex.get(info0), equalTo(position0));
        verifyAll();
    }

    @Test
    public void deletedMeteorogramInfoMiddleList() {
        Hashtable internalInfoToIndex = new Hashtable(3);
        MeteorogramInfo info0 = new MeteorogramInfo();
        int position0 = 0;
        internalInfoToIndex.put(info0, new Integer(position0));
        MeteorogramInfo info1 = new MeteorogramInfo();
        int position1 = 1;
        internalInfoToIndex.put(info1, new Integer(position1));
        MeteorogramInfo info2 = new MeteorogramInfo();
        int position2 = 2;
        internalInfoToIndex.put(info2, new Integer(position2));
        Whitebox.setInternalState(fixture, INFO_TO_INDEX_NAME, internalInfoToIndex);
        mainListMock.delete(position1);
        replayAll();
        fixture.deletedMeteorogramInfo(info1);
        internalInfoToIndex = Whitebox.getInternalState(fixture, INFO_TO_INDEX_NAME);
        assertThat(internalInfoToIndex.size(), equalTo(2));
        assertThat((Integer) internalInfoToIndex.get(info0), equalTo(position0));
        assertThat(internalInfoToIndex.get(info1), is(nullValue()));
        assertThat((Integer) internalInfoToIndex.get(info2), equalTo(--position2));
        verifyAll();
    }

    @Test
    public void deleteMeteorogramInfoNotInList() {
        Hashtable internalInfoToIndex = new Hashtable(1);
        MeteorogramInfo info = new MeteorogramInfo();
        int position = 9;
        internalInfoToIndex.put(info, new Integer(position));
        Whitebox.setInternalState(fixture, INFO_TO_INDEX_NAME, internalInfoToIndex);
        replayAll();
        fixture.deletedMeteorogramInfo(new MeteorogramInfo());
        internalInfoToIndex = Whitebox.getInternalState(fixture, INFO_TO_INDEX_NAME);
        assertThat(internalInfoToIndex.size(), equalTo(1));
        assertThat((Integer) internalInfoToIndex.get(info), equalTo(position));
        verifyAll();
    }

    @Test
    public void deletedMeteorogramInfoNull() {
        replayAll();
        fixture.deletedMeteorogramInfo(null);
        Hashtable internalInfoToIndex = Whitebox.getInternalState(fixture, INFO_TO_INDEX_NAME);
        assertThat(internalInfoToIndex.size(), equalTo(0));
        verifyAll();
    }

    @Test
    public void updatedMeteorogramInfoInList() {
        Hashtable internalInfoToIndex = new Hashtable(1);
        MeteorogramInfo info = new MeteorogramInfo();
        String name = "z";
        info.setName(name);
        int position = 12;
        internalInfoToIndex.put(info, new Integer(position));
        Whitebox.setInternalState(fixture, INFO_TO_INDEX_NAME, internalInfoToIndex);
        String newName = "x";
        expect(mainListMock.getString(position)).andReturn(name);
        expect(mainListMock.getImage(position)).andReturn(avaliableImage);
        expect(fixture.getForecastNotAvaliableImage()).andReturn(notAvaliableImage);
        mainListMock.set(position, newName, notAvaliableImage);
        replayAll();
        info.setName(newName);
        fixture.updatedMeteorogramInfo(info);
        internalInfoToIndex = Whitebox.getInternalState(fixture, INFO_TO_INDEX_NAME);
        assertThat(internalInfoToIndex.size(), equalTo(1));
        info = (MeteorogramInfo) internalInfoToIndex.keys().nextElement();
        assertThat(info.getName(), equalTo(newName));
        verifyAll();
    }

    @Test
    public void updatedMeteorogramInfoInListSameName() {
        Hashtable internalInfoToIndex = new Hashtable(1);
        MeteorogramInfo info = new MeteorogramInfo();
        String name = "z";
        info.setName(name);
        int position = 2;
        internalInfoToIndex.put(info, new Integer(position));
        Whitebox.setInternalState(fixture, INFO_TO_INDEX_NAME, internalInfoToIndex);
        expect(mainListMock.getString(position)).andReturn(name);
        expect(mainListMock.getImage(position)).andReturn(avaliableImage);
        expect(fixture.getForecastNotAvaliableImage()).andReturn(notAvaliableImage);
        mainListMock.set(position, name, notAvaliableImage);
        replayAll();
        fixture.updatedMeteorogramInfo(info);
        internalInfoToIndex = Whitebox.getInternalState(fixture, INFO_TO_INDEX_NAME);
        assertThat(internalInfoToIndex.size(), equalTo(1));
        info = (MeteorogramInfo) internalInfoToIndex.keys().nextElement();
        assertThat(info.getName(), equalTo(name));
        verifyAll();
    }

    @Test
    public void updatedMeteorogramInfoInListSameImage() {
        Hashtable internalInfoToIndex = new Hashtable(1);
        MeteorogramInfo info = new MeteorogramInfo();
        String name = "gh";
        info.setName(name);
        int position = 7;
        internalInfoToIndex.put(info, new Integer(position));
        Whitebox.setInternalState(fixture, INFO_TO_INDEX_NAME, internalInfoToIndex);
        String newName = "x";
        expect(mainListMock.getString(position)).andReturn(name);
        expect(mainListMock.getImage(position)).andReturn(notAvaliableImage);
        expect(fixture.getForecastNotAvaliableImage()).andReturn(notAvaliableImage);
        mainListMock.set(position, newName, notAvaliableImage);
        replayAll();
        info.setName(newName);
        fixture.updatedMeteorogramInfo(info);
        internalInfoToIndex = Whitebox.getInternalState(fixture, INFO_TO_INDEX_NAME);
        assertThat(internalInfoToIndex.size(), equalTo(1));
        info = (MeteorogramInfo) internalInfoToIndex.keys().nextElement();
        assertThat(info.getName(), equalTo(newName));
        verifyAll();
    }

    @Test
    public void updatedMeteorogramInfoInListSameNameAndImage() {
        Hashtable internalInfoToIndex = new Hashtable(1);
        MeteorogramInfo info = new MeteorogramInfo();
        String name = "z";
        info.setName(name);
        int position = 2;
        internalInfoToIndex.put(info, new Integer(position));
        Whitebox.setInternalState(fixture, INFO_TO_INDEX_NAME, internalInfoToIndex);
        expect(mainListMock.getString(position)).andReturn(name);
        expect(mainListMock.getImage(position)).andReturn(avaliableImage);
        expect(fixture.getForecastNotAvaliableImage()).andReturn(avaliableImage);
        replayAll();
        fixture.updatedMeteorogramInfo(info);
        internalInfoToIndex = Whitebox.getInternalState(fixture, INFO_TO_INDEX_NAME);
        assertThat(internalInfoToIndex.size(), equalTo(1));
        info = (MeteorogramInfo) internalInfoToIndex.keys().nextElement();
        assertThat(info.getName(), equalTo(name));
        verifyAll();
    }

    @Test
    public void updateMeteorogramInfoNotInList() {
        MeteorogramInfo info = new MeteorogramInfo();
        String name = "f";
        info.setName(name);
        replayAll();
        fixture.updatedMeteorogramInfo(info);
        Hashtable internalInfoToIndex = Whitebox.getInternalState(fixture, INFO_TO_INDEX_NAME);
        assertThat(internalInfoToIndex.size(), equalTo(0));
        verifyAll();
    }

    @Test
    public void updateMeteorogramInfoNull() {
        replayAll();
        fixture.updatedMeteorogramInfo(null);
        Hashtable internalInfoToIndex = Whitebox.getInternalState(fixture, INFO_TO_INDEX_NAME);
        assertThat(internalInfoToIndex.size(), equalTo(0));
        verifyAll();
    }

    @Test
    public void prepareNewMode() {
        expect(fixture.getNewEditForm()).andReturn(newEditFormMock);
        newEditFormMock.setTitle(ViewController.NEW_LOCATION_TITLE);
        expect(fixture.getNameTextField()).andReturn(nameTextFieldMock);
        nameTextFieldMock.setString(ViewController.DEFAULT_NAME);
        expect(fixture.getXTextField()).andReturn(xTextFieldMock);
        xTextFieldMock.setString(Integer.toString(ViewController.DEFAULT_X));
        expect(fixture.getYTextField()).andReturn(yTextFieldMock);
        yTextFieldMock.setString(Integer.toString(ViewController.DEFAULT_Y));
        expect(fixture.getModelChoiceGroup()).andReturn(modelChoiceGroupMock);
        modelChoiceGroupMock.setSelectedIndex(ViewController.DEFAULT_MODEL.getValue(), true);
        replayAll();
        fixture.prepareNewMode();
        Boolean actualMode = Whitebox.getInternalState(fixture, NEW_MODE_NAME);
        assertThat(actualMode, is(true));
        MeteorogramInfo actual = Whitebox.getInternalState(fixture, PROCESSED_INFO_NAME);
        assertThat(actual.getName(), equalTo(ViewController.DEFAULT_NAME));
        assertThat(actual.getX(), equalTo(ViewController.DEFAULT_X));
        assertThat(actual.getY(), equalTo(ViewController.DEFAULT_Y));
        assertThat(actual.getType(), equalTo(ViewController.DEFAULT_MODEL));
        MeteorogramInfo actualNewEdit = Whitebox.getInternalState(fixture, PROCESSED_INFO_NAME);
        assertThat(actualNewEdit, equalTo(actual));
        verifyAll();
    }

    @Test
    public void prepareEditMode() {
        Hashtable internalInfoToIndex = new Hashtable(1);
        MeteorogramInfo info = new MeteorogramInfo();
        info.setName("z");
        info.setX(4);
        info.setY(3);
        info.setType(MeteorogramType.COAMPS);
        int position = 7;
        internalInfoToIndex.put(info, new Integer(position));
        Whitebox.setInternalState(fixture, INFO_TO_INDEX_NAME, internalInfoToIndex);
        expect(fixture.getNewEditForm()).andReturn(newEditFormMock);
        newEditFormMock.setTitle(ViewController.EDIT_LOCATION_TITLE);
        expect(mainListMock.getSelectedIndex()).andReturn(position);
        expect(fixture.getNameTextField()).andReturn(nameTextFieldMock);
        nameTextFieldMock.setString(info.getName());
        expect(fixture.getXTextField()).andReturn(xTextFieldMock);
        xTextFieldMock.setString(Integer.toString(info.getX()));
        expect(fixture.getYTextField()).andReturn(yTextFieldMock);
        yTextFieldMock.setString(Integer.toString(info.getY()));
        expect(fixture.getModelChoiceGroup()).andReturn(modelChoiceGroupMock);
        modelChoiceGroupMock.setSelectedIndex(info.getType().getValue(), true);
        replayAll();
        fixture.prepareEditMode();
        Boolean actualMode = Whitebox.getInternalState(fixture, NEW_MODE_NAME);
        assertThat(actualMode, is(false));
        MeteorogramInfo actual = Whitebox.getInternalState(fixture, PROCESSED_INFO_NAME);
        assertThat(actual, equalTo(info));
        verifyAll();
    }

    @Test(expected = NullPointerException.class)
    public void prepareEditModeSelectedNotInMap() {
        expect(fixture.getNewEditForm()).andReturn(newEditFormMock);
        newEditFormMock.setTitle(ViewController.EDIT_LOCATION_TITLE);
        expect(mainListMock.getSelectedIndex()).andReturn(12);
        replayAll();
        fixture.prepareEditMode();
        verifyAll();
    }

    @Test
    public void validateNewEditFormPass() throws Exception {
        String name = "ff";
        int x = 4;
        int y = 7;
        MeteorogramType type = MeteorogramType.COAMPS;
        NewEditMeteorogramInfoFormData data = new NewEditMeteorogramInfoFormData();
        data.setName(name);
        data.setX(Integer.toString(x));
        data.setY(Integer.toString(y));
        data.setType(type.getValue());
        MeteorogramInfo info = new MeteorogramInfo();
        Whitebox.setInternalState(fixture, PROCESSED_INFO_NAME, info);
        expect(fixture.getNameTextField()).andReturn(nameTextFieldMock);
        expect(nameTextFieldMock.getString()).andReturn(name);
        expect(fixture.getXTextField()).andReturn(xTextFieldMock);
        expect(xTextFieldMock.getString()).andReturn(Integer.toString(x));
        expect(fixture.getYTextField()).andReturn(yTextFieldMock);
        expect(yTextFieldMock.getString()).andReturn(Integer.toString(y));
        expect(fixture.getModelChoiceGroup()).andReturn(modelChoiceGroupMock);
        expect(modelChoiceGroupMock.getSelectedIndex()).andReturn(type.getValue());
        expectNew(MeteorogramInfoDataValidator.class).andReturn(validatorMock);
        validatorMock.validate(eq(data));
        expect(validatorMock.getErrors()).andReturn(null);
        replayAll();
        boolean validationResult = fixture.validateNewEditForm();
        assertThat(validationResult, is(true));
        String errors = Whitebox.getInternalState(fixture, VALIDATION_ERROR_NAME);
        assertThat(errors, is(nullValue()));
        MeteorogramInfo actual = Whitebox.getInternalState(fixture, PROCESSED_INFO_NAME);
        assertThat(actual, is(not(nullValue())));
        assertThat(actual.getName(), equalTo(name));
        assertThat(actual.getX(), equalTo(x));
        assertThat(actual.getY(), equalTo(y));
        assertThat(actual.getType(), equalTo(type));
        assertThat(actual, equalTo(info));
        verifyAll();
    }

    @Test
    public void validateNewEditFormFail() throws Exception {
        int wrongType = 77;
        NewEditMeteorogramInfoFormData data = new NewEditMeteorogramInfoFormData();
        data.setName(null);
        data.setX(null);
        data.setY(null);
        data.setType(wrongType);
        MeteorogramInfo info = new MeteorogramInfo();
        Whitebox.setInternalState(fixture, PROCESSED_INFO_NAME, info);
        expect(fixture.getNameTextField()).andReturn(nameTextFieldMock);
        expect(nameTextFieldMock.getString()).andReturn(null);
        expect(fixture.getXTextField()).andReturn(xTextFieldMock);
        expect(xTextFieldMock.getString()).andReturn(null);
        expect(fixture.getYTextField()).andReturn(yTextFieldMock);
        expect(yTextFieldMock.getString()).andReturn(null);
        expect(fixture.getModelChoiceGroup()).andReturn(modelChoiceGroupMock);
        expect(modelChoiceGroupMock.getSelectedIndex()).andReturn(wrongType);
        expectNew(MeteorogramInfoDataValidator.class).andReturn(validatorMock);
        validatorMock.validate(eq(data));
        expect(validatorMock.getErrors()).andReturn(new Vector());
        replayAll();
        boolean validationResult = fixture.validateNewEditForm();
        assertThat(validationResult, is(false));
        String errors = Whitebox.getInternalState(fixture, VALIDATION_ERROR_NAME);
        assertThat(errors, is(not(nullValue())));
        assertThat(errors.startsWith("Errors: "), is(true));
        MeteorogramInfo actual = Whitebox.getInternalState(fixture, PROCESSED_INFO_NAME);
        assertThat(actual, is(not(nullValue())));
        assertThat(actual, equalTo(info));
        verifyAll();
    }

    @Test
    public void handleNewAction() {
        MeteorogramInfo info = new MeteorogramInfo();
        Whitebox.setInternalState(fixture, PROCESSED_INFO_NAME, info);
        brokerMock.createMeteorogramInfo(info);
        replayAll();
        fixture.handleNewAction();
        MeteorogramInfo actual = Whitebox.getInternalState(fixture, PROCESSED_INFO_NAME);
        assertThat(actual, equalTo(info));
        verifyAll();
    }

    @Test(expected = NullPointerException.class)
    public void handleNewActionNull() {
        MeteorogramInfo info = null;
        Whitebox.setInternalState(fixture, PROCESSED_INFO_NAME, info);
        replayAll();
        fixture.handleNewAction();
        verifyAll();
    }

    @Test
    public void handleEditAction() {
        MeteorogramInfo info = new MeteorogramInfo();
        Whitebox.setInternalState(fixture, PROCESSED_INFO_NAME, info);
        brokerMock.updateMeteorogramInfo(info);
        replayAll();
        fixture.handleEditAction();
        MeteorogramInfo actual = Whitebox.getInternalState(fixture, PROCESSED_INFO_NAME);
        assertThat(actual, equalTo(info));
        verifyAll();
    }

    @Test(expected = NullPointerException.class)
    public void handleEditActionNull() {
        MeteorogramInfo info = null;
        Whitebox.setInternalState(fixture, PROCESSED_INFO_NAME, info);
        replayAll();
        fixture.handleEditAction();
        verifyAll();
    }

    @Test
    public void prepareProcessedInfo() {
        int position = 13;
        MeteorogramInfo info = new MeteorogramInfo();
        Hashtable internalInfoToIndex = new Hashtable(1);
        internalInfoToIndex.put(info, new Integer(position));
        Whitebox.setInternalState(fixture, INFO_TO_INDEX_NAME, internalInfoToIndex);
        expect(mainListMock.getSelectedIndex()).andReturn(position);
        replayAll();
        fixture.prepareProcessedInfo();
        MeteorogramInfo actual = Whitebox.getInternalState(fixture, PROCESSED_INFO_NAME);
        assertThat(actual, equalTo(info));
        verifyAll();
    }

    @Test(expected = NullPointerException.class)
    public void prepareProcessedInfoNotInMap() {
        expect(mainListMock.getSelectedIndex()).andReturn(18);
        replayAll();
        fixture.prepareProcessedInfo();
        verifyAll();
    }

    @Test
    public void handleDeleteAction() {
        MeteorogramInfo info = new MeteorogramInfo();
        Whitebox.setInternalState(fixture, PROCESSED_INFO_NAME, info);
        brokerMock.deleteMeteorogramInfo(info);
        replayAll();
        fixture.handleDeleteAction();
        verifyAll();
    }

    @Test(expected = NullPointerException.class)
    public void handleDeleteActionNull() {
        MeteorogramInfo info = null;
        Whitebox.setInternalState(fixture, PROCESSED_INFO_NAME, info);
        replayAll();
        fixture.handleDeleteAction();
        verifyAll();
    }

    @Test
    public void statusUpdate() throws Exception {
        StatusReporter task = new StatusReporter() {};
        task.addListener(fixture);
        expectPrivate(fixture, "isDownloadWaitScreenVisible").andReturn(true);
        String statusText = "Downloading... (" + Status.FINISHED.getProgress() + "% done)";
        waitScreenMock.setText(statusText);
        replayAll();
        fixture.statusUpdate(task, Status.FINISHED);
        Vector taskListeners = task.getListeners();
        assertThat(taskListeners.contains(fixture), is(false));
        verifyAll();
    }

    @Test
    public void statusUpdateWaitScreenHidden() throws Exception {
        StatusReporter task = new StatusReporter() {};
        task.addListener(fixture);
        expectPrivate(fixture, "isDownloadWaitScreenVisible").andReturn(false);
        replayAll();
        fixture.statusUpdate(task, Status.CANCELLED);
        Vector taskListeners = task.getListeners();
        assertThat(taskListeners.contains(fixture), is(false));
        verifyAll();
    }

    @Test(expected= NullPointerException.class)
    public void statusUpdateNullSource() {
        fixture.statusUpdate(null, Status.STARTED);
    }

    @Test(expected= NullPointerException.class)
    public void statusUpdateNullStatus() {
        StatusReporter task = new StatusReporter() {};
        fixture.statusUpdate(task, null);
    }
}
