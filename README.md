# Shiro安全框架

**shiro安全框架简介**

- Apache的强大灵活的开源安全框架
- 认证、授权、企业会话管理、安全加密



**与Spring Security比较**

- 简单灵活
- 可脱离Spring
- 粒度较粗



**Shrio整体架构**



![Shiro整体架构](Shiro整体架构.png)



## 认证

1. **创建SecurityManager**
2. **主体提交认证**
3. **SecurityManager认证**
4. **Authenticator认证**
5. **Realm验证**



```java
public class AuthenticatorTest {

    SimpleAccountRealm simpleAccountRealm = new SimpleAccountRealm();

    @Before
    public void addUser() {
         // 向 Realm 添加用户（用户名、密码、角色）
        simpleAccountRealm.addAccount("Mark","123456","admin");
    }

    @Test
    public void testAuthenticator() {
        // 1.构建 SecurityManager 环境
        DefaultSecurityManager defaultSecurityManager = new DefaultSecurityManager();
        defaultSecurityManager.setRealm(simpleAccountRealm);

        // 2.主体提交认证请求
        SecurityUtils.setSecurityManager(defaultSecurityManager);
        Subject subject = SecurityUtils.getSubject();
        
        // new token
        UsernamePasswordToken token = new UsernamePasswordToken("Mark","123456");

        // token登录认证
        subject.login(token);

        // 查看认证结果
        System.out.println("isAuthenticated:" + subject.isAuthenticated());

        // 退出登录
        subject.logout();
    }
}
```



## 授权

1. 创建 SecurityManager
2. 主体授权
3. SecurityManager授权
4. Authorizer授权
5. Realm获取角色权限数据



```java
public class AuthenticatorTest {

    SimpleAccountRealm simpleAccountRealm = new SimpleAccountRealm();

    @Before
    public void addUser() {
        // 向 Realm 添加用户（用户名、密码、角色）
        simpleAccountRealm.addAccount("Mark","123456","admin","user");
    }

    @Test
    public void testAuthenticator() {
        // 1.构建 SecurityManager 环境
        DefaultSecurityManager defaultSecurityManager = new DefaultSecurityManager();
        defaultSecurityManager.setRealm(simpleAccountRealm);

        // 2.主体提交认证请求
        SecurityUtils.setSecurityManager(defaultSecurityManager);
        Subject subject = SecurityUtils.getSubject();

        // new token
        UsernamePasswordToken token = new UsernamePasswordToken("Mark","123456");

        // token登录认证
        subject.login(token);

        // 查看认证结果
        System.out.println("isAuthenticated:" + subject.isAuthenticated());

        // 验证角色,可同时验证多个角色
        subject.checkRole("admin")
        subject.checkRoles("admin","user");
```





## Realm

### IniRealm

classPath/resources/user.ini

解析：
[users]用户
有（用户名:Mark，密码:123456，角色:admin）的用户
[roles]角色
admin角色有user:delete的权限

```ini
[users]
Mark=123456,admin
[roles]
admin=user:delete
```



```java
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
```



### JdbcRealm

JdbcRealm有**默认SQL查询语句**，不配置查询语句可直接使用：

```java
    protected static final java.lang.String DEFAULT_AUTHENTICATION_QUERY = "select password from users where username = ?";

    protected static final java.lang.String DEFAULT_SALTED_AUTHENTICATION_QUERY = "select password, password_salt from users where username = ?";

    protected static final java.lang.String DEFAULT_USER_ROLES_QUERY = "select role_name from user_roles where username = ?";

    protected static final java.lang.String DEFAULT_PERMISSIONS_QUERY = "select permission from roles_permissions where role_name = ?";
```



也可以自定义SQL语句

```java
// 自定义sql语句
String authenticationSql = "select password from test_user where user_name = ?";
String rolesSql = "select role_name from test_user_roles where user_name = ?";
String permissionsSql = "select permission from test_roles_permissions where role_name = ?"

// 把定义sql语句设置为查询语句
jdbcRealm.setAuthenticationQuery(authenticationSql);
jdbcRealm.setUserRolesQuery(rolesSql);
jdbcRealm.setPermissionsQuery(permissionsSql);
```



jdbcRealm例:

**数据库**

- users

|  id  | username | password |
| :--: | :------: | :------: |
|  1   |   Mark   |  123456  |

- user_roles

|  id  | username | role_name |
| :--: | :------: | :-------: |
|  1   |   Mark   |   admin   |

