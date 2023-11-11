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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultArrayQueryValueTest {

    @Test
    public void shouldReturnArrayType() {
        ArrayQueryValue array = DefaultArrayQueryValue.of(new QueryValue<?>[]{
                BooleanQueryValue.FALSE});
        assertThat(array).isNotNull();
        ValueType type = array.type();
        assertThat(type).isEqualTo(ValueType.ARRAY);
    }

    @Test
    public void shouldReturnArrayValue() {
        ArrayQueryValue array = DefaultArrayQueryValue.of(new QueryValue<?>[]{
                BooleanQueryValue.FALSE, BooleanQueryValue.TRUE});
        assertThat(array.get()).containsExactly(BooleanQueryValue.FALSE, BooleanQueryValue.TRUE);
    }

    @Test
    public void shouldEquals(){
        ArrayQueryValue array = DefaultArrayQueryValue.of(new QueryValue<?>[]{
                BooleanQueryValue.FALSE, BooleanQueryValue.TRUE});
        ArrayQueryValue arrayB = DefaultArrayQueryValue.of(new QueryValue<?>[]{
                BooleanQueryValue.FALSE, BooleanQueryValue.TRUE});
        Assertions.assertEquals(arrayB, array);
    }

    @Test
    public void shouldHashCode(){
        ArrayQueryValue array = DefaultArrayQueryValue.of(new QueryValue<?>[]{
                BooleanQueryValue.FALSE, BooleanQueryValue.TRUE});
        ArrayQueryValue arrayB = DefaultArrayQueryValue.of(new QueryValue<?>[]{
                BooleanQueryValue.FALSE, BooleanQueryValue.TRUE});
        Assertions.assertEquals(arrayB.hashCode(), array.hashCode());
    }

    @Test
    public void shouldToString(){
        ArrayQueryValue array = DefaultArrayQueryValue.of(new QueryValue<?>[]{
                BooleanQueryValue.FALSE, BooleanQueryValue.TRUE});
        assertThat(array.toString()).isEqualTo("[BooleanQueryValue{value=false}, BooleanQueryValue{value=true}]");
    }
}