### JAVA 极速WEB+ORM框架 JFinal


JFinal 是基于 Java 语言的极速 WEB + ORM 框架，其核心设计目标是开发迅速、代码量少、学习简单、功能强大、轻量级、易扩展、Restful。在拥有Java语言所有优势的同时再拥有ruby、python等动态语言的开发效率！为您节约更多时间，去陪恋人、家人和朋友 ;)

#### JFinal有如下主要特点
- MVC架构，设计精巧，使用简单
- 遵循COC原则，零配置，无xml
- 独创Db + Record模式，灵活便利
- ActiveRecord支持，使数据库开发极致快速
- 自动加载修改后的java文件，开发过程中无需重启web server
- AOP支持，拦截器配置灵活，功能强大
- Plugin体系结构，扩展性强
- 多视图支持，支持FreeMarker、JSP、Velocity
- 强大的Validator后端校验功能
- 功能齐全，拥有struts2的绝大部分功能
- 体积小仅218K，且无第三方依赖，棒极了000

**JFinal 极速开发QQ群欢迎您的加入: 322076903、432462639**

#### 以下是JFinal实现Blog管理的示例：

**1. 控制器(支持FreeMarker、JSP、Velocity、JSON等等以及自定义视图渲染)**

```java
@Before(BlogInterceptor.class)
public class BlogController extends Controller {
    public void index() {
        setAttr("blogList", Blog.dao.find("select * from blog"));
    }
    public void add() {
    }

    @Before(BlogValidator.class)
    public void save() {
        getModel(Blog.class).save();
    }

    public void edit() {
        setAttr("blog", Blog.dao.findById(getParaToInt()));
    }

    @Before(BlogValidator.class)
    public void update() {
        getModel(Blog.class).update();
    }

    public void delete() {
        Blog.dao.deleteById(getParaToInt());
    }
}
```

**2.Model(无xml、无annotaion、无attribute、无getter、无setter)**

```java
public class Blog extends Model<Blog> {
    public static final Blog dao = new Blog();
}
```

**3.Validator(API引导式校验，比xml校验方便N倍，有代码检查不易出错)**

```java
public class BlogValidator extends Validator {
    protected void validate(Controller controller) {
        validateRequiredString("blog.title", "titleMsg", "请输入Blog标题!");
        validateRequiredString("blog.content", "contentMsg", "请输入Blog内容!");
    }

    protected void handleError(Controller controller) {
        controller.keepModel(Blog.class);
    }
}
```

**4.拦截器(在此demo中仅为示例，本demo不需要此拦截器)**

```java
public class BlogInterceptor implements Interceptor {
    public void intercept(Invocation inv) {
        System.out.println("Before invoking " + inv.getActionKey());
        inv.invoke();
        System.out.println("After invoking " + inv.getActionKey());
    }
}
```

**JFinal 官方网站：[http://www.jfinal.com](http://www.jfinal.com)**

