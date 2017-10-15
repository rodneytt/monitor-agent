package com.osight.monitor.control;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class ThreadLocalManager {
    private static final Log logger = LogFactory.getLog(ThreadLocalManager.class);

    private static ThreadLocal resources = new ThreadLocal() {
        protected Object initialValue() {
            return new HashMap();
        }
    };

    public static Map getResourceMap() {
        return (Map) resources.get();
    }

    public static Object getResource(Object key) {
        return getResourceMap().get(key);
    }

    public static boolean hasResource(Object key) {
        return getResourceMap().containsKey(key);
    }

    public static void bindResource(Object key, Object value) throws IllegalStateException {
        if (hasResource(key)) {
            throw new IllegalStateException("Already a value for key [" + key + "] bound to thread");
        }
        getResourceMap().put(key, value);
    }

    public static Object unbindResource(Object key) throws IllegalStateException {
        if (!hasResource(key)) {
            throw new IllegalStateException("No value for key [" + key + "] bound to thread");
        }
        return getResourceMap().remove(key);
    }
}
