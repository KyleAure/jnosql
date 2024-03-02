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


import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

abstract class BaseQueryBuilder {

    protected String name;

    protected boolean negate;

    protected boolean and;

    protected ColumnCondition condition;

    protected <T> void eqImpl(T value) {
        requireNonNull(value, "value is required");
        ColumnCondition newCondition = ColumnCondition.eq(Element.of(name, value));
        appendCondition(newCondition);
    }

    protected <T> void gtImpl(T value) {
        requireNonNull(value, "value is required");
        ColumnCondition newCondition = ColumnCondition.gt(Element.of(name, value));
        appendCondition(newCondition);
    }

    protected void likeImpl(String value) {
        requireNonNull(value, "value is required");
        ColumnCondition newCondition = ColumnCondition.like(Element.of(name, value));
        appendCondition(newCondition);
    }

    protected <T> void ltImpl(T value) {
        requireNonNull(value, "value is required");
        ColumnCondition newCondition = ColumnCondition.lt(Element.of(name, value));
        appendCondition(newCondition);
    }

    protected <T> void lteImpl(T value) {
        requireNonNull(value, "value is required");
        ColumnCondition newCondition = ColumnCondition.lte(Element.of(name, value));
        appendCondition(newCondition);
    }

    protected <T> void gteImpl(T value) {
        requireNonNull(value, "value is required");
        ColumnCondition newCondition = ColumnCondition.gte(Element.of(name, value));
        appendCondition(newCondition);
    }

    protected <T> void betweenImpl(T valueA, T valueB) {
        requireNonNull(valueA, "valueA is required");
        requireNonNull(valueB, "valueB is required");
        ColumnCondition newCondition = ColumnCondition.between(Element.of(name, asList(valueA, valueB)));
        appendCondition(newCondition);
    }

    protected <T> void inImpl(Iterable<T> values) {
        requireNonNull(values, "values is required");
        ColumnCondition newCondition = ColumnCondition.in(Element.of(name, values));
        appendCondition(newCondition);
    }


    protected void appendCondition(ColumnCondition newCondition) {

        ColumnCondition columnCondition = getColumnCondition(newCondition);

        if (nonNull(condition)) {
            if (and) {
                this.condition = condition.and(columnCondition);
            } else {
                this.condition = condition.or(columnCondition);
            }
        } else {
            this.condition = columnCondition;
        }
        this.negate = false;
        this.name = null;
    }

    private ColumnCondition getColumnCondition(ColumnCondition newCondition) {
        if (negate) {
            return newCondition.negate();
        } else {
            return newCondition;
        }
    }
}
