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

import net.sf.microlog.core.config.PropertyConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for {@link Status}.
 * @author Przemek Kryger
 */
public class StatusTest {

    private Status fixture;

    @BeforeClass
    public static void setUpClass() {
        PropertyConfigurator.configure("/testMicrolog.properties");
    }

    @Before
    public void setUp() {
        fixture = new Status();
    }

    @Test
    public void getSetProgress() {
        int progress = 7;
        fixture.setProgress(progress);
        int actual = fixture.getProgress();
        assertThat(actual, equalTo(progress));
    }

    @Test
    public void getSetProgressMax() {
        int progress = 100;
        fixture.setProgress(progress);
        int actual = fixture.getProgress();
        assertThat(actual, equalTo(progress));

    }

    @Test(expected = IllegalArgumentException.class)
    public void setProgressToBig() {
        fixture.setProgress(101);
    }

    @Test
    public void getSetProgressMin() {
        int progress = 0;
        fixture.setProgress(progress);
        int actual = fixture.getProgress();
        assertThat(actual, equalTo(progress));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setProgressToSmall() {
        fixture.setProgress(-1);
    }
}
