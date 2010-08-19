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

import com.kenai.weathericm.util.Status;
import net.sf.microlog.core.config.PropertyConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for {@link ForecastDataDownloaderCancellableTask}.
 * @author Przemek Kryger
 */
public class ForecastDataDownloaderCancellableTaskTest {

    @BeforeClass
    public static void setUpClass() {
        PropertyConfigurator.configure("/testMicrolog.properties");
    }
    private ForecastDataDownloaderCancellableTask fixture;

    @Before
    public void setUp() {
        fixture = new ForecastDataDownloaderCancellableTask();
    }

    @Test
    public void hasFailedNullStatus() {
        boolean actual = fixture.hasFailed();
        assertThat(actual, is(true));
    }

    @Test
    public void hasFailedNotFinished() {
        fixture.lastStatus = Status.CANCELLED;
        boolean actual = fixture.hasFailed();
        assertThat(actual, is(true));
    }

    @Test
    public void hasFailedFinished() {
        fixture.lastStatus = Status.FINISHED;
        boolean actual = fixture.hasFailed();
        assertThat(actual, is(false));
    }
}
