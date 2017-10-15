package com.osight.monitor.service;

import com.osight.monitor.data.UserData;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public interface IUserService {
    UserData newUser(String name, String password);

    void updateUser(String name, String password);

    void enableUser();

    void printUser(String name);

    void showUser(String name);

    String getPassword(String name);
}
