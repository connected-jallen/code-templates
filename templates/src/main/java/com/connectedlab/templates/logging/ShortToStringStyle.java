package com.connectedlab.templates.logging;

import org.apache.commons.lang3.builder.StandardToStringStyle;

/**
 * ToStringStyle.DEFAULT_STYLE with the change that it does not output null values.
 */
public class ShortToStringStyle extends StandardToStringStyle {

    public ShortToStringStyle() {
        setUseShortClassName(true);
        setUseIdentityHashCode(false);
    }

    public void append(StringBuffer buffer, String fieldName, Object value, Boolean fullDetail) {
        if (value == null) {
            return;
        }
        super.append(buffer, fieldName, value, fullDetail);
    }

    public void append(StringBuffer buffer, String fieldName, Object[] array, Boolean fullDetail) {
        if (array == null) {
            return;
        }
        super.append(buffer, fieldName, array, fullDetail);
    }
}
