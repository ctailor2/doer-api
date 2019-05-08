package com.doerapispring.storage;

import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.relational.core.mapping.event.BeforeSaveEvent;

@Configuration
public class SpringDataJdbcConfiguration {
    @Bean
    public ApplicationListener<BeforeSaveEvent> setIdFromUuidForNewRecord() {
//        Goofy workaround for https://jira.spring.io/browse/DATAJDBC-281
        return event -> {
            Object entity = event.getEntity();
            if (entity instanceof UuidIdentifiable) {
                UuidIdentifiable uuidIdentifiable = (UuidIdentifiable) entity;
                if (uuidIdentifiable.uuid != null) {
                    uuidIdentifiable.id = uuidIdentifiable.uuid;
                }
            }
        };
    }
}
