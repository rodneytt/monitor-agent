package com.osight.monitor.control;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import com.osight.monitor.util.RandomUtils;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class RpcInfo {
    private String traceId;
    private Stack<String> stack;
    private boolean started;
    private String current;
    private String ip;
    private AtomicInteger order = new AtomicInteger(0);

    public RpcInfo() {
        stack = new Stack<>();
        started = true;
        traceId = RandomUtils.getRandomString(16);
    }

    public Stack<String> getStack() {
        return stack;
    }

    public void setStack(Stack<String> stack) {
        this.stack = stack;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }


    public AtomicInteger getOrder() {
        return order;
    }
}
