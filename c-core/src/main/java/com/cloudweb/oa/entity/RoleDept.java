package com.cloudweb.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author fgf
 * @since 2020-02-22
 */
@Data
@TableName(value="user_role_dept")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RoleDept extends Model<RoleDept> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("roleCode")
    private String roleCode;

    @TableField("deptCode")
    private String deptCode;


    @Override
    protected Serializable pkVal() {
        return this.roleCode;
    }

}
