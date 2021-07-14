package cn.js.fan.module.cms.ext;

import com.redmoon.forum.person.UserGroupDb;
import com.redmoon.forum.person.UserDb;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class Privilege extends com.redmoon.forum.Privilege {
    public Privilege() {
    }

    /**
     * 判别用户是否具有目录中的权限
     * @param userName String
     * @param boardCode String
     * @param doWhat String
     * @return boolean
     */
    public boolean canUserDo(HttpServletRequest request, String dirCode,
                             String doWhat) {
        // 管理员具有所有的权限
        if (isMasterLogin(request))
            return true;
        return CheckPolicy(request, dirCode, doWhat);
    }

    /**
     * 检查权限控制规则
     * 检查节点上全部用户组是否允许访问
     * 如果允许，则检查所属游客型的IP组，是否有访问权限
     * 如果有，则继续检查所属其它组的权限，如果没有访问权限，则不允许访问，如果没有所属的IP组，则继续检查所属其它组的权限
     * 如果全部用户组不允许访问，则检查所属游客型的IP组是否拥有权限，有则继续检查所属其它用户组的权限
     * 所属IP组没有权限则不允许访问，如果没有所属的IP组，则不允许访问
     * @param request HttpServletRequest
     * @param dirCode String
     * @param doWhat String
     * @return boolean
     */
    public boolean CheckPolicy(HttpServletRequest request, String dirCode,
                                  String doWhat) {
        // if (dirCode.equals(""))
        //    dirCode = UserGroupPrivDb.ALLDIR; // 考虑到在forum/search.jsp中有类似情况，这儿预留了接口

        String groupCode = "";
        UserGroupPrivDb ugpd = new UserGroupPrivDb();
        boolean groupPriv = false;

        UserGroupDb ug = new UserGroupDb();
        // 取出全部用户组在dirCode节点上的权限
        ugpd = ugpd.getUserGroupPrivDb(UserGroupDb.ALL, dirCode);
        // System.out.println(getClass() + " " + doWhat + "=" + ugpd.getBoolean(doWhat));
        if (ugpd.getBoolean(doWhat)) {
            // 如果全部用户组拥有权限
            // 由IP地址来确定用户组，该组的权限判定优先于其它用户组
            groupCode = ug.getGuestGroupCodeByIP(request.getRemoteAddr());
            // groupCode不为GUEST，则说明如果用户的IP隶属于一个由IP范围来定义的游客组
            // System.out.println(getClass() + " groupCode=" + groupCode);
            if (!groupCode.equals(UserGroupDb.GUEST)) {
                // 检查IP组拥有的权限
                ugpd = ugpd.getUserGroupPrivDb(groupCode, dirCode);
                groupPriv = ugpd.getBoolean(doWhat);
                if (!groupPriv)
                    return false; // 根据IP判定无权限，则没有权限
            }

            // 如果是注册用户
            if (isUserLogin(request)) {
                String userName = getUser(request);
                UserDb ud = new UserDb();
                ud = ud.getUser(userName);
                if (!ud.isLoaded())
                    return false;

                // 当默认权限允许或者用户权限允许，则以用户组权限为准
                groupCode = ud.getUserGroupDb().getCode();

                // System.out.println(getClass() + " groupCode3=" + groupCode);

            }

            ugpd = ugpd.getUserGroupPrivDb(groupCode, dirCode);
            groupPriv = ugpd.getBoolean(doWhat);
            return groupPriv;
        } else {
            // 由IP地址来确定用户组，该组的权限判定优先于其它用户组
            groupCode = ug.getGuestGroupCodeByIP(request.getRemoteAddr());
            // System.out.println(getClass() + " groupCode2=" + groupCode);

            // groupCode不为GUEST，则说明如果用户的IP隶属于一个由IP范围来定义的游客组
            if (!groupCode.equals(UserGroupDb.GUEST)) {
                // 检查IP组拥有的权限
                ugpd = ugpd.getUserGroupPrivDb(groupCode, dirCode);
                groupPriv = ugpd.getBoolean(doWhat);
                if (!groupPriv)
                    return false; // 根据IP判定无权限，则没有权限
            } else {
                // 不属于任何GUEST型的IP组
                return false;
            }

            // 如果是注册用户
            if (isUserLogin(request)) {
                String userName = getUser(request);
                UserDb ud = new UserDb();
                ud = ud.getUser(userName);
                if (!ud.isLoaded())
                    return false;

                // 当默认权限允许或者用户权限允许，则以用户组权限为准
                groupCode = ud.getUserGroupDb().getCode();
            }

            ugpd = ugpd.getUserGroupPrivDb(groupCode, dirCode);
            groupPriv = ugpd.getBoolean(doWhat);
            return groupPriv;
        }

        /*
        // 在检查其它所属组的权限的时候，检查该用户组如具有全局权限，则拥有对所有目录下文章的查看、下载附件的权限，
        // 如不具备全局权限，则以具体目录节点上的权限为准进行判断
        ugpd = ugpd.getUserGroupPrivDb(groupCode, UserGroupPrivDb.ALLDIR);
        groupPriv = ugpd.getBoolean(doWhat);
        if (!groupPriv) {
            // 没有全局权限，则根据对应于dirCode的权限来判断
            ugpd = ugpd.getUserGroupPrivDb(groupCode, dirCode);
            groupPriv = ugpd.getBoolean(doWhat);
        }
        */
        // 注意：不再使用is_default这个字段（使用默认新用户的设置），没有太大意义
    }

}
