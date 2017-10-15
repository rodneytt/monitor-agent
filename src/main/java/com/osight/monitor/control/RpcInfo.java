package com.osight.monitor.control;

import java.util.Stack;
import java.util.UUID;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class RpcInfo {
    private String traceId;
    private Stack<String> stack;
    private boolean started;
    private String current;

    public RpcInfo() {
        stack = new Stack<>();
        stack.push("0");
        started = true;
        traceId = UUID.randomUUID().toString();
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
}
