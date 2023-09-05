package com.cloudweb.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 角色关联的部门
 * </p>
 *
 * @author fgf
 * @since 2022-03-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UserRoleDepartment extends Model<UserRoleDepartment> {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String deptCode;

    private String roleCode;

    private LocalDateTime createDate;

    private String creator;

    /**
     * 是否包含子部门
     */
    private Boolean include;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
