package com.example.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.reggie.common.R;
import com.example.reggie.entity.Employee;
import com.example.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    //从前端获取RequestBody，然后这个function返回一个泛型的employee
    //在 Spring MVC 中，@RequestBody 注解用于将 HTTP 请求体中的 JSON 字符串自动转换成 Java 对象。
    //在你提供的 EmployeeController 的 login 方法中，使用 @RequestBody Employee employee 表示这个方法期望从请求体中接收一个 JSON 对象，并将其自动转换为 Employee 类的实例。

    /**
     * 员工登录
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login (HttpServletRequest request, @RequestBody Employee employee){

        log.info("Attempting login for username: {}", employee.getUsername());

        //1、将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //第一个getUsername，在 MyBatis Plus 中，这种方法引用被用来安全地指定数据库查询的字段名。它不执行任何方法调用，
        //而是提供一个引用点，让 MyBatis Plus 能够通过反射来确定与该方法对应的数据库列名。
        //第二个getUsername，这是一个实际的方法调用。在这个上下文中，employee 是一个 Employee 类型的实例，employee.getUsername() 调用会执行该实例的 getUsername 方法，
        //返回存储在该实例中的 username 属性值。这个返回值是动态的，基于当前 employee 对象的状态。
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        //getOne 方法是 MyBatis Plus 提供的简便方法，用于查询满足条件的单个记录。
        Employee emp = employeeService.getOne(queryWrapper);

        //3、如果没有查询到则返回登录失败结果
        if(emp == null){
            log.warn("Login failed for username: {}", employee.getUsername());
            return R.error("登录失败");
        }

        //4、密码比对，如果不一致则返回登录失败结果
        if(!emp.getPassword().equals(password)){
            log.warn("Password mismatch for username: {}", employee.getUsername());
            return R.error("登录失败");
        }

        //5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if(emp.getStatus() == 0){
            log.warn("Account disabled for username: {}", employee.getUsername());
            return R.error("账号已禁用");
        }

        //6、登录成功，将员工id存入Session并返回登录成功结果
        //Session 数据存储在服务器端。
        //当你使用 Spring Boot 在本地运行你的应用时，“服务器端”指的是你本地机器上运行的 Spring Boot 应用程序的内部。
        //Spring Boot 内置了一个 Web 服务器（默认是 Tomcat），这使得它可以作为一个独立的、自包含的 Java 应用程序运行，处理 HTTP 请求和响应。
        request.getSession().setAttribute("employee",emp.getId());
        log.info("Login successful for username: {}, employee ID: {}", employee.getUsername(), emp.getId());
        return R.success(emp);
    }
}
