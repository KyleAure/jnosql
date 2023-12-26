package org.eclipse.jnosql.mapping.core.query;

import jakarta.data.page.Page;
import jakarta.data.page.Pageable;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.PageableRepository;
import jakarta.nosql.Template;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.eclipse.jnosql.mapping.IdNotFoundException.KEY_NOT_FOUND_EXCEPTION_SUPPLIER;

/**
 * An abstract template class providing a base implementation for repositories managing entities
 * through Jakarta Data's {@link PageableRepository} and {@link CrudRepository} interfaces.
 * This class encapsulates common CRUD (Create, Read, Update, Delete) operations and supports pagination
 * for a specific entity type. Subclasses are required to implement certain abstract methods to customize
 * behavior for a particular database or data model.
 *
 * @param <T> The entity type managed by this repository.
 * @param <K> The type of the entity's primary key.
 */
public abstract class AbstractRepository<T, K> implements PageableRepository<T, K>, CrudRepository<T, K> {

    /**
     * Retrieves the template associated with this repository.
     *
     * @return The template used for database operations.
     */
    protected abstract Template getTemplate();

    /**
     * Retrieves the metadata information about the entity managed by this repository.
     *
     * @return The entity metadata information.
     */
    protected abstract EntityMetadata getEntityMetadata();

    /**
     * Retrieves the Class object representing the entity type managed by this repository.
     *
     * @return The Class object of the entity type.
     */
    @SuppressWarnings("unchecked")
    protected Class<T> getType() {
        return (Class<T>) getEntityMetadata().type();
    }

    /**
     * Converts an entity ID to a Stream of entities, wrapping it in an Optional.
     *
     * @return A function that converts an entity ID to a Stream of entities.
     */
    protected Function<K, Stream<T>> optionalToStream() {
        return id -> {
            Optional<T> entity = this.findById(id);
            return entity.stream();
        };
    }

    /**
     * Retrieves the metadata information about the ID field of the entity.
     *
     * @return The metadata information about the ID field.
     * @throws org.eclipse.jnosql.mapping.IdNotFoundException if the ID field metadata is not found.
     */
    protected FieldMetadata getIdField() {
        return getEntityMetadata().id().orElseThrow(KEY_NOT_FOUND_EXCEPTION_SUPPLIER);
    }

    /**
     * Retrieves the error message template for unsupported repository methods.
     *
     * @return The error message template.
     */
    protected String getErrorMessage(){
        return "The AbstractRepository does not support %s method";
    }

    @Override
    public <S extends T> S save(S entity) {
        Objects.requireNonNull(entity, "Entity is required");
        Object id = getIdField().read(entity);
        if (nonNull(id) && existsById((K) id)) {
            return getTemplate().update(entity);
        } else {
            return getTemplate().insert(entity);
        }
    }

    @Override
    public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
        requireNonNull(entities, "entities is required");
        return StreamSupport.stream(entities.spliterator(), false)
                .map(this::save).collect(toList());
    }


    @Override
    public void deleteById(K id) {
        requireNonNull(id, "is is required");
        getTemplate().delete(getType(), id);
    }

    @Override
    public void deleteByIdIn(Iterable<K> ids) {
        requireNonNull(ids, "ids is required");
        ids.forEach(this::deleteById);
    }

    @Override
    public Optional<T> findById(K id) {
        requireNonNull(id, "id is required");
        return getTemplate().find(getType(), id);
    }


    @Override
    public Stream<T> findByIdIn(Iterable<K> ids) {
        requireNonNull(ids, "ids is required");
        return stream(ids.spliterator(), false)
                .flatMap(optionalToStream());
    }

    @Override
    public boolean existsById(K id) {
        return findById(id).isPresent();
    }

    @Override
    public void delete(T entity) {
        Objects.requireNonNull(entity, "entity is required");
        EntityMetadata metadata = getEntityMetadata();
        FieldMetadata id = metadata.id().orElseThrow(KEY_NOT_FOUND_EXCEPTION_SUPPLIER);
        getTemplate().delete(metadata.type(), id.read(entity));
    }

    @Override
    public void deleteAll(Iterable<? extends T>  entities) {
        Objects.requireNonNull(entities, "entities is required");
        StreamSupport.stream(entities.spliterator(), false)
                .forEach(this::delete);
    }

    @Override
    public <S extends T> S insert(S entity) {
        Objects.requireNonNull(entity, "entity is required");
        return getTemplate().insert(entity);
    }

    @Override
    public <S extends T> Iterable<S> insertAll(Iterable<S> entities) {
        Objects.requireNonNull(entities, "entities is required");
        return getTemplate().insert(entities);
    }

    @Override
    public boolean update(T entity) {
        Objects.requireNonNull(entity, "entity is required");
        return getTemplate().update(entity) != null;
    }

    @Override
    public int updateAll(Iterable<T> entities) {
        Objects.requireNonNull(entities, "entities is required");
        getTemplate().update(entities);
        return (int) StreamSupport.stream(entities.spliterator(), false).count();
    }

    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException(String.format(getErrorMessage(), "deleteAll"));
    }

    @Override
    public long count() {
        throw new UnsupportedOperationException(String.format(getErrorMessage(), "count"));
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        throw new UnsupportedOperationException(String.format(getErrorMessage(), "findAll"));
    }

    @Override
    public Stream<T> findAll() {
        throw new UnsupportedOperationException(String.format(getErrorMessage(), "findAll"));
    }


}