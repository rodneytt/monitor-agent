package com.osight.monitor.collect;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    public String ip;
    public String logType;
    public Integer order;

    public Statistics() {

    }

    public Statistics(Statistics stat) {
        this.begin = stat.begin;
        this.end = stat.end;
        this.useTime = stat.useTime;
        this.errorMsg = stat.errorMsg;
        this.errorType = stat.errorType;
        this.traceId = stat.traceId;
        this.ip = stat.ip;
        this.logType = stat.logType;
        this.rpcId = stat.rpcId;
        this.order = stat.order;
    }

    public String toJson() {
        List<Field> fields = getFields(this.getClass());
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        for (Field f : fields) {
            Object v = null;
            try {
                v = f.get(this);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            sb.append("\"").append(f.getName()).append("\":");
            if (v != null) {
                if (v instanceof Number) {
                    sb.append(v).append(",");
                } else {
                    sb.append("\"").append(v.toString()).append("\",");
                }
            } else {
                sb.append("\"\",");
            }
        }
        return sb.substring(0, sb.length() - 1) + "}";
    }

    private List<Field> getFields(Class clz) {
        List<Field> list = new ArrayList<>();
        while (clz.getSuperclass() != null) {
            list.addAll(Arrays.asList(clz.getDeclaredFields()));
            clz = clz.getSuperclass();
        }
        return list;
    }

}
