package com.imooc.test;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.realm.text.IniRealm;
import org.apache.shiro.subject.Subject;
import org.junit.Test;

import javax.sql.DataSource;

public class JdbcRealmTest {

    // 创建数据源
    DruidDataSource dataSource = new DruidDataSource();
    {
        dataSource.setUrl("jdbc:mysql://localhost:3306/test");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
    }

    @Test
    public void testAuthenticator() {
        // 创建 JdbcRealm对象
        JdbcRealm jdbcRealm = new JdbcRealm();

        // 设置数据源
        jdbcRealm.setDataSource(dataSource);

        // 配置打开权限查询，不配置的话默认是不可查询
        jdbcRealm.setPermissionsLookupEnabled(true);

        // 自定义sql语句
        String sql = "select password from test_user where user_name = ?";
        // 把定义sql语句设置为查询语句
        jdbcRealm.setAuthenticationQuery(sql);

        // 1.构建 SecurityManager 环境
        DefaultSecurityManager defaultSecurityManager = new DefaultSecurityManager();
        defaultSecurityManager.setRealm(jdbcRealm);

        // 2.主体提交认证请求
        SecurityUtils.setSecurityManager(defaultSecurityManager);
        Subject subject = SecurityUtils.getSubject();

        // new token
        UsernamePasswordToken token = new UsernamePasswordToken("Mark","123456");

        // token登录认证
        subject.login(token);

        // 查看认证结果
        System.out.println("isAuthenticated:" + subject.isAuthenticated());

//        subject.checkRole("admin");
//
//        subject.checkPermissions("user:select");
    }
}
