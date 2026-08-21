# Changelog

## jfinal 5.3.0（包含 5.1.2 至 5.3.0）

- Db、DbPro 新增 transaction(...) 系列事务方法，支持任意类型返回值，且事务体允许抛出 Exception，例如：

  ```java
  Ret ret = Db.transaction(tx -> {
     ...
     return Ret.ok();
  });
  ```

- 新增 Transaction 类灵活控制事务：rollback() 手动回滚、rollbackIf(boolean) 条件回滚、canCommit() 判断能否提交
- Transaction 新增 onException(...) 回调，事务异常时以其返回值作为 transaction(...) 的返回值，避免向外抛异常
- Transaction 新增 onAfterCommit(Runnable) 回调，在事务提交成功后执行，支持嵌套事务
- 新增 TransactionRollbackDecision 接口，transaction(...) 的返回值实现该接口即可决定是否回滚事务
- 新增 Transactional 拦截器支持声明式事务，配合 @Before(Transactional.class) 使用
- Config 新增 setOnTransactionException(...)、setOnBeforeTransactionCommit(...)，支持数据源级别的事务回调配置
- 新版本事务方法 transaction(...) 相互嵌套时共享同一事务，内层异常将回滚整体事务。注意老版本 tx(...) 中不能嵌套调用 transaction(...)
- Config 新增 setCallbackAfterTxCommit(Runnable)，支持在事务方法内注册事务提交成功后的回调
- Controller 新增 getProgressFile(...) 系方法，文件上传支持进度回调，新增 UploadProgress 封装进度数据
- 新增 ProgressUploadFileConfig 配置进度上传的文件 rename 策略，默认基于时间，可选 UUID、计数策略
- 带进度文件上传拆分为 ProgressUploadFileKit，消除 jfinal 内核对 commons-fileupload 的强制依赖
- 文件上传添加白名单机制，默认仅允许白名单内的文件类型，UploadConfig 新增 addWhitelist(...)、removeWhitelist(...)、clearWhitelist()
- UploadConfig 新增 setWhitelistEnabled(boolean)，支持关闭文件上传类型白名单
- UploadConfig 新增 setFileRenamePolicy(...)，支持自定义 cos 文件重命名策略
- AopManager 新增 addMapping(String name, Class<?> to) 支持 name 到实现类的映射，配合 Aop.get(String name) 获取对象
- @Inject 注解新增 remote 属性，可配置远程服务名称，扩展 AopFactory 生成代理，以调用方法的方式访问远程服务
- ThreadPoolKit 新增 ThreadPoolGroup 任务组，通过 newGroup() 创建，submit(...) 提交任务，waits()、get() 等待全部完成并获取各任务返回值
- DbKit 新增 batchListSave、batchListUpdate 分组批量插入与更新，按 model 字段状态分组生成 sql 分批执行
- Enjoy 新增 #renderOrElse 指令，模板文件存在时渲染该模板，否则渲染指令与 #end 之间包裹的内容
- Enjoy 新增 BigIntegerExt、BigDecimalExt 扩展，模板中 BigInteger、BigDecimal 支持 toInt()、toLong()、toBoolean() 等转换方法
- Enjoy 数值与 String 扩展方法新增 toBigInteger()、toBigDecimal()
- Enjoy 的 BigDecimalExt 新增 halfUp(newScale)，支持四舍五入到指定小数位
- Engine 新增 getDirectiveNames()、getSharedFunctionNames()，获取当前 engine 全部指令名与共享模板函数名
- 新增 Log4j2Log、Log4j2LogFactory 支持 log4j2 日志，日志方法支持 {} 占位符，需自行引入 log4j-core 依赖
- 新增 LineNumberActionReporter，action report 可输出 Controller 真实源码行号，建议开发模式使用
- ActionReporter 新增 setTitle(...)，时间格式精确到毫秒，buildPara、buildJsonPara 改为 protected 便于二次开发
- Jackson 初始化 ObjectMapper 时添加 findAndRegisterModules()，自动发现并注册可用的 jackson 模块
- action 参数注入增强，形参支持 Kv、Map、`List<XXX>`、数组类型，json 请求按同名 key 取值转换，例如：

  ```java
  public void demo(Kv cond, String[] names, List<User> users)
  ```

