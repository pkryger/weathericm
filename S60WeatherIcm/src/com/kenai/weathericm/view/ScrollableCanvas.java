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
     * Shall the horizontal scrollabr be shown?
     */
    private boolean isHorizontalScrollbar = false;
    /**
     * Shall the vertical scrollbar be shown?
     */
    private boolean isVerticalScrollbar = false;
    /**
     * The arc of the scrollbar.
     */
    private int scrollbarArc = 3;
    /**
     * The margin of the scrollbars.
     */
    private int scrollbarMargin = 2;
    /**
     * The size of the scrollbar.
     */
    private int scrollbarWide = 6;
    /**
     * The scrollbars' outline color.
     */
    private int backgroundOutlineColor = 0x000000;
    /**
     * The scrollbars' background color.
     */
    private int backgroundColor = 0x828282;
    /**
     * The scrollbats' bar color.
     */
    private int barColor = 0xf5f5f5;

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
        paintHorizontalScrollbar(g);
        paintVerticalScrollbar(g);

    }

    /**
     * Paints the horizontal scrollbar using given {@link Graphics}.
     * @param g the {@link Graphics} where to draw a bar.
     */
    protected void paintHorizontalScrollbar(Graphics g) {
        if (isHorizontalScrollbar) {
            // background
            int backgroundX = scrollbarMargin + 1;
            int backgroundY = getHeight() - scrollbarMargin - scrollbarWide - 1;
            int backgroundWidth = getWidth() - 2 * (scrollbarMargin + 1)
                    - (isVerticalScrollbar ? 2 * scrollbarMargin + scrollbarWide : 0);
            int backgroundHeight = scrollbarWide;
            g.setColor(getBackgroundOutlineColor());
            g.drawRoundRect(backgroundX, backgroundY,
                    backgroundWidth, backgroundHeight, scrollbarArc, scrollbarArc);
            g.setColor(getBackgroundColor());
            g.fillRoundRect(backgroundX + 1, backgroundY + 1,
                    backgroundWidth - 1, backgroundHeight - 1, scrollbarArc - 1, scrollbarArc - 1);
            // the bar
            int barX = scrollbarMargin + 2
                    + (int) ((double) translationX / (double) imageWidth * (backgroundWidth - 2));
            int barY = getHeight() - scrollbarMargin - scrollbarWide;
            int barWidth = (int) ((double) getWidth() / (double) imageWidth * (backgroundWidth - 2)) - 1;
            int barHeight = scrollbarWide - 1;
            g.setColor(getBarColor());
            g.fillRoundRect(barX, barY, barWidth, barHeight, scrollbarArc - 1, scrollbarArc - 1);
        }
    }

    /**
     * Paints the vertical scrollbar using given {@link Graphics}.
     * @param g the {@link Graphics} where to draw a bar.
     */
    protected void paintVerticalScrollbar(Graphics g) {
        if (isVerticalScrollbar) {
            // background
            int backgroundX = getWidth() - scrollbarMargin - scrollbarWide - 1;
            int backgroundY = scrollbarMargin + 1;
            int backgroundWidth = scrollbarWide;
            int backgroundHeight = getHeight() - 2 * (scrollbarMargin + 1)
                    - (isHorizontalScrollbar ? 2 * scrollbarMargin + scrollbarWide : 0);
            g.setColor(getBackgroundOutlineColor());
            g.drawRoundRect(backgroundX, backgroundY,
                    backgroundWidth, backgroundHeight, scrollbarArc, scrollbarArc);
            g.setColor(getBackgroundColor());
            g.fillRoundRect(backgroundX + 1, backgroundY + 1,
                    backgroundWidth - 1, backgroundHeight - 1, scrollbarArc - 1, scrollbarArc - 1);
            // the bar
            int barX = getWidth() - scrollbarMargin - scrollbarWide;
            int barY = scrollbarMargin + 2
                    + (int) ((double) translationY / (double) imageHeight * (backgroundHeight - 2));
            int barWidth = scrollbarWide - 1;
            int barHeight = (int) ((double) getHeight() / (double) imageHeight * (backgroundHeight - 2)) - 1;
            g.setColor(getBarColor());
            g.fillRoundRect(barX, barY, barWidth, barHeight, scrollbarArc - 1, scrollbarArc - 1);
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
        int screenWidth = getWidth();
        if (imageWidth > screenWidth) {
//#mdebug
            log.trace("Adding a horizontal scrollbar");
//#enddebug
            isHorizontalScrollbar = true;
        } else {
            isHorizontalScrollbar = false;
        }
        int screenHeight = getHeight();
        if (imageHeight > screenHeight) {
//#mdebug
            log.trace("Adding a vertical scrollbar");
//#enddebug
            isVerticalScrollbar = true;
        } else {
            isVerticalScrollbar = false;
        }
        if (isHorizontalScrollbar == false && isVerticalScrollbar == true) {
            if (imageWidth + scrollbarMargin + scrollbarWide + 1 > screenWidth) {
//#mdebug
                log.debug("Adding a horizontal scrollbar, since image + vertical is too big.");
//#enddebug
                isHorizontalScrollbar = true;
            }
        } else if (isHorizontalScrollbar == true && isVerticalScrollbar == false) {
            if (imageHeight + scrollbarMargin + scrollbarWide + 1 > screenHeight) {
//#mdebug
                log.debug("Adding a vertical scrollbar, sicnce image + horixontal is too big.");
//#enddebug
                isVerticalScrollbar = true;
            }
        }
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
            int screenWidth = getWidth();
            int scrollbarWidth = isVerticalScrollbar ? scrollbarMargin + scrollbarWide + 1 : 0;
            if (imageWidth > screenWidth) {
                translationX += deltaX;
                if (translationX < 0) {
                    translationX = 0;
                } else if (translationX + screenWidth > imageWidth + scrollbarWidth) {
                    translationX = imageWidth - screenWidth + scrollbarWidth;
                }
            }
            int screenHeight = getHeight();
            int scrollbarHeight = isHorizontalScrollbar ? scrollbarMargin + scrollbarWide + 1 : 0;
            if (imageHeight > screenHeight) {
                translationY += deltaY;
                if (translationY < 0) {
                    translationY = 0;
                } else if (translationY + screenHeight > imageHeight + scrollbarHeight) {
                    translationY = imageHeight - screenHeight + scrollbarHeight;
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

    /**
     * Gets the scrollbars' arc size (in pixels).
     * @return the scrollbarArc
     */
    public int getScrollbarArc() {
        return scrollbarArc;
    }

    /**
     * Sets the scrollbars' arc size (in pixels).
     * @param scrollbarArc the scrollbarArc to set
     */
    public void setScrollbarArc(int scrollbarArc) {
        this.scrollbarArc = scrollbarArc;
    }

    /**
     * Gets the margin for scrollbars (in pixels).
     * @return the scrollbarMargin
     */
    public int getScrollbarMargin() {
        return scrollbarMargin;
    }

    /**
     * Sets the margin for scrollbars (in pixels).
     * @param scrollbarMargin the scrollbarMargin to set
     */
    public void setScrollbarMargin(int scrollbarMargin) {
        this.scrollbarMargin = scrollbarMargin;
    }

    /**
     * Gets the scrollbars size (in pixels).
     * @return the scrollbarWide
     */
    public int getScrollbarWide() {
        return scrollbarWide;
    }

    /**
     * Sets the scrollbars size (in pixels).
     * @param scrollbarWide the scrollbarWide to set
     */
    public void setScrollbarWide(int scrollbarWide) {
        this.scrollbarWide = scrollbarWide;
    }

    /**
     * Gets the srollbars' outline color.
     * @return the backgroundOutlineColor
     */
    public int getBackgroundOutlineColor() {
        return backgroundOutlineColor;
    }

    /**
     * Sets the srollbars' outline color.
     * @param backgroundOutlineColor the backgroundOutlineColor to set
     */
    public void setBackgroundOutlineColor(int backgroundOutlineColor) {
        this.backgroundOutlineColor = backgroundOutlineColor;
    }

    /**
     * Gets the srollbars' background color.
     * @return the backgroundColor
     */
    public int getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the srollbars' background color.
     * @param backgroundColor the backgroundColor to set
     */
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * Gets the scrollbars' bar color.
     * @return the barColor
     */
    public int getBarColor() {
        return barColor;
    }

    /**
     * Sets the scrollbar's bar color.
     * @param barColor the barColor to set
     */
    public void setBarColor(int barColor) {
        this.barColor = barColor;
    }
}
