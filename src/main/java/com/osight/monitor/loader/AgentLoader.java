package com.osight.monitor.loader;

import java.io.IOException;

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
    private final ClassLoader classLoader;
    private final CtClass ctClass;

    public AgentLoader(String name, ClassLoader loader, CtClass clz) {
        this.serviceName = name;
        this.classLoader = loader;
        this.ctClass = clz;
    }

    public void buildMethod(CtMethod method, SnippetCode sc) {

        CtMethod ctMethod = method;
        String methodName = method.getName();
        try {
            ctMethod.setName(methodName+"$agent");
            CtMethod agentMethod = CtNewMethod.copy(ctMethod, methodName, ctClass, null);
            ctClass.addMethod(agentMethod);
            agentMethod.setBody(sc.insert(agentMethod));
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
}
