package com.cloudweb.oa.service;

import cn.js.fan.util.ErrMsgException;
import com.cloudweb.oa.entity.Menu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Vector;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author fgf
 * @since 2020-02-15
 */
public interface IMenuService extends IService<Menu> {
    List<Menu> getChildren(String parentCode);

    Menu getMenu(String code);

    String getFullName(Menu menu);

    String getRealLink(Menu menu);

    String getJsonTreeString(String roleCode);

    boolean updateByCode(Menu menu);

    void repairTree(Menu menu);

    String getJsonString();

    List getAllChild(List<Menu> list, Menu leaf);

    void ShowDirectoryAsOptionsToString(StringBuffer sb, Menu leaf, int rootlayer);

    boolean create(Menu menu);

    boolean del(Menu menu);

    String getName(Menu menu);

    boolean move(String code, String parentCode, int position) throws ErrMsgException;

    String getRealLinkBack(Menu menu);
}
