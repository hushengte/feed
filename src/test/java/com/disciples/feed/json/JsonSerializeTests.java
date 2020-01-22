package com.disciples.feed.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.Map;

import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

import com.disciples.feed.BaseEntity;
import com.disciples.feed.util.FormatUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class JsonSerializeTests {
    
    private ObjectMapper objectMapper;
    
    @Before
    public void setUp() {
        this.objectMapper = new ObjectMapper();
    }
    
    String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            fail();
        }
        return null;
    }
    
    <T> T parseJson(String json, Class<T> resultType) {
        try {
            return objectMapper.readValue(json, resultType);
        } catch (Exception e) {
            fail();
        }
        return null;
    }

    @Test
    public void testJsonSerializer_Deserializer() {
        Date now = new Date();
        JsonSerial data = new JsonSerial(1, now);
        
        //serialize
        String json = toJson(data);
        Map<?, ?> dataMap = parseJson(json, Map.class);
        String date = (String)dataMap.get("date");
        assertEquals(FormatUtils.formatDate(now), date);
        
        //deserialize
        JsonSerial js = parseJson(json, JsonSerial.class);
        assertEquals(FormatUtils.parseDate(date), js.getDate());
    }
    
    @Test
    public void testHibernateProxyModule() {
        objectMapper.registerModule(new HibernateProxyModule());
        
        HibernateEntity entity = new HibernateEntity();
        entity.setId(1);
        entity.setItems(Mockito.mock(PersistentCollection.class));
        
        //test persistent collection
        String json = toJson(entity);
        Map<?, ?> dataMap = parseJson(json, Map.class);
        Integer id = (Integer)dataMap.get("id");
        assertEquals(entity.getId(), id);
        assertNull(dataMap.get("items"));
        
        //test set id
        testHibernateProxySetId(HibernateEntity.class);
        testHibernateProxySetId(JsonSerial.class);
    }
    
    private void testHibernateProxySetId(Class<?> entityClass) {
        Integer proxyId = 1;
        HibernateProxy proxyEntity = Mockito.mock(HibernateProxy.class);
        LazyInitializer lazyInitializer = Mockito.mock(LazyInitializer.class);
        BDDMockito.given(proxyEntity.getHibernateLazyInitializer()).willReturn(lazyInitializer);
        BDDMockito.given(lazyInitializer.getIdentifier()).willReturn(proxyId);
        BDDMockito.given(lazyInitializer.getPersistentClass()).willReturn(entityClass);

        String json = toJson(proxyEntity);
        Map<?, ?> dataMap = parseJson(json, Map.class);
        assertEquals(proxyId, dataMap.get("id"));
        Mockito.verify(proxyEntity).getHibernateLazyInitializer();
        Mockito.verify(lazyInitializer).getIdentifier();
        Mockito.verify(lazyInitializer).getPersistentClass();
        Mockito.verifyNoMoreInteractions(proxyEntity);
        Mockito.verifyNoMoreInteractions(lazyInitializer);
    }
    
    public static class JsonSerial {
        private Integer id;
        private Date date;

        public JsonSerial() {}

        public JsonSerial(Integer id, Date date) {
            this.id = id;
            this.date = date;
        }
        public Integer getId() {
            return id;
        }
        public void setId(Integer id) {
            this.id = id;
        }
        @JsonSerialize(using = DateSerializer.class)
        public Date getDate() {
            return date;
        }
        @JsonDeserialize(using = DateDeserializer.class)
        public void setDate(Date date) {
            this.date = date;
        }
    }
    
    @SuppressWarnings("serial")
    public static class HibernateEntity extends BaseEntity {
        
        private PersistentCollection items;

        public HibernateEntity() {}

        public PersistentCollection getItems() {
            return items;
        }
        public void setItems(PersistentCollection items) {
            this.items = items;
        }
    }
    
}