- action 形参为 Map 类型时可直接接收整个 json 请求体
- I18n 的 locale 字符串支持 variant 三段式（如 zh_CN_GB），并新增携带 ResourceBundle.Control 参数的方法支持自定义 Control
- RedisPlugin 分布式锁的超时时间参数由 int 改为 double，在支持秒的基础上支持毫秒
- Redis 的 Cache 新增 withLock 重载，maxLockTime 与 retryTime 使用相同的值
- Redis 模块新增 FurySerializer 支持 fury 序列化，需自行引入 fury 依赖
- Model、Record 新增 get(String attr, Function<Object, T> converter) 与 get(String attr, T defaultValue, Function<Object, T> converter)，取值时支持自定义转换函数
- Ret、Kv、Okv 新增 getAs(Object key, Function<Object, T> converter) 系方法，同上
- Kv、Okv 的 getStr、getInt、getLong 等 get 系方法新增默认值重载，value 为 null 时返回默认值
- Kv、Okv 新增 notBlank(Object key)、isBlank(Object key)
- Kv、Okv、Ret 新增 remove(Object key) 删除指定 key，返回 this 支持链式调用
- ActiveRecordPlugin 新增 setAutoConfigDialect(boolean)，开启后根据 jdbcUrl 自动识别并配置数据库方言
- HikariCpPlugin 新增 setKeepaliveTime(...)、setPrintLog(...)、newHikariConfig()，支持连接保活与 SQL 日志配置
- TimeKit 新增 now()、nowWithMillisecond()，以字符串格式返回当前时间
- TimeKit 新增 toLong(LocalDateTime, int type) 与 nowToLong(int type)，type 取值 1 至 7 分别表示年、月、日、时、分、秒、毫秒精度
- TimeKit 新增 parse(String)、parseLocalDateTime(String)、parseLocalTime(String)，自动探测 pattern 无需手写
- TypeKit 新增 toLocalDate(Object)、toLocalTime(Object) 类型转换
- Model、Record 新增 getLocalDate(String)、getLocalTime(String)
- TypeKit.toDate(...) 支持 OffsetDateTime、ZonedDateTime 类型转换
- BaseModelGenerator、ModelGenerator、MappingKitGenerator 新增 getEngine()，便于在配置方法中定制 engine
- 生成器内部 engine 开启静态方法/字段表达式，代码生成模板可直接使用
- 生成器 TypeMapping 类型映射调整：java.sql.Date 映射为 LocalDate、java.sql.Time 映射为 LocalTime、LocalDateTime 映射为 java.util.Date，重新生成代码时注意 getter 返回类型变化
- IContainerFactory 继承 Serializable 支持序列化
- CaptchaRender 输出验证码时 cookie 添加 httpOnly，提升安全性
- Cron4jPlugin 的 taskDaemon 默认值由 true 改为 false
- CaseInsensitiveContainerFactory 的 toLowerCase 去除 static，多数据源可分别配置大小写策略
- FastStringWriter 去除 throws IOException，缩减使用时的代码量
- Engine.getTemplate(String) 不再自动追加前缀字符 "/"，前缀不同的相同文件不共用缓存
- 字符串模板缓存 key 改为直接使用内容本身，省去 MD5 计算
- ScheduledKit 调度方法的 task 参数由第一个移至最后一个
- MultipartRequest.isSafeFile()、handleIllegalUpload() 改为 protected，便于继承扩展
- TimeKit 解析日期更严格：数字段位数宽松匹配，但拒绝不存在的日期（如 2026-02-31）
- TimeKit 移除 toDate(LocalTime)，toLocalDateTime(...)、toLocalDate(...) 不再支持 java.sql.Time 转换
- Db 的 queryDate(...)、queryLocalDateTime(...) 不再支持 LocalTime
- ModelBuilder 改进 clob 字段读取方式，解决内容较多时后半部分乱码的问题
- Enjoy 的 MethodKit 禁用列表去除 java.lang.Compiler，支持 JDK 21
- HttpKit.readData 读取 buffer 由 1024 调整为 2048
- fastjson 升级为 fastjson2 2.0.64，默认兼容 fastjson 1 的循环引用与智能字段匹配，定制过 FastJson 的项目需调整相关 API 参数类型
- jedis 由 3.6.3 升级到 7.5.2，Cache 的 zrange、zrevrange、zrangeByScore 返回值由 Set 改为 List，使用 RedisPlugin 的项目需同步升级 jedis
- fury 依赖迁移为 org.apache.fury:fury-core 并升级至 0.10.3
- javassist 升级至 3.30.2-GA，解决 Method.setAccessible(...) 抛出异常问题
- 可选依赖统一由 provided 改为 optional true，copy 依赖到自己项目时需去除 optional 标签方可生效
- 固定 javax.el 版本为 3.0.0，避免动态版本范围带来的不确定性
- 移除 redis 模块的 JdkSerializer

## jfinal 5.1.1（包含 5.0.0 至 5.1.1）

