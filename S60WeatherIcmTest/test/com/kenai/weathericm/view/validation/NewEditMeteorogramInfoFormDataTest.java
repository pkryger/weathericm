/*
 *  Copyright (C) 2010 Przemek
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
package com.kenai.weathericm.view.validation;

import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for {@link NewEditFormData}.
 * @author Przemek Kryger
 */
public class NewEditMeteorogramInfoFormDataTest {

    private NewEditMeteorogramInfoFormData fixture;

    @Before
    public void setUp() {
        fixture = new NewEditMeteorogramInfoFormData();
    }

    @Test
    public void getSetName() {
        String name = "x";
        fixture.setName(name);
        String actual = fixture.getName();
        assertThat(actual, equalTo(name));
    }

    @Test
    public void getX() {
        String x = "12";
        fixture.setX(x);
        String actual = fixture.getX();
        assertThat(actual, equalTo(x));
    }

        @Test
    public void getY() {
        String y = "18";
        fixture.setY(y);
        String actual = fixture.getY();
        assertThat(actual, equalTo(y));
    }

                @Test
    public void getType() {
        int type = 0;
        fixture.setType(type);
        int actual = fixture.getType();
        assertThat(actual, equalTo(type));
    }
}
