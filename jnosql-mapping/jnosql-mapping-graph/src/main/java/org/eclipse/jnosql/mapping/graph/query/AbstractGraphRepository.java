/*
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
 */
package org.eclipse.jnosql.mapping.graph.query;

import jakarta.data.page.Page;
import jakarta.data.page.Pageable;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.PageableRepository;
import org.eclipse.jnosql.mapping.core.NoSQLPage;
import org.eclipse.jnosql.mapping.graph.GraphTemplate;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.eclipse.jnosql.mapping.IdNotFoundException.KEY_NOT_FOUND_EXCEPTION_SUPPLIER;

abstract class AbstractGraphRepository<T, K> implements PageableRepository<T, K>, CrudRepository<T, K> {

    protected abstract GraphTemplate getTemplate();

    protected abstract EntityMetadata getEntityMetadata();


    @Override
    public <S extends T> S save(S entity) {
        Objects.requireNonNull(entity, "Entity is required");
        Object id = getIdField().read(entity);
        if (nonNull(id) && existsById((K) id)) {
            return getTemplate().update(entity);
        } else {
            return getTemplate().insert(entity);
        }
    }

    @Override
    public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
        requireNonNull(entities, "entities is required");
        return StreamSupport.stream(entities.spliterator(), false).map(this::save).collect(toList());
    }

    @Override
    public void deleteById(K id) {
        requireNonNull(id, "is is required");
        getTemplate().delete(id);
    }

    @Override
    public void deleteByIdIn(Iterable<K> ids) {
        requireNonNull(ids, "ids is required");
        ids.forEach(this::deleteById);
    }

    @Override
    public Optional<T> findById(K id) {
        requireNonNull(id, "id is required");
        return getTemplate().find(id);
    }

    @Override
    public Stream<T> findByIdIn(Iterable<K> ids) {
        requireNonNull(ids, "ids is required");
        return stream(ids.spliterator(), false)
                .flatMap(optionalToStream());
    }

    @Override
    public boolean existsById(K id) {
        return findById(id).isPresent();
    }

    @Override
    public long count() {
        return getTemplate().count(getEntityMetadata().name());
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        Objects.requireNonNull(pageable, "pageable is required");
        EntityMetadata metadata = getEntityMetadata();

        List<T> entities = getTemplate().traversalVertex()
                .hasLabel(metadata.type())
                .skip(NoSQLPage.skip(pageable))
                .limit(pageable.size())
                .<T>result()
                .toList();

        return NoSQLPage.of(entities, pageable);
    }

    @Override
    public Stream<T> findAll() {
        return getTemplate()
                .<T>findAll((Class<T>) getEntityMetadata().type());
    }

    @Override
    public void delete(T entity) {
        requireNonNull(entity, "entity is required");
        EntityMetadata metadata = getEntityMetadata();
        FieldMetadata id = metadata.id().orElseThrow(KEY_NOT_FOUND_EXCEPTION_SUPPLIER);
        Object value = id.read(entity);
        getTemplate().delete(value);
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        requireNonNull(entities, "entities is required");
        EntityMetadata metadata = getEntityMetadata();
        FieldMetadata id = metadata.id().orElseThrow(KEY_NOT_FOUND_EXCEPTION_SUPPLIER);
        List<Object> ids = stream(entities.spliterator(), false).map(id::read).toList();
        getTemplate().delete(ids);
    }

    @Override
    public void deleteAll() {
        getTemplate().deleteAll(getEntityMetadata().type());
    }

    @Override
    public <S extends T> S insert(S entity) {
        Objects.requireNonNull(entity, "entity is required");
        return getTemplate().insert(entity);
    }

    @Override
    public <S extends T> Iterable<S> insertAll(Iterable<S> entities) {
        Objects.requireNonNull(entities, "entities is required");
        return getTemplate().insert(entities);
    }

    @Override
    public boolean update(T entity) {
        Objects.requireNonNull(entity, "entity is required");
        return getTemplate().update(entity) != null;
    }

    @Override
    public int updateAll(Iterable<T> entities) {
        Objects.requireNonNull(entities, "entities is required");
        getTemplate().update(entities);
        return (int) StreamSupport.stream(entities.spliterator(), false).count();
    }

    private FieldMetadata getIdField() {
        return getEntityMetadata().id().orElseThrow(KEY_NOT_FOUND_EXCEPTION_SUPPLIER);
    }

    private Function optionalToStream() {
        return id -> {
            Optional entity = this.findById((K) id);
            return entity.isPresent() ? Stream.of(entity.get()) : Stream.empty();
        };
    }
}