- 新增 SseEmitter 使用 sse 实现 chat gpt 打字机效果，方便开发 chat gpt 应用项目
- 新增 Javassist 代理，替代 cglib 以便支持 JDK 17
- 新增 H2Dialect 支持 H2 数据库
- RedisPlugin 新增 withLock、lock、unlock 支持分布式锁
- RedisPlugin 新增 psetex、scan、msetnx、renamenx、lpushx、rpushx
- RedisPlugin 新增 eval 与 script 系方法，支持 lua 脚本
- RedisPlugin 添加事务 API
- Enjoy 变量名、方法名允许使用字符 '$'
- Enjoy 添加 #returnIf(expr) 指令
- Enjoy 的 Static Method 与 Static Field 表达式需要配置才可使用，提升安全性
- Engine 新增 removeSharedMethod(String methodName, Class<?>... paraTypes) 支持移除整个 class 中的 Shared Method
- 改进 ProxyCompiler 支持 JDK 17
- Ret 新增带 T defaultValue 参数的 getAs 方法
- BaseModelGenerator 支持 BigDecimal、BigInteger、Boolean
- TypeConverter.toBoolean 方法支持 Number 下的整数类型，不支持浮点类型
- ActionHandler 404 日志级别由 warn 改为 info，减少日志输出量
- Kv、Okv getAs 方法的 defaultValue 参数使用泛型
- Cron4jPlugin 添加 boolean isStarted，控制启动流程
- ScheduledKit 新增两个带 WithTryCatch 的调度方法，避免调度出现异常后被终止
- CaptchaRender 新增小写字母，增加图片识别难度
- Tx 拦截器声明式事务添加事务函数，支持定制事务行为
- 新增 StaticHtmlRender，支持静态文件生成与输出

## jfinal 5.0.0（包含 4.9.16 至 5.0.0）

- 支持 json 请求自动解析并注入 action 方法参数，便于支持前后分离项目。使用需开启配置：

  ```java
  me.setResolveJsonRequest(true);
  ```

- #para 指令支持 sql 的 like、in 子句，例如：

  ```sql
  select * from article where title like #para(0, "like")
  select * from article where id in #para(idList, "in")
  ```

- ErrorRender 支持 json 请求响应 json 格式数据，可配置响应数据：

  ```java
  me.setErrorJsonContent(400, Ret.fail("msg", "404 Not Found").toJson());
  ```

- Enjoy 的 Field、Method 表达式支持可选链操作符 ?.，例如：

  ```
  article?.title
  article?.getTitle()
  page?.getList()?.size()
  ```

- Db.update(...)、Db.batchUpdate(...) 支持 modifyFlag，只更新被修改的字段，提升性能
- Generator 新增支持生成视图 model，需要配置：Generator.setGenerateView(true)
- Generator 新增黑白名单机制：Generator.addBlacklist(...)、addWhitelist(...)
- Model、Record 的 getInt、getLong、getDate 等 getXxx 系方法支持将 String 转成所需类型
- 新增 InformixDialect，支持 informix 数据库
- Controller 文件上传 getFile 系方法支持大于 2G 的文件，需要升级至 cos-2022.2 版
- Kv、Okv 的 getInt、getLong、getDate 等 getXxx 系方法支持将 String 转成所需类型
- Ret 添加大量定制化配置，方便前后分离项目按需求定制 json 格式
- Ret、Kv、Okv 新增 of(key, value) 方法
- Ret 添加 ok(String msg) 与 fail(String msg) 方法，减少代码量
- 新增 ScheduledKit，简化任务调度
- 新增 ThreadPoolKit，简化线程池使用
- redis Cache 新增 call 方法，支持如下用法：

  ```java
  Long ret = Redis.use().call(jedis -> {
     return jedis.incrBy("key", 1);
  });
  ```

  便于调用 jedis 内部提供的任意 API

- redis Cache 新增 setnx 方法
- Engine 新增 create/createIfAbsent 方法，便于在创建的同时进行配置：

  ```java
  Engine engine = Engine.createIfAbsent("myEngine", e -> {
     e.setDevMode(true);
     e.setToClassPathSourceFactory();
  });
  ```

- TypeConverter 新增 setConvertFunc 方法，可定制转换逻辑
- activerecord 添加 IRow 接口，支持统一的方式来处理 Model 和 Record
- activerecord 中的 TypeMapping 将 LocalDateTime、LocalDate 映射为 Date
- TypeMapping 添加 removeTypeMapping(...)，方便定制映射关系
- FastJson 添加 toJson(Object object, SerializerFeature... features)
- ActionHandler 当 Action 不存在时的日志由 warn 改为 info
- 移除 velocity 模板引擎

## jfinal 4.9.15（包含 4.9.09､ 4.9.10､ 4.9.11､ 4.9.12､ 4.9.13､ 4.9.14）

> 注意：由于新版本的 `PathKit.getWebRootPath()` 方法得到了改进，所以这个方法在 eclipse、IDEA
> 中以非 web 方法使用时可以得到正确的路径，所以原先在生成器中使用的 `PathKit.getWebRootPath()`
> 需要改成：`System.getProperty("user.dir")`
>
> 官网可下载的项目资源中的生成器已经改为上述代码，可以下载使用

