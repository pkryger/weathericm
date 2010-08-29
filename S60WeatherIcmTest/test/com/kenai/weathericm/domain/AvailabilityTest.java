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
        assertThat(Availability.AVALIABLE, is(not(Availability.NOT_AVALIABLE)));
        assertThat(Availability.AVALIABLE, is(not(Availability.AVALIABLE_OLD)));
        assertThat(Availability.AVALIABLE_OLD, is(not(Availability.NOT_AVALIABLE)));
    }

    @Test
    public void displayString() {
        assertThat(Availability.AVALIABLE.toString(), equalTo("Available"));
        assertThat(Availability.NOT_AVALIABLE.toString(), equalTo("Not Available"));
        assertThat(Availability.AVALIABLE_OLD.toString(), equalTo("Available Old"));
    }
}
