/* Copyright (C) 2005 Tim Fennell
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the license with this software. If not,
 * it can be found online at http://www.fsf.org/licensing/licenses/lgpl.html
 */
package net.sourceforge.stripes.validation;

import net.sourceforge.stripes.controller.StripesFilter;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * <p>Provides a mechanism for creating localizable error messages for presentation to the user.
 * Uses ResourceBundles to provide the localization of the error message.  Messages may contain one
 * or more &quot;replacement parameters &quot;.  Two replacement parameters are provided by default,
 * they are the field name and field value, and are indices 0 and 1 respectively.  To use
 * replacement parameters a message must contain the replacement token {#} where # is the numeric
 * index of the replacement parameter.</p>
 *
 * <p>For example, to construct an error message with one additional replacement parameter which is
 * the action the user was trying to perform, you might have a properties file entry like:</p>
 *
 * <pre>/action/MyAction.myErrorMessage={1} is not a valid {0} when trying to {2}</pre>
 *
 * <p>At runtime this might get replaced out to result in an error message for the user that looks
 * like &quot;<em>Fixed</em> is not a valid <em>status</em> when trying to create a new
 * <em>bug</em>&quot;.</p>
 *
 * <p>One last point of interest is where the user friendly field name comes from. Firstly an
 * attempt is made to look up the localized name in the applicable resource bundle using the
 * String <em>actionPath.fieldName</em> where actionPath is the action of the form in the JSP
 * (or equally, the path given in the @UrlBinding annotation in the ActionBean class),
 * and fieldName is the name of the field on the form.</p>
 *
 * @see java.text.MessageFormat
 * @see java.util.ResourceBundle
 */
public class LocalizableError extends SimpleError {
    private String messageKey;

    /**
     * Creates a new LocalizableError with the message key provided, and optionally zero or more
     * replacement parameters to use in the message.  It should be noted that the replacement
     * parameters provided here can be referenced in the error message <b>starting with number
     * 2</b>.
     *
     * @param messageKey a key to lookup a message in the resource bundle
     * @param parameter one or more replacement parameters to insert into the message
     */
    public LocalizableError(String messageKey, Object... parameter) {
        super(parameter);
        this.messageKey = messageKey;

    }

    /**
     * Method responsible for using the information supplied to the error object to find a
     * message template. In this class this is done simply by looking up the resource
     * corresponding to the messageKey supplied in the constructor.
     */
    @Override
    protected String getMessageTemplate(Locale locale) {
        ResourceBundle bundle = StripesFilter.getConfiguration().
                getLocalizationBundleFactory().getErrorMessageBundle(locale);

        return bundle.getString(messageKey);
    }

    /** Generated equals method that compares each field and super.equals(). */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final LocalizableError that = (LocalizableError) o;

        if (messageKey != null ? !messageKey.equals(that.messageKey) : that.messageKey != null) {
            return false;
        }

        return true;
    }

    /** Generated hashCode method. */
    public int hashCode() {
        int result = super.hashCode();
        result = 29 * result + (messageKey != null ? messageKey.hashCode() : 0);
        return result;
    }
}
