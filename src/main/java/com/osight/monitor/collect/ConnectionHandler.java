package com.osight.monitor.collect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;

import com.osight.monitor.collect.JdbcCommonCollect.JdbcStatistics;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class ConnectionHandler implements InvocationHandler {
    private final JdbcCommonCollect collect;
    private final Connection sqlConnection;
    private final ClassLoader classLoader;

    public ConnectionHandler(JdbcCommonCollect collect, Connection conn, ClassLoader classLoader) {
        this.collect = collect;
        this.sqlConnection = conn;
        this.classLoader = classLoader;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        int i = 0;
        for (String m : JdbcCommonCollect.connection_agent_methods) {
            if (m.equals(method.getName())) {
                i = 1;
                break;
            }
        }
        Object result = null;
        JdbcStatistics localJdbcStatistics = null;
        try {
            if (i != 0) {
                localJdbcStatistics = (JdbcStatistics) this.collect.begin(proxy.getClass().getName(), method.getName());
                localJdbcStatistics.jdbcUrl = this.sqlConnection.getMetaData().getURL();
                localJdbcStatistics.sql = ((String) args[0]);
            }
            result = method.invoke(this.sqlConnection, args);
            if ((i != 0) && ((result instanceof PreparedStatement))) {
                PreparedStatement localPreparedStatement = (PreparedStatement) result;
                result = this.collect.proxyPreparedStatement(localPreparedStatement, localJdbcStatistics, classLoader);
            }
        } catch (Throwable t) {
            this.collect.error(localJdbcStatistics, t);
            this.collect.end(localJdbcStatistics);
            throw t;
        }
        return result;
    }
}
