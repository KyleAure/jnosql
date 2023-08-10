/*
 *
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.column;

import jakarta.nosql.column.ColumnTemplate;
import org.eclipse.jnosql.communication.column.ColumnDeleteQuery;
import org.eclipse.jnosql.communication.column.ColumnQuery;

import java.util.Optional;
import java.util.stream.Stream;


/**
 * A {@link ColumnTemplate} specialization that operates on both ColumnDeleteQuery and ColumnQuery
 */
public interface JNoSQLColumnTemplate extends ColumnTemplate {

    /**
     * Deletes an entity
     *
     * @param query query to delete an entity
     * @throws NullPointerException when query is null
     */
    void delete(ColumnDeleteQuery query);

    /**
     * Finds entities from query
     *
     * @param query - query to figure out entities
     * @param <T>   the instance type
     * @return entities found by query
     * @throws NullPointerException when query is null
     */
    <T> Stream<T> select(ColumnQuery query);

    /**
     * Returns the number of items in the column family that match a specified query.
     * @param query the query
     * @return the number of documents from query
     * @throws NullPointerException when query is null
     */
    long count(ColumnQuery query);

    /**
     * Returns whether an entity that match a specified query.
     * @param query the query
     * @return true if an entity with the given query exists, false otherwise.
     * @throws NullPointerException when query it null
     */
    boolean exists(ColumnQuery query);

    /**
     * Returns a single entity from query
     *
     * @param query - query to figure out entities
     * @param <T>   the instance type
     * @return an entity on {@link Optional} or {@link Optional#empty()} when the result is not found.
     * @throws NullPointerException     when query is null
     */
    <T> Optional<T> singleResult(ColumnQuery query);

    /**
     * Returns all entities on the database
     * @param type the entity type filter
     * @return the {@link Stream}
     * @param <T> the entity type
     * @throws NullPointerException when type is null
     */
    <T> Stream<T> findAll(Class<T> type);

    /**
     * delete all entities from the database
     * @param type the entity type filter
     * @param <T> the entity type
     * @throws NullPointerException when type is null
     */
    <T> void deleteAll(Class<T> type);

}
