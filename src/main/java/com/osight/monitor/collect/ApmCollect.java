package com.osight.monitor.collect;

import javassist.CtClass;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public interface ApmCollect {
    boolean isTarget(String className, ClassLoader classLoader, CtClass ctClass);

    byte[] transform(String className, byte[] arrayOfByte, CtClass ctClass);
}
