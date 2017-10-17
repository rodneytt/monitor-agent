package com.osight.monitor.service.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;
import com.osight.monitor.control.ThreadLocalUtil;
import com.osight.monitor.data.UserData;
import com.osight.monitor.service.IUserService;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class UserServiceImpl implements IUserService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public UserData newUser(String name, String password) {
        UserData user = new UserData();
        user.setId("1");
        user.setName(name);
        user.setPassword(password);
        return user;
    }

    public void inner() {
        newUser("zhangsan", "123456");
        getUserByOracle();
        enableUser();
        updateUser("user", "aaa");
    }

    @Override
    public void printThreadLocal() {
        System.out.println(Thread.currentThread() + ":" + ThreadLocalUtil.getResourceMap().size());
    }

    @Override
    public void updateUser(String name, String password) {

    }

    @Override
    public void enableUser() {
    }

    @Override
    public void printUser(String name) {
        showUser(name);
    }

    @Override
    public String getPassword(String name) {
        return "aaaa";
    }

    @Override
    public String getUserByOracle() {

        //声明Connection对象
        Connection con;
        //驱动程序名
        String driver = "oracle.jdbc.OracleDriver";
        //URL指向要访问的数据库名mydata
        String url = "jdbc:oracle:thin:@172.16.1.97:1521:ora9";
        //MySQL配置时的用户名
        String user = "apply";
        //MySQL配置时的密码
        String password = "zxcvb";

        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(url);
        dataSource.setPassword(password);
        dataSource.setUsername(user);
        try {
            dataSource.init();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        //遍历查询结果集
        try {
            con = dataSource.getConnection();
            //要执行的SQL语句
            String sql = "select * from yzdwsx where rownum<2";
            //2.创建statement类对象，用来执行SQL语句！！
            PreparedStatement statement = con.prepareStatement(sql);
            //3.ResultSet类，用来存放获取的结果集！！
            ResultSet rs = statement.executeQuery();

            String job = null;
            String id = null;
            while (rs.next()) {
                //获取stuname这列数据
                job = rs.getString("dwmc");
                //获取stuid这列数据
                id = rs.getString("dwdm");

                //输出结果
                System.out.println(id + "\t" + job);
            }
            rs.close();
            con.close();
        } catch (SQLException e) {
            //数据库连接失败异常处理
            e.printStackTrace();
        } finally {
            dataSource.close();
        }
        return null;
    }

    @Override
    public void showUser(String name) {
        updateUser("aa", "bb");
        //声明Connection对象
        Connection con;
        //驱动程序名
        String driver = "com.mysql.jdbc.Driver";
        //URL指向要访问的数据库名mydata
        String url = "jdbc:mysql://localhost:3306/test";
        //MySQL配置时的用户名
        String user = "root";
        //MySQL配置时的密码
        String password = "12qwaszx";
        //遍历查询结果集
        try {
            //加载驱动程序
            Class.forName(driver);
            //1.getConnection()方法，连接MySQL数据库！！
            con = DriverManager.getConnection(url, user, password);
            //要执行的SQL语句
            String sql = "select * from t_user";
            //2.创建statement类对象，用来执行SQL语句！！
            PreparedStatement statement = con.prepareStatement(sql);
            //3.ResultSet类，用来存放获取的结果集！！
            ResultSet rs = statement.executeQuery();

            String job = null;
            String id = null;
            while (rs.next()) {
                //获取stuname这列数据
                job = rs.getString("name");
                //获取stuid这列数据
                id = rs.getString("id");

                //输出结果
                System.out.println(id + "\t" + job);
            }
            rs.close();
            con.close();
        } catch (ClassNotFoundException e) {
            //数据库驱动类异常处理
            System.out.println("Sorry,can`t find the Driver!");
            e.printStackTrace();
        } catch (SQLException e) {
            //数据库连接失败异常处理
            e.printStackTrace();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        } finally {
        }
        enableUser();
    }
}
