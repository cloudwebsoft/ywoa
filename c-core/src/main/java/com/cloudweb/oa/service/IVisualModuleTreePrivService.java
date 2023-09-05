package com.cloudweb.oa.service;

import com.cloudweb.oa.entity.VisualModuleTreePriv;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author fgf
 * @since 2022-08-20
 */
public interface IVisualModuleTreePrivService extends IService<VisualModuleTreePriv> {

    List<VisualModuleTreePriv> list(String rootCode, String nodeCode, int pageSize, int curPage);

    List<VisualModuleTreePriv> list(String rootCode, String nodeCode);

}
