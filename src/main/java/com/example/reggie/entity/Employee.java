package com.example .reggie.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

//在你的 Employee 类中使用了 @Data 注解，这是 Lombok 库的一部分，可以自动生成 getter 和 setter 方法。
//当你在一个类上应用 @Data 注解时，Lombok 会在编译时自动为该类的每个字段生成对应的 getter 和 setter 方法，即使在源代码中你没有显式地定义这些方法。
@Data
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String name;

    private String password;

    private String phone;

    private String sex;

    private String idNumber;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

}
