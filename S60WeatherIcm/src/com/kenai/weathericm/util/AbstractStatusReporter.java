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

import java.util.Enumeration;
import java.util.Vector;
//#mdebug
import net.sf.microlog.core.Logger;
import net.sf.microlog.core.LoggerFactory;
//#enddebug

/**
 * This is the default abstract implementation of {@link StatusReporter}.
 * @author Przemek Kryger
 */
public abstract class AbstractStatusReporter implements StatusReporter {

//#mdebug
    /**
     * The class logger.
     */
    private final static Logger log = LoggerFactory.getLogger(AbstractStatusReporter.class);
//#enddebug
    /**
     * All the registered {@link StatusListener}s instances.
     */
    private final Vector listeners = new Vector();

    /**
     * Registers a new listener.
     * @param listener the {@link StatusListener} to register.
     */
    public void addListener(StatusListener listener) {
        if (listener == null) {
            return;
        }
        synchronized (listeners) {
            if (!listeners.contains(listener)) {
                listeners.addElement(listener);
            }
        }

//#mdebug
        log.debug(this + ": Listener added");
//#enddebug
    }

    /**
     * Notifies all the registered listeners on the {@code status}.
     * @param status the {@link Status} to be reported.
     */
    protected void fireStatusUpdate(Status status) {
//#mdebug
        log.trace(this + ": Notifying on status update = " + status);
//#enddebug
        synchronized (listeners) {
            Enumeration e = getListeners().elements();
            while (e.hasMoreElements()) {
                StatusListener listener = (StatusListener) e.nextElement();
                listener.statusUpdate(this, status);
            }
        }
    }

    /**
     * Gets all the registered {@link StatusListener}s registered.
     * @return the {@link Vector} with all registered listeners.
     */
    public Vector getListeners() {
        Vector retValue = null;
        synchronized (listeners) {
            retValue = new Vector(listeners.size());
            Enumeration listenersEnum = listeners.elements();
            while (listenersEnum.hasMoreElements()) {
                Object listener = listenersEnum.nextElement();
                retValue.addElement(listener);
            }
        }
        return retValue;
    }

    /**
     * Removes the {@code listener}.
     * @param listener the {@link StatusListener} to remove.
     */
    public void removeListener(StatusListener listener) {
        if (listener != null) {
            synchronized (listeners) {
                if (listeners.contains(listener)) {
                    listeners.removeElement(listener);
                }
            }
        }
//#mdebug
        log.debug(this + ": Listener removed");
//#enddebug
    }
}
