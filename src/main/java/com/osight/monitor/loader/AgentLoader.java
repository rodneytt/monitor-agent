package com.osight.monitor.loader;

import java.io.IOException;

import com.osight.monitor.util.StringUtils;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class AgentLoader {
    private final String serviceName;
    private final CtClass ctClass;

    public AgentLoader(String name, CtClass clz) {
        this.serviceName = name;
        this.ctClass = clz;
    }

    public void buildMethod(CtMethod method, SnippetCode sc) {

        CtMethod ctMethod = method;
        String methodName = method.getName();
        try {
            String agentName = "$" + StringUtils.substringAfterLast(serviceName, ".") + "Agent";
            ctMethod.setName(methodName + agentName);
            CtMethod agentMethod = CtNewMethod.copy(ctMethod, methodName, ctClass, null);
            ctClass.addMethod(agentMethod);
            agentMethod.setBody(sc.insert(agentMethod, agentName));
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }
    }

    public byte[] build() {
        try {
            return ctClass.toBytecode();
        } catch (IOException | CannotCompileException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        String serviceName = "com.osight.monitor.loader.AgentLoader";
        String agentName = "$" + StringUtils.substringAfterLast(serviceName, ".") + "_agent";
        System.out.println(agentName);
    }
}
