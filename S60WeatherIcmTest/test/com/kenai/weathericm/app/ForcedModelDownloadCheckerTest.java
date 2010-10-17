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
package com.kenai.weathericm.app;

import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests for {@link ForcedModelDownloadChecker} class.
 * @author Przemek Kryger
 */
public class ForcedModelDownloadCheckerTest {

    ForcedModelDownloadChecker fixture = null;

    @Before
    public void setUp() {
        fixture = new ForcedModelDownloadChecker();
    }

    @Test
    public void isDownloadNeeded() {
        boolean actual = fixture.isDownloadNeeded(null, null);
        assertThat(actual, is(true));
    }
}
