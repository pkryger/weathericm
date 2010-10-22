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

import java.util.Vector;

/**
 * This is an interface for the tasks that wish to report their status via
 * {@link Status} objects.
 * @author Przemek Kryger
 */
public interface StatusReporter {

    /**
     * Registers a new listener.
     * @param listener the {@link StatusListener} to register.
     */
    void addListener(StatusListener listener);

    /**
     * Gets all the registered {@link StatusListener}s registered.
     * @return the {@link Vector} with all registered listeners.
     */
    Vector getListeners();

    /**
     * Removes the {@code listener}.
     * @param listener the {@link StatusListener} to remove.
     */
    void removeListener(StatusListener listener);

}
