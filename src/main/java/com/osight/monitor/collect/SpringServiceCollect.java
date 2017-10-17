package com.osight.monitor.collect;

import com.osight.monitor.loader.AgentLoader;
import com.osight.monitor.loader.SnippetCode;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class SpringServiceCollect extends AbstractCollect implements ApmCollect {
    private static final String beginSrc;
    private static final String errorSrc;
    private static final String endSrc;
    public static final SpringServiceCollect INSTANCE = new SpringServiceCollect();

    static {
        StringBuilder localStringBuilder = new StringBuilder();
        localStringBuilder.append(SpringServiceCollect.class.getName() + " instance= ");
        localStringBuilder.append(SpringServiceCollect.class.getName() + ".INSTANCE;\r\n");
        localStringBuilder.append(SpringServiceCollect.class.getName() + ".ServiceStatistics statistic =instance.begin(\"%s\",\"%s\");");
        beginSrc = localStringBuilder.toString();
        localStringBuilder = new StringBuilder();
        localStringBuilder.append("instance.end(statistic);");
        endSrc = localStringBuilder.toString();
        localStringBuilder = new StringBuilder();
        localStringBuilder.append("instance.error(statistic,e);");
        errorSrc = localStringBuilder.toString();
    }

    public boolean isTarget(String paramString, ClassLoader paramClassLoader, CtClass paramCtClass) {
        return (paramString.startsWith("com.chsi") || paramString.startsWith("com.osight")) && paramString.endsWith("Impl");
    }

    public byte[] transform(String serviceName, byte[] arrayOfByte, CtClass ctClass) {
        AgentLoader loader = new AgentLoader(serviceName, ctClass);
        CtMethod[] methods = ctClass.getDeclaredMethods();
        for (CtMethod method : methods) {
            if (Modifier.isPublic(method.getModifiers()) && !Modifier.isStatic(method.getModifiers()) && !Modifier.isNative(method.getModifiers())) {
                SnippetCode sc = new SnippetCode();
                sc.setBegin(String.format(beginSrc, serviceName, method.getName()));
                sc.setError(errorSrc);
                sc.setEnd(endSrc);
                loader.buildMethod(method, sc);
            }
        }
        return loader.build();
    }

    @Override
    public Statistics begin(String name, String method) {
        ServiceStatistics stat = new ServiceStatistics(super.begin(name, method));
        stat.serviceName = name;
        stat.methodName = method;
        stat.logType = "service";
        return stat;
    }


    public void sendStatistics(Statistics stat) {
        sendStatisticsByHttp(stat, "monitor");
    }

    private static class ServiceStatistics extends Statistics {
        public String serviceName;
        public String methodName;

        public ServiceStatistics(Statistics stat) {
            super(stat);
        }
    }
}
