package com.osight.monitor.collect;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;

import com.osight.monitor.loader.AgentLoader;
import com.osight.monitor.loader.SnippetCode;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class JdbcCommonCollect extends AbstractCollect implements ApmCollect {
    public static final JdbcCommonCollect INSTANCE = new JdbcCommonCollect();
    static final String[] connection_agent_methods = {"prepareStatement"};
    static final String[] prepared_statement_methods = {"execute", "executeUpdate", "executeQuery"};

    private static final String beginSrc;
    private static final String endSrc;
    private static final String errorSrc;

    static {
        beginSrc = "com.osight.monitor.collect.JdbcCommonCollect inst=com.osight.monitor.collect.JdbcCommonCollect.INSTANCE;";
        errorSrc = "inst.error(null,e);";
        endSrc = "result=inst.proxyConnection((java.sql.Connection)result,Thread.currentThread().getContextClassLoader());";
    }

    @Override
    public boolean isTarget(String className, CtClass ctClass) {
        return className.equals("org.h2.Driver") || className.equals("oracle.jdbc.driver.OracleDriver") || className.equals("com.mysql.jdbc.NonRegisteringDriver");
    }

    @Override
    public byte[] transform(String className, byte[] arrayOfByte, CtClass ctClass) {
        AgentLoader loader = new AgentLoader(className, ctClass);
        CtMethod method = null;
        try {
            method = ctClass.getMethod("connect", "(Ljava/lang/String;Ljava/util/Properties;)Ljava/sql/Connection;");
        } catch (NotFoundException e) {
            e.printStackTrace();
            return null;
        }
        SnippetCode sc = new SnippetCode();
        sc.setBegin(beginSrc);
        sc.setError(errorSrc);
        sc.setEnd(endSrc);
        loader.buildMethod(method, sc);
        return loader.build();
    }

    public Connection proxyConnection(Connection paramConnection, ClassLoader classLoader) {
        return (Connection) Proxy.newProxyInstance(classLoader, new Class[]{Connection.class}, new ConnectionHandler(this, paramConnection, classLoader));
    }

    PreparedStatement proxyPreparedStatement(PreparedStatement paramPreparedStatement, JdbcCommonCollect.JdbcStatistics paramJdbcStatistics, ClassLoader classLoader) {
        return (PreparedStatement) Proxy.newProxyInstance(classLoader, new Class[]{PreparedStatement.class}, new PreparedStatementHandler(this, paramPreparedStatement, paramJdbcStatistics));
    }

    @Override
    public Statistics begin(String name, String method) {
        JdbcStatistics stat = new JdbcStatistics(super.begin(name, method));
        stat.logType = "sql";
        return stat;
    }

    @Override
    public void end(Statistics stat) {
        JdbcStatistics statistics = (JdbcStatistics) stat;
        super.end(statistics);
    }

    @Override
    public void sendStatistics(Statistics stat) {
        sendStatisticsByHttp(stat, "monitor");
    }

    private static String getDbName(String paramString) {
        int i = paramString.indexOf("?");
        if (i != -1) {
            String str = paramString.substring(i + 1, paramString.length());
            paramString = paramString.substring(0, i);
        }
        return paramString.substring(paramString.lastIndexOf("/") + 1);
    }

    public static class JdbcStatistics extends Statistics {
        public String jdbcUrl;
        public String sql;

        JdbcStatistics(Statistics stat) {
            super(stat);
        }
    }
}
