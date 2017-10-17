package com.osight.monitor.collect;

import com.osight.monitor.loader.AgentLoader;
import com.osight.monitor.loader.SnippetCode;

import javassist.CtClass;
import javassist.CtMethod;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class FilterCollect extends AbstractCollect implements ApmCollect {
    public static FilterCollect INSTANCE = new FilterCollect();
    private static final String beginSrc;
    private static final String endSrc;
    private static final String errorSrc;

    /**
     * "com.osight.monitor.control.RpcInfo rpcInfo = new " + RpcInfo.class.getName() + "();\n" + RpcThreadLocalManager.class.getName() + ".bind(rpcInfo);\n" +
     FilterCollect.class.getName() + " instace = " + FilterCollect.class.getName() + ".INSTANCE;\n" +
     "javax.servlet.http.HttpServletRequest request = (javax.servlet.http.HttpServletRequest) $1;\n" +
     "rpcInfo.setIp(request.getRemoteAddr());\n" +
     "String url = com.chsi.framework.util.URLUtil.getURL(request, null);\n" +
     FilterStatistics.class.getName() + " statistics = (" + FilterStatistics.class.getName() + ") instace.begin(\"%s\", \"%s\");\n" +
     "statistics.url = url;\n";
     */
    static {
        beginSrc =
                FilterCollect.class.getName() + " instace = " + FilterCollect.class.getName() + ".INSTANCE;\n" +
                        FilterStatistics.class.getName() + " statistics = (" + FilterStatistics.class.getName() + ") instace.begin(\"%s\", \"%s\");\n" +
                        "javax.servlet.http.HttpServletRequest request = (javax.servlet.http.HttpServletRequest) $1;\n" +
                        "statistics.ip=request.getRemoteAddr();\n" +
                        "String url = com.chsi.framework.util.URLUtil.getURL(request, null);\n" +
                        "statistics.url = url;\n";
        endSrc = "instace.end(statistics);";
        errorSrc = "instace.error(statistics, e);";
    }


    @Override
    public void sendStatistics(Statistics stat) {
        sendStatisticsByHttp(stat, "monitor");
    }

    @Override
    public boolean isTarget(String className, ClassLoader classLoader, CtClass ctClass) {
        return className.equals("com.chsi.framework.web.filter.BindCallInfoFilter");
    }

    @Override
    public byte[] transform(String className, byte[] arrayOfByte, CtClass ctClass) {
        AgentLoader loader = new AgentLoader(className, ctClass);
        CtMethod[] methods = ctClass.getDeclaredMethods();
        for (CtMethod method : methods) {
            if (method.getName().equals("doFilter")) {
                SnippetCode sc = new SnippetCode();
                sc.setBegin(String.format(beginSrc, className, method.getName()));
                sc.setError(errorSrc);
                sc.setEnd(endSrc);
                loader.buildMethod(method, sc);
            }
        }
        return loader.build();
    }

    @Override
    public Statistics begin(String name, String method) {
        FilterStatistics stat = new FilterStatistics(super.begin(name, method));
        stat.serviceName = name;
        stat.methodName = method;
        stat.logType = "filter";
        return stat;
    }

    public static class FilterStatistics extends Statistics {
        public String serviceName;
        public String methodName;
        public String url;

        public FilterStatistics(Statistics stat) {
            super(stat);
        }
    }
}
