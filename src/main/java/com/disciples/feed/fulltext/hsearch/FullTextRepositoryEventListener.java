package com.disciples.feed.fulltext.hsearch;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.transaction.Synchronization;

import org.hibernate.search.backend.TransactionContext;
import org.hibernate.search.backend.spi.Work;
import org.hibernate.search.backend.spi.WorkType;
import org.hibernate.search.backend.spi.Worker;
import org.hibernate.search.engine.integration.impl.ExtendedSearchIntegrator;
import org.hibernate.search.engine.spi.AbstractDocumentBuilder;
import org.hibernate.search.engine.spi.EntityIndexBinding;
import org.hibernate.search.spi.IndexedTypeIdentifier;
import org.hibernate.search.spi.IndexingMode;
import org.hibernate.search.spi.SearchIntegrator;
import org.springframework.util.Assert;

import com.disciples.feed.entity.Identifiable;
import com.disciples.feed.rest.AbstractRepositoryListener;

public class FullTextRepositoryEventListener extends AbstractRepositoryListener<Identifiable<?>> implements TransactionContext {
    
    private ExtendedSearchIntegrator searchIntegrator;
    private boolean eventsDisabled;
    private Set<String> indexedTypeNames;
    
    public FullTextRepositoryEventListener(SearchIntegrator searchIntegrator) {
        Assert.notNull(searchIntegrator, "SearchIntegrator is required.");
        this.searchIntegrator = searchIntegrator.unwrap(ExtendedSearchIntegrator.class);
        this.eventsDisabled = eventsDisabled(searchIntegrator);
        this.indexedTypeNames = new HashSet<>();
        for (IndexedTypeIdentifier indexedTypeIdentifier : searchIntegrator.getIndexedTypeIdentifiers()) {
            indexedTypeNames.add(indexedTypeIdentifier.getName());
        }
    }

    public static boolean eventsDisabled(SearchIntegrator extendedIntegrator) {
        if (extendedIntegrator.getIndexingMode() == IndexingMode.EVENT) {
            return extendedIntegrator.getIndexBindings().size() == 0;
        }
		if (extendedIntegrator.getIndexingMode() == IndexingMode.MANUAL) {
            return true;
        }
        return false;
    }
    
    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        return indexedTypeNames.contains(sourceType.getName());
    }

    @Override
    protected void onAfterCreate(Identifiable<?> entity) {
        if (eventsDisabled) return;
        if (getDocumentBuilder(entity.getClass()) != null) {
            processWork(entity, entity.getId(), WorkType.ADD);
        }
    }

    @Override
    protected void onAfterUpdate(Identifiable<?> entity) {
        if (eventsDisabled) return;
        if (getDocumentBuilder(entity.getClass()) != null) {
            processWork(entity, entity.getId(), WorkType.UPDATE);
        }
    }

    @Override
    protected void onAfterDelete(Identifiable<?> entity) {
        if (eventsDisabled) return;
        if (getDocumentBuilder(entity.getClass()) != null) {
            processWork(entity, entity.getId(), WorkType.DELETE);
        }
    }
    
    protected void processWork(Object entity, Serializable id, WorkType workType) {
        Work work = new Work(entity, id, workType);
        Worker worker = searchIntegrator.getWorker();
        worker.performWork(work, this);
        worker.flushWorks(this);
    }
    
    protected AbstractDocumentBuilder getDocumentBuilder(Class<?> docClass) {
        IndexedTypeIdentifier type = searchIntegrator.getIndexBindings().keyFromPojoType(docClass);
        EntityIndexBinding entityIndexBinding = searchIntegrator.getIndexBinding(type);
        if (entityIndexBinding != null) {
            return entityIndexBinding.getDocumentBuilder();
        } else {
            return searchIntegrator.getDocumentBuilderContainedEntity(type);
        }
    }

    @Override
    public boolean isTransactionInProgress() {
        return true;
    }

    @Override
    public Object getTransactionIdentifier() {
        return this;
    }

    @Override
    public void registerSynchronization(Synchronization synchronization) {
    }

}
