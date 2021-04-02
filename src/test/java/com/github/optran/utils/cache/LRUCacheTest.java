/*
 * MIT License
 * 
 * Copyright (c) 2021 Ashutosh Wad
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
*/
package com.github.optran.utils.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class LRUCacheTest {
	private LRUCache<Integer, String> cache;
	private Queue<String> evictedQueue;

	@Before
	public void before() {
		cache = new LRUCache<>(10);
		evictedQueue = new LinkedList<>();
		cache.addEvictionListener(new CacheEvictionListener<Integer, String>() {
			@Override
			public void evict(Integer key, String value) {
				evictedQueue.add(value);
			}
		});
	}

	@Test
	public void testCacheInitialization() {
		cache = new LRUCache<>(0);
		assertEquals(0, cache.size());
		assertEquals(1, cache.capacity());
		cache = new LRUCache<>(2);
		assertEquals(0, cache.size());
		assertEquals(2, cache.capacity());
		cache = new LRUCache<>();
		assertEquals(0, cache.size());
		assertEquals(1, cache.capacity());
	}

	@Test
	public void testEvict() {
		for (int i = 0; i < 10; i++) {
			cache.put(i, Integer.toString(i));
			assertTrue(evictedQueue.isEmpty());
		}
		assertTrue(cache.size() == cache.capacity());
		cache.evict();
		assertTrue(evictedQueue.size() == cache.capacity());
		for (int i = 0; i < 10; i++) {
			assertEquals(Integer.toString(i), evictedQueue.remove());
		}

		cache.evict(2);
		assertTrue(evictedQueue.isEmpty());
		for (int i = 0; i < 10; i++) {
			cache.put(i, Integer.toString(i));
			assertTrue(evictedQueue.isEmpty());
			cache.evict(i);
			assertEquals(Integer.toString(i), evictedQueue.remove());
		}
	}

	@Test
	public void testClear() {
		for (int i = 0; i < 10; i++) {
			cache.put(i, Integer.toString(i));
			assertTrue(evictedQueue.isEmpty());
		}
		assertTrue(cache.size() == cache.capacity());
		cache.clear();
		assertTrue(evictedQueue.isEmpty());
	}

	@Test
	public void testAction() {
		Queue<String> fetchedQueue = new LinkedList<String>();
		cache.perform(null);
		assertTrue(fetchedQueue.isEmpty());

		for (int i = 0; i < 10; i++) {
			cache.put(i, Integer.toString(i));
			assertTrue(evictedQueue.isEmpty());
		}
		assertTrue(cache.size() == cache.capacity());

		cache.perform(null);
		assertTrue(fetchedQueue.isEmpty());

		cache.perform(new CacheAction<Integer, String>() {
			@Override
			public void perform(Integer key, String value) {
				fetchedQueue.add(value);
			}
		});
		assertFalse(fetchedQueue.isEmpty());
		for (int i = 0; i < 10; i++) {
			assertEquals(Integer.toString(i), fetchedQueue.remove());
		}
		assertTrue(fetchedQueue.isEmpty());
	}

	@Test
	public void testEvictionListener() {
		cache = new LRUCache<>();
		evictedQueue = new LinkedList<>();
		CacheEvictionListener<Integer, String> listener = new CacheEvictionListener<Integer, String>() {
			@Override
			public void evict(Integer key, String value) {
				evictedQueue.add(value);
			}
		};

		// The following should not throw any exception
		cache.removeEvictionListener(null);
		cache.addEvictionListener(null);
		for (int i = 0; i < 10; i++) {
			cache.put(i, Integer.toString(i));
			assertTrue(evictedQueue.isEmpty());
		}

		cache.addEvictionListener(listener);
		for (int i = 0; i < 10; i++) {
			cache.put(i, Integer.toString(i));
			assertFalse(evictedQueue.isEmpty());
			assertNotNull(evictedQueue.remove());
			assertTrue(evictedQueue.isEmpty());
		}

		cache.removeEvictionListener(listener);
		for (int i = 0; i < 10; i++) {
			cache.put(i, Integer.toString(i));
			assertTrue(evictedQueue.isEmpty());
		}

		cache.addEvictionListener(listener);
		cache.removeEvictionListeners();
		for (int i = 0; i < 10; i++) {
			cache.put(i, Integer.toString(i));
			assertTrue(evictedQueue.isEmpty());
		}
	}

	@Test
	public void testCache() {
		for (int i = 0; i < 10; i++) {
			assertNull(cache.get(i));
			assertFalse(cache.contains(i));
			cache.put(i, Integer.toString(i));
			assertTrue(evictedQueue.isEmpty());
			assertTrue(cache.contains(i));
		}
		for (int i = 0; i < 10; i++) {
			cache.put(i, Integer.toString(i));
			assertTrue(evictedQueue.isEmpty());
			assertEquals(Integer.toString(i), cache.get(i).get());
		}
		for (int i = 9; i >= 0; i--) {
			// Trigger an update forcing the LRU algorithm to reorder its entries.
			cache.put(i, "");
			assertTrue(evictedQueue.isEmpty());
			// Resets the value so we get elements evicted in the reverse order.
			cache.put(i, Integer.toString(i));
			assertTrue(evictedQueue.isEmpty());
		}
		cache.put(10, Integer.toString(10));
		assertEquals("9", evictedQueue.remove());
		cache.put(11, Integer.toString(11));
		assertEquals("8", evictedQueue.remove());
		assertEquals("5", cache.remove(5).get());
		assertNull(cache.remove(5));
		assertEquals(9, cache.size());
	}

	@Test
	public void testCacheUpdate() {
		for (int i = 0; i < 10; i++) {
			cache.put(i, Integer.toString(i));
			assertTrue(evictedQueue.isEmpty());
		}
		cache.put(2, "30");
		assertTrue(evictedQueue.isEmpty());
		String[] result = new String[] { "0", "1", "3", "4", "5", "6", "7", "8", "9", "30" };
		cache.evict();
		for (int i = 0; i < 10; i++) {
			assertEquals(result[i], evictedQueue.remove());
		}
	}

	@Test
	public void testCacheNonUpdate() {
		for (int i = 0; i < 10; i++) {
			cache.put(i, Integer.toString(i));
			assertTrue(evictedQueue.isEmpty());
		}
		cache.put(2, "2");
		assertTrue(evictedQueue.isEmpty());
		cache.evict();
		for (int i = 0; i < 10; i++) {
			assertEquals(Integer.toString(i), evictedQueue.remove());
		}
	}
}
