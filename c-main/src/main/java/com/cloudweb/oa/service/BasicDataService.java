package com.cloudweb.oa.service;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.utils.ConstUtil;
import com.redmoon.oa.basic.TreeSelectDb;
import com.redmoon.oa.basic.TreeSelectMgr;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.visual.FormDAO;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BasicDataService {

    public String getNewNodeCode(String rootCode, String parentCode) {
        String newNodeCode = "";
        try {
            TreeSelectMgr dir = new TreeSelectMgr();
            int codeCount = 0;
            int index = parentCode.length();
            TreeSelectDb pdd = new TreeSelectDb();
            pdd = pdd.getTreeSelectDb(parentCode);//得到父节点
            Vector children = dir.getChildren(parentCode);
            if (children.isEmpty()) {
                codeCount = 1;
            } else {
                int count = children.size();
                Iterator ri = children.iterator();
                int i = 0;
                int[] arr = new int[count];
                while (ri.hasNext()) {
                    TreeSelectDb childlf = (TreeSelectDb) ri.next();
                    String eachCode = childlf.getCode();
                    // 老版中有些节点是自定义的，而不是自动生成的，所以编码没有规律
                    if (index > eachCode.length() - 1) {
                        arr[i] = i;
                    } else {
                        String diffCode = eachCode.substring(index);//去掉父节点code的前缀
                        int NumberCode = Integer.valueOf(diffCode);
                        arr[i] = NumberCode;
                    }
                    i++;
                }
                Arrays.sort(arr);
                codeCount = arr[arr.length - 1] + 1;
            }

            int num = codeCount;
            TreeSelectDb dd = null;
            do {
                if (rootCode.equals(parentCode)) {
                    newNodeCode = rootCode + StrUtil.PadString(String.valueOf(num), '0', 4, true);
                } else {
                    newNodeCode = parentCode + StrUtil.PadString(String.valueOf(num), '0', 4, true);
                }
                num++;
                dd = new TreeSelectDb(newNodeCode);
            } while (dd != null && dd.isLoaded());
        } catch (ErrMsgException e) {
            e.printStackTrace();
        }
        return newNodeCode;
    }

    /**
     * 根据层级取得树形结构节点描述
     * @param layer
     * @return
     * @throws ErrMsgException
     */
    public FormDAO getNodeDescByLayer(String basicCode, int layer) throws ErrMsgException {
        String sql = "select id from form_table_" + ConstUtil.BASIC_TREE_NODE + " where code=" + StrUtil.sqlstr(basicCode);
        FormDAO fdao = new FormDAO();
        boolean isFound = false;
        List<FormDAO> list = fdao.list(ConstUtil.BASIC_TREE_NODE, sql);
        if (list.size() == 0) {
            throw new ErrMsgException("请设置节点描述");
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
        String sql = "select id from form_table_" + formCode + " where " + moduleField + "=" + StrUtil.sqlstr(nodeCode);
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        FormDAO fdaoModule = new FormDAO(fd);
        List<FormDAO> list = new ArrayList<>();
        try {
             list = fdaoModule.list(formCode, sql);
        } catch (ErrMsgException e) {
            e.printStackTrace();
        }
        return list;
    }
}
