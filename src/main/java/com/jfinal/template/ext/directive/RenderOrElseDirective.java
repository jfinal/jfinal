package com.jfinal.template.ext.directive;

import com.jfinal.kit.PathKit;
import com.jfinal.kit.SyncWriteMap;
import com.jfinal.template.Directive;
import com.jfinal.template.EngineConfig;
import com.jfinal.template.Env;
import com.jfinal.template.TemplateException;
import com.jfinal.template.expr.ast.Assign;
import com.jfinal.template.expr.ast.ExprList;
import com.jfinal.template.io.Writer;
import com.jfinal.template.source.ISource;
import com.jfinal.template.stat.Ctrl;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Parser;
import com.jfinal.template.stat.Scope;
import com.jfinal.template.stat.ast.Define;
import com.jfinal.template.stat.ast.Include;
import com.jfinal.template.stat.ast.Stat;
import com.jfinal.template.stat.ast.StatList;

import java.io.File;
import java.util.Map;

/**
 * 默认行为与RenderDirective一样
 * 界面上的自定义render渲染模版
 * 模版文件存在的时候渲染
 * 模版文件不存在的时渲染插槽内容
 * #renderOrElse("/_view/include/xxx.html")
 * <a href="/admin/xxx/edit/1">xxxx</a>
 * #end
 */
public class RenderOrElseDirective extends Directive {
	private String parentFileName;
	private Map<String, RenderOrElseDirective.SubStat> subStatCache = new SyncWriteMap<String, RenderOrElseDirective.SubStat>(16, 0.5F);

	public void setExprList(ExprList exprList) {
		int len = exprList.length();
		if (len == 0) {
			throw new ParseException("The parameter of #renderOrElse directive can not be blank", location);
		}
		if (len > 1) {
			for (int i = 1; i < len; i++) {
				if (!(exprList.getExpr(i) instanceof Assign)) {
					throw new ParseException("The " + (i + 1) + "th parameter of #renderOrElse directive must be an assignment expression", location);
				}
			}
		}

		/**
		 * 从 location 中获取父模板的 fileName，用于生成 subFileName
		 * 如果是孙子模板，那么 parentFileName 为最顶层的模板，而非直接上层的模板
		 */
		this.parentFileName = location.getTemplateFile();
		this.exprList = exprList;
	}

	/**
	 * 对 exprList 进行求值，并将第一个表达式的值作为模板名称返回，
	 * 开启 local assignment 保障 #render 指令参数表达式列表
	 * 中的赋值表达式在当前 scope 中进行，有利于模块化
	 */
	private Object evalAssignExpressionAndGetFileName(Scope scope) {
		Ctrl ctrl = scope.getCtrl();
		try {
			ctrl.setLocalAssignment();
			return exprList.evalExprList(scope)[0];
		} finally {
			ctrl.setWisdomAssignment();
		}
	}

	public void exec(Env env, Scope scope, Writer writer) {
		// 在 exprList.eval(scope) 之前创建，使赋值表达式在本作用域内进行
		scope = new Scope(scope);

		Object value = evalAssignExpressionAndGetFileName(scope);
		if (!(value instanceof String)) {
			throw new TemplateException("The parameter value of #renderOrElse directive must be String", location);
		}

		String subFileName = Include.getSubFileName((String)value, parentFileName);
		// TODO 未考虑模板 source 为 classpath 的情况，也未考虑 baseTemplatePath 参数据，需参考以下代码进行改进
		//      config.getSourceFactory().getSource(config.getBaseTemplatePath(), subFileName, ...)
		String enjoyHtmlFilePath = PathKit.getWebRootPath() + subFileName;
		File enjoyHtmlFile = new File(enjoyHtmlFilePath);
		if (enjoyHtmlFile.exists()) {
			RenderOrElseDirective.SubStat subStat = subStatCache.get(subFileName);
			if (subStat == null) {
				subStat = parseSubStat(env, subFileName);
				subStatCache.put(subFileName, subStat);
			} else if (env.isDevMode()) {
				// subStat.env.isSourceListModified() 逻辑可以支持 #render 子模板中的 #include 过来的子模板在 devMode 下在修改后可被重加载
				if (subStat.source.isModified() || subStat.env.isSourceListModified()) {
					subStat = parseSubStat(env, subFileName);
					subStatCache.put(subFileName, subStat);
				}
			}

			subStat.exec(null, scope, writer);	// subStat.stat.exec(subStat.env, scope, writer);

			scope.getCtrl().setJumpNone();
		} else {
			stat.exec(env, scope, writer);
		}
	}

	private RenderOrElseDirective.SubStat parseSubStat(Env env, String subFileName) {
		EngineConfig config = env.getEngineConfig();
		// FileSource subFileSource = new FileSource(config.getBaseTemplatePath(), subFileName, config.getEncoding());
		ISource subFileSource = config.getSourceFactory().getSource(config.getBaseTemplatePath(), subFileName, config.getEncoding());

		try {
			RenderOrElseDirective.SubEnv subEnv = new RenderOrElseDirective.SubEnv(env);
			StatList subStatList = new Parser(subEnv, subFileSource.getContent(), subFileName).parse();
			return new RenderOrElseDirective.SubStat(subEnv, subStatList.getActualStat(), subFileSource);
		} catch (Exception e) {
			throw new ParseException(e.getMessage(), location, e);
		}
	}

	public static class SubStat extends Stat {
		public RenderOrElseDirective.SubEnv env;
		public Stat stat;
		public ISource source;

		public SubStat(RenderOrElseDirective.SubEnv env, Stat stat, ISource source) {
			this.env = env;
			this.stat = stat;
			this.source = source;
		}

		@Override
		public void exec(Env env, Scope scope, Writer writer) {
			stat.exec(this.env, scope, writer);
		}
	}

	/**
	 * SubEnv 用于将子模板与父模板中的模板函数隔离开来，
	 * 否则在子模板被修改并被重新解析时会再次添加子模板中的
	 * 模板函数，从而抛出异常
	 *
	 * SubEnv 也可以使子模板中定义的模板函数不与上层产生冲突，
	 * 有利于动态型模板渲染的模块化
	 *
	 * 注意： #render 子模板中定义的模板函数无法在父模板中调用
	 */
	public static class SubEnv extends Env {
		public Env parentEnv;

		public SubEnv(Env parentEnv) {
			super(parentEnv.getEngineConfig());
			this.parentEnv = parentEnv;
		}

		/**
		 * 接管父类 getFunction()，先从子模板中找模板函数，找不到再去父模板中找
		 */
		@Override
		public Define getFunction(String functionName) {
			Define func = functionMap.get(functionName);
			return func != null ? func : parentEnv.getFunction(functionName);
		}
	}

	@Override
	public boolean hasEnd() {
		return true;
	}
}
