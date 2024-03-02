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

import org.eclipse.jnosql.communication.Entry;
import org.eclipse.jnosql.communication.TypeSupplier;
import org.eclipse.jnosql.communication.Value;

import java.util.Objects;

/**
 * A Column is a tuple (pair) that consists of the name and its respective value.
 * A {@link ColumnEntity} has one or more Columns.
 */
public interface Column extends Entry {
    /**
     * Alias to {@link Value#get(Class)}
     *
     * @param type the type class
     * @param <T>  the instance type
     * @return {@link Value#get(Class)}
     * @throws NullPointerException          see {@link Value#get(Class)}
     * @throws UnsupportedOperationException see {@link Value#get(Class)}
     */
    <T> T get(Class<T> type) ;

    /**
     * Alias to {@link Value#get(TypeSupplier)}
     *
     * @param supplier {@link Value#get(Class)}
     * @param <T>      {@link Value#get(Class)}
     * @return {@link Value#get(TypeSupplier)}
     * @throws NullPointerException          see {@link Value#get(Class)}
     * @throws UnsupportedOperationException see {@link Value#get(Class)}
     */
    <T> T get(TypeSupplier<T> supplier);

    /**
     * Alias to {@link Value#get()}
     *
     * @return {@link Value#get()}
     */
    Object get();


    /**
     * Creates a column instance
     *
     * @param name  - column's name
     * @param value - column's value
     * @param <V>   the value type
     * @return a column instance
     * @throws NullPointerException when name is null
     * @see Columns
     */
    static <V> Column of(String name, V value) {
        Objects.requireNonNull(name, "name is required");
        return new DefaultColumn(name, getValue(value));
    }

    private static Value getValue(Object value) {
        if (value instanceof Value) {
            return (Value) value;
        } else {
            return Value.of(value);
        }
    }
}
