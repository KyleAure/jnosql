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



import org.eclipse.jnosql.communication.Value;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

/**
 * Utilitarian class to {@link Element}
 */
public final class Columns {

    private static final Predicate<Map.Entry<String, ?>> IS_VALUE_NULL = e -> Objects.nonNull(e.getValue());

    private Columns() {
    }

    /**
     * Creates a column instance
     *
     * @param name  column's name
     * @param value column's value
     * @return a column's instance
     * @throws NullPointerException when name is null
     */
    public static Element of(String name, Object value) {
        return Element.of(name, Value.of(value));
    }

    /**
     * Converts a Map to columns where: the key gonna be a column's name the value a column's value and null values
     * elements will be ignored.
     *
     * @param values map to be converted
     * @return a list of columns
     * @throws NullPointerException when values is null
     */
    public static List<Element> of(Map<String, ?> values) {
        Objects.requireNonNull(values, "values is required");
        return values.entrySet().stream()
                .filter(IS_VALUE_NULL)
                .map(e -> Element.of(e.getKey(), getValue(e.getValue())))
                .collect(toList());
    }

    private static Object getValue(Object value) {

        if (value instanceof Map) {
            List list = Columns.of((Map.class.cast(value)));
            if(list.size() == 1) {
                return list.get(0);
            }
            return list;
        }
        if (value instanceof Iterable) {
            return stream(Iterable.class.cast(value).spliterator(), false)
                    .map(Columns::getValue).collect(toList());
        }
        return value;
    }
}
