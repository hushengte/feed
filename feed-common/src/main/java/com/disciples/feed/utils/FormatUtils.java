package com.disciples.feed.utils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class FormatUtils {
    
    public static final class DateDeserializer extends JsonDeserializer<Date> {
        @Override
        public Date deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return FormatUtils.parseDate(jp.getText());
        }
    }
    
    public static final class DateSerializer extends JsonSerializer<Date> {
        @Override
        public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider)
                throws IOException, JsonProcessingException {
            jgen.writeString(FormatUtils.formatDate(value));
        }
    }
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    
    public static String formatDate(Date date) {
        if (date != null) {
            return DATE_FORMAT.format(date);
        }
        return null;
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
    
}
