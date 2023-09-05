package com.cloudweb.oa.module.desktop;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.visual.FormDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class DesktopCardUtil {
    String formCode = "config_card";

    public List<DesktopCard> listByPortal(long portalId) {
        List<DesktopCard> list = new ArrayList<>();
        String sql = "select id from ft_" + formCode + " where kind='门户型' and portal_id=" + portalId + " and is_show=1 order by orders";
        FormDAO fdao = new FormDAO();
        try {
            Vector<FormDAO> v = fdao.list(formCode, sql);
            for (FormDAO formDAO : v) {
                DesktopCard desktopCard = new DesktopCard();
                desktopCard.setId(formDAO.getId());
                desktopCard.setName(formDAO.getFieldValue("name"));
                desktopCard.setTitle(formDAO.getFieldValue("title"));
                desktopCard.setCardType(formDAO.getFieldValue("card_type"));
                desktopCard.setMenuItem(formDAO.getFieldValue("menu_item"));
                desktopCard.setStartVal(StrUtil.toInt(formDAO.getFieldValue("start_val"), 0));
                desktopCard.setLink("1".equals(formDAO.getFieldValue("is_link")));
                desktopCard.setUrl(formDAO.getFieldValue("url"));
                desktopCard.setUnit(formDAO.getFieldValue("unit"));
                desktopCard.setBgColor(formDAO.getFieldValue("bg_color"));
                desktopCard.setIcon(formDAO.getFieldValue("icon_font"));
                desktopCard.setModuleCode(formDAO.getFieldValue("module_code"));
                desktopCard.setRoles(formDAO.getFieldValue("roles"));
                desktopCard.setStyle(StrUtil.toInt(formDAO.getFieldValue("style"), 0));
                desktopCard.setEndValfunc(formDAO.getFieldValue("end_val_func"));
                desktopCard.setColor(formDAO.getFieldValue("color"));
                list.add(desktopCard);
            }
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return list;
    }

    public DesktopCard getCardById(long id) {
        DesktopCard desktopCard = new DesktopCard();
        String sql = "select id from ft_" + formCode + " where id=" + id;
        FormDAO fdao = new FormDAO();
        try {
            Vector<FormDAO> v = fdao.list(formCode, sql);
            for (FormDAO formDAO : v) {
                desktopCard.setId(formDAO.getId());
                desktopCard.setName(formDAO.getFieldValue("name"));
                desktopCard.setTitle(formDAO.getFieldValue("title"));
                desktopCard.setCardType(formDAO.getFieldValue("card_type"));
                desktopCard.setMenuItem(formDAO.getFieldValue("menu_item"));
                desktopCard.setStartVal(StrUtil.toInt(formDAO.getFieldValue("start_val"), 0));
                desktopCard.setLink("1".equals(formDAO.getFieldValue("is_link")));
                desktopCard.setUrl(formDAO.getFieldValue("url"));
                desktopCard.setUnit(formDAO.getFieldValue("unit"));
                desktopCard.setBgColor(formDAO.getFieldValue("bg_color"));
                desktopCard.setIcon(formDAO.getFieldValue("icon_font"));
                desktopCard.setModuleCode(formDAO.getFieldValue("module_code"));
                desktopCard.setRoles(formDAO.getFieldValue("roles"));
                desktopCard.setStyle(StrUtil.toInt(formDAO.getFieldValue("style"), 0));
                desktopCard.setEndValfunc(formDAO.getFieldValue(("end_val_func")));
            }
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return desktopCard;
    }

    public List<DesktopCard> listByApplication(long portalId, int count) {
        List<DesktopCard> list = new ArrayList<>();
        String sql = "select id from ft_" + formCode + " where kind='应用型' and portal_id=" + portalId + " and is_show=1 order by orders";
        FormDAO fdao = new FormDAO();
        try {
            Vector<FormDAO> v = fdao.listResult(formCode, sql, 1, count).getResult();
            for (FormDAO formDAO : v) {
                DesktopCard desktopCard = new DesktopCard();
                desktopCard.setId(formDAO.getId());
                desktopCard.setName(formDAO.getFieldValue("name"));
                desktopCard.setTitle(formDAO.getFieldValue("title"));
                desktopCard.setCardType(formDAO.getFieldValue("card_type"));
                desktopCard.setMenuItem(formDAO.getFieldValue("menu_item"));
                desktopCard.setStartVal(StrUtil.toInt(formDAO.getFieldValue("start_val"), 0));
                desktopCard.setLink("1".equals(formDAO.getFieldValue("is_link")));
                desktopCard.setUrl(formDAO.getFieldValue("url"));
                desktopCard.setUnit(formDAO.getFieldValue("unit"));
                desktopCard.setBgColor(formDAO.getFieldValue("bg_color"));
                desktopCard.setIcon(formDAO.getFieldValue("icon_font"));
                desktopCard.setModuleCode(formDAO.getFieldValue("module_code"));
                desktopCard.setRoles(formDAO.getFieldValue("roles"));
                desktopCard.setEndValfunc(formDAO.getFieldValue(("end_val_func")));
                list.add(desktopCard);
            }
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return list;
    }

    public List<DesktopCard> listAll() {
        List<DesktopCard> list = new ArrayList<>();
        String sql = "select id from ft_" + formCode + " where is_show=1 order by orders";
        FormDAO fdao = new FormDAO();
        try {
            Vector<FormDAO> v = fdao.list(formCode, sql);
            for (FormDAO formDAO : v) {
                DesktopCard desktopCard = new DesktopCard();
                desktopCard.setId(formDAO.getId());
                desktopCard.setName(formDAO.getFieldValue("name"));
                desktopCard.setTitle(formDAO.getFieldValue("title"));
                desktopCard.setCardType(formDAO.getFieldValue("card_type"));
                desktopCard.setMenuItem(formDAO.getFieldValue("menu_item"));
                desktopCard.setStartVal(StrUtil.toInt(formDAO.getFieldValue("start_val"), 0));
                desktopCard.setLink("1".equals(formDAO.getFieldValue("is_link")));
                desktopCard.setUrl(formDAO.getFieldValue("url"));
                desktopCard.setUnit(formDAO.getFieldValue("unit"));
                desktopCard.setBgColor(formDAO.getFieldValue("bg_color"));
                desktopCard.setIcon(formDAO.getFieldValue("icon_font"));
                desktopCard.setModuleCode(formDAO.getFieldValue("module_code"));
                desktopCard.setRoles(formDAO.getFieldValue("roles"));
                desktopCard.setEndValfunc(formDAO.getFieldValue(("end_val_func")));
                list.add(desktopCard);
            }
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return list;
    }

    public List<DesktopCard> listByModule(String moduleCode) {
        List<DesktopCard> list = new ArrayList<>();
        String sql = "select id from ft_" + formCode + " where kind='模块型' and card_module=" + StrUtil.sqlstr(moduleCode) + " and is_show=1 order by orders";
        FormDAO fdao = new FormDAO();
        try {
            Vector<FormDAO> v = fdao.list(formCode, sql);
            for (FormDAO formDAO : v) {
                DesktopCard desktopCard = new DesktopCard();
                desktopCard.setId(formDAO.getId());
                desktopCard.setName(formDAO.getFieldValue("name"));
                desktopCard.setTitle(formDAO.getFieldValue("title"));
                desktopCard.setCardType(formDAO.getFieldValue("card_type"));
                desktopCard.setMenuItem(formDAO.getFieldValue("menu_item"));
                desktopCard.setStartVal(StrUtil.toInt(formDAO.getFieldValue("start_val"), 0));
                desktopCard.setLink("1".equals(formDAO.getFieldValue("is_link")));
                desktopCard.setUrl(formDAO.getFieldValue("url"));
                desktopCard.setUnit(formDAO.getFieldValue("unit"));
                desktopCard.setBgColor(formDAO.getFieldValue("bg_color"));
                desktopCard.setIcon(formDAO.getFieldValue("icon_font"));
                desktopCard.setModuleCode(formDAO.getFieldValue("module_code"));
                desktopCard.setRoles(formDAO.getFieldValue("roles"));
                desktopCard.setEndValfunc(formDAO.getFieldValue(("end_val_func")));
                list.add(desktopCard);
            }
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return list;
    }
}
