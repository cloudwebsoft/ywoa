package com.cloudweb.oa.service;

import com.cloudweb.oa.bean.User;
import org.springframework.stereotype.Service;

@Service
public interface UserRegistService {
	boolean register(User user);

	User loginCheck(User user);
}
