package com.osight.monitor.loader;

import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class SnippetCode {
    private String begin;
    private String error;
    private String end;

    public void setBegin(String begin) {
        this.begin = begin;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String insert(CtMethod method) {
        try {
            String str1 = method.getReturnType().getName().equals("void") ? "{\n%s        try {\n            %s$agent($$);\n        } catch (Throwable e) {\n%s            throw e;\n        }finally{\n%s        }\n}\n" : "{\n%s        Object result=null;\n       try {\n            result=($w)%s$agent($$);\n        } catch (Throwable e) {\n%s            throw e;\n        }finally{\n%s        }\n        return ($r) result;\n}\n";
            String str2 = this.begin == null ? "" : this.begin;
            String str3 = this.error == null ? "" : this.error;
            String str4 = this.end == null ? "" : this.end;
            String str5 = String.format(str1, new Object[] {str2, method.getName(), str3, str4});
            return str5;
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
