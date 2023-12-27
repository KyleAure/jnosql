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

import jakarta.data.exceptions.MappingException;
import jakarta.data.page.Page;
import jakarta.data.page.Pageable;
import jakarta.data.repository.PageableRepository;
import jakarta.enterprise.inject.spi.CDI;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.NoSQLPage;
import org.eclipse.jnosql.mapping.core.query.AbstractRepository;
import org.eclipse.jnosql.mapping.core.repository.DynamicQueryMethodReturn;
import org.eclipse.jnosql.mapping.graph.GraphConverter;
import org.eclipse.jnosql.mapping.graph.GraphTemplate;
import org.eclipse.jnosql.mapping.core.query.RepositoryType;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;
import org.eclipse.jnosql.mapping.core.repository.ThrowingSupplier;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Template method to {@link PageableRepository} proxy on Graph
 *
 * @param <T> the entity type
 * @param <K> the K entity
 */
abstract class AbstractGraphRepositoryProxy<T, K> implements InvocationHandler {


    protected abstract EntityMetadata entityMetadata();

    protected abstract AbstractRepository<T, K> repository();

    protected abstract Graph graph();

    protected abstract GraphConverter converter();

    protected abstract GraphTemplate template();

    protected abstract Converters converters();

    protected abstract Class<?> repositoryType();


    @Override
    public Object invoke(Object instance, Method method, Object[] args) throws Throwable {

        RepositoryType type = RepositoryType.of(method, repositoryType());
        Class<?> typeClass = entityMetadata().type();

        switch (type) {
            case DEFAULT -> {
                return unwrapInvocationTargetException(() -> method.invoke(repository(), args));
            }
            case FIND_BY -> {
                return findBy(method, args, typeClass);
            }
            case FIND_ALL -> {
                return findAll(method, typeClass, args);
            }
            case DELETE_BY -> {
                return executeDeleteMethod(method, args);
            }
            case OBJECT_METHOD -> {
                return unwrapInvocationTargetException(() -> method.invoke(this, args));
            }
            case COUNT_BY -> {
                return countBy(method, args);
            }
            case EXISTS_BY -> {
                return existsBy(method, args);
            }
            case DEFAULT_METHOD -> {
                return unwrapInvocationTargetException(() -> InvocationHandler.invokeDefault(instance, method, args));
            }
            case ORDER_BY ->
                    throw new MappingException("Eclipse JNoSQL has not support for method that has OrderBy annotation");
            case QUERY -> {
                DynamicQueryMethodReturn methodReturn = DynamicQueryMethodReturn.builder()
                        .withArgs(args)
                        .withMethod(method)
                        .withTypeClass(typeClass)
                        .withPrepareConverter(q -> template().prepare(q))
                        .withQueryConverter(q -> template().query(q)).build();
                return methodReturn.execute();
            }case CUSTOM_REPOSITORY -> {
                Object customRepository = CDI.current().select(method.getDeclaringClass()).get();
                return unwrapInvocationTargetException(() -> method.invoke(customRepository, args));
            }
            default -> {
                return Void.class;
            }
        }
    }

    private Object findAll(Method method, Class<?> typeClass, Object[] args) {

        Supplier<Stream<?>> querySupplier = () -> {

            GraphTraversal<Vertex, Vertex> traversal = graph().traversal().V().hasLabel(entityMetadata().name());

            SelectQueryConverter.updateDynamicParameter(args, traversal, entityMetadata());
            return traversal.toStream()
                    .map(converter()::toEntity);
        };

        return converter(method, typeClass, querySupplier, args);
    }

    private Object existsBy(Method method, Object[] args) {
        Long countBy = (Long) countBy(method, args);
        return countBy > 0;
    }

    private Object countBy(Method method, Object[] args) {

        Supplier<Long> querySupplier = () -> {
            GraphQueryMethod queryMethod = new GraphQueryMethod(entityMetadata(),
                    graph().traversal().V(),
                    converters(), method, args);
            return CountQueryConverter.INSTANCE.apply(queryMethod, args);
        };

        return querySupplier.get();
    }

    private Object findBy(Method method, Object[] args, Class<?> typeClass) {

        Supplier<Stream<?>> querySupplier = () -> {
            GraphQueryMethod queryMethod = new GraphQueryMethod(entityMetadata(),
                    graph().traversal().V(),
                    converters(), method, args);

            return SelectQueryConverter.INSTANCE.apply(queryMethod, args)
                    .map(converter()::toEntity);
        };

        return converter(method, typeClass, querySupplier, args);
    }

    private Object converter(Method method, Class<?> typeClass,
                             Supplier<Stream<?>> querySupplier,
                             Object[] args) {

        Supplier<Optional<?>> singleSupplier =
                DynamicReturn.toSingleResult(method).apply(querySupplier);

        Function<Pageable, Page<?>> pageFunction = p -> {
            List<?> entities = querySupplier.get().toList();
            return NoSQLPage.of(entities, p);
        };

        DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                .withClassSource(typeClass)
                .withMethodSource(method)
                .withResult(querySupplier)
                .withSingleResult(singleSupplier)
                .withPagination(DynamicReturn.findPageable(args))
                .withStreamPagination(p -> querySupplier.get())
                .withSingleResultPagination(p -> singleSupplier.get())
                .withPage(pageFunction)
                .build();

        return dynamicReturn.execute();
    }

    private Object executeDeleteMethod(Method method, Object[] args) {

        GraphQueryMethod queryMethod = new GraphQueryMethod(entityMetadata(),
                graph().traversal().V(),
                converters(), method, args);

        List<Vertex> vertices = DeleteQueryConverter.INSTANCE.apply(queryMethod);
        vertices.forEach(Vertex::remove);
        return Void.class;
    }

    private Object unwrapInvocationTargetException(ThrowingSupplier<Object> supplier) throws Throwable {
        try {
            return supplier.get();
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }
}