- roles_permissions

|  id  | role_name | permission  |
| :--: | :-------: | :---------: |
|  1   |   admin   | user:select |

**JdbcRealmTest类**

```java
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
        
        subject.checkRole("admin");
        subject.checkPermissions("user:select");
    }
}
```



### 自定义Realm

继承AuthorizingRealm类，

重写**doGetAuthenticationInfo认证方法**和**doGetAuthorizationInfo授权方法**



```java
public class CustomRealm extends AuthorizingRealm {
    /**
     * 认证
     * @param token
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        // 1.从主体传过来的认证信息中，获得用户名
        String username = (String) token.getPrincipal();

        // 2.通过用户名到数据库中获取凭证
        String password = getPasswordByUsername(username);
        if (password == null) {
            return null;
        }

        // 创建返回对象
        SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo("Mark",
                password, "customRealm");
        //加盐
        simpleAuthenticationInfo.setCredentialsSalt(ByteSource.Util.bytes("Mark"));
        return simpleAuthenticationInfo;
    }

    /**
     * 授权
     * @param principals
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String username = (String) principals.getPrimaryPrincipal();
        // 从数据库或者缓存中获得角色数据
        Set<String> roles = getRolesByUserName(username);
        Set<String> permissions = getPermissionsByUserName(username);

        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        simpleAuthorizationInfo.setStringPermissions(permissions);
        simpleAuthorizationInfo.setRoles(roles);

        return simpleAuthorizationInfo;
    }
    
    
    Map<String, String> userMap = new HashMap<>(16);
    //模拟数据库或缓存的数据
    {
        //加密
        //		Md5Hash md5 = new Md5Hash("123456");
        //加盐
        Md5Hash md5 = new Md5Hash("123456", "Mark");
        userMap.put("Mark", md5.toString());
        // userMap.put("Mark", "123456");
        super.setName("customRealm");
    }
   

    private Set<String> getPermissionsByUserName(String username) {
        Set<String> sets = new HashSet<>();
        sets.add("user:delete");
        sets.add("user:add");
        return sets;
    }

    private Set<String> getRolesByUserName(String username) {
        Set<String> sets = new HashSet<>();
        sets.add("admin");
        sets.add("user");
        return sets;
    }
    
    private String getPasswordByUsername(String username) {
        return userMap.get(username);
    }
}
```



Test:

```java
public class CustomRealmTest {

    private CustomRealm customRealm = new CustomRealm();

    @Test
    public void testCustomRealm() {
        // 1.构建SecurityManager环境
        DefaultSecurityManager defaultSecurityManager = new DefaultSecurityManager();
        defaultSecurityManager.setRealm(customRealm);

        // 2.声明CustomRealm使用了Md5加密
        HashedCredentialsMatcher matcher = new HashedCredentialsMatcher();
        matcher.setHashAlgorithmName("md5");
        matcher.setHashIterations(1);
        customRealm.setCredentialsMatcher(matcher);

        // 3.主题提交认证请求
        SecurityUtils.setSecurityManager(defaultSecurityManager);
        Subject subject = SecurityUtils.getSubject();

        UsernamePasswordToken token = new UsernamePasswordToken("Mark", "123456");
        subject.login(token);
        System.out.println("是否登录:" + subject.isAuthenticated());

        subject.checkRoles("admin"); //授权
        subject.checkPermission("user:delete");

        subject.logout();
        System.out.println("是否登录:" + subject.isAuthenticated());
    }
}
```





## 密码加密

**Shiro散列配置**

- HashedCredentialsMatcher类
- 自定义Realm中使用散列
- 盐的使用



例：

加密算法：(通过这个算法算出密码对应的结果，存在数据库中)

```java
Md5Hash md5Password = new Md5Hash("Password", "盐");
```



在RealmTest中：

```java
// 创建HashedCredentialsMatcher对象
HashedCredentialsMatcher matcher = new HashedCredentialsMatcher();
// 设置加密算法名称
matcher.setHashAlgorithmName("md5");
// 设置加密次数
matcher.setHashIterations(1);
// 声明CustomRealm使用了Md5加密
customRealm.setCredentialsMatcher(matcher);
```



在CustomRealm重写的认证方法中

```java
............
// 创建返回对象
SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo("用户名",
password, "customRealm");
//加盐
simpleAuthenticationInfo.setCredentialsSalt(ByteSource.Util.bytes("盐"));
return simpleAuthenticationInfo;
```