- 添加 Engine.createIfAbsent(...)，便于在未创建 engine 时便捷创建，免去 if 判断
- 改进 Lexer，非解析块（原样输出块）处在独立行时，删除尾部换行字符
- 解决了上一个版本在模板文件包含字符 '\r' 时，生成的内容多出来空行的问题
- FileSource、ClassPathSource、HttpKit 中的 BufferedReader 改为使用 InputStreamReader，避免 8KB 的内存分配，并且可以保留模板中 windows 换行字符 \r
- 新增 Engine.getTemplateByString(String content, String cacheKey)，常用于模板内容存放数据库的场景，其数据库中的 id 值可以作为 key
- 优化 Engine.getTemplateByString，避免在缓存模板之前两次创建 cacheKey
- MethodKit 新增 forbiddenClass 与 forbiddenMethod
- 新增支持 ActiveRecordPlugin 支持定制 JavaType，也即在启动时可干预数据库字段类型与 java 类型的关系
- Controller.doSetCookie 开放为 protected。path 为空字符串时处理成 "/"
- SqlReporter 构造方法与 getConnection() 改为 public，便于扩展
- Table 添加 setColumnType 与 setColumnTypeMap 开放为 public
- PathKit.detectWebRootPath() 支持 maven 项目在开发环境下探测到正确的 webRootPath
- 支持扩展 ActionMapping，方便自定义路由规则，通过 me.setActionMapping(...) 实现
- Captcha 添加三个 setter 方法

## jfinal 4.9.12

- 请勿使用 4.9.12 这个版本，而是直接使用 4.9.13 版。因为模板文件在具有字符 '\r' 时会多出来空行。新版本 CharTable.isBlank(char) 添加 '\r' 判断解决了该问题

## jfinal 4.9.08（包含 4.9.07）

- Record.getDate(...) 添加对 LocalDateTime、LocalDate、LocalTime 类型的自动转换
- Model.getDate(...) 添加对 LocalDateTime、LocalDate、LocalTime 类型的自动转换
- Db 的 queryDate(...) 添加对 LocalDateTime、LocalDate、LocalTime 类型的自动转换
- Record 添加 getLocalDateTime(...)
- Model 添加 getLocalDateTime(...)
- Db 添加 queryLocalDateTime(...)
- DbTemplate 添加 queryLocalDateTime(...)
- JFinalJson 添加 LocalDateTime、LocalDate、LocalTime 支持
- Converters、TypeConverter 添加 LocalDateTime、LocalDate 支持
- Generator 添加 addTypeMapping(...) 支持定制类型映射
- Generator 新增两种可自动转换类型的 getter 方法：getDate、getLocalDateTime，例如：

  ```java
  generator.addTypeMapping(Date.class, LocalDateTime.class)
  ```

- Dialect 添加 forPaginateTotalRow(...)，为分页方法生成查询 totalRow 值的 sql，支持定制 total row 查询 sql
- Db 添加 queryBigInteger
- Model 的 doPaginate 与 deleteById 由 private 改为 protected，方便扩展
- enjoy 下标取值表达式参数扩大支持类型的范围，由 Integer 改为 Number
- FastJson 中的 Exception 改为 Throwable
- redis 插件中的 ICallback 添加泛型

## jfinal 4.9.06（包含 4.9.04､4.9.05）

- Model、Db 新增 each 方法
- DbTemplate、DaoTemplate 新增 each 与 getSqlPara 方法
- MetaBuilder 中的 filter 方法更名为 skip
- 微调 PathScanner，在跳过 provided 依赖时输出一条 debug 信息
- DaoTemplate 添加 queryDouble、queryFloat、queryDate、queryBoolean
- 修正 redis plugin 中 hgetCounter 方法的 bug，添加 hgetFloatCounter

## jfinal 4.9.03

- Routes 添加路由扫描配置，通过 Routes.scan(basePackage) 扫描路由
- 添加 Path 注解以及 PathScanner 实现路由扫描功能
- controllerKey 改为 controllerPath，多个 Controller 可共享同一个 controllerPath
- Invocation 中的 getControllerKey() 改名为 getControllerPath()，原方法保留并 @Deprecated

## jfinal 4.9.02（包含 4.9.01）

- enjoy 支持中文变量名、中文方法名、中文模板函数名，开启配置 Engine.setChineseExpression(boolean enable)
- #number 指令舍入规则由默认银行家舍入法改为四舍五入法
- Engine 添加 setRoundingMode(...) 用于配置 #number 指令、Arith 的舍入规则，默认为四舍五入
- #date 指令支持 LocalDateTime、LocalDate、LocalTime
- ActionReporter 支持定制，添加配置方法 Constants.setActionReporter(...)
- 改进 JFinalJson，支持可重入转换
- Prop 的 get(String)、get(String, String) 方法对返回值添加 trim() 操作
- Enjoy 的 Compressor 改进算法，提升压缩率，压缩 sql 模板格式更美观，sql 压缩配置方法:

  ```java
  activeRecordPlugin.getEngine().setCompressorOn(' ');
  ```

