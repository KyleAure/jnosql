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
package org.eclipse.jnosql.communication.semistructured;


import jakarta.data.Direction;
import jakarta.data.Sort;
import org.eclipse.jnosql.communication.Params;
import org.eclipse.jnosql.communication.QueryException;
import org.eclipse.jnosql.communication.query.SelectQuery;
import org.eclipse.jnosql.communication.query.SelectQueryConverter;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public final class SelectQueryParser implements BiFunction<SelectQuery, ColumnObserverParser, ColumnQueryParams> {


    public SelectQueryParser() {
    }

    Stream<ColumnEntity> query(String query, ColumnManager manager, ColumnObserverParser observer) {

        ColumnQuery columnQuery = getColumnQuery(query, observer);
        return manager.select(columnQuery);
    }


    ColumnPreparedStatement prepare(String query, ColumnManager manager, ColumnObserverParser observer) {

        Params params = Params.newParams();
        SelectQueryConverter converter = new SelectQueryConverter();
        SelectQuery selectQuery = converter.apply(query);

        ColumnQuery columnQuery = getColumnQuery(params, selectQuery, observer);
        return ColumnPreparedStatement.select(columnQuery, params, query, manager);
    }


    @Override
    public ColumnQueryParams apply(SelectQuery selectQuery, ColumnObserverParser observer) {
        Objects.requireNonNull(selectQuery, "selectQuery is required");
        Objects.requireNonNull(observer, "observer is required");

        Params params = Params.newParams();
        ColumnQuery columnQuery = getColumnQuery(params, selectQuery, observer);
        return new ColumnQueryParams(columnQuery, params);
    }


    private ColumnQuery getColumnQuery(String query, ColumnObserverParser observer) {

        SelectQueryConverter converter = new SelectQueryConverter();
        SelectQuery selectQuery = converter.apply(query);
        String columnFamily = observer.fireEntity(selectQuery.entity());
        long limit = selectQuery.limit();
        long skip = selectQuery.skip();
        List<String> columns = selectQuery.fields().stream()
                .map(f -> observer.fireField(columnFamily, f))
                .collect(Collectors.toList());
        List<Sort<?>> sorts = selectQuery.orderBy().stream().map(s -> toSort(s, observer, columnFamily))
                .collect(toList());

        Params params = Params.newParams();
        ColumnCondition condition = selectQuery.where()
                .map(c -> Conditions.getCondition(c, params, observer, columnFamily)).orElse(null);

        if (params.isNotEmpty()) {
            throw new QueryException("To run a query with a parameter use a PrepareStatement instead.");
        }
        return new DefaultColumnQuery(limit, skip, columnFamily, columns, sorts, condition);
    }

    private ColumnQuery getColumnQuery(Params params, SelectQuery selectQuery, ColumnObserverParser observer) {

        String columnFamily = observer.fireEntity(selectQuery.entity());
        long limit = selectQuery.limit();
        long skip = selectQuery.skip();
        List<String> columns = selectQuery.fields().stream()
                .map(f -> observer.fireField(columnFamily, f))
                .collect(Collectors.toList());

        List<Sort<?>> sorts = selectQuery.orderBy().stream().map(s -> toSort(s, observer, columnFamily)).collect(toList());
        ColumnCondition condition = selectQuery.where()
                .map(c -> Conditions.getCondition(c, params, observer, columnFamily))
                .orElse(null);

        return new DefaultColumnQuery(limit, skip, columnFamily, columns, sorts, condition);
    }

    private Sort<?> toSort(Sort<?> sort, ColumnObserverParser observer, String entity) {
        return Sort.of(observer.fireField(entity, sort.property()),
                sort.isAscending()? Direction.ASC: Direction.DESC, false);
    }


}