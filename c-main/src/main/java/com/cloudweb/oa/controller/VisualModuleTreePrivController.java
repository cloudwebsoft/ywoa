package com.cloudweb.oa.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.cache.DepartmentCache;
import com.cloudweb.oa.cache.GroupCache;
import com.cloudweb.oa.cache.RoleCache;
import com.cloudweb.oa.cache.UserCache;
import com.cloudweb.oa.constants.ModuleConst;
import com.cloudweb.oa.entity.VisualModuleTreePriv;
import com.cloudweb.oa.permission.ModuleTreePermission;
import com.cloudweb.oa.security.AuthUtil;
import com.cloudweb.oa.service.IVisualModuleTreePrivService;
import com.cloudweb.oa.vo.Result;
import com.cloudweb.oa.vo.RoleVO;
import com.cloudweb.oa.vo.VisualModuleTreePrivVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.redmoon.oa.basic.TreeSelectDb;
import com.redmoon.oa.pvg.UserGroupPrivCache;
import com.redmoon.oa.sys.DebugUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.bytebuddy.asm.Advice;
import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author fgf
 * @since 2022-08-20
 */
@Api(tags = "基础数据树形的权限控制器")
@RestController
@RequestMapping("/visual/tree/priv")
public class VisualModuleTreePrivController {

    @Autowired
    IVisualModuleTreePrivService visualModuleTreePrivService;

    @Autowired
    DozerBeanMapper dozerBeanMapper;

    @Autowired
    UserCache userCache;

    @Autowired
    RoleCache roleCache;

    @Autowired
    GroupCache groupCache;

    @Autowired
    DepartmentCache departmentCache;

    @Autowired
    AuthUtil authUtil;

    @Autowired
    ModuleTreePermission moduleTreePermission;

    @ApiOperation(value = "创建权限", notes = "创建权限")
    @PostMapping(value="/create",consumes= MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<Object> create(@RequestBody VisualModuleTreePriv[] visualModuleTreePrivs) {
        boolean re = false;
        for (VisualModuleTreePriv visualModuleTreePriv : visualModuleTreePrivs) {
            re = visualModuleTreePrivService.save(visualModuleTreePriv);
        }
        return new Result<>(re);
    }

    @ApiOperation(value = "更新权限", notes = "更新权限")
    @PostMapping(value="/update",consumes= MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<Object> update(@RequestBody VisualModuleTreePriv visualModuleTreePriv) {
        return new Result<>(visualModuleTreePrivService.updateById(visualModuleTreePriv));
    }

    @ApiOperation(value = "删除权限", notes = "删除权限")
    @PostMapping(value="/del", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<Object> del(Long id) {
        return new Result<>(visualModuleTreePrivService.removeById(id));
    }

    @ApiOperation(value = "权限列表", notes = "权限列表")
    @PostMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<Object> list(String rootCode, String nodeCode, @RequestParam(value = "pageSize", defaultValue = "20") int pageSize, @RequestParam(value = "page", defaultValue = "1") int curPage) {
        JSONObject jobject = new JSONObject();
        List<VisualModuleTreePrivVO> listVo = new ArrayList<>();
        TreeSelectDb tsd = new TreeSelectDb();

        List<VisualModuleTreePriv> list = visualModuleTreePrivService.list(rootCode, nodeCode, pageSize, curPage);
        for (VisualModuleTreePriv visualModuleTreePriv : list) {
            VisualModuleTreePrivVO vo = dozerBeanMapper.map(visualModuleTreePriv, VisualModuleTreePrivVO.class);
            if (vo.getPrivType() == ModuleConst.PRIV_TYPE_USERGROUP) {
                vo.setTitle(groupCache.getGroup(vo.getName()).getDescription());
                vo.setPrivTypeName("用户组");
            } else if (vo.getPrivType() == ModuleConst.PRIV_TYPE_USER) {
                vo.setTitle(userCache.getUser(vo.getName()).getRealName());
                vo.setPrivTypeName("用户");
            } else if (vo.getPrivType() == ModuleConst.PRIV_TYPE_ROLE) {
                vo.setTitle(roleCache.getRole(vo.getName()).getDescription());
                vo.setPrivTypeName("角色");
            } else if (vo.getPrivType() == ModuleConst.PRIV_TYPE_DEPT) {
                vo.setTitle(departmentCache.getDepartment(vo.getName()).getName());
                vo.setPrivTypeName("部门");
            } else {
                vo.setTitle("类型" + vo.getPrivType() + "不存在");
            }

            tsd = tsd.getTreeSelectDb(vo.getNodeCode());
            vo.setNodeName(tsd.getName());

            listVo.add(vo);
        }

        PageInfo<VisualModuleTreePriv> pageInfo = new PageInfo<>(list);
        jobject.put("list", listVo);
        jobject.put("page", curPage);
        jobject.put("total", pageInfo.getTotal());
        return new Result<>(jobject);
    }

    // 暂无用
    @ApiOperation(value = "取得用户在树形视图某节点上的权限列表", notes = "暂无用，不能通过前端来控制权限")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "nodeCode", value = "节点编码", dataType = "String"),
    })
    @PostMapping(value = "/getModuleTreeNodePriv", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<Object> getModuleTreeNodePriv(String nodeCode) {
        String userName = authUtil.getUserName();
        boolean canSee = moduleTreePermission.canSee(userName, nodeCode);
        boolean canAdd = moduleTreePermission.canAdd(userName, nodeCode);
        boolean canEdit = moduleTreePermission.canEdit(userName, nodeCode);
        boolean canDel = moduleTreePermission.canDel(userName, nodeCode);

        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("priv", "see");
        jsonObject.put("have", canSee);
        jsonArray.add(jsonObject);
        jsonObject = new JSONObject();
        jsonObject.put("priv", "add");
        jsonObject.put("have", canAdd);
        jsonArray.add(jsonObject);
        jsonObject = new JSONObject();
        jsonObject.put("priv", "edit");
        jsonObject.put("have", canEdit);
        jsonArray.add(jsonObject);
        jsonObject = new JSONObject();
        jsonObject.put("priv", "del");
        jsonObject.put("have", canDel);
        jsonArray.add(jsonObject);
        return new Result<>(jsonArray);
    }

    @ApiOperation(value = "取得用户是否可以管理某节点", notes = "取得用户是否可以管理某节点")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "nodeCode", value = "节点编码", dataType = "String"),
    })
    @PostMapping(value = "/isManagerOfNode", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<Object> isManagerOfNode(String nodeCode) {
        String userName = authUtil.getUserName();
        boolean re = moduleTreePermission.canManage(userName, nodeCode);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("canManage", re);
        return new Result<>(jsonObject);
    }
}

