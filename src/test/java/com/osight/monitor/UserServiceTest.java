package com.osight.monitor;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.osight.monitor.collect.ActionCollect;
import com.osight.monitor.collect.SpringServiceCollect;
import com.osight.monitor.control.RpcInfo;
import com.osight.monitor.control.RpcThreadLocalManager;
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
        RpcThreadLocalManager.bind(info);
    }

    @After
    public void after() {
        RpcThreadLocalManager.unbind();
    }

    @Test
    public void testClass() {
        IUserService userService = new UserServiceImpl();
        System.out.println(userService.getClass().getName());
        System.out.println(userService.getClass().getCanonicalName());
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
        byte[] classByte = collect.transform(loader, className, null, ctClass);
        String pathname = System.getProperty("user.dir") + "/target/UserAction.class";
        Path path = new File(pathname).toPath();
        Files.write(path, classByte);
        System.out.println(pathname);

    }

    @Test
    public void test() {
        IUserService userService = new UserServiceImpl();
        userService.newUser("zhangsan", "123456");
        userService.showUser("zhangsan");
        userService.enableUser();
        userService.updateUser("user", "aaa");
        userService.printUser("aaa");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void buildClassTest() throws NotFoundException, IOException, URISyntaxException {
        final String className = "com.osight.monitor.service.impl.UserServiceImpl";
        ClassLoader loader = getClass().getClassLoader();
        ClassPool localClassPool = new ClassPool();
        localClassPool.insertClassPath(new LoaderClassPath(loader));
        CtClass ctClass = localClassPool.get(className);
        SpringServiceCollect collect = SpringServiceCollect.INSTANCE;
        byte[] classByte = collect.transform(loader, className, null, ctClass);
        String pathname = System.getProperty("user.dir") + "/target/UserServiceImpl.class";
        Path path = new File(pathname).toPath();
        Files.write(path, classByte);
        System.out.println(pathname);

    }
}
