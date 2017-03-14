package com.disciples.feed.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FormatUtils {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public static String formatDate(Date date) {
        return date != null ? DATE_FORMAT.format(date) : null;
    }
    
    public static String formatDateTime(Date date) {
        return date != null ? DATETIME_FORMAT.format(date) : null;
    }
    
    public static Date parseDate(String dateStr) {
        if (dateStr != null) {
            try {
                return DATE_FORMAT.parse(dateStr);
            } catch (ParseException e) {
                return new Date();
            }
        }
        return null;
    }
    
    public static Date parseDateTime(String dateStr) {
        if (dateStr != null) {
            try {
                return DATETIME_FORMAT.parse(dateStr);
            } catch (ParseException e) {
                return new Date();
            }
        }
        return null;
    }
    
}
