package com.disciples.feed.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class FormatUtils {
    
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    public static String formatDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        return date != null ? formatter.format(date) : null;
    }
    
    public static String formatDateTime(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATETIME_FORMAT);
        return date != null ? formatter.format(date) : null;
    }
    
    public static Date parseDate(String dateStr) {
        if (dateStr != null) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
                return formatter.parse(dateStr);
            } catch (ParseException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return null;
    }
    
    public static Date parseDateTime(String dateStr) {
        if (dateStr != null) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat(DATETIME_FORMAT);
                return formatter.parse(dateStr);
            } catch (ParseException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return null;
    }
    
}
