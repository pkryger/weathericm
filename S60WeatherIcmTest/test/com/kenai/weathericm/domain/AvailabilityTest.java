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
package com.kenai.weathericm.domain;

import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for {@link Availability} class.
 * @author Przemek Kryger
 */
public class AvailabilityTest {

    @Test
    public void unique() {
        assertThat(Availability.AVAILABLE, is(not(Availability.NOT_AVAILABLE)));
        assertThat(Availability.AVAILABLE, is(not(Availability.AVAILABLE_OLD)));
        assertThat(Availability.AVAILABLE_OLD, is(not(Availability.NOT_AVAILABLE)));
        assertThat(Availability.AVAILABLE.getValue(), is(not(Availability.NOT_AVAILABLE.getValue())));
        assertThat(Availability.AVAILABLE.getValue(), is(not(Availability.AVAILABLE_OLD.getValue())));
        assertThat(Availability.AVAILABLE_OLD.getValue(), is(not(Availability.NOT_AVAILABLE.getValue())));
    }

    @Test
    public void displayString() {
        assertThat(Availability.AVAILABLE.toString(), equalTo("Available [" + Availability.AVAILABLE_VALUE + "]"));
        assertThat(Availability.NOT_AVAILABLE.toString(), equalTo("Not Available [" + Availability.NOT_AVAILABLE_VALUE + "]"));
        assertThat(Availability.AVAILABLE_OLD.toString(), equalTo("Available Old [" + Availability.AVAILABLE_OLD_VALUE + "]"));
    }
}
