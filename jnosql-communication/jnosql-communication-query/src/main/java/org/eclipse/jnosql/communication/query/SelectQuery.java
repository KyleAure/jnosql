/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */

package org.eclipse.jnosql.communication.query;

import jakarta.data.Sort;

import java.util.List;
import java.util.Optional;

/**
 * The select statement reads one or more fields for one or more entities.
 * It returns a result-set of the entities matching the request, where each entity contains the fields
 * for corresponding to the query.
 */
public interface SelectQuery extends Query {
    /**
     * The fields that will retrieve in this query, if this fields is empty, this query will retrieve the whole entity.
     *
     * @return the fields list
     */
    List<String> fields();

    /**
     * The entity name
     *
     * @return the entity name
     */
    String entity();

    /**
     * The condition at this {@link DefaultSelectQuery}, if the Where is empty that means may retrieve the whole entities.
     *
     * @return the {@link Where} entity otherwise {@link Optional#empty()}
     */
    Optional<Where> where();

    /**
     * Statement defines where the query should start
     *
     * @return the number to skip, otherwise either negative value or zero
     */
    long skip();

    /**
     * Statement limits the number of rows returned by a query,
     *
     * @return the maximum of result, otherwise either negative value or zero
     */
    long limit();

    /**
     * The list of orders, it is used to sort the result-set in ascending or descending order.
     *
     * @return the order list
     */
    List<Sort> orderBy();
}
