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
        StringBuilder localStringBuilder = new StringBuilder();
        beginSrc = "com.osight.monitor.collect.JdbcCommonCollect inst=com.osight.monitor.collect.JdbcCommonCollect.INSTANCE;";
        errorSrc = "inst.error(null,e);";
        endSrc = "result=inst.proxyConnection((java.sql.Connection)result);";
    }

    @Override
    public boolean isTarget(String className, ClassLoader classLoader, CtClass ctClass) {
        return className.equals("com.mysql.jdbc.NonRegisteringDriver");
    }

    @Override
    public byte[] transform(ClassLoader classLoader, String className, byte[] arrayOfByte, CtClass ctClass) {
        AgentLoader loader = new AgentLoader(className, classLoader, ctClass);
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

    public Connection proxyConnection(Connection paramConnection) {
        Object localObject = Proxy.newProxyInstance(JdbcCommonCollect.class.getClassLoader(), new Class[] {Connection.class}, new ConnectionHandler(this, paramConnection));
        return (Connection) localObject;
    }

    public PreparedStatement proxyPreparedStatement(PreparedStatement paramPreparedStatement, JdbcCommonCollect.JdbcStatistics paramJdbcStatistics) {
        Object localObject = Proxy.newProxyInstance(JdbcCommonCollect.class.getClassLoader(), new Class[] {PreparedStatement.class}, new PreparedStatementHandler(this, paramPreparedStatement, paramJdbcStatistics));
        return (PreparedStatement) localObject;
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
        if (statistics.jdbcUrl != null) {
            statistics.databaseName = getDbName(statistics.jdbcUrl);
        }
        super.end(stat);
    }

    @Override
    public void sendStatistics(Statistics stat) {
        sendStatisticsByHttp(stat, "sqlLog");
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
        public String databaseName;

        JdbcStatistics(Statistics stat) {
            super(stat);
        }
    }
}
