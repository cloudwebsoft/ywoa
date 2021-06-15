package com.cloudweb.oa.entity;

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
 * @since 2020-02-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DeptUser extends Model<DeptUser> {

    private static final long serialVersionUID = 1L;

    @TableId("ID")
    private Integer id;

    @TableField("DEPT_CODE")
    private String deptCode;

    @TableField("USER_NAME")
    private String userName;

    @TableField("ORDERS")
    private Integer orders;

    @TableField(value="RANK", exist=false)
    private String rank;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
