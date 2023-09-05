package com.cloudweb.oa.service.impl;

import com.cloudweb.oa.service.UserRegistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cloudweb.oa.bean.User;
import com.cloudweb.oa.dao.UserDao;

@Service
public class UserRegistServiceImpl implements UserRegistService {

	@Autowired
	private UserDao userDao;

	@Override
	public User loginCheck(User user) {
		User u = userDao.findUserByUserName(user.getName());
		if (user.getPassword().equals(u.getPassword())) {
			return u;
		} else {
			return null;
		}
	}

	@Override
	public boolean register(User user) {
		User u = userDao.findUserByUserName(user.getName());
		if (u.getId() == 0) {
			userDao.register(user);
			return true;
		} else {
			return false;
		}
	}

}
