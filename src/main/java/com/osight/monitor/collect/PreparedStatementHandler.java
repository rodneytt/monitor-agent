package com.osight.monitor.collect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;

import com.osight.monitor.collect.JdbcCommonCollect.JdbcStatistics;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class PreparedStatementHandler implements InvocationHandler {
    private final JdbcCommonCollect collect;
    private final PreparedStatement statement;
    private final JdbcStatistics statistics;

    PreparedStatementHandler(JdbcCommonCollect collect, PreparedStatement ps, JdbcStatistics stat) {
        this.collect = collect;
        this.statement = ps;
        this.statistics = stat;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        boolean agent = false;
        for (String m : JdbcCommonCollect.prepared_statement_methods) {
            if (m.equals(method.getName())) {
                agent = true;
                break;
            }
        }
        Object result = null;
        try {
            result = method.invoke(this.statement, args);
        } catch (Throwable t) {
            if (agent) {
                this.collect.error(this.statistics, t);
            }
            throw t;
        } finally {
            if (agent) {
                this.collect.end(this.statistics);
            }
        }
        return result;
    }
}
