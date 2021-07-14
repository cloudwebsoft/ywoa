package com.cloudweb.oa.service;

import com.cloudweb.oa.entity.Log;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author fgf
 * @since 2020-02-15
 */
public interface ILogService extends IService<Log> {

    List<Log> list(String userName, String op, String logType, String userAction, int device, Date beginDate, Date endDate, String deptCode);

    String getTypeDesc(int type);
}
