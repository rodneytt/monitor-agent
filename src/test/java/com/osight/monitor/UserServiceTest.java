package com.osight.monitor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.osight.monitor.collect.ActionCollect;
import com.osight.monitor.collect.FilterCollect;
import com.osight.monitor.collect.FilterCollect.FilterStatistics;
import com.osight.monitor.collect.SpringServiceCollect;
import com.osight.monitor.collect.Statistics;
import com.osight.monitor.control.RpcInfo;
import com.osight.monitor.control.ThreadLocalUtil;
import com.osight.monitor.service.IUserService;
import com.osight.monitor.service.impl.UserServiceImpl;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class UserServiceTest {
    @Before
    public void before() {
        RpcInfo info = new RpcInfo();
        //RpcThreadLocalManager.bind(info);
    }

    @After
    public void after() {
        // RpcThreadLocalManager.unbind();
    }

    @Test
    public void testHandler() {
        ThreadLocalUtil.getResourceMap().put("a", "a");
        final IUserService userService = new UserServiceImpl();
        userService.printThreadLocal();

        FutureTask<IUserService> task = new FutureTask<IUserService>(new Callable<IUserService>() {
            @Override
            public IUserService call() throws Exception {
                System.out.println(Thread.currentThread() + ":" + ThreadLocalUtil.getResourceMap().size());
                return (IUserService) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{IUserService.class}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        System.out.println(Thread.currentThread() + ":" + ThreadLocalUtil.getResourceMap().size());
                        return method.invoke(userService, args);
                    }
                });
            }
        });
        new Thread(task).start();
        IUserService userService1 = null;
        try {
            userService1 = task.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        userService1.printThreadLocal();
    }

    @Test
    public void testClass() {
        IUserService userService = new UserServiceImpl();
        System.out.println(userService.getClass().getName());
        System.out.println(userService.getClass().getCanonicalName());
        Statistics stat = new Statistics();
        FilterStatistics fs = new FilterStatistics(stat);
        fs.logType = "web";
        fs.serviceName = "service";
        fs.begin = System.currentTimeMillis();
        System.out.println(fs.toJson());
    }


    @Test
    public void testAction() throws InterruptedException {
        UserAction action = new UserAction();
        action.setName("zhangsan");
        action.setPassword("123456");
        action.getName();
        action.index();
        action.list();
        action.getAge();
        Thread.sleep(2000);
    }

    @Test
    public void buildActionTest() throws NotFoundException, IOException, URISyntaxException {
        final String className = "com.osight.monitor.UserAction";
        ClassLoader loader = getClass().getClassLoader();
        ClassPool localClassPool = new ClassPool();
        localClassPool.insertClassPath(new LoaderClassPath(loader));
        CtClass ctClass = localClassPool.get(className);
        ActionCollect collect = ActionCollect.INSTANCE;
        byte[] classByte = collect.transform(className, null, ctClass);
        String pathname = System.getProperty("user.dir") + "/target/UserAction.class";
        Path path = new File(pathname).toPath();
        Files.write(path, classByte);
        System.out.println(pathname);
    }

    @Test
    public void buildFilterTest() throws NotFoundException, IOException {
        final String className = "com.chsi.framework.web.filter.BindCallInfoFilter";
        ClassLoader loader = getClass().getClassLoader();
        ClassPool localClassPool = new ClassPool();
        localClassPool.insertClassPath(new LoaderClassPath(loader));
        CtClass ctClass = localClassPool.get(className);
        FilterCollect collect = FilterCollect.INSTANCE;
        byte[] classByte = collect.transform(className, null, ctClass);
        String pathname = System.getProperty("user.dir") + "/target/BindCallInfoFilter.class";
        Path path = new File(pathname).toPath();
        Files.write(path, classByte);
        System.out.println(pathname);
    }

    @Test
    public void testOracle() {
        IUserService userService = new UserServiceImpl();
        userService.getUserByOracle();
    }

    @Test
    public void test() {
        IUserService userService = new UserServiceImpl();
        userService.newUser("zhangsan", "123456");
        //userService.showUser("zhangsan");
        userService.getUserByOracle();
        userService.enableUser();
        userService.updateUser("user", "aaa");
        //userService.printUser("aaa");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testThreadLocal() {
        ThreadLocal<String> t1 = new ThreadLocal<String>() {
            @Override
            protected String initialValue() {
                return "aaaa";
            }
        };
        ThreadLocal<Integer> t2 = new ThreadLocal<Integer>() {
            @Override
            protected Integer initialValue() {
                return 1234;
            }
        };
        System.out.println(t1.get());
        System.out.println(t2.get());
        t1.remove();
    }

    @Test
    public void testIn() {
        IUserService userService = new UserServiceImpl();
        userService.inner();
    }


    @Test
    public void buildClassTest() throws NotFoundException, IOException, URISyntaxException {
        final String className = "com.osight.monitor.service.impl.UserServiceImpl";
        ClassLoader loader = getClass().getClassLoader();
        ClassPool localClassPool = new ClassPool();
        localClassPool.insertClassPath(new LoaderClassPath(loader));
        CtClass ctClass = localClassPool.get(className);
        SpringServiceCollect collect = SpringServiceCollect.INSTANCE;
        byte[] classByte = collect.transform(className, null, ctClass);
        String pathname = System.getProperty("user.dir") + "/target/UserServiceImpl.class";
        Path path = new File(pathname).toPath();
        Files.write(path, classByte);
        System.out.println(pathname);

    }
}
