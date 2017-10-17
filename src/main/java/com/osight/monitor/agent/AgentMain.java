package com.osight.monitor.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.osight.monitor.collect.ActionCollect;
import com.osight.monitor.collect.ApmCollect;
import com.osight.monitor.collect.FilterCollect;
import com.osight.monitor.collect.JdbcCommonCollect;
import com.osight.monitor.collect.SpringServiceCollect;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class AgentMain implements ClassFileTransformer {
    private static ApmCollect[] collects;
    private Map<ClassLoader, ClassPool> classPoolMap = new ConcurrentHashMap<>();

    public static void premain(String args, Instrumentation inst) {
        collects = new ApmCollect[]{SpringServiceCollect.INSTANCE, JdbcCommonCollect.INSTANCE, ActionCollect.INSTANCE, FilterCollect.INSTANCE};
        AgentMain agentMain = new AgentMain();
        inst.addTransformer(agentMain);
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if ((className == null) || (loader == null) || (loader.getClass().getName().equals("sun.reflect.DelegatingClassLoader")) || (loader.getClass().getName().equals("org.apache.catalina.loader.StandardClassLoader")) || (loader.getClass().getName().equals("javax.management.remote.rmi.NoCallStackClassLoader")) || (loader.getClass().getName().equals("com.alibaba.fastjson.util.ASMClassLoader")) || (className.contains("$Proxy")) || (className.startsWith("java"))) {
            return null;
        }
        if (!this.classPoolMap.containsKey(loader)) {
            ClassPool localClassPool = new ClassPool();
            localClassPool.insertClassPath(new LoaderClassPath(loader));
            this.classPoolMap.put(loader, localClassPool);
        }
        ClassPool localClassPool = this.classPoolMap.get(loader);
        try {
            className = className.replaceAll("/", ".");
            CtClass localCtClass = localClassPool.get(className);
            for (ApmCollect collect : collects) {
                if (collect.isTarget(className, loader, localCtClass)) {
                    byte[] arrayOfByte = collect.transform(className, classfileBuffer, localCtClass);
                    System.out.println(String.format("%s APM agent insert success", className));
                    return arrayOfByte;
                }
            }
        } catch (Throwable localThrowable) {
            System.out.println(String.format("%s APM agent insert fail", className));
            return null;

        }

        return null;
    }
}
