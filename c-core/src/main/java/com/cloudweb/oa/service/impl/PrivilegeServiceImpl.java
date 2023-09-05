package com.cloudweb.oa.service.impl;

import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.entity.Privilege;
import com.cloudweb.oa.exception.ValidateException;
import com.cloudweb.oa.mapper.PrivilegeMapper;
import com.cloudweb.oa.service.IPrivilegeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudweb.oa.utils.ConstUtil;
import com.redmoon.oa.kernel.License;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author fgf
 * @since 2020-01-23
 */
@Service
public class PrivilegeServiceImpl extends ServiceImpl<PrivilegeMapper, Privilege> implements IPrivilegeService {
    @Autowired
    PrivilegeMapper privilegeMapper;

    @Override
    public List<Privilege> getAll() {
        QueryWrapper<Privilege> qw = new QueryWrapper<>();
        qw.orderByAsc("orders");
        return list(qw);
    }

    @Override
    public boolean del(String priv) {
        QueryWrapper<Privilege> qw = new QueryWrapper<>();
        qw.eq("priv", priv);
        return remove(qw);
    }

    @Override
    public Privilege getByPriv(String priv) {
        QueryWrapper<Privilege> qw = new QueryWrapper<>();
        qw.eq("priv", priv);
        return getOne(qw, false);
    }

    @Override
    public boolean updateByPriv(Privilege privilege) {
        QueryWrapper<Privilege> qw = new QueryWrapper<>();
        qw.eq("priv", privilege.getPriv());
        return update(privilege, qw);
    }

    public String getSqlList() {
        License license = License.getInstance();
        String sql = "select priv,description,isSystem,layer,orders from privilege";
        if (license.isGov()) {
            sql = "select priv,description,isSystem,layer,orders from privilege where kind=" + ConstUtil.PRIV_KIND_DEFAULT + " or kind=" + ConstUtil.PRIV_KIND_GOV;
        } else if (license.isGov()) {
            sql = "select priv,description,isSystem,layer,orders from privilege where kind=" + ConstUtil.PRIV_KIND_DEFAULT + " or kind=" + ConstUtil.PRIV_KIND_COM;
        }

        sql += " order by orders asc";
        return sql;
    }

    @Override
    public List<Privilege> listByLicense() {
        return privilegeMapper.listBySql(getSqlList());
    }

    @Override
    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public boolean setPrivs(HttpServletRequest request, String rowOrder, String oldPrivs) throws ValidateException {
        String[] uniqueIndexes = null;
        if (rowOrder == null) {
            uniqueIndexes = new String[0];
        } else {
            uniqueIndexes = rowOrder.split(",");
        }

        boolean re = false;

        // 找出被删除的项
        StringBuffer privsToDel = new StringBuffer();
        Vector privToDelV = new Vector();
        String[] ary = StrUtil.split(oldPrivs, ",");
        for (int k = 0; k < ary.length; k++) {
            String oldPriv = ary[k];
            boolean isFound = false;
            for (int i = 0; i < uniqueIndexes.length; i++) {
                String priv = ParamUtil.get(request, "tblPrivs_priv_" + uniqueIndexes[i]);
                if (oldPriv.equals(priv)) {
                    isFound = true;
                    break;
                }
            }
            if (!isFound) {
                privToDelV.addElement(oldPriv);
                StrUtil.concat(privsToDel, ",", StrUtil.sqlstr(oldPriv));
            }
        }

        for (int i = 0; i < uniqueIndexes.length; i++) {
            String desc = ParamUtil.get(request, "tblPrivs_desc_" + uniqueIndexes[i]);
            String desc2 = ParamUtil.get(request, "tblPrivs_desc2_" + uniqueIndexes[i]);
            if (!"".equals(desc) && !"".equals(desc2)) {
                throw new ValidateException(desc + " 只能填写一级或二级，不能都填写！");
            }
            String priv = ParamUtil.get(request, "tblPrivs_priv_" + uniqueIndexes[i]);
            int layer = 1;
            if ("".equals(desc)) {
                desc = desc2;
                layer = 2;
            }

            Privilege privilege = new Privilege();
            privilege.setDescription(desc);
            privilege.setOrders(i + 1);
            privilege.setLayer(layer);
            privilege.setPriv(priv);

            re = updateByPriv(privilege);
        }

        if (re) {
            if (privsToDel.length() > 0) {
                Iterator irToDel = privToDelV.iterator();
                while (irToDel.hasNext()) {
                    String privDel = (String) irToDel.next();
                    del(privDel);
                }
            }
        }
        return re;
    }


    @Override
    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public boolean setPrivsList(HttpServletRequest request, String newRowOrder, String oldPrivs) throws ValidateException {
        String[] uniqueIndexes = null;
        if (newRowOrder == null) {
            uniqueIndexes = new String[0];
        } else {
            uniqueIndexes = newRowOrder.split(",");
        }

        boolean re = false;

        // 找出被删除的项
        StringBuffer privsToDel = new StringBuffer();
        Vector privToDelV = new Vector();
        String[] ary = StrUtil.split(oldPrivs, ",");
        for (int k = 0; k < ary.length; k++) {
            String oldPriv = ary[k];
            boolean isFound = false;
            for (int i = 0; i < uniqueIndexes.length; i++) {
                String priv = ParamUtil.get(request, "new_rivs_" + uniqueIndexes[i]);
                if (oldPriv.equals(priv)) {
                    isFound = true;
                    break;
                }
            }
            if (!isFound) {
                privToDelV.addElement(oldPriv);
                StrUtil.concat(privsToDel, ",", StrUtil.sqlstr(oldPriv));
            }
        }

        for (int i = 0; i < uniqueIndexes.length; i++) {
            String desc = ParamUtil.get(request, "new_rivs_desc_" + uniqueIndexes[i]);
            String desc2 = ParamUtil.get(request, "new_rivs_desc2_" + uniqueIndexes[i]);
            if (!"".equals(desc) && !"".equals(desc2)) {
                throw new ValidateException(desc + " 只能填写一级或二级，不能都填写！");
            }
            String priv = ParamUtil.get(request, "new_rivs_" + uniqueIndexes[i]);
            int layer = 1;
            if ("".equals(desc)) {
                desc = desc2;
                layer = 2;
            }

            Privilege privilege = new Privilege();
            privilege.setDescription(desc);
            privilege.setOrders(i + 1);
            privilege.setLayer(layer);
            privilege.setPriv(priv);

            re = updateByPriv(privilege);
        }

        if (re) {
            if (privsToDel.length() > 0) {
                Iterator irToDel = privToDelV.iterator();
                while (irToDel.hasNext()) {
                    String privDel = (String) irToDel.next();
                    del(privDel);
                }
            }
        }
        return re;
    }


}
