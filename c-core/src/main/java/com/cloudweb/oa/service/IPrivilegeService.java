package com.cloudweb.oa.service;

import com.cloudweb.oa.entity.Privilege;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudweb.oa.exception.ValidateException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author fgf
 * @since 2020-01-23
 */
public interface IPrivilegeService extends IService<Privilege> {
    List<Privilege> getAll();

    Privilege getByPriv(String priv);

    List<Privilege> listByLicense();

    boolean del(String priv);

    boolean updateByPriv(Privilege privilege);

    boolean setPrivs(HttpServletRequest request, String rowOrder, String oldPrivs) throws ValidateException;

    boolean setPrivsList(HttpServletRequest request, String newRowOrder, String oldPrivs) throws ValidateException;

}
