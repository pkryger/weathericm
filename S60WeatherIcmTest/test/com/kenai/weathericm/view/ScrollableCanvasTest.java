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

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import net.sf.microlog.core.config.PropertyConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.createPartialMock;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

/**
 * Tests for {@link ScrollableCanvas} class.
 * @author Przemek Kryger
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ScrollableCanvas.class)
@SuppressStaticInitializationFor({"javax.microedition.lcdui.Displayable",
    "javax.microedition.lcdui.Canvas",
    "javax.microedition.lcdui.Graphics",
    "javax.microedition.lcdui.Image"})
public class ScrollableCanvasTest {

    @BeforeClass
    public static void setUpClass() {
        PropertyConfigurator.configure("/testMicrolog.properties");
    }
    private ScrollableCanvas fixture;
    private Image imageMock;
    private Graphics graphicsMock;
    private final static String IMAGE = "image";
    private final static String IMAGE_WIDTH = "imageWidth";
    private final static String IMAGE_HEIGHT = "imageHeight";
    private final static String T_X = "translationX";
    private final static String T_Y = "translationY";
    private final static String LAST_X = "lastPointerX";
    private final static String LAST_Y = "lastPointerY";

    @Before
    public void setUp() {
        fixture = createPartialMock(ScrollableCanvas.class,
                "repaint", "getWidth", "getHeight");
        imageMock = createMock(Image.class);
        graphicsMock = createMock(Graphics.class);
    }

    @Test
    public void getSetImage() {
        int width = 30;
        int height = 40;
        Image actual = fixture.getImage();
        assertThat(actual, is(nullValue()));
        fixture.repaint();
        expect(imageMock.getHeight()).andReturn(height);
        expect(imageMock.getWidth()).andReturn(width);
        Whitebox.setInternalState(fixture, T_X, 7);
        Whitebox.setInternalState(fixture, T_Y, 8);
        replayAll();
        fixture.setImage(imageMock);
        actual = fixture.getImage();
        assertThat(actual, is(imageMock));
        Integer actualHeight = Whitebox.getInternalState(fixture, IMAGE_HEIGHT);
        Integer actualWidth = Whitebox.getInternalState(fixture, IMAGE_WIDTH);
        Integer tX = Whitebox.getInternalState(fixture, T_X);
        Integer tY = Whitebox.getInternalState(fixture, T_Y);
        assertThat(actualHeight, equalTo(height));
        assertThat(actualWidth, equalTo(width));
        assertThat(tX, equalTo(0));
        assertThat(tY, equalTo(0));
        verifyAll();
    }

    @Test
    public void paintNotNullImage() {
        int width = 320;
        int height = 240;
        Whitebox.setInternalState(fixture, IMAGE, imageMock);
        graphicsMock.setColor(0xffffff);
        graphicsMock.fillRect(0, 0, width, height);
        expect(fixture.getWidth()).andReturn(width);
        expect(fixture.getHeight()).andReturn(height);
        graphicsMock.drawImage(imageMock, 0, 0, Graphics.TOP | Graphics.LEFT);
        replayAll();
        fixture.paint(graphicsMock);
        verifyAll();
    }

    @Test
    public void paintNullImage() {
        int width = 320;
        int height = 240;
        graphicsMock.setColor(0xffffff);
        graphicsMock.fillRect(0, 0, width, height);
        expect(fixture.getWidth()).andReturn(width);
        expect(fixture.getHeight()).andReturn(height);
        replayAll();
        fixture.paint(graphicsMock);
        verifyAll();
    }

    @Test
    public void scrollImageXBelow() {
        int width = 320;
        int height = 240;
        int imageWidth = 10 + width;
        int imageHeight = 20 + height;
        Whitebox.setInternalState(fixture, IMAGE, imageMock);
        Whitebox.setInternalState(fixture, IMAGE_WIDTH, imageWidth);
        expect(fixture.getWidth()).andReturn(width);
        expect(fixture.getHeight()).andReturn(imageHeight + 1);
        fixture.repaint();
        replayAll();
        fixture.scrollImage(-1, 0);
        Integer tX = Whitebox.getInternalState(fixture, T_X);
        assertThat(tX, equalTo(0));
        verifyAll();
    }

    @Test
    public void scrollImageXBehind() {
        int width = 320;
        int height = 240;
        int imageWidth = 10 + width;
        int imageHeight = 20 + height;
        Whitebox.setInternalState(fixture, IMAGE, imageMock);
        Whitebox.setInternalState(fixture, IMAGE_WIDTH, imageWidth);
        expect(fixture.getWidth()).andReturn(width).times(3);
        expect(fixture.getHeight()).andReturn(imageHeight + 1);
        fixture.repaint();
        replayAll();
        fixture.scrollImage(imageWidth + 1, 0);
        Integer tX = Whitebox.getInternalState(fixture, T_X);
        assertThat(tX, equalTo(imageWidth - width));
        verifyAll();
    }

    @Test
    public void scrollImageYBelow() {
        int width = 320;
        int height = 240;
        int imageWidth = 10 + width;
        int imageHeight = 20 + height;
        Whitebox.setInternalState(fixture, IMAGE, imageMock);
        Whitebox.setInternalState(fixture, IMAGE_HEIGHT, imageHeight);
        expect(fixture.getWidth()).andReturn(imageWidth + 1);
        expect(fixture.getHeight()).andReturn(height);
        fixture.repaint();
        replayAll();
        fixture.scrollImage(0, -1);
        Integer tY = Whitebox.getInternalState(fixture, T_Y);
        assertThat(tY, equalTo(0));
        verifyAll();
    }

    @Test
    public void scrollImageYBehind() {
        int width = 320;
        int height = 240;
        int imageWidth = 10 + width;
        int imageHeight = 20 + height;
        Whitebox.setInternalState(fixture, IMAGE, imageMock);
        Whitebox.setInternalState(fixture, IMAGE_HEIGHT, imageHeight);
        expect(fixture.getWidth()).andReturn(imageWidth + 1);
        expect(fixture.getHeight()).andReturn(height).times(3);
        fixture.repaint();
        replayAll();
        fixture.scrollImage(0, imageHeight + 1);
        Integer tY = Whitebox.getInternalState(fixture, T_Y);
        assertThat(tY, equalTo(imageHeight - height));
        verifyAll();
    }

    @Test
    public void scrollImageNull() {
        replayAll();
        fixture.scrollImage(7, -7);
        verifyAll();
    }

    @Test
    public void getSetScrollSpeed() {
        int speed = 10;
        replayAll();
        fixture.setScrollSpeed(speed);
        assertThat(fixture.getScrollSpeed(), equalTo(speed));
        verifyAll();
    }

    @Test(expected = IllegalArgumentException.class)
    public void setScrollSpeedInvalid() {
        replayAll();
        fixture.setScrollSpeed(0);
        verifyAll();
    }

    @Test
    public void keyRepeated() {
        int keyCode = 19;
        fixture = createPartialMock(ScrollableCanvas.class, "keyPressed");
        fixture.keyPressed(keyCode);
        replayAll();
        fixture.keyRepeated(keyCode);
        verifyAll();
    }

    @Test
    public void keyPressedUp() {
        int keyCode = 8;
        fixture = createPartialMock(ScrollableCanvas.class, "scrollImage", "getGameAction");
        Whitebox.setInternalState(fixture, IMAGE, imageMock);
        expect(fixture.getGameAction(keyCode)).andReturn(ScrollableCanvas.UP);
        fixture.scrollImage(0, -fixture.getScrollSpeed());
        replayAll();
        fixture.keyPressed(keyCode);
        verifyAll();
    }

    @Test
    public void keyPressedDown() {
        int keyCode = 8;
        fixture = createPartialMock(ScrollableCanvas.class, "scrollImage", "getGameAction");
        Whitebox.setInternalState(fixture, IMAGE, imageMock);
        expect(fixture.getGameAction(keyCode)).andReturn(ScrollableCanvas.DOWN);
        fixture.scrollImage(0, fixture.getScrollSpeed());
        replayAll();
        fixture.keyPressed(keyCode);
        verifyAll();
    }

    @Test
    public void keyPressedLeft() {
        int keyCode = 8;
        fixture = createPartialMock(ScrollableCanvas.class, "scrollImage", "getGameAction");
        Whitebox.setInternalState(fixture, IMAGE, imageMock);
        expect(fixture.getGameAction(keyCode)).andReturn(ScrollableCanvas.LEFT);
        fixture.scrollImage(-fixture.getScrollSpeed(), 0);
        replayAll();
        fixture.keyPressed(keyCode);
        verifyAll();
    }

    @Test
    public void keyPressedRight() {
        int keyCode = 8;
        fixture = createPartialMock(ScrollableCanvas.class, "scrollImage", "getGameAction");
        Whitebox.setInternalState(fixture, IMAGE, imageMock);
        expect(fixture.getGameAction(keyCode)).andReturn(ScrollableCanvas.RIGHT);
        fixture.scrollImage(fixture.getScrollSpeed(), 0);
        replayAll();
        fixture.keyPressed(keyCode);
        verifyAll();
    }

    @Test
    public void keyPressedOther() {
        int keyCode = 8;
        fixture = createPartialMock(ScrollableCanvas.class, "scrollImage", "getGameAction");
        Whitebox.setInternalState(fixture, IMAGE, imageMock);
        expect(fixture.getGameAction(keyCode)).andReturn(ScrollableCanvas.FIRE);
        replayAll();
        fixture.keyPressed(keyCode);
        verifyAll();
    }

    @Test
    public void keyPressedNull() {
        int keyCode = 8;
        replayAll();
        fixture.keyPressed(keyCode);
        verifyAll();
    }

    @Test
    public void pointerPressed() {
        int x = 5;
        int y = 6;
        replayAll();
        fixture.pointerPressed(x, y);
        Integer lastX = Whitebox.getInternalState(fixture, LAST_X);
        Integer lastY = Whitebox.getInternalState(fixture, LAST_Y);
        assertThat(lastX, equalTo(x));
        assertThat(lastY, equalTo(y));
        verifyAll();
    }

    @Test
    public void pointerRelased() {
        int x = 5;
        int y = 6;
        replayAll();
        fixture.pointerReleased(x, y);
        Integer lastX = Whitebox.getInternalState(fixture, LAST_X);
        Integer lastY = Whitebox.getInternalState(fixture, LAST_Y);
        assertThat(lastX, equalTo(-1));
        assertThat(lastY, equalTo(-1));
        verifyAll();
    }

    @Test
    public void pointerDragged() {
        int lastStoredX = 0;
        int lastStoredY = 0;
        int x = 5;
        int y = 6;
        fixture = createPartialMock(ScrollableCanvas.class, "scrollImage");
        Whitebox.setInternalState(fixture, LAST_X, lastStoredX);
        Whitebox.setInternalState(fixture, LAST_Y, lastStoredY);
        fixture.scrollImage(lastStoredX - x, lastStoredY - y);
        replayAll();
        fixture.pointerDragged(x, y);
        Integer lastX = Whitebox.getInternalState(fixture, LAST_X);
        Integer lastY = Whitebox.getInternalState(fixture, LAST_Y);
        assertThat(lastX, equalTo(x));
        assertThat(lastY, equalTo(y));
        verifyAll();
    }
}
