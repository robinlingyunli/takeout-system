package com.example.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.common.R;
import com.example.reggie.entity.Employee;
import com.example.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 员工退出
     * @param request 
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清理Session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    @PostMapping
    public  R<String> save(HttpServletRequest request, @RequestBody Employee employee){
        log.info("新增员工，员工信息：{}", employee.toString());
        //设置初始密码123456，需要进行md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        employeeService.save(employee);

        return R.success("新增员工成功");
    }

    /**
     * 员工信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    // 在Spring MVC框架中，int page, int pageSize, String name 这些参数是从HTTP请求的查询参数中获取的。
    // 当客户端（例如浏览器或其他HTTP客户端）向服务器发送一个GET请求时，可以在URL中包含查询参数。
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("page = {},pageSize = {},name = {}" ,page,pageSize,name);

        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);

        //构造条件构造器
        //LambdaQueryWrapper 是 MyBatis-Plus 提供的一个条件构造器，用于构造查询条件。
        //它支持链式调用和 Lambda 表达式，使得构造查询条件的代码更加简洁和类型安全。
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 根据id修改员工信息
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info(employee.toString());

        long id = Thread.currentThread().getId();
        log.info("employee里, 线程id为：{}",id);
        
        employeeService.updateById(employee);

        return R.success("员工信息修改成功");
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工信息...");
        Employee employee = employeeService.getById(id);
        if(employee != null){
            return R.success(employee);
        }
        return R.error("没有查询到对应员工信息");
    }
}
