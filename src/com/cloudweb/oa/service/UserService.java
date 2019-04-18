package com.cloudweb.oa.service;

import com.cloudweb.oa.bean.User;

public interface UserService {
	public boolean register(User user);

	public User loginCheck(User user);
}
