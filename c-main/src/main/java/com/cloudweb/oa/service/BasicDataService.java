package com.cloudweb.oa.service;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.api.IBasicDataService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.basic.TreeSelectDb;
import com.redmoon.oa.basic.TreeSelectMgr;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.visual.FormDAO;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.*;

@Service
public class BasicDataService implements IBasicDataService {

    public String getNewNodeCode(String rootCode, String parentCode) {
        String newNodeCode = "";
        try {
            TreeSelectMgr dir = new TreeSelectMgr();
            int codeCount = 0;
            int index = parentCode.length();
            Vector<TreeSelectDb> children = dir.getChildren(parentCode);
            if (children.isEmpty()) {
                codeCount = 1;
            } else {
                int count = children.size();
                int i = 0;
                int[] arr = new int[count];
                for (TreeSelectDb childlf : children) {
                    String eachCode = childlf.getCode();
                    // 老版中有些节点是自定义的，而不是自动生成的，所以编码没有规律
                    if (index > eachCode.length() - 1) {
                        arr[i] = i;
                    } else {
                        String diffCode = eachCode.substring(index);//去掉父节点code的前缀
                        int numberCode = Integer.parseInt(diffCode);
                        arr[i] = numberCode;
                    }
                    i++;
                }
                Arrays.sort(arr);
                codeCount = arr[arr.length - 1] + 1;
            }

            int num = codeCount;
            TreeSelectDb dd = new TreeSelectDb();
            do {
                if (rootCode.equals(parentCode)) {
                    newNodeCode = rootCode + StrUtil.PadString(String.valueOf(num), '0', 4, true);
                } else {
                    newNodeCode = parentCode + StrUtil.PadString(String.valueOf(num), '0', 4, true);
                }
                num++;
                dd = dd.getTreeSelectDb(newNodeCode);
            } while (dd.isLoaded());
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return newNodeCode;
    }

    /**
     * 根据层级取得树形结构节点描述
     * @param layer
     * @return
     * @throws ErrMsgException
     */
    public FormDAO getNodeDescByLayer(String basicCode, int layer) {
        String sql = "select id from ft_" + ConstUtil.BASIC_TREE_NODE + " where code=" + StrUtil.sqlstr(basicCode);
        FormDAO fdao = new FormDAO();
        boolean isFound = false;
        List<FormDAO> list = fdao.list(ConstUtil.BASIC_TREE_NODE, sql);
        if (list.size() == 0) {
            return null;
        } else {
            String strLayer = String.valueOf(layer);
            for (FormDAO formDAO : list) {
                if (strLayer.equals(formDAO.getFieldValue("layer"))) {
                    fdao = formDAO;
                    isFound = true;
                    break;
                }
            }
            if (!isFound) {
                fdao = list.get(0);
            }
        }
        return fdao;
    }

    public List<FormDAO> listByNode(String formCode, String moduleField, String nodeCode) {
        String sql = "select id from ft_" + formCode + " where " + moduleField + "=" + StrUtil.sqlstr(nodeCode);
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        FormDAO fdaoModule = new FormDAO(fd);
        List<FormDAO> list = new ArrayList<>();
        try {
             list = fdaoModule.list(formCode, sql);
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return list;
    }

    @Override
    public boolean delTreeSelect(String code) {
        TreeSelectDb tsd = new TreeSelectDb();
        tsd = tsd.getTreeSelectDb(code);
        return tsd.del();
    }

    @Override
    public boolean initTreeSelect(String rootCode, String rootName) {
        TreeSelectDb tsd = new TreeSelectDb();
        tsd = tsd.getTreeSelectDb(rootCode);
        if (tsd!=null && tsd.isLoaded()) {
            return true;
        }
        int childCount = 0, orders = 1;
        String parent_code = "-1";

        String insertsql = "insert into oa_tree_select (code,name,parentCode,description,orders,rootCode,childCount,layer, link, pre_code, form_code, meta_data) values (";
        insertsql += StrUtil.sqlstr(rootCode) + "," + StrUtil.sqlstr(rootName) +
                "," + StrUtil.sqlstr(parent_code) +
                "," + StrUtil.sqlstr("") + "," +
                orders + "," + StrUtil.sqlstr(rootCode) + "," +
                childCount + ",1, " + StrUtil.sqlstr("") + "," + StrUtil.sqlstr("") + "," + StrUtil.sqlstr("") + "," + StrUtil.sqlstr("") + ")";

        int r = 0;
        JdbcTemplate jt = new JdbcTemplate();
        try {
            r = jt.executeUpdate(insertsql);
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        return r==1;
    }
}
