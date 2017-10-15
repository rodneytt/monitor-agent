package com.osight.monitor.collect;

import java.util.Date;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class Statistics {
    public String traceId;
    public String rpcId;
    public Long begin;
    public Long end;
    public Long useTime;
    public String errorMsg;
    public String errorType;
    public Date createTime;
    public String ip;
    public String logType;

    public Statistics() {

    }

    public Statistics(Statistics stat) {
        this.begin = stat.begin;
        this.end = stat.end;
        this.useTime = stat.useTime;
        this.errorMsg = stat.errorMsg;
        this.errorType = stat.errorType;
        this.createTime = stat.createTime;
        this.traceId = stat.traceId;
        this.ip = stat.ip;
        this.logType = stat.logType;
        this.rpcId = stat.rpcId;
    }
}
