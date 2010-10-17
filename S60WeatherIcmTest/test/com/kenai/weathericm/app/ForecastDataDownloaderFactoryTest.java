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

import net.sf.microlog.core.config.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for {@link ForecastDataDownloaderFactory}.
 * @author Przemek Kryger
 */
public class ForecastDataDownloaderFactoryTest {

    @BeforeClass
    public static void setUpClass() {
        PropertyConfigurator.configure("/testMicrolog.properties");
    }

    @Test
    public void getForcedDownloader() {
        ForecastDataDownloader downloader = ForecastDataDownloaderFactory.getForcedDownloader();
        assertThat(downloader, is(notNullValue()));
        StartDateDownloader startDateDownloader =
                Whitebox.getInternalState(downloader, "startDateDownloader");
        assertThat(startDateDownloader, is(notNullValue()));
        ModelResultDownloader modelResultDownloader =
                Whitebox.getInternalState(downloader, "modelResultDownloader");
        assertThat(modelResultDownloader, is(notNullValue()));
        ModelDownloadChecker modelDownloadChecker =
                Whitebox.getInternalState(downloader, "modelDownloadChecker");
        assertThat(modelDownloadChecker, is(notNullValue()));
        assertThat(modelDownloadChecker, is(instanceOf(ForcedModelDownloadChecker.class)));
    }

    @Test
    public void getCheckedDownloader() {
        ForecastDataDownloader downloader = ForecastDataDownloaderFactory.getCheckedDownloader();
        assertThat(downloader, is(notNullValue()));
        StartDateDownloader startDateDownloader =
                Whitebox.getInternalState(downloader, "startDateDownloader");
        assertThat(startDateDownloader, is(notNullValue()));
        ModelResultDownloader modelResultDownloader =
                Whitebox.getInternalState(downloader, "modelResultDownloader");
        assertThat(modelResultDownloader, is(notNullValue()));
        ModelDownloadChecker modelDownloadChecker =
                Whitebox.getInternalState(downloader, "modelDownloadChecker");
        assertThat(modelDownloadChecker, is(notNullValue()));
        assertThat(modelDownloadChecker, is(instanceOf(SmartModelDownloadChecker.class)));
    }
}
