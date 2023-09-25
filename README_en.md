## JAVA Ultra-fast WEB+ORM Framework JFinal
[中文](README.md) [English](README_en.md)

JFinal is an ultra-fast WEB + ORM framework based on the Java language. Its core design goals are rapid development, minimal code, simple learning, powerful functionality, lightweight, easy to expand, and Restful. While possessing all the advantages of the Java language, it also has the development efficiency of dynamic languages like ruby and python! Save more time for you to spend with your loved ones, family, and friends ;)

#### JFinal has the following key features:
- MVC architecture, elegantly designed, easy to use.
- Adheres to the COC principle, supports zero-configuration, and is XML-free.
- Unique Db + Record mode, flexible and convenient.
- Supports ActiveRecord, making database development extremely fast.
- Minimalistic, powerful, high-performance template engine Enjoy. Master 90% of its usage within 10 minutes.
- Auto-reloads modified Java files, eliminating the need to restart services during development.
- Supports AOP, flexible interceptor configuration, and robust functionality.
- Plugin architecture, highly extensible.
- Supports multiple views, including Enjoy, FreeMarker, and JSP.
- Powerful Validator for backend validation.
- Feature-rich, possessing most of the core functionalities of traditional SSH frameworks.
- Small in size at only 832 KB, with no third-party dependencies.

**Join JFinal's ultra-fast WeChat official account development: JFinal**

## Maven Coordinates

```java
<dependency>
    <groupId>com.jfinal</groupId>
    <artifactId>jfinal</artifactId>
    <version>5.1.1</version>
</dependency>
```

## Below is an example of how JFinal implements Blog management:

**1. Controller (Supports Enjoy, JSP, JSON, etc., as well as custom view rendering)**

```java
@Before(BlogInterceptor.class)
public class BlogController extends Controller {

    @Inject
    BlogService service;

    public void index() {
        set("blogPage", service.paginate(getInt(0, 1), 10));
        render("blog.html");
    }

    public void add() {
    }

    @Before(BlogValidator.class)
    public void save() {
        getModel(Blog.class).save();
        redirect("/blog");
    }

    public void edit() {
        set("blog", service.findById(getInt()));
    }

    @Before(BlogValidator.class)
    public void update() {
        getModel(Blog.class).update();
        redirect("/blog");
    }

    public void delete() {
        service.deleteById(getInt());
        redirect("/blog");
    }
}
```

**2. All business and SQL are placed in the Service layer**

```java
public class BlogService {

    private Blog dao = new Blog().dao();
    
    public Page<Blog> paginate(int pageNumber, int pageSize) {
        return dao.paginate(pageNumber, pageSize, "select *", "from blog order by id asc");
    }
    
    public Blog findById(int id) {
        return dao.findById(id);
    }
    
    public void deleteById(int id) {
        dao.deleteById(id);
    }
}
```

**3. Model (No XML, no annotations, no attributes)**

```java
public class Blog extends Model<Blog> {
    
}
```

**4. Validator (API-guided validation, much more convenient than XML validation, code-checked to minimize errors)**

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

**5. Interceptor (Only for demonstration in this demo, this demo does not require this interceptor)**

```java
public class BlogInterceptor implements Interceptor {
    public void intercept(Invocation inv) {
        System.out.println("Before invoking " + inv.getActionKey());
        inv.invoke();
        System.out.println("After invoking " + inv.getActionKey());
    }
}
```

## More Support:
- JFinal official website [https://jfinal.com](https://jfinal.com/)
- Scan to follow the official WeChat official account and enjoy the latest updates first.

![JFinal](https://jfinal.com/assets/img/jfinal_weixin_service_qr_code_150.jpg)