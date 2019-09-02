package com.imooc.test;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.text.IniRealm;
import org.apache.shiro.subject.Subject;
import org.junit.Test;

public class IniRealmTest {

    @Test
    public void testAuthenticator() {
        // 创建 IniRealm对象
        IniRealm iniRealm = new IniRealm("classpath:user.ini");

        // 1.构建 SecurityManager 环境
        DefaultSecurityManager defaultSecurityManager = new DefaultSecurityManager();
        defaultSecurityManager.setRealm(iniRealm);

        // 2.主体提交认证请求
        SecurityUtils.setSecurityManager(defaultSecurityManager);
        Subject subject = SecurityUtils.getSubject();

        // new token
        UsernamePasswordToken token = new UsernamePasswordToken("Mark","123456");

        // token登录认证
        subject.login(token);

        // 查看认证结果
        System.out.println("isAuthenticated:" + subject.isAuthenticated());

        // 验证角色
        subject.checkRole("admin");

        // 验证权限
        subject.checkPermission("user:delete");

    }
}