## 通过注解方式配置授权

需要添加aspectj依赖

```xml
<!-- Shiro注解 -->
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>1.8.9</version>
</dependency>
```

配置XML:

```xml
<!-- 开启AOP -->
<aop:config proxy-target-class="true"/>

<!-- 保证 Shiro内部生命周期 -->
<bean class="org.apache.shiro.spring.LifecycleBeanPostProcessor"/>

<!-- 开启Shiro授权生效 -->
<bean class="org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor">
    <property name="securityManager" ref="securityManager"/>
</bean>
```





在Controller中:

- @RequiresRoles("")

```java
//通过注解设置角色权限为admin才可访问
@RequiresRoles("admin")
@RequestMapping(value = "/testRole", method = RequestMethod.GET)
@ResponseBody
public String testRole() {
    return "test role success";
}
```



- RequiresPermissions("")

```java
// 具备权限的用户才能使用主体方法
@RequiresPermissions("xxx")
@RequestMapping("/xxx")
@ResponseBody
public void xxx(){
    ...
}
```



## 内置过滤器

**Shiro内置过滤器**

- anon无需认证，authBasic，authc需要认证后访问，user需要当前存在用户，logout退出
- perms权限，roles角色，ssl协议，port端口



Controller中：

```java
/**
* 通过在service.xml配置访问权限
*/
@RequestMapping(value = "/testRoles", method = RequestMethod.GET)
@ResponseBody
public String testRoles() {
return "test roles success";
}

@RequestMapping(value = "/testRoles1", method = RequestMethod.GET)
@ResponseBody
public String testRoles1() {
return "test roles1 success";
}

@RequestMapping(value = "/testPerms", method = RequestMethod.GET)
@ResponseBody
public String testPerms() {
return "test roles success";
}

@RequestMapping(value = "/testPerms1", method = RequestMethod.GET)
@ResponseBody
public String testPerms1() {
return "test roles1 success";
}
```



XML中：

```xml
    <bean id="shiroFilter"
          class="org.apache.shiro.spring.web.ShiroFilterFactoryBean">
        <property name="securityManager" ref="securityManager"/>
        <!-- 没有登录的用户请求需要登录的页面时自动跳转到登录页面，不是必须的属性，不输入地址的话会自动寻找项目web项目的根目录下的”/login.jsp”页面 -->
        <property name="loginUrl" value="login.html"/>
        <!-- 没有权限默认跳转的页面 -->
        <property name="unauthorizedUrl" value="403.html"/>
        <property name="filterChainDefinitions">
            <!-- 自上到下 --><!-- anon:表示可以匿名使用。 authc:表示需要认证(登录)才能使用，没有参数.  roles["admin,guest"],每个参数通过才算通过，user表示必须存在用户 -->
            <value>
                /login.html = anon
                /subLogin = anon
                /testRoles = roles["admin"]
                /testPerms = perms["user:delete"]
                /testPerms1 = perms["user:delete","user:updata"]
                /* = authc
            </value>
        </property>
    </bean>
```



自定义Filter

```java
/**
 * 功能：传多个Roles，满足其中一个即可
 */
public class RolesOrFilter extends AuthorizationFilter {

    @Override
    protected boolean isAccessAllowed(ServletRequest servletRequest,
                                      ServletResponse servletResponse, Object o) throws Exception {
        Subject subject = getSubject(servletRequest, servletResponse);

        String[] roles = (String[]) o;

        if (roles == null || roles.length == 0) {
            return true;
        }

        for (String role : roles) {
            if (subject.hasRole(role)) {
                return true;
            }
        }

        return false;
    }
}
```

XML:

```xml
<!-- 自定义权限filter -->
<bean id="rolesOrFilter" class="shiro.filter.RolesOrFilter"/>

<bean id="shiroFilter" class="org.apache.shiro.spring.web.ShiroFilterFactoryBean">
    <property name="securityManager" ref="securityManager"/>
    <property name="filterChainDefinitions">
        <value>
            /testRoles1 = rolesOr["admin","admin1"]
        </value>
    </property>
    <property name="filters">
        <map>
            <entry key="rolesOr" value-ref="rolesOrFilter"/>
        </map>
    </property>
</bean>
```



## 会话管理

**Shiro Session管理**

- SessionManager、SessionDAO
- Redis实现Session共享
- Redis实现Session共享可能存在的问题
