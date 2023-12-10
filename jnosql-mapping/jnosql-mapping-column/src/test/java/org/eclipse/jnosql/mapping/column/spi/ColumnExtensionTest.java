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
package org.eclipse.jnosql.mapping.column.spi;

import jakarta.inject.Inject;
import jakarta.nosql.column.ColumnTemplate;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.column.ColumnEntityConverter;
import org.eclipse.jnosql.mapping.column.MockProducer;
import org.eclipse.jnosql.mapping.column.entities.Person;
import org.eclipse.jnosql.mapping.column.entities.PersonRepository;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.spi.EntityMetadataExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnableAutoWeld
@AddPackages(value = {Converters.class, ColumnEntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({EntityMetadataExtension.class, ColumnExtension.class})
class ColumnExtensionTest {

    @Inject
    @Database(value = DatabaseType.COLUMN, provider = "columnRepositoryMock")
    private ColumnTemplate templateMock;

    @Inject
    private ColumnTemplate template;

    @Inject
    @Database(value = DatabaseType.COLUMN)
    private PersonRepository repository;

    @Inject
    @Database(value = DatabaseType.COLUMN, provider = "columnRepositoryMock")
    private PersonRepository repositoryMock;

    @Test
    void shouldInstance() {
        assertNotNull(template);
        assertNotNull(templateMock);
    }

    @Test
    void shouldSave() {
        Person person = template.insert(Person.builder().build());
        Person personMock = templateMock.insert(Person.builder().build());

        assertEquals("Default", person.getName());
        assertEquals("columnRepositoryMock", personMock.getName());
    }

    @Test
    void shouldInjectRepository() {
        assertNotNull(repository);
        assertNotNull(repositoryMock);
    }

    @Test
    void shouldInjectTemplate() {
        assertNotNull(templateMock);
        assertNotNull(template);
    }
}
