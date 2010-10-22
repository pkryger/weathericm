/*
 * Copyright 2008 The Microproperties project @sourceforge.net
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kenai.weathericm.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;

/**
 * This code has been copied from Microlog package and adjusted to satisfy
 * this project needs. This causes this file has a different license than the
 * rest of the project, and little bit different formatting.
 *
 * The <code>Properties</code> class is used for storing application properties.
 * This is greatly influenced by the <code>java.util.Properties</code> class
 * found in Java SE. However it is created from scratch to make it as small and
 * fast as possible for use in a Java ME environment.
 *
 * @author Johan Karlsson (johan.karlsson@jayway.se)
 * @author Przemyslaw Kryger
 */
public class Properties extends Hashtable {

    private static final long serialVersionUID = -5762797412194039268L;
    private static final char CR = '\r';
    private static final char LF = '\n';
    private static final String COMMENT_SIGN = "#";
    private static final char EQUAL_CHAR = '=';
    private static final int DEFAULT_BUFFER_SIZE = 256;
    private final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
    private final StringBuffer stringBuffer = new StringBuffer(
            2 * DEFAULT_BUFFER_SIZE);

    /**
     * Create a <code>Properties</code> object with no default
     * <code>Properties</code> object.
     */
    public Properties() {
    }

    /**
     * Expand a string that can contain references to other properties.
     * Expansions can be nested, but property names can not be built up
     * of expansions.
     *
     * @param value string to be expanded.
     * @return the expanded input string
     */
    private String expandValue(Stack nameStack, String value) {
        final String beginVar = "${";
        final String endVar = "}";

        StringBuffer retval = new StringBuffer();
        int ci = 0; // currentIndex
        int bi = 0; // beginIndex
        int ei = 0; // endIndex

        while (ci < value.length()) {
            bi = value.indexOf(beginVar, ci);
            if (bi != -1) {
                /* we have a start of a variable
                 */
                ei = value.indexOf(endVar, bi);
                if (ei != -1) {
                    /* and we have an end of a variable name so
                     * lets expand it.
                     */
                    String name = value.substring(bi + beginVar.length(), ei);
                    String defv = value.substring(bi, ei + endVar.length());
                    String val = getExpandedProperty(nameStack, name, defv);

                    retval.append(value.substring(ci, bi));
                    retval.append(val);
                    ci = ei + endVar.length();
                } else {
                    retval.append(value.substring(ci));
                    ci = value.length();
                }
            } else {  // noting to expand
                retval.append(value.substring(ci));
                ci = value.length();
            }
        }
        return retval.toString();
    }

    private String getPlainProperty(String key, String defVal) {
        String val = null;
        {
            Object obj = get(key);
            if (obj instanceof String) {
                val = (String) obj;
            } else if (obj != null) {
                val = obj.toString();
            }
        }
        if (val == null) {
            val = defVal;
        }
        return val;
    }

    private String getExpandedProperty(Stack keyStack,
            String key, String defVal) {
        if (keyStack.search(key) > 0) {
            return defVal;
        }
        keyStack.push(key);

        String val = getPlainProperty(key, defVal);
        if (val != null) {
            val = expandValue(keyStack, val);
        }

        keyStack.pop();
        return val;
    }

    /**
     * Get the property for the
     *
     * @param key
     *            the <code>key</code> to get the value for.
     * @return the property as a <code>String</code>.
     */
    public String getProperty(String key) {
        return getExpandedProperty(new Stack(), key, null);
    }

    /**
     * Get the property for the specified key. If no value is found, the
     * defaultValue is returned.
     *
     * @param key
     *            the key to search for.
     * @param defaultValue
     *            the default value to be used when no value was found.
     *
     * @return the property value.
     */
    public String getProperty(String key, String defaultValue) {
        return getExpandedProperty(new Stack(), key, defaultValue);
    }

    /**
     * Set the property, with the specified <code>key</code> to the
     * <code>value</code>.
     *
     * @param key
     *            the <code>key</code> to use.
     * @param value
     *            the <code>value</code> to set for the specified
     *            <code>key</code>.
     */
    public Object setProperty(String key, String value) {
        return put(key, value);
    }

    /**
     * List the properties to the specified <code>PrintStream</code>.
     *
     * @param printStream
     *            the <code>PrintStream</code> to list the properties into.
     */
    public void list(PrintStream printStream) {

        Enumeration keys = keys();

        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            printStream.print(key);
            printStream.print('=');
            String value = getPlainProperty(key, null);
            printStream.println(value);
        }

    }

    /**
     * Load the properties using the specified <code>inputStream</code>.
     *
     * @param inputStream
     *            the <code>InputStream</code> to read from.
     */
    public void load(InputStream inputStream) {
        String configString = readPropertyfile(inputStream);

        if (configString != null && configString.length() > 3) {
            parseConfigString(this, configString);
        }
    }

    /**
     * Save the properties to the specified <code>outputStream</code>.
     *
     * @param outputStream
     *            the <code>OutputStream</code> to save the properties to.
     * @param header
     *            the header to add. If this is <code>null</code> no header is
     *            added.
     */
    public void save(OutputStream outputStream, String header) {
        // TODO implement the save method
    }

    /**
     * Read the property file and put into the a String.
     *
     * @return a <code>String</code> that contains the content of the file.
     */
    private String readPropertyfile(InputStream inputStream) {

        String configString = null;

        // get a string with the contents of the file; configString
        try {
            int readBytes = inputStream.read(buffer);
            while (readBytes > 0) {
                String string = new String(buffer, 0, readBytes, "UTF-8");
                stringBuffer.append(string);
                readBytes = inputStream.read(buffer);
            }

            if (stringBuffer.length() > 0) {
                configString = stringBuffer.toString();
            }
        } catch (IOException e) {
            System.err.println("Failed to read property file " + e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                System.err.println("Failed to close the property file " + e);
            }
        }

        return configString;
    }

    /**
     * Parse the configuration string that was read from the file.
     *
     * @param properties
     *            the properties <code>Hashtable</code> to put the properties
     *            into.
     * @param configString
     *            the configuration string to parse.
     */
    private void parseConfigString(Hashtable properties, String configString) {

        int currentIndex = 0;
        int length = configString.length();
        int linefeedIndex = 0;

        while (currentIndex < length) {
            linefeedIndex = configString.indexOf(LF, currentIndex);

            int endIndex = configString.length();
            if (linefeedIndex != -1) {

                // Extract one line
                endIndex = linefeedIndex;

                if (configString.charAt(linefeedIndex - 1) == CR) {
                    endIndex--;
                }

            }

            String currentLine = configString.substring(currentIndex, endIndex);

            // Check if it a comment line => skip it
            if (currentLine.startsWith(COMMENT_SIGN)) {
                currentIndex = linefeedIndex + 1;
                endIndex = currentIndex;
            } else {
                int equalIndex = currentLine.indexOf(EQUAL_CHAR);

                if (equalIndex > 0) {
                    String key = currentLine.substring(0, equalIndex);
                    String value = currentLine.substring(equalIndex + 1);
                    properties.put(key, value);
                }
            }

            if (linefeedIndex != -1) {
                currentIndex = linefeedIndex + 1;
            } else {
                currentIndex = configString.length();
            }
        }
    }
}
