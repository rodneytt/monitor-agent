package com.osight.monitor.control;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class RpcThreadLocalManager {
    private static final String RPC_INFO = "rpcINFO_343$%_@og";

    public static RpcInfo get() {
        return (RpcInfo) ThreadLocalUtil.getResource(RPC_INFO);
    }

    public static void bind(RpcInfo info) {
        ThreadLocalUtil.bindResource(RPC_INFO, info);
    }

    public static void unbind() {
        ThreadLocalUtil.unbindResource(RPC_INFO);
    }

}