- 改进 RedirectRender 在 nginx 反向代理时的支持，redirect(...) 保持住 https
- Writer 实现 AutoCloseable 接口，支持 try with resources 语法
- Template 对于 Writer 的使用改为 try with resources 语法
- 新增 TimeKit 用于简化 JDK 8 新增的时间 API
- Converters、DateKit、Validator、Jackson 中的 SimpleDateFormat 改为使用 TimeKit
- Kv、Okv 添加 keep(String...)
- CPI 添加 getAction(Controller)，可获取 Controller 内部的 Action 属性，提升扩展性
- enjoy 模板引擎 buffer 支持 reentrant，添加 reentrantBufferSize 配置
- Redis 的 Cache 添加订阅、发布 API（感谢 jfinal 俱乐部 @杜福忠 贡献代码）
- 添加 Constants.setToJavaAwtHeadless() 配置。在缺少显示设备、键盘或鼠标时 Graphics、Font、Color、ImageIO 等等 API 仍然能够使用
- WriterBuffer.bufferSize 默认值由 2048 改为 1024，Engine.setWriterBufferSize(int) 更名为 setBufferSize(int)
- #random 指令中的 Random 改为使用 ThreadLocalRandom
- HashKit 中的 Random 改为使用 ThreadLocalRandom
- Model.find(Config, Connection, String, Object...) 改为 protected
- PropKit、Prop 添加 getDouble 方法
- NowDirective 指令中 IOException 改为 Exception，在非 IO 异常时输出模板文件与行号
- Injector.injectBean(...) 异常信息添加 paraName
- Controller.createToken(...) 添加返回值
- MethodKit.addExtensionMethod 支持扩展方法的第一个参数为父类
- Encoder 由抽象类改为接口
- Render 实现类添加 OutputStream 与 Writer 的 flush() 操作，提升对客户端的响应速度
- 将 ActionHandler 中对 Controller 的依赖注入挪至 ControllerFactory，便于用户扩展时控制注入
- JFinalJson 添加 setModelAndRecordFieldNameToCamelCase(boolean toLowerCaseAnyway)
- Enjoy 的 Unary 添加对 short、byte、BigInteger 的支持
- RangeArray 消除除法操作
- Db、DbPro、DbTemplate 添加 queryBigInteger(...)
- Druid 添加三个配置：defaultTransactionIsolation、validationQueryTimeout、keepAlive
- JFinalConfig 添加 useFirstFound(...)
- Fastjson 配置为 setSafeMode 禁用 autoType，添加 addSerializer、setSafeMode 方法
- 输出指令与 #escape 指令去除 Short 类型判断分支
- FieldKeyBuilder 的 classHash 改为 int

## jfinal 4.9

- 优化、重构 JFinalJson 模块，性能提升至 jackson 的 3 倍，增强扩展性，并增加了更多功能性配置
- enjoy 添加 html 压缩功能，配置方法 engine.setCompressorOn();
- Engine 添加 addEnum(...)，便于支持枚举类型在模板中的使用
- enjoy 的 Ctrl 添加一个 Object attachment 对象及其 getter、setter 方法，便于穿透 Scope 传递变量
- Scope 添加 getSharedObject 方法
- 优化 StrKit.toCamelCase(...)，并添加一个带有 boolean toLowerCaseAnyway 参数的方法，便于支持本来就是驼峰格式的场景
- 优化 StrKit.isBlank(...) 性能
- enjoy 的 Arith 针对 BigDecimal.divide(...) 添加 scale、rounding mode 两个配置。默认值分别为：5、四舍五入
- Lexer 在字符 # 与关键字指令相继出现时，不再要求一定解析出指令，便于支持 $("#if").method() 这类 jquery 用法
- enjoy 的 Arith 与 Compare 表达式添加对 BigInteger 的支持
- enjoy 的 Template 添加四个携带 Func 接口的 render 方法，支持更深度的扩展
- Engine 添加 setCacheStringTemplate(boolean) 用于控制是否缓存通过 getTemplateByString(String) 获取的模板
- EngineConfig 添加 setWriterBuffer，便于用户自己扩展 WriterBuffer
- 添加 CORSInterceptor、EnableCORS，支持跨域资源共享（jpress、jboot 作者 @海哥 贡献）
- FileRender 添加 boolean normalRenderOnly，用于强制客户端只能单线程下载，减轻服务器带宽压力
- Db 添加两个 txInNewThread 方法，支持在新线程开启事务
- Db.queryBigDecimal(...) 支持 String 类型
- Model、Record 的 getBigDecimal 支持 String 类型
- TableMapping.putTable(...) 不再检测重复 key 值，用于支持动态更新 model 映射
- TableBuilder 改为 public 可见性，便于用户利用其动态更新 model 映射
- DruidPlugin 添加 setDefaultTransactionIsolation 配置，所有属性改为 protected
- Model、DbPro 中开启的 PrepareStatement 的调用在 try-with-resources 中保障被 close() 掉
- Db 带有 List<Record 参数的 batch 方法改为 ? extends Record，支持 Record 子类
- 添加 CPI.addModelToConfigMapping(...)
- Ret、Kv、Okv 添加 getDouble、getFloat 方法
- MixedJsonFactory 支持 setDatePattern
- Json 的 datePattern、defaultDatePattern 允许配置为 null，用于支持输出 long 型时间戳
- HttpKit 添加 setConnectionTimeout 与 setReadTimeout 配置
- jackson 添加配置: objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false), 不存在 getter 方法时不抛异常
- FastStringWriter 的 MAX_BUFFER_SIZE 由 256K 改为 512K
- enjoy 的 Logic 去除 newWorkMode 这个控制变量及其相关代码
- 添加 Func lambda 函数工具箱
- WriterBuffer 的 MAX_BUFFER_SIZE 由 10M 改为 2M
- InterceptorStack 添加对内部拦截器的注入
- cos 升级到 2020.4，提升安全性，同时支持 jfinal 与 cos 中的 ExceededSizeException 两个异常类型
- jackson 升级到 2.11.0
- fastjson 升级到 1.2.68

