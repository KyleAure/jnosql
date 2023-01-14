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
package org.eclipse.jnosql.mapping.document.query;


import org.eclipse.jnosql.communication.Params;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.document.Document;
import org.eclipse.jnosql.communication.document.DocumentCondition;
import org.eclipse.jnosql.communication.document.DocumentQuery;
import org.eclipse.jnosql.communication.document.DocumentQueryParams;
import org.eclipse.jnosql.communication.document.SelectQueryParser;
import org.eclipse.jnosql.communication.query.SelectQuery;
import org.eclipse.jnosql.mapping.Converters;
import org.eclipse.jnosql.mapping.reflection.EntityMetadata;
import org.eclipse.jnosql.mapping.reflection.EntitiesMetadata;
import org.eclipse.jnosql.mapping.test.entities.Person;
import org.eclipse.jnosql.mapping.test.jupiter.CDIExtension;
import org.eclipse.jnosql.mapping.util.ParamsBinder;
import org.eclipse.jnosql.communication.query.method.SelectMethodProvider;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@CDIExtension
class ParamsBinderTest {


    @Inject
    private EntitiesMetadata mappings;

    @Inject
    private Converters converters;

    private ParamsBinder paramsBinder;

    @Test
    public void shouldConvert() {

        Method method = Stream.of(PersonRepository.class.getMethods())
                .filter(m -> m.getName().equals("findByAge")).findFirst().get();
        EntityMetadata entityMetadata = mappings.get(Person.class);
        RepositoryDocumentObserverParser parser = new RepositoryDocumentObserverParser(entityMetadata);
        paramsBinder = new ParamsBinder(entityMetadata, converters);

        SelectMethodProvider selectMethodFactory = SelectMethodProvider.INSTANCE;
        SelectQuery selectQuery = selectMethodFactory.apply(method, entityMetadata.getName());
        SelectQueryParser queryParser = new SelectQueryParser();
        DocumentQueryParams documentQueryParams = queryParser.apply(selectQuery, parser);
        Params params = documentQueryParams.params();
        Object[] args = {10};
        paramsBinder.bind(params, args, method);
        DocumentQuery query = documentQueryParams.query();
        DocumentCondition columnCondition = query.condition().get();
        Value value = columnCondition.document().value();
        assertEquals(10, value.get());

    }

    @Test
    public void shouldConvert2() {

        Method method = Stream.of(PersonRepository.class.getMethods())
                .filter(m -> m.getName().equals("findByAgeAndName")).findFirst().get();
        EntityMetadata entityMetadata = mappings.get(Person.class);
        RepositoryDocumentObserverParser parser = new RepositoryDocumentObserverParser(entityMetadata);
        paramsBinder = new ParamsBinder(entityMetadata, converters);

        SelectMethodProvider selectMethodFactory = SelectMethodProvider.INSTANCE;
        SelectQuery selectQuery = selectMethodFactory.apply(method, entityMetadata.getName());
        SelectQueryParser queryParser = new SelectQueryParser();
        DocumentQueryParams queryParams = queryParser.apply(selectQuery, parser);
        Params params = queryParams.params();
        paramsBinder.bind(params, new Object[]{10L, "Ada"}, method);
        DocumentQuery query = queryParams.query();
        DocumentCondition columnCondition = query.condition().get();
        List<DocumentCondition> conditions = columnCondition.document().get(new TypeReference<>() {
        });
        List<Object> values = conditions.stream().map(DocumentCondition::document)
                .map(Document::value)
                .map(Value::get).collect(Collectors.toList());
        assertEquals(10, values.get(0));
        assertEquals("Ada", values.get(1));

    }


    interface PersonRepository {

        List<Person> findByAge(Integer age);

        List<Person> findByAgeAndName(Long age, String name);
    }


}