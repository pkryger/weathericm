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

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
//#mdebug
import net.sf.microlog.core.Logger;
import net.sf.microlog.core.LoggerFactory;
//#enddebug

/**
 * This class is responsible for displaying the given image and allows for
 * srolling ig both horizontaly and verticaly.
 * The code bases on the one by Alessandro La Rosa as can be found on
 * {@
 * @author Przemek Kryger
 */
public class ScrollableCanvas extends Canvas {
//#mdebug

    /**
     * The class logger.
     */
    private final static Logger log = LoggerFactory.getLogger(ScrollableCanvas.class);
//#enddebug
    /**
     * The image to be drawn.
     */
    private Image image;
    /**
     * The drawing image height.
     */
    private int imageHeight = 0;
    /**
     * The drawing image width.
     */
    private int imageWidth = 0;
    /**
     * How many pixels to shift on X axis.
     */
    private int translationX = 0;
    /**
     * How many pixels to shift on Y axis.
     */
    private int translationY = 0;
    /**
     * The x coordinate where the touch bagun.
     */
    private int lastPointerX = -1;
    /**
     * The y coordinate where the touch bagun.
     */
    private int lastPointerY = -1;
    /**
     * How many pixels shall scroll at single click.
     */
    private int scrollSpeed = 1;

    /**
     * Paints the attached image. Uses the {@value #translationX}
     * and {@value #translationY} to correctly drow the attached image.
     * When the {@value #image} is {@code null} blank screen is drawn.
     */
    public void paint(Graphics g) {
        g.setColor(0xffffff);
        g.fillRect(0, 0, getWidth(), getHeight());
        if (image != null) {
            g.drawImage(image, -translationX, -translationY, Graphics.TOP | Graphics.LEFT);
        }
        
    }

    /**
     * Called when a key is pressed.
     */
    protected void keyPressed(int keyCode) {
        if (image != null) {
            int action = getGameAction(keyCode);
            switch (action) {
                case UP:
                    scrollImage(0, -getScrollSpeed());
                    break;
                case DOWN:
                    scrollImage(0, getScrollSpeed());
                    break;
                case LEFT:
                    scrollImage(-getScrollSpeed(), 0);
                    break;
                case RIGHT:
                    scrollImage(getScrollSpeed(), 0);
                    break;
//#mdebug
                default:
                    log.debug("No action for the keyCode = " + keyCode
                            + ", action = " + action);
//#enddebug
            }
        } else {
//#mdebug
            log.trace("Image is null - no scrolling is needed.");
//#enddebug
        }
    }

    /**
     * Called when a key is released.
     */
    protected void keyReleased(int keyCode) {
    }

    /**
     * Called when a key is repeated (held down).
     */
    protected void keyRepeated(int keyCode) {
        keyPressed(keyCode);
    }

    /**
     * Called when the pointer is dragged.
     */
    protected void pointerDragged(int x, int y) {
        scrollImage(lastPointerX - x, lastPointerY - y);
        lastPointerX = x;
        lastPointerY = y;
    }

    /**
     * Called when the pointer is pressed.
     */
    protected void pointerPressed(int x, int y) {
        lastPointerX = x;
        lastPointerY = y;
    }

    /**
     * Called when the pointer is released.
     */
    protected void pointerReleased(int x, int y) {
        lastPointerX = -1;
        lastPointerY = -1;
    }

    /**
     * Gets the current image to be handled by this canvas.
     * @return the image
     */
    public Image getImage() {
        return image;
    }

    /**
     * Sets the image to be handled by this canvas.
     * @param image the image to set
     */
    public void setImage(Image image) {
        this.image = image;
        if (this.image != null) {
            imageHeight = this.image.getHeight();
            imageWidth = this.image.getWidth();
        } else {
//#mdebug
            log.debug("Setting null image");
//#enddebug
            imageHeight = 0;
            imageWidth = 0;
        }
        translationX = 0;
        translationY = 0;
        repaint();
    }

    /**
     * Scrolls the image by the given {@code deltaX} and {@code deltaY}
     * @param deltaX how many pixels scroll image horizontaly.
     * @param deltaY how many pixels scroll image vertiacaly.
     */
    protected void scrollImage(int deltaX, int deltaY) {
        if (image != null) {
//#mdebug
            log.trace("Scrolling image by: x = " + deltaX
                    + ", y = " + deltaY);
//#enddebug
            if (imageWidth > getWidth()) {
                translationX += deltaX;
                if (translationX < 0) {
                    translationX = 0;
                } else if (translationX + getWidth() > imageWidth) {
                    translationX = imageWidth - getWidth();
                }
            }
            if (imageHeight > getHeight()) {
                translationY += deltaY;
                if (translationY < 0) {
                    translationY = 0;
                } else if (translationY + getHeight() > imageHeight) {
                    translationY = imageHeight - getHeight();
                }
            }
            repaint();
        } else {
//#mdebug
            log.warn("Scrolling a null image!");
//#enddebug
        }
    }

    /**
     * Gets the amount of pixels the image shall be scrolled on a sinlge click.
     * @return the scrollSpeed
     */
    public int getScrollSpeed() {
        return scrollSpeed;
    }

    /**
     * Gets the amount of pixels the image shall be scrolled on a sinlge click.
     * @param scrollSpeed the scrollSpeed to set
     */
    public void setScrollSpeed(int scrollSpeed) {
        if (scrollSpeed > 0) {
            this.scrollSpeed = scrollSpeed;
        } else {
//#mdebug
            log.error("Cannot set scroll speed to " + scrollSpeed);
//#enddebug
            throw new IllegalArgumentException("Speed must be higher than 0!");
        }
    }
}