## jfinal 4.8（包含 4.3､ 4.4、4.5、4.6、4.7 版）

- Enjoy 模板引擎添加 FastFieldGetter，支持动态类型的前提下性能提升 13%
- 添加 Engine.setFastMode(boolean)，用于配置极速模式
- 增强 Enjoy 模板引擎，新增 addDirective(.., .., boolean keepLineBlank) 方法，支持指令独占一行时保留前后空白字符：与输出指令 #() 行为一样
- Enjoy 模板引擎的 #date、#number、#escape 等输出型指令，配置成 keepLineBlank = true
- sql 管理模块的 #para 指令配置为 keepLineBlank = true
- 优化注释对输出格式的影响，注释与指令处于同一行时保留行尾的换行字符（注释处在独立行时无此问题）
- 优化 FastStringWriter 性能
- ByteWriter、CharWriter 递归优化为 while 循环
- 优化 #escape 指令性能，StringBuilder 字符串拼接改为直接向 Writer 输出
- 输出指令 Output 去掉对 Boolean 类型的判断
- 添加 Engine.removeSharedObject(...) 方法，可配合 addSharedObject(...) 支持动态切换共享对象，适用更多应用场景
- 添加 JdkEncoderFactory 方便配置 Engine.setToJdkEncoderFactory() 支持 utf8mb4 编码的 emoji 表情
- 改进 Utf8Encoder，在极端情况下碰到无法编码的字符时，抛异常改为输出问号，提升用户体验
- EngineConfig.getSharedObjectMap() 改为 public 可见性
- DbPro 中的 8 个方法由默认可见性改为 protected，便于继承扩展
- 添加 MetaBuilder.filter(...) 方法，更方便的 table 过滤支持
- 改进 @Para 注解，支持单独指定 defaultValue 参数值，支持传入空字符串为默认值
- 改进 ProxyCompiler，解决 tomcat 丢失 CLASSPATH 导致的代理编译异常
- 改进 ProxyGenerator，解决单一数组参数方法无法被代理的问题
- proxy_class_template.jf Invocation 与 Callback 的实参改为 p0
- StrKit 添加 defaultIfBlank(...)、join(...) 方法
- HandlerKit 添加对 queryString 追加参数的支持
- 文件上传组件 cos 升级到 2019.8 版本
- 添加 Constants.setDenyAccessJsp(boolean) 配置方法，默认不允许直接访问 jsp 文件，加固 tomcat、jetty 安全
- JsonRender、TextRender 中去掉对 http header 的缓存控制，减少数据传输量，有利于构建超高并发 json 服务
- Controller.renderText(text, contentType) 的 contentType 参数支持简写为 "xml"、"js"、"json"、"html"，提升开发体验
- ActionHandler 抽取 getAction() 方法，便于子类覆盖自由定制路由
- 改进 MetaBuilder，支持多 catalog 且同名 table 情况下的备注生成
- RedisPlugin 去除对于 password 参数的 null 值判断，方便无 password 环境下使用
- Template 添加无参 renderToString() 方法
- 生成器辅助类 TypeMapping，java.sql.Time 映射为其自身
- 添加 Constants.setToCglibProxyFactory()
- 解决 AopFactory 在某些条件下的循环依赖注入无法保障单例的问题
- log 模块添加 slf4j 日志支持，添加可变参数 API，并且添加 trace 日志级别
- 添加 Constants.setToSlf4jLogFactory() 支持快捷配置到 slf4j 日志
- redis 插件的 Cache 添加 hgetCounter(Object key, Object field)
- aop 模块添加 addSingletonObject(Class<?> type, Object singletonObject)，支持指定类型的映射
- Enjoy 模板引擎添加 NullMethodInfo，简化代码提升性能
- Enjoy 模板引擎 Scope 中的 getGlobal、setGlobal、removeGlobal 支持顶层 data 为 null
- Enjoy 模板引擎 #set 系列指令放开对于自增、自减表达式的使用限制
- JFinalFilter 所有属性改为 protected ，便于继承扩展

