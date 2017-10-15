package com.osight.monitor.collect;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.osight.monitor.control.RpcInfo;
import com.osight.monitor.control.RpcThreadLocalManager;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public abstract class AbstractCollect {
    private static final ExecutorService threadService;
    private static final String localIp;

    static {
        threadService = new ThreadPoolExecutor(20, 100, 20000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(1000), new RejectedHandler());
        localIp = null;
    }

    public Statistics begin(String name, String method) {
        Statistics stat = new Statistics();
        stat.begin = System.currentTimeMillis();
        stat.createTime = Calendar.getInstance().getTime();

        RpcInfo info = RpcThreadLocalManager.get();
        if (info.isStarted()) {
            info.setCurrent(info.getStack().peek() + ".1");
        }
        info.getStack().push(info.getCurrent());
        info.setStarted(true);

        stat.traceId = info.getTraceId();
        stat.rpcId = info.getCurrent();
        return stat;
    }

    public void end(Statistics stat) {
        RpcInfo info = RpcThreadLocalManager.get();
        info.setStarted(false);
        String top = info.getStack().pop();
        String parent = StringUtils.substringBeforeLast(top, ".");
        int current = Integer.parseInt(StringUtils.substringAfterLast(top, "."));
        current++;
        info.setCurrent(parent + "." + current);
        stat.end = System.currentTimeMillis();
        stat.useTime = stat.end - stat.begin;
        sendStatistics(stat);
    }

    public void error(Statistics stat, Throwable throwable) {
        if (stat != null) {
            stat.errorMsg = throwable.getMessage();
            stat.errorType = throwable.getClass().getName();
            if (throwable instanceof InvocationTargetException) {
                stat.errorType = ((InvocationTargetException) throwable).getTargetException().getClass().getName();
                stat.errorMsg = ((InvocationTargetException) throwable).getTargetException().getMessage();
            }
        }
        sendStatistics(stat);
    }

    public abstract void sendStatistics(Statistics stat);

    public void sendStatisticsByHttp(Statistics stat, String type) {
        execHttp(stat);
    }

    protected void execHttp(final Statistics stat) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                GsonBuilder gb = new GsonBuilder();
                Gson gson = gb.create();
                IndexRequest indexRequest = new IndexRequest("monitor", stat.logType);
                indexRequest.source(gson.toJson(stat), XContentType.JSON);

                RestClient restClient = null;
                try {
                    restClient = RestClient.builder(new HttpHost("localhost", 9200)).build();
                    RestHighLevelClient highLevelClient = new RestHighLevelClient(restClient);
                    IndexResponse response = highLevelClient.index(indexRequest);
                    System.out.println(response.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (restClient != null)
                            restClient.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
