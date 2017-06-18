### Some quick modification for PostgreSQL

# The problem demo

## MySQL with auto_increment primary key

```mysql

mysql> create table test1 ( 
	id int auto_increment not null  primary key,
	nickname varchar(30),
	sal int
);
Query OK, 0 rows affected

mysql> insert into test1 (id, nickname, sal) values (1, 'feitian', 11);
Query OK, 1 row affected

mysql> select * from test1;
+----+----------+-----+
| id | nickname | sal |
+----+----------+-----+
|  1 | feitian  |  11 |
+----+----------+-----+
1 row in set

mysql> insert into test1 (nickname, sal) values ('feitian1', 111);
Query OK, 1 row affected

mysql> select * from test1;
+----+----------+-----+
| id | nickname | sal |
+----+----------+-----+
|  1 | feitian  |  11 |
|  2 | feitian1 | 111 |
+----+----------+-----+
2 rows in set

mysql> 
```

First, designated the primary key column when inserting a new row, the autoincrement primary key increases.

Second, **not** designated the primary key column when inserting a new row, the autoincrement primary key **also** increases.


## PostgrSQL with serial primary key

```sql

postgres=# create table test1 (
	id serial primary key,
	nickname varchar(30),
	sal int
);
Command OK

postgres=# insert into test1 (id, nickname, sal) values (1, 'feitian', 11);
Command OK - 1 row affected

postgres=# select * from test1;
+----+----------+-----+
| id | nickname | sal |
+----+----------+-----+
|  1 | feitian  |  11 |
+----+----------+-----+
1 row in set

postgres=# insert into test1 (nickname, sal) values ('feitian1', 111);
ERROR:  duplicate key value violates unique constraint "test1_pkey"
DETAIL:  Key (id)=(1) already exists.
postgres=# 

```

First, designated the primary key column when inserting a new row, the autoincrement primary key increases.

Second, **not** designated the primary key column when inserting a new row, you got an unique constraint violation.

Maybe it's feature for postgresql work this way. There're many workaroud ways, but the most dirty way is to modify the source code of jFinal as:

```java
	public void forModelSave(Table table, Map<String, Object> attrs, StringBuilder sql, List<Object> paras) {
		sql.append("insert into \"").append(table.getName()).append("\"(");
		StringBuilder temp = new StringBuilder(") values(");
        String pKey = table.getPrimaryKey();
		for (Entry<String, Object> e: attrs.entrySet()) {
			String colName = e.getKey();
			if (table.hasColumnLabel(colName) && !colName.equalsIgnoreCase(pKey)) {
				if (paras.size() > 0) {
					sql.append(", ");
					temp.append(", ");
				}
                if (colName.equalsIgnoreCase(pKey))
				{
					continue;
				} else {

                    sql.append('\"').append(colName).append('\"');
                    temp.append('?');
                    paras.add(e.getValue());
                }
			}
		}
		sql.append(temp.toString()).append(')');
	}
```

You "escape" the primay key when inserting a new row.

----
### JAVA 极速WEB+ORM框架 JFinal


JFinal 是基于 Java 语言的极速 WEB + ORM 框架，其核心设计目标是开发迅速、代码量少、学习简单、功能强大、轻量级、易扩展、Restful。在拥有Java语言所有优势的同时再拥有ruby、python等动态语言的开发效率！为您节约更多时间，去陪恋人、家人和朋友 ;)

#### JFinal有如下主要特点
- MVC架构，设计精巧，使用简单
- 遵循COC原则，零配置，无xml
- 独创Db + Record模式，灵活便利
- ActiveRecord支持，使数据库开发极致快速
- 极简、高性能Template Engine，十分钟内掌握基本用法
- 自动加载修改后的java文件，开发过程中无需重启web server
- AOP支持，拦截器配置灵活，功能强大
- Plugin体系结构，扩展性强
- 多视图支持，支持FreeMarker、JSP、Velocity
- 强大的Validator后端校验功能
- 功能齐全，拥有struts2的绝大部分功能
- 体积小仅538K

**JFinal 极速开发微信公众号欢迎你的加入: JFinal**

#### 以下是JFinal实现Blog管理的示例：

**1. 控制器(支持JFinal Template、JSP、Velocity、JSON等等以及自定义视图渲染)**

```java
@Before(BlogInterceptor.class)
public class BlogController extends Controller {
    static BlogService service = new BlogService();

    public void index() {
        setAttr("blogPage", service.paginate(getParaToInt(0, 1), 10));
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
        setAttr("blog", service.findById(getParaToInt()));
    }

    @Before(BlogValidator.class)
    public void update() {
        getModel(Blog.class).update();
        redirect("/blog");
    }

    public void delete() {
        service.deleteById(getParaToInt());
        redirect("/blog");
    }
}
```

**2.Service所有业务与sql全部放在Service层**

```java
public class BlogService {
    private static final Blog dao = new Blog().dao();
    
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

**3.Model(无xml、无annotaion、无attribute)**

```java
public class Blog extends Model<Blog> {
    
}
```

**4.Validator(API引导式校验，比xml校验方便N倍，有代码检查不易出错)**

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

**5.拦截器(在此demo中仅为示例，本demo不需要此拦截器)**

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

