/*
 *
 *  Copyright (c) 2017 Otávio Santana and others
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
package org.eclipse.jnosql.diana.column.query;

import jakarta.nosql.column.Column;
import org.eclipse.jnosql.diana.column.Columns;

import javax.json.JsonObject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.util.List;
import java.util.Map;

final class JsonObjects {

    private static final Jsonb JSON = JsonbBuilder.create();

    private JsonObjects() {
    }

    static List<Column> getColumns(JsonObject jsonObject) {
        Map<String, Object> map = JSON.fromJson(jsonObject.toString(), Map.class);
        return Columns.of(map);
    }

}
