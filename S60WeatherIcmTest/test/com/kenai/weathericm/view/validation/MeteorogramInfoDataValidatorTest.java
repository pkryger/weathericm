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

import java.util.Vector;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for {@link MeteorogramInfoDataValidator}
 * @author Przemek Kryger
 */
public class MeteorogramInfoDataValidatorTest {

    private final static String ERRORS_NAME = "errors";
    private MeteorogramInfoDataValidator fixture = null;

    @Before
    public void setUp() {
        fixture = new MeteorogramInfoDataValidator();
    }

    @Test
    public void getErrors() {
        Vector actual = fixture.getErrors();
        assertThat(actual, is(nullValue()));
        Vector errors = new Vector();
        Whitebox.setInternalState(fixture, ERRORS_NAME, errors);
        actual = fixture.getErrors();
        assertThat(actual, equalTo(errors));
    }

    private void assertSingleValidationError(String error) {
        Vector errors = fixture.getErrors();
        assertThat(errors.size(), equalTo(1));
        String actual = (String) errors.get(0);
        assertThat(actual, equalTo(error));
    }

    @Test
    public void validateName() {
        fixture.validateName("name");
        Vector errors = fixture.getErrors();
        assertThat(errors, is(nullValue()));
    }

    @Test
    public void validateNameNull() {
        fixture.validateName(null);
        assertSingleValidationError("The name must be not null!");
    }

    @Test
    public void validateNameTooLong() {
        StringBuffer nameBuffer = new StringBuffer(MeteorogramInfoDataValidator.MAX_NAME_LENGTH + 1);
        for (int i = 0; i < MeteorogramInfoDataValidator.MAX_NAME_LENGTH + 1; i++) {
            nameBuffer.append('a');
        }
        String name = nameBuffer.toString();
        fixture.validateName(name);
        assertSingleValidationError("The name must be shorter or equal to "
                + MeteorogramInfoDataValidator.MAX_NAME_LENGTH + " characters!");
    }

    @Test
    public void validateNameEmpty() {
        fixture.validateName("");
        assertSingleValidationError("The name must contain at least one character!");
    }

    @Test
    public void validateX() {
        fixture.validateX(Integer.toString(MeteorogramInfoDataValidator.MIN_X));
        fixture.validateX(Integer.toString(MeteorogramInfoDataValidator.MAX_X));
        Vector errors = fixture.getErrors();
        assertThat(errors, is(nullValue()));
    }

    @Test
    public void validateXNull() {
        fixture.validateX(null);
        assertSingleValidationError("The x must be not null!");
    }

    @Test
    public void validateXToBig() {
        fixture.validateX(Integer.toString(MeteorogramInfoDataValidator.MAX_X + 1));
        assertSingleValidationError("The x must be lower or equal to "
                + MeteorogramInfoDataValidator.MAX_X + "!");
    }

    @Test
    public void validateXToSmall() {
        fixture.validateX(Integer.toString(MeteorogramInfoDataValidator.MIN_X - 1));
        assertSingleValidationError("The x must be greater or equal to "
                + MeteorogramInfoDataValidator.MIN_X + "!");
    }

    @Test
    public void validateXEmpty() {
        fixture.validateX("");
        assertSingleValidationError("The x must be an integer value!");
    }

    @Test
    public void validateXNotANumber() {
        fixture.validateX("a");
        assertSingleValidationError("The x must be an integer value!");
    }

    @Test
    public void validateY() {
        fixture.validateY(Integer.toString(MeteorogramInfoDataValidator.MIN_Y));
        fixture.validateY(Integer.toString(MeteorogramInfoDataValidator.MAX_Y));
        Vector errors = fixture.getErrors();
        assertThat(errors, is(nullValue()));
    }

    @Test
    public void validateYNull() {
        fixture.validateY(null);
        assertSingleValidationError("The y must be not null!");
    }

    @Test
    public void validateYToBig() {
        fixture.validateY(Integer.toString(MeteorogramInfoDataValidator.MAX_Y + 1));
        assertSingleValidationError("The y must be lower or equal to "
                + MeteorogramInfoDataValidator.MAX_Y + "!");
    }

    @Test
    public void validateYToSmall() {
        fixture.validateY(Integer.toString(MeteorogramInfoDataValidator.MIN_Y - 1));
        assertSingleValidationError("The y must be greater or equal to "
                + MeteorogramInfoDataValidator.MIN_Y + "!");
    }

    @Test
    public void validateYEmpty() {
        fixture.validateY("");
        assertSingleValidationError("The y must be an integer value!");
    }

    @Test
    public void validateYNotANumber() {
        fixture.validateY("a");
        assertSingleValidationError("The y must be an integer value!");
    }

    @Test
    public void validateType() {
        fixture.validateType(MeteorogramInfoDataValidator.MIN_TYPE);
        fixture.validateType(MeteorogramInfoDataValidator.MAX_TYPE);
        Vector errors = fixture.getErrors();
        assertThat(errors, is(nullValue()));
    }

    @Test
    public void validateTypeToBig() {
        fixture.validateType(MeteorogramInfoDataValidator.MAX_TYPE + 1);
        assertSingleValidationError("The model must be one of: UM or COAMPS!");
    }

    @Test
    public void validateTypeToSmall() {
        fixture.validateType(MeteorogramInfoDataValidator.MIN_TYPE - 1);
        assertSingleValidationError("The model must be one of: UM or COAMPS!");
    }

    @Test
    public void validate() {
        NewEditMeteorogramInfoFormData data = new NewEditMeteorogramInfoFormData();
        data.setName("name");
        data.setX(Integer.toString(MeteorogramInfoDataValidator.MIN_X + 1));
        data.setY(Integer.toString(MeteorogramInfoDataValidator.MIN_Y + 1));
        data.setType(MeteorogramInfoDataValidator.MIN_TYPE);
        fixture.validate(data);
        Vector errors = fixture.getErrors();
        assertThat(errors, is(nullValue()));
    }

    @Test
    public void validateBad() {
        NewEditMeteorogramInfoFormData data = new NewEditMeteorogramInfoFormData();
        data.setName(null);
        data.setX(null);
        data.setY(null);
        data.setType(MeteorogramInfoDataValidator.MIN_TYPE - 1);
        fixture.validate(data);
        Vector errors = fixture.getErrors();
        assertThat(errors, is(not(nullValue())));
        assertThat(errors.size(), equalTo(4));
    }
}
