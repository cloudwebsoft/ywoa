package com.cloudweb.oa.vo;

import com.cloudweb.oa.entity.Menu;
import com.cloudweb.oa.entity.Role;
import com.cloudweb.oa.entity.Group;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserAuthorityVO {
    String code;
    String name;

    Integer layer;

    Boolean isAdmin = false;

    /**
     * 是否被授权
     */
    boolean authorized;

    /**
     * 角色被授权
     */
    boolean roleAuthorized;

    /**
     * 拥有权限的角色
     */
    List<Role> roleList = new ArrayList<>();

    /**
     * 用户组被授权
     */
    boolean groupAuthorized;

    /**
     * 拥有权限的用户组
     */
    List<Group> groupList = new ArrayList<>();

    /**
     * 拥有权限的菜单
     */
    List<Menu> menuList = new ArrayList<>();

}
