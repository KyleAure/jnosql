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
package org.eclipse.jnosql.mapping.semistructured.configuration;

import org.eclipse.jnosql.communication.Settings;
import org.eclipse.jnosql.communication.column.ColumnConfiguration;
import org.eclipse.jnosql.communication.column.ColumnDeleteQuery;
import org.eclipse.jnosql.communication.column.ColumnEntity;
import org.eclipse.jnosql.communication.column.ColumnManager;
import org.eclipse.jnosql.communication.column.ColumnManagerFactory;
import org.eclipse.jnosql.communication.column.ColumnQuery;

import java.time.Duration;
import java.util.stream.Stream;

public class ColumnConfigurationMock2 implements ColumnConfiguration {


    @Override
    public ColumnManagerFactoryMock apply(Settings settings) {
        return new ColumnManagerFactoryMock(settings);
    }


    public record ColumnManagerFactoryMock(Settings settings) implements ColumnManagerFactory {

        @Override
            public ColumnManagerMock apply(String database) {
                return new ColumnManagerMock(database);
            }

            @Override
            public void close() {

            }
        }

    public record ColumnManagerMock(String name) implements ColumnManager {

            @Override
            public ColumnEntity insert(ColumnEntity entity) {
                return null;
            }

            @Override
            public ColumnEntity update(ColumnEntity entity) {
                return null;
            }

            @Override
            public Iterable<ColumnEntity> update(Iterable<ColumnEntity> entities) {
                return null;
            }

            @Override
            public ColumnEntity insert(ColumnEntity entity, Duration ttl) {
                return null;
            }

            @Override
            public Iterable<ColumnEntity> insert(Iterable<ColumnEntity> entities) {
                return null;
            }

            @Override
            public Iterable<ColumnEntity> insert(Iterable<ColumnEntity> entities, Duration ttl) {
                return null;
            }

            @Override
            public void delete(ColumnDeleteQuery query) {

            }

            @Override
            public Stream<ColumnEntity> select(ColumnQuery query) {
                return null;
            }

            @Override
            public long count(String columnFamily) {
                return 0;
            }

            @Override
            public void close() {

            }
        }
}
