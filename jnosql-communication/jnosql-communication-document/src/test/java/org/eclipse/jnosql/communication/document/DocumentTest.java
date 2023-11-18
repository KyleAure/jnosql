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

import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class DocumentTest {


    private static final Value DEFAULT_VALUE = Value.of(12);

    @Test
    public void shouldReturnNameWhenNameIsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> Document.of(null, DEFAULT_VALUE));
    }

    @Test
    public void shouldReturnNameWhenValueIsNull() {
        Document document = Document.of("Name", null);
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(document.name()).isEqualTo("Name");
            soft.assertThat(document.value().isNull()).isTrue();
        });

    }

    @Test
    public void shouldCreateAnDocumentInstance() {
        String name = "name";
        Document document = Document.of(name, DEFAULT_VALUE);
        assertNotNull(document);
        assertEquals(name, document.name());
        assertEquals(DEFAULT_VALUE, document.value());
    }

    @Test
    public void shouldBeEquals() {
        assertEquals(Document.of("name", DEFAULT_VALUE), Document.of("name", DEFAULT_VALUE));
    }

    @Test
    public void shouldReturnGetObject() {
        Value value = Value.of("text");
        Document document = Document.of("name", value);
        assertEquals(value.get(), document.get());
    }

    @Test
    public void shouldReturnGetClass() {
        Value value = Value.of("text");
        Document document = Document.of("name", value);
        assertEquals(value.get(String.class), document.get(String.class));
    }


    @Test
    public void shouldReturnGetType() {
        Value value = Value.of("text");
        Document document = Document.of("name", value);
        TypeReference<List<String>> typeReference = new TypeReference<>() {
        };
        assertEquals(value.get(typeReference), document.get(typeReference));
    }
}