package cn.js.fan.base;

import java.io.*;
import java.net.URL;

import cn.js.fan.util.XMLProperties;
import com.cloudweb.oa.base.IConfigUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import org.jdom.Document;

import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;
import org.jdom.input.SAXBuilder;
import org.jdom.Element;
import java.util.List;
import java.util.Iterator;
import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.db.KeyUnit;
import java.util.HashMap;
import cn.js.fan.base.ObjectCache;
import org.jdom.output.Format;
import java.net.URLDecoder;
import cn.js.fan.util.StrUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.xml.sax.InputSource;

/**
 *
 * <p>Title: 从configDB.xml中读取表、主键、SQL语句等的信息</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class DBConfig {
    RMCache rmCache;
    final String group = "DBConfig";
    
    public static Document doc = null;
    public static Element root = null;
    public static boolean isInited = false;

    private static final String CONFIG_FILENAME = "configDB.xml";

    public DBConfig() {
        rmCache = RMCache.getInstance();
    }

    public static void init() {
        if (!isInited) {
            InputStream inputStream = null;
            try {
                Resource resource = new ClassPathResource(CONFIG_FILENAME);
                inputStream = resource.getInputStream();
                SAXBuilder sb = new SAXBuilder();
                doc = sb.build(inputStream);
                root = doc.getRootElement();

                isInited = true;
            } catch (JDOMException | IOException e) {
                LogUtil.getLog(DBConfig.class).error(e.getMessage());
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        LogUtil.getLog(DBConfig.class ).error(e);
                    }
                }
            }
        }
    }

    public Element getRootElement() {
        return root;
    }

    public void reload() {
        isInited = false;
        try  {
            rmCache.invalidateGroup(group);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
    }

    /**
     * 取得表的配置信息
     * @param objectName String
     * @return DBTable
     */
    public DBTable getDBTable(String objectName) {
        DBTable dt = null;
        try {
            dt = (DBTable)rmCache.getFromGroup(objectName, group);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("getDBTable1:" + e.getMessage());
        }
        if (dt==null) {
            init();
            Element tables = root.getChild("tables");
            List list = tables.getChildren();
            if (list != null) {
                Iterator ir = list.iterator();
                while (ir.hasNext()) {
                    Element child = (Element) ir.next();
                    String objName = child.getAttributeValue("objName");
                    // LogUtil.getLog(getClass()).info("objName=" + objName + " objectName=" + objectName);
                    if (objName.equals(objectName)) {
                        String name = child.getAttributeValue("name");
                        String create = child.getChildText("create");
                        String load = child.getChildText("load");
                        String queryList = child.getChildText("list");
                        String save = child.getChildText("save");
                        String del = child.getChildText("del");
                        String objCache = child.getChildText("objectCache");

                        boolean objCachable = !StrUtil.getNullStr(child.getChildText("objCachable")).equals("false");
                        boolean listCachable = !StrUtil.getNullStr(child.getChildText("listCachable")).equals("false");

                        ObjectCache oc = null;
                        try {
                            oc = (ObjectCache) Class.forName(objCache).newInstance();
                        } catch (Exception e) {
                            LogUtil.getLog(getClass()).error("getDBTable:" + e.getMessage());
                        }

                        dt = new DBTable(name, objName);
                        dt.setQueryCreate(create);
                        dt.setQueryLoad(load);
                        dt.setQueryList(queryList);
                        dt.setQuerySave(save);
                        dt.setQueryDel(del);
                        dt.setObjectCache(oc);
                        dt.setObjCachable(objCachable);
                        dt.setListCachable(listCachable);

                        Element pk = child.getChild("primaryKey");
                        String pkType = pk.getAttributeValue("type");
                        if (pkType.equals("String")) {
                            Element unit = pk.getChild("unit");
                            String pkName = unit.getChildText("name");
                            dt.setPrimaryKey(new PrimaryKey(pkName, PrimaryKey.TYPE_STRING));
                        } else if (pkType.equals("int")) {
                            Element unit = pk.getChild("unit");
                            String pkName = unit.getChildText("name");
                            dt.setPrimaryKey(new PrimaryKey(pkName, PrimaryKey.TYPE_INT));
                        } else if (pkType.equals("long")) {
                            Element unit = pk.getChild("unit");
                            String pkName = unit.getChildText("name");
                            dt.setPrimaryKey(new PrimaryKey(pkName, PrimaryKey.TYPE_LONG));
                        }
                        else if (pkType.equals("Date")) {
                            Element unit = pk.getChild("unit");
                            String pkName = unit.getChildText("name");
                            dt.setPrimaryKey(new PrimaryKey(pkName, PrimaryKey.TYPE_DATE));
                        }
                        else if (pkType.equals("compound")) {
                            List listpmk = pk.getChildren("unit");
                            Iterator irunit = listpmk.iterator();
                            HashMap key = new HashMap();
                            int orders = 0;
                            while (irunit.hasNext()) {
                                Element e = (Element) irunit.next();
                                String keyName = e.getChildTextTrim("name");
                                String keyType = e.getChildTextTrim("type");
                                switch (keyType) {
                                    case "String":
                                        key.put(keyName,
                                                new KeyUnit(PrimaryKey.TYPE_STRING, orders));
                                        break;
                                    case "int":
                                        key.put(keyName, new KeyUnit(PrimaryKey.TYPE_INT, orders));
                                        break;
                                    case "long":
                                        key.put(keyName, new KeyUnit(PrimaryKey.TYPE_LONG, orders));
                                        break;
                                    case "Date":
                                        key.put(keyName, new KeyUnit(PrimaryKey.TYPE_DATE, orders));
                                        break;
                                    default:
                                        LogUtil.getLog(getClass()).error("getDBTable: 解析表" + name + "的主键时，type=" + keyType + " 未知!");
                                        break;
                                }
                                orders ++;
                            }
                            dt.primaryKey = new PrimaryKey(key);
                        } else {
                            LogUtil.getLog(getClass()).info("getDBTable: 解析表" + name + "的主键时，type=" + pkType + " 未知!");
                        }

                        try {
                            rmCache.putInGroup(objName, group,
                                               dt);
                        } catch (Exception e) {
                            LogUtil.getLog(getClass()).error("getDBTable:" + e.getMessage());
                        }
                        return dt;
                    }
                }
            }
        }
        else {
            dt.renew();
        }
        return dt;
    }
}


