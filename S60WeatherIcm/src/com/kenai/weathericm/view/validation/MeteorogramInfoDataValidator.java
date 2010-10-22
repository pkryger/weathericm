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
package com.kenai.weathericm.view.validation;

import java.util.Vector;
//#mdebug
import net.sf.microlog.core.Logger;
import net.sf.microlog.core.LoggerFactory;
//#enddebug
import com.kenai.weathericm.domain.MeteorogramInfo;
import com.kenai.weathericm.domain.MeteorogramType;

/**
 * This is a validator for {@link NewEditMeteorogramInfoFormData} objects. It exposes
 * two methods: {@value #validate(NewEditMeteorogramInfoFormData)} to perform validation
 * and {@value #getErrorrs()} to get validation errors.
 * @author Przemek Kryger
 */
public class MeteorogramInfoDataValidator {

    /**
     * The maximum length for name.
     */
    public final static int MAX_NAME_LENGTH = 30;
    /**
     * The max value of X.
     */
    public final static int MAX_X = MeteorogramInfo.MAX_X;
    /**
     * The min value of X.
     */
    public final static int MIN_X = 0;
    /**
     * The max value of Y.
     */
    public final static int MAX_Y = MeteorogramInfo.MAX_Y;
    /**
     * The min value of Y.
     */
    public final static int MIN_Y = 0;
    /**
     * The max value of type.
     */
    public final static int MAX_TYPE = MeteorogramType.getAllTypes().size() - 1;
    /**
     * The min value of type.
     */
    public final static int MIN_TYPE = 0;
//#mdebug
    /**
     * Logger for the class.
     */
    private final static Logger log = LoggerFactory.getLogger(MeteorogramInfoDataValidator.class);
//#enddebug
    /**
     * Holder for errors.
     */
    private Vector errors = null;

    /**
     * Return all the errors that the last {@value #validate(NewEditMeteorogramInfoFormData)}
     * call produced. In case there was no errors {@code null} value is returned.
     * @return the {@link Vector} with errors
     */
    public Vector getErrors() {
        Vector retValue = errors;
        errors = null;
        return retValue;
    }

    /**
     * Performs validation of passed {@code data}. When it finishes, the
     * {@value #getErrors()} may be used to get the validation errors.
     * @param data the {@link NewEditMeteorogramInfoFormData} to be validated.
     */
    public void validate(NewEditMeteorogramInfoFormData data) {
//#mdebug
        log.debug("Validating...");
//#enddebug
        validateName(data.getName());
        validateX(data.getX());
        validateY(data.getY());
        validateType(data.getType());
//#mdebug
        log.debug("Found " + (errors == null ? "no" : Integer.toString(errors.size())) + " errors");
//#enddebug
    }

    /**
     * Validates if the passed {@code name} doesn't break constraints and appends
     * all found errors into {@value #errors}.
     * @param name the {@link String} to be validated.
     */
    protected void validateName(String name) {
        if (name == null) {
//#mdebug
            log.error("Name is null!");
//#enddebug
            appendError("The name must be not null!");
        } else if (name.length() == 0) {
            appendError("The name must contain at least one character!");
        } else if (name.length() > MAX_NAME_LENGTH) {
            appendError("The name must be shorter or equal to "
                    + MAX_NAME_LENGTH + " characters!");
        } else {
//#mdebug
            log.debug("The: " + name + " is OK for name");
//#enddebug
        }
    }

    /**
     * Validates if the passed {@code x} doesn't break constraints and appends
     * all found errors into {@value #errors}.
     * @param x the {@link String} to be validated.
     */
    protected void validateX(String x) {
        if (x == null) {
//#mdebug
            log.error("X is null!");
//#enddebug
            appendError("The x must be not null!");
        } else {
            try {
                int value = Integer.parseInt(x);
                if (value < MIN_X) {
                    appendError("The x must be greater or equal to " + MIN_X + "!");
                } else if (value > MAX_X) {
                    appendError("The x must be lower or equal to " + MAX_X + "!");
                }
//#mdebug
                log.debug("The " + x + " is OK for x");
//#enddebug
            } catch (NumberFormatException ex) {
//#mdebug
                log.warn("The " + x + " is not an int value!", ex);
//#enddebug
                appendError("The x must be an integer value!");
            }
        }
    }

    /**
     * Validates if the passed {@code y} doesn't break constraints and appends
     * all found errors into {@value #errors}.
     * @param y the {@link String} to be validated.

     */
    protected void validateY(String y) {
        if (y == null) {
//#mdebug
            log.error("X is null!");
//#enddebug
            appendError("The y must be not null!");
        } else {
            try {
                int value = Integer.parseInt(y);
                if (value < MIN_Y) {
                    appendError("The y must be greater or equal to " + MIN_Y + "!");
                } else if (value > MAX_Y) {
                    appendError("The y must be lower or equal to " + MAX_Y + "!");
                }
//#mdebug
                log.debug("The " + y + " is OK for y");
//#enddebug
            } catch (NumberFormatException ex) {
//#mdebug
                log.warn("The " + y + " is not an int value!", ex);
//#enddebug
                appendError("The y must be an integer value!");
            }
        }
    }

    /**
     * Validates if the passed {@code type} doesn't break constraints and appends
     * all found errors into {@value #errors}.
     * @param type the {@code int} to be validated.
     */
    protected void validateType(int type) {
        if (type < MIN_TYPE || type > MAX_TYPE) {
//#mdebug
            log.error("The " + type + " is not good for type!");
//#enddebug
            appendError("The model must be one of: UM or COAMPS!");
        } else {
//#mdebug
            log.debug("The " + type + " is OK for type");
//#enddebug
        }
    }

    /**
     * Appends {@code error} to {@value #errors}. If {@value #errors}
     * is {@code null} then new {@link Vector} is created.
     * @param error the {@link String} to be added.
     */
    private void appendError(String error) {
        if (errors == null) {
            errors = new Vector(1);
        }
        errors.addElement(error);
    }
}
