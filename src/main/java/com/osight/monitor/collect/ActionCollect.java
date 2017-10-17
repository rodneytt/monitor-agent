package com.osight.monitor.collect;


import com.osight.monitor.loader.AgentLoader;
import com.osight.monitor.loader.SnippetCode;
import com.osight.monitor.util.StringUtils;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class ActionCollect extends AbstractCollect implements ApmCollect {
    public static ActionCollect INSTANCE = new ActionCollect();
    private static final String beginSrc;
    private static final String endSrc;
    private static final String errorSrc;
    private String requestUrl = "";

    static {
        StringBuilder sb = new StringBuilder();
        sb.append(ActionCollect.class.getName()).append(" instance= ");
        sb.append(ActionCollect.class.getName()).append(".INSTANCE;\r\n");
        sb.append(ActionCollect.class.getName()).append(".WebStatistics statistic =(").append(ActionCollect.class.getName()).append(".WebStatistics").append(")instance.begin(\"%s\",\"%s\");");
        beginSrc = sb.toString();
        endSrc = "instance.end(statistic);";
        errorSrc = "instance.error(statistic,e);";
    }

    @Override
    public boolean isTarget(String className, ClassLoader classLoader, CtClass ctClass) {
        return (StringUtils.startsWith(className, "com.chsi") || StringUtils.startsWith(className, "com.osight")) && StringUtils.endsWithIgnoreCase(className, "Controller");
    }

    @Override
    public byte[] transform(String className, byte[] arrayOfByte, CtClass ctClass) {
        AgentLoader loader = new AgentLoader(className, ctClass);
        CtMethod[] methods = ctClass.getDeclaredMethods();
        for (CtMethod method : methods) {
            try {
                if (Modifier.isPublic(method.getModifiers()) && !Modifier.isStatic(method.getModifiers()) && !Modifier.isNative(method.getModifiers()) && method.getParameterTypes().length <= 1 && method.getReturnType().getName().equals("java.lang.String")) {
                    SnippetCode sc = new SnippetCode();
                    sc.setBegin(String.format(beginSrc, className, method.getName(), requestUrl));
                    sc.setError(errorSrc);
                    sc.setEnd(endSrc);
                    loader.buildMethod(method, sc);
                }
            } catch (NotFoundException e) {
                return null;
            }
        }
        return loader.build();
    }

    @Override
    public void sendStatistics(Statistics stat) {
        sendStatisticsByHttp(stat, "monitor");
    }

    @Override
    public Statistics begin(String name, String method) {
        WebStatistics stat = new WebStatistics(super.begin(name, method));
        stat.serviceName = name;
        stat.methodName = method;
        stat.logType = "web";
        return stat;
    }

    public class WebStatistics extends Statistics {
        public String serviceName;
        public String methodName;

        public WebStatistics(Statistics stat) {
            super(stat);
        }
    }
}
