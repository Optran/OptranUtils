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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

import com.github.optran.utils.ObjectUtils;

/**
 * This is an implementation of a completely in memory cache utilizing the least
 * recently used algorithm.
 * 
 * @author Ashutosh Wad
 *
 * @param <Key>   The key type for all keys in this cache.
 * @param <Value> The value type for all values in this cache.
 */
public class LRUCache<Key, Value> {
	private static final int DEFAULT_CAPACITY = 1;
	private final AtomicLong order;
	private final int capacity;
	private final Map<Key, LRUCacheEntry<Key, Value>> cacheMap;
	private CacheLoader<Key, Value>cacheLoader;
	private final TreeMap<Long, LRUCacheEntry<Key, Value>> cacheEvictionMap;
	private final List<CacheEvictionListener<Key, Value>> evictionListenerList;

	/**
	 * This constructor creates a cache with the default capacity of 1 element.
	 */
	public LRUCache() {
		this(DEFAULT_CAPACITY);
	}

	/**
	 * This constructor creates a cache with the capacity that has been specified.
	 * It should be noted that if the capacity provided is less than 1 then this
	 * cache is created with the default capacity of 1.
	 * 
	 * @param capacity The desired capacity.
	 */
	public LRUCache(int capacity) {
		if (capacity < DEFAULT_CAPACITY) {
			this.capacity = DEFAULT_CAPACITY;
		} else {
			this.capacity = capacity;
		}
		order = new AtomicLong();
		order.set(0);
		cacheMap = new HashMap<>();
		cacheEvictionMap = new TreeMap<>();
		evictionListenerList = new LinkedList<>();
	}

	public void setCacheLoader(CacheLoader<Key, Value>cacheLoader) {
		this.cacheLoader = cacheLoader;
	}
	
	public void removeCacheLoader() {
		this.cacheLoader = null;
	}

	/**
	 * Adds the listener provided to the list of listeners each of which is invoked
	 * in the case of an eviction from this cache.
	 * 
	 * @param evictionListener The listener to be added.
	 */
	public void addEvictionListener(CacheEvictionListener<Key, Value> evictionListener) {
		if (null == evictionListener) {
			return;
		}
		evictionListenerList.add(evictionListener);
	}

	/**
	 * Removes the listener provided if it exists.
	 * 
	 * @param evictionListener The listener to be removed.
	 */
	public void removeEvictionListener(CacheEvictionListener<Key, Value> evictionListener) {
		if (null == evictionListener) {
			return;
		}
		evictionListenerList.remove(evictionListener);
	}

	/**
	 * Removes all associated listeners from this cache.
	 */
	public void removeEvictionListeners() {
		evictionListenerList.clear();
	}

	/**
	 * This method returns true if the key under consideration is present in this
	 * cache.
	 * 
	 * @param key The key to be checked.
	 * @return True if this cache contains this key, false otherwise.
	 */
	public boolean contains(Key key) {
		return cacheMap.containsKey(key);
	}

