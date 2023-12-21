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

import jakarta.data.Sort;
import org.eclipse.jnosql.communication.Condition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jnosql.communication.document.DocumentQuery.select;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class DefaultDocumentQueryTest {


    private DocumentQuery query;


    @BeforeEach
    void setUp() {
        query = select().from("columnFamily").build();
    }


    @Test
    void shouldNotRemoveColumns() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            List<String> columns = query.documents();
            assertTrue(columns.isEmpty());
            columns.clear();
        });
    }


    @Test
    void shouldNotRemoveSort() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            List<Sort> sorts = query.sorts();
            assertTrue(sorts.isEmpty());
            sorts.clear();
        });
    }

    @Test
    void shouldConvertCountyBy() {
        DocumentQuery query = DocumentQuery.select().from("entity")
                .where("name").eq("predicate")
                .orderBy("name").asc().build();

        DocumentQuery countQuery = DefaultDocumentQuery.countBy(query);
        Assertions.assertNotNull(countQuery);
        assertEquals("entity", countQuery.name());
        assertEquals(0, countQuery.limit());
        assertEquals(0, countQuery.skip());
        assertTrue(countQuery.sorts().isEmpty());
        DocumentCondition condition = countQuery.condition().orElseThrow();
        Assertions.assertEquals(Condition.EQUALS, condition.condition());
    }

    @Test
    void shouldConvertExistsBy() {
        DocumentQuery query = DocumentQuery.select().from("entity")
                .where("name").eq("predicate")
                .orderBy("name").asc().build();

        DocumentQuery countQuery = DefaultDocumentQuery.existsBy(query);
        Assertions.assertNotNull(countQuery);
        assertEquals("entity", countQuery.name());
        assertEquals(1, countQuery.limit());
        assertEquals(0, countQuery.skip());
        assertTrue(countQuery.sorts().isEmpty());
        DocumentCondition condition = countQuery.condition().orElseThrow();
        Assertions.assertEquals(Condition.EQUALS, condition.condition());
    }

    @Test
    void shouldGenerateEquals(){
        DocumentQuery query = DocumentQuery.select().from("entity")
                .where("name").eq("predicate")
                .orderBy("name").asc().build();

        DocumentQuery query2 = DocumentQuery.select().from("entity")
                .where("name").eq("predicate")
                .orderBy("name").asc().build();

        assertThat(query).isEqualTo(query2);
        assertThat(query).isEqualTo(query);
        assertThat(query).isNotEqualTo("query");
    }

    @Test
    void shouldGenerateHashCode(){
        DocumentQuery query = DocumentQuery.select().from("entity")
                .where("name").eq("predicate")
                .orderBy("name").asc().build();

        DocumentQuery query2 = DocumentQuery.select().from("entity")
                .where("name").eq("predicate")
                .orderBy("name").asc().build();

        assertThat(query.hashCode()).isEqualTo(query2.hashCode());
    }
}