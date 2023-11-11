/*
 *
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.communication.document;


import jakarta.data.repository.Sort;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;

class DefaultDocumentQuery implements DocumentQuery {

    private final long limit;

    private final long skip;

    private final String documentCollection;

    private final DocumentCondition condition;

    private final List<Sort> sorts;

    private final List<String> documents;

    DefaultDocumentQuery(long limit, long skip, String documentCollection,
                         List<String> documents, List<Sort> sorts, DocumentCondition condition) {

        this.limit = limit;
        this.skip = skip;
        this.documentCollection = documentCollection;
        this.condition = ofNullable(condition).map(DocumentCondition::readOnly).orElse(null);
        this.sorts = sorts;
        this.documents = documents;
    }

    @Override
    public long limit() {
        return limit;
    }

    @Override
    public long skip() {
        return skip;
    }

    @Override
    public String name() {
        return documentCollection;
    }

    @Override
    public Optional<DocumentCondition> condition() {
        return ofNullable(condition);
    }

    @Override
    public List<Sort> sorts() {
        return unmodifiableList(sorts);
    }

    @Override
    public List<String> documents() {
        return unmodifiableList(documents);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DocumentQuery that)) {
            return false;
        }
        return limit == that.limit() &&
                skip == that.skip() &&
                Objects.equals(documentCollection, that.name()) &&
                Objects.equals(condition, that.condition().orElse(null)) &&
                Objects.equals(sorts, that.sorts()) &&
                Objects.equals(documents, that.documents());
    }

    @Override
    public int hashCode() {
        return Objects.hash(limit, skip, documentCollection, condition, sorts, documents);
    }

    @Override
    public String toString() {
        return "DocumentQuery{" + "maxResult=" + limit +
                ", firstResult=" + skip +
                ", documentCollection='" + documentCollection + '\'' +
                ", condition=" + condition +
                ", sorts=" + sorts +
                ", documents=" + documents +
                '}';
    }

    static DocumentQuery countBy(DocumentQuery query) {
        return new DefaultDocumentQuery(0, 0, query.name(), query.documents(),
                Collections.emptyList(), query.condition().orElse(null));
    }
    static DocumentQuery existsBy(DocumentQuery query) {
        return new DefaultDocumentQuery(1, 0, query.name(), query.documents(),
                Collections.emptyList(), query.condition().orElse(null));
    }
}
