package com.cloudweb.oa.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudweb.oa.bean.User;
import com.cloudweb.oa.dao.UserDao;
import com.cloudweb.oa.service.UserService;

@Service
@Transactional
public class UserServiceImpl implements UserService {

	@Autowired
	private UserDao userDao;

	@Override
	public User loginCheck(User user) {
		User u = userDao.findUserByUserName(user.getName());
		System.out.println("id=" + u.getId() + ",  userName=" + u.getName()
				+ ", password=" + u.getPassword());
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
			System.out.println("id=" + u.getId() + ",  userName=" + u.getName()
					+ ", password=" + u.getPassword());
			return false;
		}
	}

}
