/*
 * Copyright 2022 CloudburstMC
 *
 * CloudburstMC licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.cloudburstmc.netty.util;

import java.util.Collection;
import java.util.Iterator;

public class RoundRobinIterator<E> implements Iterator<E> {
    private final Collection<E> collection;
    private Iterator<E> iterator;

    public RoundRobinIterator(Collection<E> collection) {
        this.collection = collection;
        this.iterator = this.collection.iterator();
    }

    @Override
    public synchronized boolean hasNext() {
        return !collection.isEmpty();
    }

    @Override
    public synchronized E next() {
        if (!this.iterator.hasNext()) {
            this.iterator = this.collection.iterator();
        }
        return this.iterator.next();
    }
}
