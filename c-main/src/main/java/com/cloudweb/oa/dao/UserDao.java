package com.cloudweb.oa.dao;

import com.cloudweb.oa.bean.User;

public interface UserDao {
	public void register(User user);
    public User findUserByUserName(final String userName);
}
