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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import io.netty.util.ReferenceCountUtil;

public class RoundRobinArray<E> implements Collection<E> {

    private final Object[] elements;
    private final int mask;

    public RoundRobinArray(int fixedCapacity) {
        fixedCapacity = RakUtils.powerOfTwoCeiling(fixedCapacity);

        this.elements = new Object[fixedCapacity];
        this.mask = fixedCapacity - 1;
    }

    @SuppressWarnings("unchecked")
    public E get(int index) {
        return (E) this.elements[index & this.mask];
    }

    public void set(int index, E value) {
        int idx = index & this.mask;
        Object element = this.elements[idx];
        this.elements[idx] = value;
        // Make sure to release any reference counted objects that get overwritten.
        ReferenceCountUtil.release(element);
    }

    public void remove(int index, E expected) {
        int idx = index & this.mask;
        Object element = this.elements[idx];
        if (element == expected) {
            this.elements[idx] = null;
            // Make sure to release the element on removal.
            ReferenceCountUtil.release(element);
        }
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(this.elements, this.elements.length);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        if (a.length < this.elements.length)
            // Make a new array of a's runtime type, but my contents:
            return (T[]) Arrays.copyOf(this.elements, this.elements.length, a.getClass());
        System.arraycopy(this.elements, 0, a, 0, this.elements.length);
        if (a.length > this.elements.length)
            a[this.elements.length] = null;
        return a;
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        Arrays.fill(this.elements, null);
    }

    private class Itr implements Iterator<E> {
        int cursor;       // index of next element to return
        int lastRet = -1; // index of last element returned; -1 if no such

        Itr() {
        }

        public boolean hasNext() {
            return cursor != elements.length;
        }

        @SuppressWarnings("unchecked")
        public E next() {
            int i = cursor;
            if (i >= elements.length) {
                throw new NoSuchElementException();
            }
            cursor = i + 1;
            return (E) elements[lastRet = i];
        }

        public void remove() {
            if (lastRet < 0) {
                throw new IllegalStateException();
            }

            Object object = elements[lastRet];
            elements[lastRet] = null;
            ReferenceCountUtil.release(object);
            cursor = lastRet;
            lastRet = -1;
        }
    }
}
