/*
* Copyright 2008-2011 Jeff McWhirter/ramadda.org
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this 
* software and associated documentation files (the "Software"), to deal in the Software 
* without restriction, including without limitation the rights to use, copy, modify, 
* merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
* permit persons to whom the Software is furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies 
* or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
* PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
* OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
* DEALINGS IN THE SOFTWARE.
*/

package org.ramadda.util;


import java.util.Date;
import java.util.Hashtable;



/**
 * Supports a cache that holds time limited entries (time to live)
 * Note: this only removes items from the cache when a get is performed and the item has expired
 *
 *
 * @param <KEY>
 * @param <VALUE>
 */
public class TTLCache<KEY, VALUE> {

    /** helper for ttl */
    public static long MS_IN_A_MINUTE = 1000 * 60;

    /** _more_ */
    public static long MS_IN_AN_HOUR = 1000 * 60 * 60;

    /** helper for ttl */
    public static long MS_IN_A_DAY = MS_IN_AN_HOUR * 24;

    /** the cache */
    private Hashtable<KEY, CacheEntry<VALUE>> cache =
        new Hashtable<KEY, CacheEntry<VALUE>>();

    /** how long should the objects be in the cache */
    private long timeThreshold;

    /** should we update the time when a get is performed */
    private boolean updateTimeOnGet = false;

    /** how big should the cache become until its cleared */
    private int sizeLimit = -1;


    /**
     * default ctor. 1 hour in cache. No time reset. No size limit
     */
    public TTLCache() {
        this(MS_IN_AN_HOUR, -1, false);
    }

    /**
     * ctor. No time reset. No size limit
     *
     *
     * @param timeThresholdInMilliseconds time in cache
     */
    public TTLCache(long timeThresholdInMilliseconds) {
        this(timeThresholdInMilliseconds, -1, false);
    }

    /**
     * ctor. No time reset.
     *
     *
     * @param timeThresholdInMilliseconds time in cache
     * @param sizeLimit cache size limit
     */
    public TTLCache(long timeThresholdInMilliseconds, int sizeLimit) {
        this(timeThresholdInMilliseconds, sizeLimit, false);
    }


    /**
     * ctor. No time reset.
     *
     *
     * @param timeThresholdInMilliseconds time in cache
     * @param sizeLimit cache size limit
     * @param updateTimeOnGet if true then on a get reset the time to current time
     */
    public TTLCache(long timeThresholdInMilliseconds, int sizeLimit,
                    boolean updateTimeOnGet) {
        this.timeThreshold   = timeThresholdInMilliseconds;
        this.sizeLimit       = sizeLimit;
        this.updateTimeOnGet = updateTimeOnGet;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int size() {
        return cache.size();
    }

    /**
     * _more_
     *
     * @param t _more_
     */
    public void setTimeThreshold(long t) {
        this.timeThreshold = t;
    }

    /**
     * put the value
     *
     * @param key key
     * @param value value
     */
    public void put(KEY key, VALUE value) {
        if ((sizeLimit > 0) && (cache.size() > sizeLimit)) {
            cache = new Hashtable<KEY, CacheEntry<VALUE>>();
        }
        cache.put(key, new CacheEntry<VALUE>(value));
    }

    /**
     * _more_
     *
     * @param key _more_
     */
    public void remove(Object key) {
        cache.remove(key);
    }


    /**
     * get the value
     *
     * @param key key
     *
     * @return value or null if not in cache or entry has expired
     */
    public VALUE get(Object key) {
        CacheEntry cacheEntry = cache.get(key);
        if (cacheEntry == null) {
            return null;
        }
        Date now      = new Date();
        long timeDiff = now.getTime() - cacheEntry.time;
        if (timeDiff > timeThreshold) {
            cache.remove(key);
            return null;
        }
        if (updateTimeOnGet) {
            cacheEntry.resetTime();
        }
        return (VALUE) cacheEntry.object;
    }




    /**
     * Class description
     *
     *
     * @author     Jeff McWhirter (jeffmc@unavco.org)
     *
     * @param <VALUE> Type of object
     */
    private class CacheEntry<VALUE> {

        /** time put in cache */
        long time;

        /** the object */
        VALUE object;

        /**
         * ctor
         *
         * @param object the object
         */
        public CacheEntry(VALUE object) {
            this.object = object;
            resetTime();
        }

        /**
         * reset time in cache
         */
        public void resetTime() {
            this.time = new Date().getTime();
        }


    }


}
