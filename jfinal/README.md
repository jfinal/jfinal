JFinal 是基于Java 语言的极速 WEB + ORM 开发框架，其核心设计目标是开发迅速、代码量少、学习简单、功能强大、轻量级、易扩展、Restful。在拥有Java语言所有优势的同时再拥有ruby、python等动态语言的开发效率！为您节约更多时间，去陪恋人、家人和朋友 ;)
==JFinal主要特点：==
 * MVC架构，设计精巧，使用简单
 * 遵循COC原则，零配置，无xml
 * ActiveRecord支持，使数据库开发极致快速
 * 自动加载修改后的java文件，开发过程中无需重启web server
 * AOP支持，拦截器配置灵活，功能强大
 * Plugin体系结构，扩展性强
 * 多视图支持，支持FreeMarker、JSP、Velocity
 * 强大的Validator后端校验功能
 * 功能齐全，拥有struts2的绝大部分功能
 * 体积小仅180K，且无第三方依赖

以下是JFinal实现Blog管理的代码:
{{{
/**
 * BlogController
 */
public class BlogController extends Controller {
	public void index() {
		setAttr("blogList", Blog.dao.find("select * from blog order by id asc"));
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


/**
 * Blog model.
 */
public class Blog extends Model<Blog> {
	public static final Blog dao = new Blog();
}


/**
 * BlogValidator.
 */
public class BlogValidator extends Validator {
	protected void validate(Controller controller) {
		validateRequiredString("blog.title", "titleMsg", "请输入Blog标题!");
		validateRequiredString("blog.content", "contentMsg", "请输入Blog内容!");
	}
	
	protected void handleError(Controller controller) {
		controller.keepModel(Blog.class);
	}
}

}}}