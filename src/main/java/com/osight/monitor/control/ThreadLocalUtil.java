package com.osight.monitor.control;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class ThreadLocalUtil {

    public volatile static ThreadLocal<Map> map = new ThreadLocal<Map>() {
        protected Map initialValue() {
            return new ConcurrentHashMap<>();
        }
    };

    public static Map getResourceMap() {
        return map.get();
    }

    public static Object getResource(Object key) {
        return getResourceMap().get(key);
    }

    public static boolean hasResource(Object key) {
        return getResourceMap().containsKey(key);
    }

    public static void bindResource(Object key, Object value) throws IllegalStateException {
        if (!hasResource(key)) {
            getResourceMap().put(key, value);
        }
    }

    public static Object unbindResource(Object key) throws IllegalStateException {
        if (hasResource(key)) {
            return getResourceMap().remove(key);
        }
        return null;
    }
}
