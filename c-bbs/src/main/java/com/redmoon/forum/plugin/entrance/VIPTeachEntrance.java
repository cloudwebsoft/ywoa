package com.redmoon.forum.plugin.entrance;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ErrMsgException;
import com.redmoon.forum.plugin.base.IPluginEntrance;
import com.redmoon.forum.BoardEntranceDb;
import com.redmoon.forum.Privilege;
import cn.js.fan.util.ParamUtil;
import java.net.URL;
import org.jdom.Element;
import org.jdom.Document;
import java.io.FileInputStream;
import org.jdom.input.SAXBuilder;
import org.apache.log4j.Logger;
import java.util.List;
import java.util.Iterator;
import java.net.URLDecoder;
import cn.js.fan.web.SkinUtil;

/**
 * <p>Title: </p>
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
public class VIPTeachEntrance implements IPluginEntrance {
    public static String CODE = "teach";
    Logger logger = Logger.getLogger(this.getClass().getName());

    public VIPTeachEntrance() {
    }

    /**
     * canEnter
     *
     * @param request HttpServletRequest
     * @param boardCode String
     * @return boolean
     * @throws ErrMsgException
     * @todo Implement this com.redmoon.forum.plugin.base.IPluginEntrance
     *   method
     */
    public boolean canEnter(HttpServletRequest request, String boardCode) throws
            ErrMsgException {
        Privilege privilege = new Privilege();
        if (!privilege.isUserLogin(request))
           throw new ErrMsgException(SkinUtil.LoadString(request,"res.forum.plugin.entrance","err_entrance_pay"));//对不起，只有付费用户才能进入！
        String userName = privilege.getUser(request);
        VIPCardDb vtu = new VIPCardDb();
        String kind = ParamUtil.get(request, "kind");
        // vtu = vtu.getVIPTeachUserDb(userName, kind, ;
        if (vtu != null && vtu.isLoaded()) {
            return true;
        }
        return false;
    }

    /**
     * isPluginBoard
     *
     * @param boardCode String
     * @return boolean
     * @todo Implement this com.redmoon.forum.plugin.base.IPluginEntrance
     *   method
     */
    public boolean isPluginBoard(String boardCode) {
        BoardEntranceDb be = new BoardEntranceDb();
        be = be.getBoardEntranceDb(boardCode, CODE);
        if (be.isLoaded())
            return true;
        else
            return false;
    }

    public String[][] getAllKind() {
        String FILENAME = "VIP_teach_kind.xml";
        Document doc = null;
        Element root = null;
        String xmlPath;
        URL confURL = getClass().getResource("/" + FILENAME);
        xmlPath = confURL.getPath();
        xmlPath = URLDecoder.decode(xmlPath);

        SAXBuilder sb = new SAXBuilder();
        try {
            FileInputStream fin = new FileInputStream(xmlPath);
            doc = sb.build(fin);
            root = doc.getRootElement();
            fin.close();
        } catch (org.jdom.JDOMException e) {
            logger.error("getAllKind1:" + e.getMessage());
            return null;
        } catch (java.io.IOException e) {
            logger.error("getAllKind2:" + e.getMessage());
            return null;
        }

        List list = root.getChildren();
        String[][] re = new String[list.size()][2];
        if (list != null) {
            Iterator ir = list.iterator();
            int i = 0;
            while (ir.hasNext()) {
                Element child = (Element) ir.next();
                String code = child.getAttributeValue("code");
                String name = child.getChildText("name");
                re[i][0] = code;
                re[i][1] = name;
                i++;
            }
        }
        return re;
    }

    public boolean canAddReply(HttpServletRequest request, String boardCode, long rootid) {
        return true;
    }

    public boolean canAddNew(HttpServletRequest request, String boardCode) {
        return true;
    }
    
    public boolean canVote(HttpServletRequest request, String boardCode) throws ErrMsgException {
    	return true;
    }    
}
