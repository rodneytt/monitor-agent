package com.osight.monitor.collect;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.osight.monitor.control.RpcInfo;
import com.osight.monitor.control.RpcThreadLocalManager;
import com.osight.monitor.netty.MonitorClient;
import com.osight.monitor.util.StringUtils;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public abstract class AbstractCollect {
    private static final ExecutorService threadService;
    private static MonitorClient monitorClient;
    private static final String localIp;
    private static String host = "localhost";
    private static int port = 8709;

    static {
        threadService = new ThreadPoolExecutor(20, 100, 20000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(1000), new RejectedHandler());
        monitorClient = new MonitorClient(host, port);
        localIp = null;
    }

    public Statistics begin(String name, String method) {
        Statistics stat = new Statistics();
        stat.begin = System.currentTimeMillis();
        RpcInfo info = RpcThreadLocalManager.get();
        if (info == null) {
            info = new RpcInfo();
            RpcThreadLocalManager.bind(info);
        }
        if (info.isStarted()) {
            if (info.getStack().isEmpty()) {
                info.setCurrent("0");
            } else {
                info.setCurrent(info.getStack().peek() + ".1");
            }
        }
        info.getStack().push(info.getCurrent());
        info.setStarted(true);
        stat.traceId = info.getTraceId();
        stat.rpcId = info.getCurrent();
        stat.ip = info.getIp();
        stat.order = info.getOrder().getAndIncrement();
        return stat;
    }

    public void end(Statistics stat) {
        if (stat != null) {
            RpcInfo info = RpcThreadLocalManager.get();
            if (info != null) {
                info.setStarted(false);
                String top = info.getStack().pop();
                if (!top.equals("0")) {
                    String parent = StringUtils.substringBeforeLast(top, ".");
                    int current = Integer.parseInt(StringUtils.substringAfterLast(top, "."));
                    current++;
                    info.setCurrent(parent + "." + current);
                } else {
                    RpcThreadLocalManager.unbind();
                }
            }
            stat.end = System.currentTimeMillis();
            stat.useTime = stat.end - stat.begin;
            sendStatistics(stat);
        }
    }

    public void error(Statistics stat, Throwable throwable) {
        if (stat != null) {
            stat.errorMsg = throwable.getMessage();
            stat.errorType = throwable.getClass().getName();
            if (throwable instanceof InvocationTargetException) {
                stat.errorType = ((InvocationTargetException) throwable).getTargetException().getClass().getName();
                stat.errorMsg = ((InvocationTargetException) throwable).getTargetException().getMessage();
            }
            sendStatistics(stat);
        }
    }

    public abstract void sendStatistics(Statistics stat);

    public void sendStatisticsByHttp(Statistics stat, String index) {
        execHttp(stat, index);
    }

    protected void execHttp(final Statistics stat, final String index) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                try {
                    monitorClient.send(stat.toJson());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        threadService.execute(run);
    }

    private static class RejectedHandler implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            System.err.println("upload Task  rejected from " + executor.toString() + " rejectedCount:" + AbstractCollect.localIp);
        }
    }


}
