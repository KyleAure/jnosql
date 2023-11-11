/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import org.eclipse.jnosql.communication.Condition;

import java.util.Objects;

/**
 * The WHERE clause specifies a filter to the result. These filters are booleans operations that are composed of one or
 * more conditions appended with the and ({@link Condition#AND}) and or ({@link Condition#OR}) operators.
 */
public final class Where {

    private final QueryCondition condition;

    Where(QueryCondition condition) {
        this.condition = condition;
    }

    /**
     * The condition
     * @return the condition
     */
    public QueryCondition condition() {
        return condition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Where that)) {
            return false;
        }
        return Objects.equals(condition, that.condition);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(condition);
    }

    @Override
    public String toString() {
        return "where " + condition;
    }

    public static Where of(QueryCondition condition) {
        return new Where(condition);
    }
}
