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
package org.eclipse.jnosql.mapping.graph.query;

import jakarta.data.repository.Page;
import jakarta.data.repository.Pageable;
import jakarta.data.repository.PageableRepository;
import jakarta.data.repository.Slice;
import jakarta.data.repository.Sort;
import org.eclipse.jnosql.mapping.Converters;
import org.eclipse.jnosql.mapping.graph.GraphConverter;
import org.eclipse.jnosql.mapping.graph.entities.Person;
import org.eclipse.jnosql.mapping.reflection.EntitiesMetadata;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.eclipse.jnosql.mapping.graph.GraphTemplate;
import org.eclipse.jnosql.mapping.test.jupiter.CDIExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.inject.Inject;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@CDIExtension
public class GraphRepositoryProxyPageableTest {

    private GraphTemplate template;

    @Inject
    private GraphTemplate graphTemplate;
    @Inject
    private EntitiesMetadata entities;

    @Inject
    private Graph graph;

    @Inject
    private GraphConverter converter;

    @Inject
    private Converters converters;

    private PersonRepository personRepository;

    @BeforeEach
    public void setUp() {

        graph.traversal().V().toList().forEach(Vertex::remove);
        graph.traversal().E().toList().forEach(Edge::remove);


        this.template = Mockito.mock(GraphTemplate.class);

        GraphRepositoryProxy personHandler = new GraphRepositoryProxy(template,
                entities, PersonRepository.class, graph, converter, converters);


        when(template.insert(any(Person.class))).thenReturn(Person.builder().build());
        when(template.update(any(Person.class))).thenReturn(Person.builder().build());

        when(template.traversalVertex()).thenReturn(graphTemplate.traversalVertex());
        personRepository = (PersonRepository) Proxy.newProxyInstance(PersonRepository.class.getClassLoader(),
                new Class[]{PersonRepository.class},
                personHandler);
    }

    @AfterEach
    public void after() {
        graph.traversal().V().toList().forEach(Vertex::remove);
        graph.traversal().E().toList().forEach(Edge::remove);

    }

    @Test
    public void shouldFindByNameAndAge() {

        graph.addVertex(T.label, "Person", "name", "name", "age", 20);
        graph.addVertex(T.label, "Person", "name", "name", "age", 20);
        graph.addVertex(T.label, "Person", "name", "name", "age", 20);
        graph.addVertex(T.label, "Person", "name", "name", "age", 20);
        graph.addVertex(T.label, "Person", "name", "name", "age", 20);
        graph.addVertex(T.label, "Person", "name", "name", "age", 20);

        List<Person> people = personRepository.findByNameAndAge("name", 20, Pageable.ofPage(1).size(2));
        assertEquals(2, people.size());

    }

    @Test
    public void shouldFindByAgeAndName() {

        graph.addVertex(T.label, "Person", "name", "name", "age", 20);
        graph.addVertex(T.label, "Person", "name", "name", "age", 20);
        graph.addVertex(T.label, "Person", "name", "name", "age", 20);
        graph.addVertex(T.label, "Person", "name", "name", "age", 20);

        Set<Person> people = personRepository.findByAgeAndName(20, "name", Pageable.ofPage(1).size(3));
        assertEquals(3, people.size());

    }

    @Test
    public void shouldFindByAge() {

        graph.addVertex(T.label, "Person", "name", "name", "age", 20);
        Optional<Person> person = personRepository.findByAge(20, Pageable.ofPage(1).size(2));
        assertTrue(person.isPresent());
    }


    @Test
    public void shouldFindAll() {
        graph.addVertex(T.label, "Person", "name", "name", "age", 20);
        graph.addVertex(T.label, "Person", "name", "name", "age", 20);
        List<Person> people = personRepository.findAll(Pageable.ofPage(2).size(1)).content();
        assertFalse(people.isEmpty());
        assertEquals(1, people.size());
    }

    @Test
    public void shouldReturnEmptyAtFindAll() {
        List<Person> people = personRepository.findAll(Pageable.ofPage(1).size(2)).content();
        assertTrue(people.isEmpty());
    }

    @Test
    public void shouldFindByName() {
        graph.addVertex(T.label, "Person", "name", "Otavio", "age", 30);
        graph.addVertex(T.label, "Person", "name", "Otavio", "age", 20);
        List<Person> people = personRepository.findByName("Otavio", Pageable.ofPage(2).size(1)
                .sortBy(Sort.asc("age")));
        assertFalse(people.isEmpty());
        assertEquals(1, people.size());
        assertThat(people).hasSize(1).map(Person::getAge)
                .contains(30);
    }

    @Test
    public void shouldFindByNameOrderByAge() {
        graph.addVertex(T.label, "Person", "name", "Otavio", "age", 30);
        graph.addVertex(T.label, "Person", "name", "Otavio", "age", 20);
        Page<Person> page = personRepository.findByNameOrderByAge("Otavio", Pageable.ofPage(1).size(1));

        assertThat(page.content()).hasSize(1).map(Person::getAge)
                .contains(20);
        Pageable next = page.nextPageable();

        page = personRepository.findByNameOrderByAge("Otavio", next);
        assertThat(page.content()).hasSize(1).map(Person::getAge)
                .contains(30);
    }

    @Test
    public void shouldFindByAgeOrderByName() {
        graph.addVertex(T.label, "Person", "name", "Otavio", "age", 30);
        graph.addVertex(T.label, "Person", "name", "Otavio", "age", 20);
        Slice<Person> slice = personRepository.findByAgeOrderByName(20, Pageable.ofPage(1).size(1));
        assertNotNull(slice);

        assertThat(slice.content()).hasSize(1).map(Person::getAge)
                .contains(20);
    }


    interface PersonRepository extends PageableRepository<Person, Long> {

        List<Person> findByName(String name, Pageable Pageable);

        Page<Person> findByNameOrderByAge(String name, Pageable Pageable);

        Slice<Person> findByAgeOrderByName(Integer age, Pageable Pageable);

        Optional<Person> findByAge(Integer age, Pageable pagination);

        List<Person> findByNameAndAge(String name, Integer age, Pageable pagination);

        Set<Person> findByAgeAndName(Integer age, String name, Pageable pagination);

    }

}