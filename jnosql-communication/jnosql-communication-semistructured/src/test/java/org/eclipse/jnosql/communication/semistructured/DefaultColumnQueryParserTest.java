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

import org.eclipse.jnosql.communication.Condition;
import jakarta.data.exceptions.NonUniqueResultException;
import org.eclipse.jnosql.communication.QueryException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultColumnQueryParserTest {
    
    private final ColumnQueryParser parser = new ColumnQueryParser();


    private final ColumnManager manager = Mockito.mock(ColumnManager.class);

    @Test
    void shouldReturnNPEWhenThereIsNullParameter() {
        assertThrows(NullPointerException.class, () -> parser.query(null, manager, ColumnObserverParser.EMPTY));
        assertThrows(NullPointerException.class, () -> parser.query("select * from God", null, ColumnObserverParser.EMPTY));
    }

    @Test
    void shouldReturnErrorWhenHasInvalidQuery() {
        assertThrows(QueryException.class, () -> parser.query("inva", manager, ColumnObserverParser.EMPTY));
        assertThrows(QueryException.class, () -> parser.query("invalid", manager, ColumnObserverParser.EMPTY));
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"select * from God"})
    void shouldReturnParserQuery(String query) {
        ArgumentCaptor<ColumnQuery> captor = ArgumentCaptor.forClass(ColumnQuery.class);
        parser.query(query, manager, ColumnObserverParser.EMPTY);
        Mockito.verify(manager).select(captor.capture());
        ColumnQuery columnQuery = captor.getValue();

        assertTrue(columnQuery.columns().isEmpty());
        assertTrue(columnQuery.sorts().isEmpty());
        assertEquals(0L, columnQuery.limit());
        assertEquals(0L, columnQuery.skip());
        assertEquals("God", columnQuery.name());
        assertFalse(columnQuery.condition().isPresent());

    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"delete from God"})
    void shouldReturnParserQuery1(String query) {
        ArgumentCaptor<ColumnDeleteQuery> captor = ArgumentCaptor.forClass(ColumnDeleteQuery.class);
        parser.query(query, manager, ColumnObserverParser.EMPTY);
        Mockito.verify(manager).delete(captor.capture());
        ColumnDeleteQuery columnDeleteQuery = captor.getValue();

        assertTrue(columnDeleteQuery.columns().isEmpty());
        assertEquals("God", columnDeleteQuery.name());
        assertFalse(columnDeleteQuery.condition().isPresent());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"insert God (name = \"Diana\")"})
    void shouldReturnParserQuery2(String query) {
        ArgumentCaptor<ColumnEntity> captor = ArgumentCaptor.forClass(ColumnEntity.class);
        parser.query(query, manager, ColumnObserverParser.EMPTY);
        Mockito.verify(manager).insert(captor.capture());
        ColumnEntity entity = captor.getValue();


        assertEquals("God", entity.name());
        assertEquals(Column.of("name", "Diana"), entity.find("name").get());
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"update God (name = \"Diana\")"})
    void shouldReturnParserQuery3(String query) {
        ArgumentCaptor<ColumnEntity> captor = ArgumentCaptor.forClass(ColumnEntity.class);
        parser.query(query, manager, ColumnObserverParser.EMPTY);
        Mockito.verify(manager).update(captor.capture());
        ColumnEntity entity = captor.getValue();


        assertEquals("God", entity.name());
        assertEquals(Column.of("name", "Diana"), entity.find("name").get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"delete from God where age = @age"})
    void shouldExecutePrepareStatement(String query) {
        ArgumentCaptor<ColumnDeleteQuery> captor = ArgumentCaptor.forClass(ColumnDeleteQuery.class);

        ColumnPreparedStatement prepare = parser.prepare(query, manager, ColumnObserverParser.EMPTY);
        prepare.bind("age", 12);
        prepare.result();
        Mockito.verify(manager).delete(captor.capture());
        ColumnDeleteQuery columnDeleteQuery = captor.getValue();
        ColumnCondition columnCondition = columnDeleteQuery.condition().get();
        Column column = columnCondition.column();
        assertEquals(Condition.EQUALS, columnCondition.condition());
        assertEquals("age", column.name());
        assertEquals(12, column.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"insert God (name = @name)"})
    void shouldExecutePrepareStatement1(String query) {
        ArgumentCaptor<ColumnEntity> captor = ArgumentCaptor.forClass(ColumnEntity.class);
        ColumnPreparedStatement prepare = parser.prepare(query, manager, ColumnObserverParser.EMPTY);
        prepare.bind("name", "Diana");
        prepare.result();
        Mockito.verify(manager).insert(captor.capture());
        ColumnEntity entity = captor.getValue();
        assertEquals("God", entity.name());
        assertEquals(Column.of("name", "Diana"), entity.find("name").get());

    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"select  * from God where age = @age"})
    void shouldExecutePrepareStatement2(String query) {
        ArgumentCaptor<ColumnQuery> captor = ArgumentCaptor.forClass(ColumnQuery.class);

        ColumnPreparedStatement prepare = parser.prepare(query, manager, ColumnObserverParser.EMPTY);
        prepare.bind("age", 12);
        prepare.result();
        Mockito.verify(manager).select(captor.capture());
        ColumnQuery columnQuery = captor.getValue();
        ColumnCondition columnCondition = columnQuery.condition().get();
        Column column = columnCondition.column();
        assertEquals(Condition.EQUALS, columnCondition.condition());
        assertEquals("age", column.name());
        assertEquals(12, column.get());
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"update God (name = @name)"})
    void shouldExecutePrepareStatement3(String query) {
        ArgumentCaptor<ColumnEntity> captor = ArgumentCaptor.forClass(ColumnEntity.class);
        ColumnPreparedStatement prepare = parser.prepare(query, manager, ColumnObserverParser.EMPTY);
        prepare.bind("name", "Diana");
        prepare.result();
        Mockito.verify(manager).update(captor.capture());
        ColumnEntity entity = captor.getValue();
        assertEquals("God", entity.name());
        assertEquals(Column.of("name", "Diana"), entity.find("name").get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"select  * from God where age = @age"})
    void shouldSingleResult(String query) {
        ArgumentCaptor<ColumnQuery> captor = ArgumentCaptor.forClass(ColumnQuery.class);

        Mockito.when(manager.select(Mockito.any(ColumnQuery.class)))
                .thenReturn(Stream.of(Mockito.mock(ColumnEntity.class)));

        ColumnPreparedStatement prepare = parser.prepare(query, manager, ColumnObserverParser.EMPTY);
        prepare.bind("age", 12);
        final Optional<ColumnEntity> result = prepare.singleResult();
        Mockito.verify(manager).select(captor.capture());
        ColumnQuery columnQuery = captor.getValue();
        ColumnCondition columnCondition = columnQuery.condition().get();
        Column column = columnCondition.column();
        assertEquals(Condition.EQUALS, columnCondition.condition());
        assertEquals("age", column.name());
        assertEquals(12, column.get());
        assertTrue(result.isPresent());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"select  * from God where age = @age"})
    void shouldReturnEmptySingleResult(String query) {
        ArgumentCaptor<ColumnQuery> captor = ArgumentCaptor.forClass(ColumnQuery.class);

        Mockito.when(manager.select(Mockito.any(ColumnQuery.class)))
                .thenReturn(Stream.empty());

        ColumnPreparedStatement prepare = parser.prepare(query, manager, ColumnObserverParser.EMPTY);
        prepare.bind("age", 12);
        final Optional<ColumnEntity> result = prepare.singleResult();
        Mockito.verify(manager).select(captor.capture());
        ColumnQuery columnQuery = captor.getValue();
        ColumnCondition columnCondition = columnQuery.condition().get();
        Column column = columnCondition.column();
        assertEquals(Condition.EQUALS, columnCondition.condition());
        assertEquals("age", column.name());
        assertEquals(12, column.get());
        assertFalse(result.isPresent());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"select  * from God where age = @age"})
    void shouldReturnErrorSingleResult(String query) {
        ArgumentCaptor<ColumnQuery> captor = ArgumentCaptor.forClass(ColumnQuery.class);

        Mockito.when(manager.select(Mockito.any(ColumnQuery.class)))
                .thenReturn(Stream.of(Mockito.mock(ColumnEntity.class), Mockito.mock(ColumnEntity.class)));

        ColumnPreparedStatement prepare = parser.prepare(query, manager, ColumnObserverParser.EMPTY);
        prepare.bind("age", 12);
       assertThrows(NonUniqueResultException.class, prepare::singleResult);
    }

}
