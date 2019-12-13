* [一、spring包含哪些模块](#一spring包含哪些模块)
* [二、使用spring框架有哪些好处](#二使用spring框架有哪些好处)
* [三、IoC与依赖注入](#三IoC与依赖注入)
* [四、BeanFactory和ApplicationContext有什么区别](#四BeanFactory和ApplicationContext有什么区别)
* [五、spring有几种配置方式](#五spring有几种配置方式)
* [六、spring-bean的生命周期](#六spring-bean的生命周期)
* [七、spring-bean的作用域](#七spring-bean的作用域)
* [八、spring-inner-beans](#八spring-inner-beans)
* [九、spring单例beans是线程安全的吗](#九spring单例beans是线程安全的吗)
* [十、spring注入java-collection](#十spring注入java-collection)
* [十一、spring五种自动装配](#十一spring五种自动装配)
* [十二、spring常见注解](#十二spring常见注解)
* [十三、设值注入和构造注入](#十三设值注入和构造注入)
* [十四、Spring框架中有哪些不同类型的事件](#十四Spring框架中有哪些不同类型的事件)
* [十五、FileSystemResource和ClassPathResource有何区别](#十五FileSystemResource和ClassPathResource有何区别)
* [十六、spring框架中用到了哪些设计模式](#十六spring框架中用到了哪些设计模式)
* [十七、spring-boot有哪些优点](#十七spring-boot有哪些优点)
* [十八、spring-boot监视器](#十八spring-boot监视器)
* [十九、spring-boot常用的starter有哪些](#十九spring-boot常用的starter有哪些)
* [二十、spring-boot-starter加载过程](#二十spring-boot-starter加载过程)
* [二十一、spring事务](#二十一spring事务)


# 一、spring包含哪些模块

### spring core
最基础部分，提供依赖注入管理bean容器
### spring context
国家化，事件传播，资源装载，以及透明创建上下文
### spring dao
消除了JDBC编码和解析数据库厂商特有的错误代码，提供声明性事务
### spring orm
为流行的关系/对象映射APIs提供了集成层，JDO，hibernate，mybatis
### spring aop
实现面向切面编程，例如方法拦截器和切点
### spring web
提供web一些工具类支持
### spring MVC
提供了面向web应用的model-view-controller实现

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/spring/spring组件.jpg" width="450px">
</div>


# 二、使用spring框架有哪些好处
方便解耦，简化开发（通过IoC，对象之间的依赖交由spring管理）

AOP编程支持

声明式事务支持

方便集成各种优秀框架

降低java EE API的使用难度(spring对很多难用的API提供了薄薄的封装)

异常处理(将具体技术相关异常(JDBC抛出的)转换为一致的unchecked异常)

# 三、IoC与依赖注入
### IoC（控制反转）
spring 提供容器，去管理控制业务对象之间的依赖关系
控制权由应用代码转到了外部容器，降低了类之间的耦合度

### 依赖注入
IoC的一种重要实现，对象之间会相互依赖，IoC既然负责了对象的创建，那么这个依赖关系就必须由IoC容器负责，负责的方式就是DI-依赖注入


# 四、BeanFactory和ApplicationContext有什么区别

### BeanFactory
spring里面最低层的接口，提供了最简单的容器的功能（实例化对象和拿对象）
只有从容器中拿bean的时候，才会实例化

### ApplicationContext
直接实例化，也可以配置lazy-init=true
继承BeanFactory，提供了
国际化(xml中配置messageSource)
访问资源，如URL和文件（ResourceLoader，对资源文件进行存取操作，例如properties）
载入多个上下文（applicationContext*.xml）
消息发送，响应机制
AOP(例如页面的权限控制)


# 五、spring有几种配置方式

### XML显示配置

### 显示配置JavaConfig，基于java配置类
    @Configuration与@Bean结合使用
    
### 隐式的bean扫描，基于java注解配置，自动注入
    @Component等

# 六、spring-bean的生命周期

### 实例化Instantiation

### 属性赋值Populate

### 初始化Initialization

### 销毁Destruction

# 七、spring-bean的作用域

#### singleton
全局只有一个

#### prototype
每次调用产生一个实例

#### request
每次请求产生一个bean

#### session
每个用户session产生一个bean

#### globalSession
与session类似，只是使用portlet的时候使用

# 八、spring-inner-beans
bean被使用，当仅被调用了一个属性，一个明智的做法是将这个bean声明为内部bean
内部bean可以用setter注入属性和构造方法注入构造参数实现

    当Customer需要一个Person实例的时候
    public class Customer {
        private Person person;
    }
    class Person {
        private String name;
    }
    XML配置
    <bean id="CustomerBean" class="com.zhangze.common.Customer">
        <property name="person">
            <bean class="com.zhangze.common.Person">
                <property name="name" value="zhangze" />
            </bean>
        </property>
    </bean>
    
# 九、spring单例beans是线程安全的吗
spring 框架并没有对单例bean做任何多线程的封装处理
但实际上，大部分的bean并没有可变的状态（service，dao类）
需要开发者自行处理多线程问题，最浅显的解决办法就是将多态bean的作用域由singleton变为prototype

# 十、spring注入java-collection

spring提供四种集合类的配置元素

<list>,<set>,<map>,<props>

# 十一、spring五种自动装配
### 开启
<context:component-scan>

### 类型
#### no

开发者需要自行在bean定义中用标签明确依赖关系

#### byName
根据bean名称设置依赖关系
@Resource：提供bean名称属性，如果属性为空，将变量名或方法名作为bean名称

#### byType
根据bean类型设置依赖关系
@Autowired：按类型匹配bean，没有匹配，直接报错，如果接受找不到，设置required = false

#### constructor
构造器，和byType类似，仅仅适用于与有构造器相同参数的bean

#### autodetect
自动探测使用构造器或者byType自动装配

# 十二、spring常见注解
#### @Required
应用于bean属性的setter方法，表示属性配置时必须放在XML配置文件中

#### @Qualifier
当spring上下文中，一样的类型存在不止一个bean，我们可以使用此注解配合@Autowired来解决问题

#### @ControllerAdvice
spring boot的统一异常处理

#### @ConfigurationProperties
把yml或者properties配置文件转化为bean

#### @EnableConfigurationProperties
使@ConfigurationProperties生效

容器级别异常需要继承BasicErrorController(Filter抛出的异常，没有匹配的URL，请求参数错误等)

# 十三、设值注入和构造注入

使用场景不一样，一种是通过setter方法设定依赖关系，一种是通过构造函数

对依赖无变化的注入，一般采用构造注入，其他情况考虑设值注入，推荐使用设值注入


# 十四、Spring框架中有哪些不同类型的事件

#### ContextRefreshedEvent:上下文更新事件
ApplicationContext被初始化和更新时发布，也可以在调用ConfigurableApplicationContext的refresh方法时被触发

#### ContextStartedEvent:上下文开始事件
ConfigurableApplicationContext的start方法开始/重新开始时触发

#### ContextStoppedEvent:上下文停止事件
……stop方法停止容器时

#### ContextClosedEvent:上下文关闭事件
ApplicationContext被关闭时触发该事件，其所有管理的bean被销毁

#### RequestHandledEvent:请求处理事件
一个http请求触发

# 十五、FileSystemResource和ClassPathResource有何区别

#### FileSystemResource
需要给出spring-config.xml文件在你项目中的相对路径或者绝对路径
默认是src路径

#### ClassPathResource
spring会在ClassPath中自动搜寻配置文件，所以要把ClassPathResource文件放在ClassPath下

简而言之：FileSystemResource在配置文件中读取配置，ClassPathResource在环境变量中读取

# 十六、spring框架中用到了哪些设计模式

单例模式-spring配置文件中定义bean默认就是

代理模式-在AOP和remoting中用的比较多

模板方法-用来解决代码重复问题，比如RestTemplate,JmsTemplate

工厂模式-BeanFactory用来创建对象的实例


# 十七、spring-boot有哪些优点

良好的基因，基于spring 4.0

简化编码，简化配置（spring被誉为配置地狱）

简化部署，内嵌Tomcat

上手简单

spring-boot-devtools支持修改代码热部署，发现有class文件变动，就会创建一个新的ClassLoader进行加载

# 十八、spring-boot监视器

actuator 是spring启动框架的重要功能之一，帮助访问生产环境正在运行的应用程序的当前状态

有几个指标必须在生产环境中进行检查和监控，公开了一组可以直接作为HTTP URL访问的REST端点来检查状态

#### 端点
actuator监控项称为端点，默认除了shutdown之外，所有端点都启动

    配置默认关闭并开启指定的info端点
    management.endpoints.enabled-by-default=false
    management.endpoint.info.enabled=true

# 十九、spring-boot常用的starter有哪些

spring-boot-starter-web：嵌入Tomcat和web开发需要servlet与jsp支持

spring-boot-starter-data-jpa：数据库支持

spring-boot-starter-data-redis：redis数据库支持

spring-boot-starter-data-solr：solr支持

mybatis-spring-boot-starter：mybatis集成starter

# 二十、spring-boot-starter加载过程

### starter中的配置（mybatis为例）
每个start的META-INF下都有一个spring.factories文件
    
    内容如下
    # Auto Configure
    org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
    org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration

MybatisAutoConfiguration是一个带@Configuration和@Bean的类，就是一个java代码的配置类
@Conditional*是依赖条件
@EnableConfigurationProperties是配置参数

    @Configuration
    @ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class})
    @ConditionalOnBean({DataSource.class})
    @EnableConfigurationProperties({MybatisProperties.class})
    @AutoConfigureAfter({DataSourceAutoConfiguration.class})
    public class MybatisAutoConfiguration {
        ……
        @Bean
        @ConditionalOnMissingBean
        public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
            ExecutorType executorType = this.properties.getExecutorType();
            return executorType != null ? new SqlSessionTemplate(sqlSessionFactory, executorType) : new SqlSessionTemplate(sqlSessionFactory);
        }
    }

### Spring Boot

默认扫描启动类所在包下的主类与子类的所有组件，不包括依赖包中的类
spring启动的时候添加@SpringBootApplication注解
@SpringBootApplication下包含
    @Configuration
    @EnableAutoConfiguration(借助@Import，收集和注册依赖包中相关的bean定义)
    @ComponentScan(自动扫描并加载符合条件的组件，比如@Component,@Repository等)

@EnableAutoConfiguration下包含
    @AutoConfigurationPackage(加载启动类所在的所有包下的主类与子类)
    @Import(EnableAutoConfigurationImportSelector.class)(外部依赖的bean)
    
    EnableAutoConfigurationImportSelector
    通过loadFactoryNames方法扫描所有包META-INF/sping.factories文件，并返回数组
    
### 扩展，@Import如何创建类

#### 创建一个bean
    
    class User{}
#### 创建一个ItpscSelector继承ImportSelector接口并实现selectImports方法

    class ItpscSelector implements ImportSelector {
        public String[] selectImports(AnnotationMetadata importingClassMetadata) {
            return new String[]{"com.zhangze.User"}; // user类的地址
        }
    }   
#### 创建ImportConfig类，使用@Configuration，@Import(ItpscSelector.class)注解

    @Configuration
    @Import(ItpscSelector.class)
    public class ImportConfig {}

#### 从容器中加载bean

    通过前面的步骤，就可以加载bean了
    
     ApplicationContext ctx = new AnnotationConfigApplicationContext(ImportConfig.class);
     String[] beanDefinitionNames = ctx.getBeanDefinitionNames();
     for (String name : beanDefinitionNames) {
          System.out.println(name);
     }
     
     运行结果
     org.springframework.context.annotation.internalConfigurationAnnotationProcessor
     org.springframework.context.annotation.internalAutowiredAnnotationProcessor
     org.springframework.context.annotation.internalRequiredAnnotationProcessor
     org.springframework.context.annotation.internalCommonAnnotationProcessor
     org.springframework.context.event.internalEventListenerProcessor
     org.springframework.context.event.internalEventListenerFactory
     importConfig
     com.itpsc.entity.User

# 二十一、spring事务
@Transactional不加属性，只能在抛出RuntimeException或者error时才会触发事务回滚

如果要在非RuntimeException时也触发回滚，需要在注解上添加rollbackFor = {Exception.class}