## jfinal 4.2 (包含 4.0、4.1)

- 新增 proxy 模块，enjoy、class loader、dynamic compile 美妙结合，以 613 行代码消除对 cglib-nodep (19505 行代码）的第三方依赖
- 删掉 cglib-nodep 依赖，使得 jfinal 告别第三方依赖
- Enhancer、Duang 中对 cglib 的依赖改为使用 proxy
- 删除 @Enhance 注解，新增的 proxy 模块会根据拦截器存在与否自动决定是否 enhance，使用 @Clear 清除拦截器便可消解掉 @Enhance
- 去掉 aop 模块有关 enhance 配置的代码，已被 proxy 消解
- Model、Db 添加 template 模板查询 API，使得用户相关代码量再减少 50%，进一步提升开发体验
- 添加 DaoTemplate、DbTemplate，用于实现模板查询
- Validator 添加 setRet(...)、getRet() 便于接管验证结果，方便业务层返回值与 Validator 返回值统一成 Ret，进而便于前端 js 统一处理逻辑
- 改进 ActionHandler，异常输出增加 action 签名，方便更快定位
- 添加 Reflect.getMethodSignature(...)
- Controller 中的 getDate/getParaToDate 改为使用 TypeConverter，支持更广泛的 date pattern
- 增强 Converters，支持 HTML 5 的 date time 组件，格式为：2019-01-23T11:22
- configInterceptor 回调次序调整到 configRoute 之前，支持 configRoute 中的 routes 级拦截器被注入的对象应用全局拦截器
- Enjoy 的 field 表达式，调整 RealFieldGetter 优先级高于 ModelFieldGetter，支持 field 表达式获取 Model 中的 public 属性，便于代替 static field 用法节省代码量
- enjoy 模块三处代码改为 try with resource 风格，老风格是为了老版本兼容 JDK 1.6
- TextRender 判断 contentType 是否包含 "charset" 来决定是否 setEncoding，避免 setEncoding 覆盖掉 contentType 中的 "charset"
- 删除 Ret、Kv 中早已被 @Deprecated 的代码
- 删除早已被 @Deprecated 过的 JMap，早已被 Kv 完全取代
- 删除早已被 @Deprecated 的 Sqls，早已被 sql 模板完全取代
- jetty-server 升级到 2019.3
- 新增 ProxyFactory 扩展 CglibProxyFactory

## jfinal 3.8

- AopFactory 修复子类没有属性时，超类无法被注入的问题
- Aop 中的有关配置的 API 转移至 AopManager
- Constants 中添加 setInjectSuperClass(boolean) 配置是否对超类进行注入
- Validator 添加注入功能
- Cron4jPlugin 添加 getTaskInfoList()，便于扩展
- Callback 优化性能，并去除 injectTarget 机制
- Invocation 中去除 useInjectTarget

## jfinal 3.7

- 增强 Aop 模块，支持任意层次数的注入，完美支持循环依赖
- Aop 支持对超类进行注入，需要配置 Aop.setInjectSuperClass(true)，默认不开启
- PropKit 添加 useFirstFound(String... fileNames) 方法，支持配置文件优先加载策略。具体用法参考 jfinal_demo_for_maven，可在官网首页右侧下载
- 去除 AopFactory 中的 injectDepth 检测机制，已更好支持循环依赖注入
- ControllerFactory 添加 recycle(Controller) 方法，方便用户自定义 controller 回收策略
- 增强 MetaBuilder，解决 Oracle 驱动 bug：生成重复主键
- 增强 DataDictionaryGenerator，解决 Oracle 驱动 bug：生成字典文件时报异常
- Enjoy 的 FieldKit、MethodKit 添加 clearCache() 方法，便于扩展
- Engine 添加 setEncoderFactory(...) 方法，方便定制 Encoder
- #include 指令模板参数为变量时，提示使用 #render 指令，提升开发体验
- ClassPathSource 找不到文件时的异常提示信息如果发生在 class path、jar 包中，提示信息中添加 "in CLASSPATH or JAR" 文本，定位错误到 class path 与 jar 包而非文件系统目录加快排错效率

## jfinal 3.6

- enjoy 引擎添加 #switch、#case、#default 指令，与 java 12 的 switch 新特性保持一致，既减少了代码量，又增强了功能
- enjoy 引擎添加 #call 指令，支持调用模板函数时，函数名与函数参数动态化，进一步提升 enjoy 引擎的灵活性
- Generator、MetaBuilder 添加生成 remarks 备注功能，需要配置：gen.setGenerateRemarks(true)
- Controller 添加 set(String name, Object value)，可替代 setAttr(...) 进一步节省代码量
- Controller 添加 get(...) 系方法，可替代 getPara(...) 系方法，可进一步节省大量代码
- Controller 添加 getInt(...)、getLong(...) 等系方法，可替代 getParaToInt(...) 等系方法，进一步节省代码量
- 增强 Tx 事务拦截器，在 Controller 中使用 try catch 时，可以在 catch 块中使用 render
- Kv、Okv、Ret 添加 setIfNotBlank(...)，使得 if(StrKit.notBlank(v)) kv.set(k, v) 可以改写为 kv.setIfNotBlank(k, v)，进一步减少代码量。 还添加了 setIfNotNull(...) 原因同上
- Db、DbPro 添加 getSqlParaByString(...) 方法，便于从 String 变量的模板中获取 SqlPara 对象，在 String 变量中使用 enjoy 引擎管理 sql 的生成远远优于使用字符串拼接方式
- Model、Db、DbPro 添加 findAll() 方便获取类似于字典 table 这样的小表数据
- EvictInterceptor 支持多 cacheName，用逗号分隔即可：@CacheName("a", "b")
- JFinalFilter 添加对 onStart()、onStop() 回调，并针对 jfinal undertow 优化异常提示信息
- ErrorRender 中的 String 型数据改为 byte[] 型数据，提性能。去除 flush()，避免 undertow 之下客户端主动断开连接时的 IO 异常
- 去掉 Render 子类中的 flush() 调用，避免 undertow 之下客户端主动中断连接时的 IO 异常，减少日志输出量
- Routes 添加 setMappingSuperClass(boolean) 配置控制器超类中的 public 方法是否被映射为 action，默认值为 false
- Model、Db、DbPro 中的针对多主键(联合主键) 的 findById(...) 方法更名为 findByIds(...)，deleteById(...) 更名为 deleteByIds(...) 解决 JDK 8 之下的参数类型转换异常问题
- ActionMapping 针对 Routes.setMappingSuperClass(...) 优化映射性能，大型项目启动速度提升 200 毫秒左右
- Model 添加 setOrPut(...) 方法，可自动判断数据是否属于数据表中的字段，如果是则添加修改标记，方便支持 update()
- JFinalConfig 添加 onStart()、onStop() 取代 afterJFinalStart()、beforeJFinalStop()，在减少代码输入量的同时，降低输入手误的概率。原方法被 @Deprecated 但仍然可用，方便升级兼容
- Sql 管理模块使用的 Engine 对象添加配置：engine.setToClassPathSourceFactory()，默认将从 class path 与 jar 包中加载 sql 文件
- 改进 MethodKit、FieldGetter 放开对 getMethod()、getField() 等方法的调用
- NestedTransactionHelpException、ValidateException 添加 fillInStackTrace() 优化性能。消除抛异常时 Throwable.fillInStackTrace() 的耗时动作，这两个异常中的 stack 信息不会被使用，所以没必要在 fillInStackTrace() 中进行任何操作
- Aop、AopFactory 添加 get(`Class<T>` targetClass, int injectDepth)，支持指定注入深度
- 优化 AopFactory，消除为 Controller 多级注入时多余的动作，进一步减少代码量
- Db、DbPro 添加 paginate(int, int, boolean isGroupBySql, SqlPara sqlPara)，在支持 SqlPara 参数的同时还支持 boolean isGroupBySql 参数
- Routes 添加 getRoutesList()、getControllerKeySet() 便于深度扩展，例如扩展可插拔路由功能
- Dialect、MysqlDialect、PostgreSqlDialect 添加 forFindAll(...)
- DataGetter 改为通过 Converter.convert(v) 进行类型转换，兼容更多 pattern 格式的日期型数据
- com.jfinal.core.paragetter 包下所有抛出 ActionException 异常的状态码由 404 改为 400，与 controller 中保持一致
- ParaProcessorBuilder 警告信息添加类名与方法名，便于开发过程中快速定位问题地点
- Record 添加 getObject(...) 方法
- Page 添加所有属性的 setter 方法，便于 fastjson 这类第三方反序列化
- com.jfinal.plugin.activerecord.CPI 添加 getConfig(...)、getTable(...)、getUsefulClass(...)，便于扩展 active record 功能
- DruidPlugin 的 initialSize 默认值改为 1 加快启动速度。maxActive 默认值改为 32 节省 Connection 资源
- KeepByteAndShortModelBuilder、KeepByteAndShortRecordBuilder 避免 JDBC 将 Short、Byte 类型字段的 null 值转换为 0
- Prop 添加 public 无参构造方法，并为属性赋予默认值，便于扩展
- Redis.call(...) 返回值由 Object 改为泛型 `<T>` T
- jetty-server 从 2018.11 升到 2019.1
