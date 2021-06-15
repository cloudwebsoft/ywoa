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
 * @since 2020-01-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UserOfRole extends Model<UserOfRole> {

    private static final long serialVersionUID = 1L;

    @TableId("userName")
    private String userName;

    @TableField("roleCode")
    private String roleCode;

    private Integer orders;

    private String depts;

    @Override
    protected Serializable pkVal() {
        return this.userName;
    }

}
