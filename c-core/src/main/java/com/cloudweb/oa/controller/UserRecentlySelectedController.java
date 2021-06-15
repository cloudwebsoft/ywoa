package com.cloudweb.oa.controller;


import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.entity.UserRecentlySelected;
import com.cloudweb.oa.service.impl.UserRecentlySelectedServiceImpl;
import com.cloudweb.oa.utils.SpringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author fgf
 * @since 2020-02-02
 */
@RestController
@RequestMapping("/user-recently-selected")
public class UserRecentlySelectedController {
    @Autowired
    UserRecentlySelectedServiceImpl userRecentlySelectedService;

    /**
     * 更新最近选择用户列表
     * @param userNames
     * @return
     */
    @RequestMapping(value = "/update", produces = {"text/html;", "application/json;charset=UTF-8;"})
    public String update(String userNames) {
        String logonUserName = SpringUtil.getUserName();
        String[] users = StrUtil.split(userNames, ",");
        //从表user_recently_selected中查询出此登陆用户已经选择的所有用户
        QueryWrapper<UserRecentlySelected> qw = new QueryWrapper<>();
        qw.eq("name", logonUserName);
        List<UserRecentlySelected> list = userRecentlySelectedService.list(qw);

        for (String userName : users) {
            boolean isFind = false;
            for (UserRecentlySelected userRecentlySelected : list) {
                if (userName.equals(userRecentlySelected.getUserName())) {
                    userRecentlySelected.setTimes(userRecentlySelected.getTimes() + 1);
                    qw = new QueryWrapper<>();
                    qw.eq("name", logonUserName).eq("userName", userName);
                    userRecentlySelected.update(qw);

                    isFind = true;
                    break;
                }
            }
            if (isFind) {
                UserRecentlySelected userRecentlySelected = new UserRecentlySelected();
                userRecentlySelected.setTimes(1);
                userRecentlySelected.setName(logonUserName);
                userRecentlySelected.setUserName(userName);
                qw = new QueryWrapper<>();
                qw.eq("name", logonUserName).eq("userName", userName);
                userRecentlySelected.update(qw);
            }
        }

        JSONObject json = new JSONObject();
        json.put("ret", "1");
        return json.toString();
    }
}
