/*
 *
 *  Copyright (c) 2022 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 *
 */
package org.eclipse.jnosql.communication.document.query;

import jakarta.nosql.Sort;
import jakarta.nosql.document.DocumentCollectionManager;
import jakarta.nosql.document.DocumentCondition;
import jakarta.nosql.document.DocumentDeleteQuery;
import jakarta.nosql.document.DocumentDeleteQuery.DocumentDeleteQueryBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

class DefaultDeleteQueryBuilder implements DocumentDeleteQueryBuilder {

    private List<String> documents = new ArrayList<>();

    private List<Sort> sorts = new ArrayList<>();

    private String documentCollection;

    private DocumentCondition condition;


    @Override
    public DocumentDeleteQueryBuilder delete(String document) {
        Objects.requireNonNull(document, "document is required");
        this.documents.add(document);
        return this;
    }

    @Override
    public DocumentDeleteQueryBuilder delete(String... documents) {
        Consumer<String> validNull = d -> requireNonNull(d, "there is null document in the query");
        Consumer<String> consume = this.documents::add;
        Stream.of(documents).forEach(validNull.andThen(consume));
        return this;
    }

    @Override
    public DocumentDeleteQueryBuilder from(String documentCollection) {
        Objects.requireNonNull(documentCollection, "documentCollection is required");
        this.documentCollection = documentCollection;
        return this;
    }

    @Override
    public DocumentDeleteQueryBuilder where(DocumentCondition condition) {
        Objects.requireNonNull(condition, "condition is required");
        this.condition = condition;
        return this;
    }

    @Override
    public DocumentDeleteQuery build() {
        if (Objects.isNull(documentCollection)) {
            throw new IllegalArgumentException("The document collection is mandatory to build");
        }
        return new DefaultDocumentDeleteQuery(documentCollection, condition, documents);
    }

    @Override
    public void delete(DocumentCollectionManager manager) {
        Objects.requireNonNull(manager, "manager is required");
        manager.delete(build());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DefaultDeleteQueryBuilder that = (DefaultDeleteQueryBuilder) o;
        return Objects.equals(documents, that.documents)
                && Objects.equals(sorts, that.sorts)
                && Objects.equals(documentCollection, that.documentCollection)
                && Objects.equals(condition, that.condition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documents, sorts, documentCollection, condition);
    }
}