	/**
	 * Iteratively performs an action on all current members of the cache without
	 * evicting any elements.
	 * 
	 * @param action The action to be performed.
	 */
	public void perform(CacheAction<Key, Value> action) {
		if (size() == 0) {
			return;
		}
		if (null == action) {
			return;
		}
		for (LRUCacheEntry<Key, Value> entry : cacheMap.values()) {
			action.perform(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * This method returns the value stored at the specified key.
	 * 
	 * @param key The key in the cache whose associated value is desired.
	 * @return The null if no entry present in the cache, or an optional so as to
	 *         guard against the possibility that the value that was saved was null.
	 */
	public Optional<Value> get(Key key) {
		if (cacheMap.containsKey(key)) {
			return Optional.ofNullable(cacheMap.get(key).getValue());
		}
		if(null==cacheLoader) {
			return null;
		}
		Optional<Value>optional = cacheLoader.get(key);
		if(null==optional) {
			return null;
		}
		Value value = null;
		if(optional.isPresent()) {
			value = optional.get();
		}
		put(key, value);
		return Optional.ofNullable(value);
	}

	/**
	 * Adds the required key value pair to the cache. Additionally this method only
	 * considers an update to be a change in value, so no matter how many times the
	 * same value is inserted for a key, it will not impact the eviction order
	 * unless the current value is different from the previous one.
	 * 
	 * @param key   The key to be associated to the cached value.
	 * @param value The value to be cached.
	 */
	public void put(Key key, Value value) {
		LRUCacheEntry<Key, Value> entry = null;
		if (!cacheMap.containsKey(key)) {
			entry = new LRUCacheEntry<>(key, value, order.incrementAndGet());
			cacheMap.put(entry.getKey(), entry);
			cacheEvictionMap.put(entry.getOrder(), entry);
			evictSurplus();
			return;
		}
		entry = cacheMap.get(key);
		if (ObjectUtils.isNotEqual(entry.getValue(), value)) {
			cacheMap.remove(key);
			cacheEvictionMap.remove(entry.getOrder());
			entry = new LRUCacheEntry<>(key, value, order.incrementAndGet());
			cacheMap.put(entry.getKey(), entry);
			cacheEvictionMap.put(entry.getOrder(), entry);
		}
		evictSurplus();
	}

	/**
	 * Removes the value associated to the key that has been provided from the
	 * cache. Removal is not eviction and hence no eviction listeners are triggered.
	 * 
	 * @param key The key whose associated value needs to be removed.
	 * @return The value of one was found, else null.
	 */
	public Optional<Value> remove(Key key) {
		if (!cacheMap.containsKey(key)) {
			return null;
		}
		LRUCacheEntry<Key, Value> entry = cacheMap.get(key);
		cacheMap.remove(key);
		cacheEvictionMap.remove(entry.getOrder());
		return Optional.ofNullable(entry.getValue());
	}

	/**
	 * Evicts the value associated to the key that has been provided from the cache.
	 * 
	 * @param key The key whose associated value needs to be removed.
	 * @return The value of one was found, else null.
	 */
	public void evict(Key key) {
		if (!cacheMap.containsKey(key)) {
			return;
		}
		LRUCacheEntry<Key, Value> entry = cacheMap.get(key);
		cacheMap.remove(key);
		cacheEvictionMap.remove(entry.getOrder());
		triggerListeners(entry);
	}

	/**
	 * Evicts all entries currently in the cache.
	 */
	public void evict() {
		while (size() > 0) {
			long order = cacheEvictionMap.firstKey();
			LRUCacheEntry<Key, Value> entry = cacheEvictionMap.get(order);
			cacheMap.remove(entry.getKey());
			cacheEvictionMap.remove(entry.getOrder());
			triggerListeners(entry);
		}
	}

	/**
	 * Empties the entire cache. The clear method does not invoke any eviction
	 * listeners, for that the evict method should be used.
	 */
	public void clear() {
		order.set(0);
		cacheMap.clear();
		cacheEvictionMap.clear();
	}

	/**
	 * This method returns the total number of entries currently in this cache.
	 * 
	 * @return The total number of entries currently in this cache.
	 */
	public int size() {
		return cacheMap.size();
	}

	/**
	 * This method returns the maximum number of entries that can be contained in
	 * this cache.
	 * 
	 * @return The maximum number of entries that can be contained in this cache.
	 */
	public int capacity() {
		return capacity;
	}

	/**
	 * Performs an eviction from the cache if the number of elements has exceeded
	 * the capacity.
	 */
	private void evictSurplus() {
		if (cacheEvictionMap.size() <= capacity) {
			return;
		}
		long order = cacheEvictionMap.firstKey();
		LRUCacheEntry<Key, Value> entry = cacheEvictionMap.get(order);
		cacheMap.remove(entry.getKey());
		cacheEvictionMap.remove(entry.getOrder());
		triggerListeners(entry);
	}

	/**
	 * This is an internal helper method that invokes all the listeners assigned to
	 * this cache for the entry being evicted.
	 * 
	 * @param entry The entry being evicted.
	 */
	private void triggerListeners(LRUCacheEntry<Key, Value> entry) {
		for (CacheEvictionListener<Key, Value> evictionListener : evictionListenerList) {
			evictionListener.evict(entry.getKey(), entry.getValue());
		}
	}
}
