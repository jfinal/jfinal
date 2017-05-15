/**
 * Copyright (c) 2011-2017, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jfinal.plugin.redis;

import com.jfinal.plugin.redis.serializer.ISerializer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;
import java.util.Map.Entry;

/**
 * Cache.
 * Cache api 添加了中文注释，便于工程师更方便使用，另外还原样保持了
 * Jedis api 的方法名称及使用方法，以便于仅仅通过查看 Redis 文档
 * 即可快速掌握使用方法
 * Redis 命令参考: http://redisdoc.com/
 */
public class Cache {
	
	protected String name;
	protected JedisPool jedisPool;
	protected ISerializer serializer;
	protected IKeyNamingPolicy keyNamingPolicy;
	
	protected final ThreadLocal<Jedis> threadLocalJedis = new ThreadLocal<Jedis>();
	
	protected Cache() {
		
	}
	
	public Cache(String name, JedisPool jedisPool, ISerializer serializer, IKeyNamingPolicy keyNamingPolicy) {
		this.name = name;
		this.jedisPool = jedisPool;
		this.serializer = serializer;
		this.keyNamingPolicy = keyNamingPolicy;
	}
	
	/**
	 * 存放 key value 对到 redis
	 * 如果 key 已经持有其他值， SET 就覆写旧值，无视类型。
	 * 对于某个原本带有生存时间（TTL）的键来说， 当 SET 命令成功在这个键上执行时， 这个键原有的 TTL 将被清除。
	 */
	public String set(Object key, Object value) {
		Jedis jedis = getJedis();
		try {
			return jedis.set(keyToBytes(key), valueToBytes(value));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 存放 key value 对到 redis，并将 key 的生存时间设为 seconds (以秒为单位)。
	 * 如果 key 已经存在， SETEX 命令将覆写旧值。
	 */
	public String setex(Object key, int seconds, Object value) {
		Jedis jedis = getJedis();
		try {
			return jedis.setex(keyToBytes(key), seconds, valueToBytes(value));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 返回 key 所关联的 value 值
	 * 如果 key 不存在那么返回特殊值 nil 。
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(Object key) {
		Jedis jedis = getJedis();
		try {
			return (T)valueFromBytes(jedis.get(keyToBytes(key)));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 删除给定的一个 key
	 * 不存在的 key 会被忽略。
	 */
	public Long del(Object key) {
		Jedis jedis = getJedis();
		try {
			return jedis.del(keyToBytes(key));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 删除给定的多个 key
	 * 不存在的 key 会被忽略。
	 */
	public Long del(Object... keys) {
		Jedis jedis = getJedis();
		try {
			return jedis.del(keysToBytesArray(keys));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 查找所有符合给定模式 pattern 的 key 。
	 * KEYS * 匹配数据库中所有 key 。
	 * KEYS h?llo 匹配 hello ， hallo 和 hxllo 等。
	 * KEYS h*llo 匹配 hllo 和 heeeeello 等。
	 * KEYS h[ae]llo 匹配 hello 和 hallo ，但不匹配 hillo 。
	 * 特殊符号用 \ 隔开
	 */
	public Set<String> keys(String pattern) {
		Jedis jedis = getJedis();
		try {
			return jedis.keys(pattern);
		}
		finally {close(jedis);}
	}
	
	/**
	 * 同时设置一个或多个 key-value 对。
	 * 如果某个给定 key 已经存在，那么 MSET 会用新值覆盖原来的旧值，如果这不是你所希望的效果，请考虑使用 MSETNX 命令：它只会在所有给定 key 都不存在的情况下进行设置操作。
	 * MSET 是一个原子性(atomic)操作，所有给定 key 都会在同一时间内被设置，某些给定 key 被更新而另一些给定 key 没有改变的情况，不可能发生。
	 * <pre>
	 * 例子：
	 * Cache cache = RedisKit.use();			// 使用 Redis 的 cache
	 * cache.mset("k1", "v1", "k2", "v2");		// 放入多个 key value 键值对
	 * List list = cache.mget("k1", "k2");		// 利用多个键值得到上面代码放入的值
	 * </pre>
	 */
	public String mset(Object... keysValues) {
		if (keysValues.length % 2 != 0)
			throw new IllegalArgumentException("wrong number of arguments for met, keysValues length can not be odd");
		Jedis jedis = getJedis();
		try {
			byte[][] kv = new byte[keysValues.length][];
			for (int i=0; i<keysValues.length; i++) {
				if (i % 2 == 0)
					kv[i] = keyToBytes(keysValues[i]);
				else
					kv[i] = valueToBytes(keysValues[i]);
			}
			return jedis.mset(kv);
		}
		finally {close(jedis);}
	}
	
	/**
	 * 返回所有(一个或多个)给定 key 的值。
	 * 如果给定的 key 里面，有某个 key 不存在，那么这个 key 返回特殊值 nil 。因此，该命令永不失败。
	 */
	@SuppressWarnings("rawtypes")
	public List mget(Object... keys) {
		Jedis jedis = getJedis();
		try {
			byte[][] keysBytesArray = keysToBytesArray(keys);
			List<byte[]> data = jedis.mget(keysBytesArray);
			return valueListFromBytesList(data);
		}
		finally {close(jedis);}
	}
	
	/**
	 * 将 key 中储存的数字值减一。
	 * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 DECR 操作。
	 * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误。
	 * 本操作的值限制在 64 位(bit)有符号数字表示之内。
	 * 关于递增(increment) / 递减(decrement)操作的更多信息，请参见 INCR 命令。
	 */
	public Long decr(Object key) {
		Jedis jedis = getJedis();
		try {
			return jedis.decr(keyToBytes(key));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 将 key 所储存的值减去减量 decrement 。
	 * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 DECRBY 操作。
	 * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误。
	 * 本操作的值限制在 64 位(bit)有符号数字表示之内。
	 * 关于更多递增(increment) / 递减(decrement)操作的更多信息，请参见 INCR 命令。
	 */
	public Long decrBy(Object key, long longValue) {
		Jedis jedis = getJedis();
		try {
			return jedis.decrBy(keyToBytes(key), longValue);
		}
		finally {close(jedis);}
	}
	
	/**
	 * 将 key 中储存的数字值增一。
	 * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCR 操作。
	 * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误。
	 * 本操作的值限制在 64 位(bit)有符号数字表示之内。
	 */
	public Long incr(Object key) {
		Jedis jedis = getJedis();
		try {
			return jedis.incr(keyToBytes(key));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 将 key 所储存的值加上增量 increment 。
	 * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCRBY 命令。
	 * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误。
	 * 本操作的值限制在 64 位(bit)有符号数字表示之内。
	 * 关于递增(increment) / 递减(decrement)操作的更多信息，参见 INCR 命令。 
	 */
	public Long incrBy(Object key, long longValue) {
		Jedis jedis = getJedis();
		try {
			return jedis.incrBy(keyToBytes(key), longValue);
		}
		finally {close(jedis);}
	}
	
	/**
	 * 检查给定 key 是否存在。
	 */
	public boolean exists(Object key) {
		Jedis jedis = getJedis();
		try {
			return jedis.exists(keyToBytes(key));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 从当前数据库中随机返回(不删除)一个 key 。
	 */
	public String randomKey() {
		Jedis jedis = getJedis();
		try {
			return jedis.randomKey();
		}
		finally {close(jedis);}
	}
	
	/**
	 * 将 key 改名为 newkey 。
	 * 当 key 和 newkey 相同，或者 key 不存在时，返回一个错误。
	 * 当 newkey 已经存在时， RENAME 命令将覆盖旧值。
	 */
	public String rename(Object oldkey, Object newkey) {
		Jedis jedis = getJedis();
		try {
			return jedis.rename(keyToBytes(oldkey), keyToBytes(newkey));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 将当前数据库的 key 移动到给定的数据库 db 当中。
	 * 如果当前数据库(源数据库)和给定数据库(目标数据库)有相同名字的给定 key ，或者 key 不存在于当前数据库，那么 MOVE 没有任何效果。
	 * 因此，也可以利用这一特性，将 MOVE 当作锁(locking)原语(primitive)。
	 */
	public Long move(Object key, int dbIndex) {
		Jedis jedis = getJedis();
		try {
			return jedis.move(keyToBytes(key), dbIndex);
		}
		finally {close(jedis);}
	}
	
	/**
	 * 将 key 原子性地从当前实例传送到目标实例的指定数据库上，一旦传送成功， key 保证会出现在目标实例上，而当前实例上的 key 会被删除。
	 */
	public String migrate(String host, int port, Object key, int destinationDb, int timeout) {
		Jedis jedis = getJedis();
		try {
			return jedis.migrate(valueToBytes(host), port, keyToBytes(key), destinationDb, timeout);
		}
		finally {close(jedis);}
	}
	
	/**
	 * 切换到指定的数据库，数据库索引号 index 用数字值指定，以 0 作为起始索引值。
	 * 默认使用 0 号数据库。
	 * 注意：在 Jedis 对象被关闭时，数据库又会重新被设置为初始值，所以本方法 select(...)
	 * 正常工作需要使用如下方式之一：
	 * 1：使用 RedisInterceptor，在本线程内共享同一个 Jedis 对象
	 * 2：使用 Redis.call(ICallback) 进行操作
	 * 3：自行获取 Jedis 对象进行操作
	 */
	public String select(int databaseIndex) {
		Jedis jedis = getJedis();
		try {
			return jedis.select(databaseIndex);
		}
		finally {close(jedis);}
	}
	
	/**
	 * 为给定 key 设置生存时间，当 key 过期时(生存时间为 0 )，它会被自动删除。
	 * 在 Redis 中，带有生存时间的 key 被称为『易失的』(volatile)。
	 */
	public Long expire(Object key, int seconds) {
		Jedis jedis = getJedis();
		try {
			return jedis.expire(keyToBytes(key), seconds);
		}
		finally {close(jedis);}
	}
	
	/**
	 * EXPIREAT 的作用和 EXPIRE 类似，都用于为 key 设置生存时间。不同在于 EXPIREAT 命令接受的时间参数是 UNIX 时间戳(unix timestamp)。
	 */
	public Long expireAt(Object key, long unixTime) {
		Jedis jedis = getJedis();
		try {
			return jedis.expireAt(keyToBytes(key), unixTime);
		}
		finally {close(jedis);}
	}
	
	/**
	 * 这个命令和 EXPIRE 命令的作用类似，但是它以毫秒为单位设置 key 的生存时间，而不像 EXPIRE 命令那样，以秒为单位。
	 */
	public Long pexpire(Object key, long milliseconds) {
		Jedis jedis = getJedis();
		try {
			return jedis.pexpire(keyToBytes(key), milliseconds);
		}
		finally {close(jedis);}
	}
	
	/**
	 * 这个命令和 EXPIREAT 命令类似，但它以毫秒为单位设置 key 的过期 unix 时间戳，而不是像 EXPIREAT 那样，以秒为单位。
	 */
	public Long pexpireAt(Object key, long millisecondsTimestamp) {
		Jedis jedis = getJedis();
		try {
			return jedis.pexpireAt(keyToBytes(key), millisecondsTimestamp);
		}
		finally {close(jedis);}
	}
	
	/**
	 * 将给定 key 的值设为 value ，并返回 key 的旧值(old value)。
	 * 当 key 存在但不是字符串类型时，返回一个错误。
	 */
	@SuppressWarnings("unchecked")
	public <T> T getSet(Object key, Object value) {
		Jedis jedis = getJedis();
		try {
			return (T)valueFromBytes(jedis.getSet(keyToBytes(key), valueToBytes(value)));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 移除给定 key 的生存时间，将这个 key 从『易失的』(带生存时间 key )转换成『持久的』(一个不带生存时间、永不过期的 key )。
	 */
	public Long persist(Object key) {
		Jedis jedis = getJedis();
		try {
			return jedis.persist(keyToBytes(key));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 返回 key 所储存的值的类型。
	 */
	public String type(Object key) {
		Jedis jedis = getJedis();
		try {
			return jedis.type(keyToBytes(key));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 以秒为单位，返回给定 key 的剩余生存时间(TTL, time to live)。
	 */
	public Long ttl(Object key) {
		Jedis jedis = getJedis();
		try {
			return jedis.ttl(keyToBytes(key));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 这个命令类似于 TTL 命令，但它以毫秒为单位返回 key 的剩余生存时间，而不是像 TTL 命令那样，以秒为单位。
	 */
	public Long pttl(Object key) {
		Jedis jedis = getJedis();
		try {
			return jedis.pttl(keyToBytes(key));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 对象被引用的数量
	 */
	public Long objectRefcount(Object key) {
		Jedis jedis = getJedis();
		try {
			return jedis.objectRefcount(keyToBytes(key));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 对象没有被访问的空闲时间
	 */
	public Long objectIdletime(Object key) {
		Jedis jedis = getJedis();
		try {
			return jedis.objectIdletime(keyToBytes(key));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 将哈希表 key 中的域 field 的值设为 value 。
	 * 如果 key 不存在，一个新的哈希表被创建并进行 HSET 操作。
	 * 如果域 field 已经存在于哈希表中，旧值将被覆盖。
	 */
	public Long hset(Object key, Object field, Object value) {
		Jedis jedis = getJedis();
		try {
			return jedis.hset(keyToBytes(key), fieldToBytes(field), valueToBytes(value));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 同时将多个 field-value (域-值)对设置到哈希表 key 中。
	 * 此命令会覆盖哈希表中已存在的域。
	 * 如果 key 不存在，一个空哈希表被创建并执行 HMSET 操作。
	 */
	public String hmset(Object key, Map<Object, Object> hash) {
		Jedis jedis = getJedis();
		try {
			Map<byte[], byte[]> para = new HashMap<byte[], byte[]>();
			for (Entry<Object, Object> e : hash.entrySet())
				para.put(fieldToBytes(e.getKey()), valueToBytes(e.getValue()));
			return jedis.hmset(keyToBytes(key), para);
		}
		finally {close(jedis);}
	}
	
	/**
	 * 返回哈希表 key 中给定域 field 的值。
	 */
	@SuppressWarnings("unchecked")
	public <T> T hget(Object key, Object field) {
		Jedis jedis = getJedis();
		try {
			return (T)valueFromBytes(jedis.hget(keyToBytes(key), fieldToBytes(field)));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 返回哈希表 key 中，一个或多个给定域的值。
	 * 如果给定的域不存在于哈希表，那么返回一个 nil 值。
	 * 因为不存在的 key 被当作一个空哈希表来处理，所以对一个不存在的 key 进行 HMGET 操作将返回一个只带有 nil 值的表。
	 */
	@SuppressWarnings("rawtypes")
	public List hmget(Object key, Object... fields) {
		Jedis jedis = getJedis();
		try {
			List<byte[]> data = jedis.hmget(keyToBytes(key), fieldsToBytesArray(fields));
			return valueListFromBytesList(data);
		}
		finally {close(jedis);}
	}
	
	/**
	 * 删除哈希表 key 中的一个或多个指定域，不存在的域将被忽略。
	 */
	public Long hdel(Object key, Object... fields) {
		Jedis jedis = getJedis();
		try {
			return jedis.hdel(keyToBytes(key), fieldsToBytesArray(fields));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 查看哈希表 key 中，给定域 field 是否存在。
	 */
	public boolean hexists(Object key, Object field) {
		Jedis jedis = getJedis();
		try {
			return jedis.hexists(keyToBytes(key), fieldToBytes(field));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 返回哈希表 key 中，所有的域和值。
	 * 在返回值里，紧跟每个域名(field name)之后是域的值(value)，所以返回值的长度是哈希表大小的两倍。
	 */
	@SuppressWarnings("rawtypes")
	public Map hgetAll(Object key) {
		Jedis jedis = getJedis();
		try {
			Map<byte[], byte[]> data = jedis.hgetAll(keyToBytes(key));
			Map<Object, Object> result = new HashMap<Object, Object>();
			for (Entry<byte[], byte[]> e : data.entrySet())
				result.put(fieldFromBytes(e.getKey()), valueFromBytes(e.getValue()));
			return result;
		}
		finally {close(jedis);}
	}
	
	/**
	 * 返回哈希表 key 中所有域的值。
	 */
	@SuppressWarnings("rawtypes")
	public List hvals(Object key) {
		Jedis jedis = getJedis();
		try {
			List<byte[]> data = jedis.hvals(keyToBytes(key));
			return valueListFromBytesList(data);
		}
		finally {close(jedis);}
	}
	
	/**
	 * 返回哈希表 key 中的所有域。
	 * 底层实现此方法取名为 hfields 更为合适，在此仅为与底层保持一致
	 */
	public Set<Object> hkeys(Object key) {
		Jedis jedis = getJedis();
		try {
			Set<byte[]> fieldSet = jedis.hkeys(keyToBytes(key));
			Set<Object> result = new HashSet<Object>();
			fieldSetFromBytesSet(fieldSet, result);
			return result;
		}
		finally {close(jedis);}
	}
	
	/**
	 * 返回哈希表 key 中域的数量。 
	 */
	public Long hlen(Object key) {
		Jedis jedis = getJedis();
		try {
			return jedis.hlen(keyToBytes(key));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 为哈希表 key 中的域 field 的值加上增量 increment 。
	 * 增量也可以为负数，相当于对给定域进行减法操作。
	 * 如果 key 不存在，一个新的哈希表被创建并执行 HINCRBY 命令。
	 * 如果域 field 不存在，那么在执行命令前，域的值被初始化为 0 。
	 * 对一个储存字符串值的域 field 执行 HINCRBY 命令将造成一个错误。
	 * 本操作的值被限制在 64 位(bit)有符号数字表示之内。
	 */
	public Long hincrBy(Object key, Object field, long value) {
		Jedis jedis = getJedis();
		try {
			return jedis.hincrBy(keyToBytes(key), fieldToBytes(field), value);
		}
		finally {close(jedis);}
	}
	
	/**
	 * 为哈希表 key 中的域 field 加上浮点数增量 increment 。
	 * 如果哈希表中没有域 field ，那么 HINCRBYFLOAT 会先将域 field 的值设为 0 ，然后再执行加法操作。
	 * 如果键 key 不存在，那么 HINCRBYFLOAT 会先创建一个哈希表，再创建域 field ，最后再执行加法操作。
	 * 当以下任意一个条件发生时，返回一个错误：
	 * 1:域 field 的值不是字符串类型(因为 redis 中的数字和浮点数都以字符串的形式保存，所以它们都属于字符串类型）
	 * 2:域 field 当前的值或给定的增量 increment 不能解释(parse)为双精度浮点数(double precision floating point number)
	 * HINCRBYFLOAT 命令的详细功能和 INCRBYFLOAT 命令类似，请查看 INCRBYFLOAT 命令获取更多相关信息。
	 */
	public Double hincrByFloat(Object key, Object field, double value) {
		Jedis jedis = getJedis();
		try {
			return jedis.hincrByFloat(keyToBytes(key), fieldToBytes(field), value);
		}
		finally {close(jedis);}
	}
	
	/**
	 * 返回列表 key 中，下标为 index 的元素。
	 * 下标(index)参数 start 和 stop 都以 0 为底，也就是说，以 0 表示列表的第一个元素，以 1 表示列表的第二个元素，以此类推。
	 * 你也可以使用负数下标，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推。
	 * 如果 key 不是列表类型，返回一个错误。
	 */
	@SuppressWarnings("unchecked")
	
	/**
	 * 返回列表 key 中，下标为 index 的元素。
	 * 下标(index)参数 start 和 stop 都以 0 为底，也就是说，以 0 表示列表的第一个元素，
	 * 以 1 表示列表的第二个元素，以此类推。
	 * 你也可以使用负数下标，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推。
	 * 如果 key 不是列表类型，返回一个错误。
	 */
	public <T> T lindex(Object key, long index) {
		Jedis jedis = getJedis();
		try {
			return (T)valueFromBytes(jedis.lindex(keyToBytes(key), index));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 获取记数器的值
	 */
	public Long getCounter(Object key) {
		Jedis jedis = getJedis();
		try {
			return Long.parseLong((String)jedis.get(keyNamingPolicy.getKeyName(key)));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 返回列表 key 的长度。
	 * 如果 key 不存在，则 key 被解释为一个空列表，返回 0 .
	 * 如果 key 不是列表类型，返回一个错误。
	 */
	public Long llen(Object key) {
		Jedis jedis = getJedis();
		try {
			return jedis.llen(keyToBytes(key));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 移除并返回列表 key 的头元素。
	 */
	@SuppressWarnings("unchecked")
	public <T> T lpop(Object key) {
		Jedis jedis = getJedis();
		try {
			return (T)valueFromBytes(jedis.lpop(keyToBytes(key)));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 将一个或多个值 value 插入到列表 key 的表头
	 * 如果有多个 value 值，那么各个 value 值按从左到右的顺序依次插入到表头： 比如说，
	 * 对空列表 mylist 执行命令 LPUSH mylist a b c ，列表的值将是 c b a ，
	 * 这等同于原子性地执行 LPUSH mylist a 、 LPUSH mylist b 和 LPUSH mylist c 三个命令。
	 * 如果 key 不存在，一个空列表会被创建并执行 LPUSH 操作。
	 * 当 key 存在但不是列表类型时，返回一个错误。
	 */
	public Long lpush(Object key, Object... values) {
		Jedis jedis = getJedis();
		try {
			return jedis.lpush(keyToBytes(key), valuesToBytesArray(values));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 将列表 key 下标为 index 的元素的值设置为 value 。
	 * 当 index 参数超出范围，或对一个空列表( key 不存在)进行 LSET 时，返回一个错误。
	 * 关于列表下标的更多信息，请参考 LINDEX 命令。
	 */
	public String lset(Object key, long index, Object value) {
		Jedis jedis = getJedis();
		try {
			return jedis.lset(keyToBytes(key), index, valueToBytes(value));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 根据参数 count 的值，移除列表中与参数 value 相等的元素。
	 * count 的值可以是以下几种：
	 * count > 0 : 从表头开始向表尾搜索，移除与 value 相等的元素，数量为 count 。
	 * count < 0 : 从表尾开始向表头搜索，移除与 value 相等的元素，数量为 count 的绝对值。
	 * count = 0 : 移除表中所有与 value 相等的值。
	 */
	public Long lrem(Object key, long count, Object value) {
		Jedis jedis = getJedis();
		try {
			return jedis.lrem(keyToBytes(key), count, valueToBytes(value));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 返回列表 key 中指定区间内的元素，区间以偏移量 start 和 stop 指定。
	 * 下标(index)参数 start 和 stop 都以 0 为底，也就是说，以 0 表示列表的第一个元素，以 1 表示列表的第二个元素，以此类推。
	 * 你也可以使用负数下标，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推。
	 * <pre>
	 * 例子：
	 * 获取 list 中所有数据：cache.lrange(listKey, 0, -1);
	 * 获取 list 中下标 1 到 3 的数据： cache.lrange(listKey, 1, 3);
	 * </pre>
	 */
	@SuppressWarnings("rawtypes")
	public List lrange(Object key, long start, long end) {
		Jedis jedis = getJedis();
		try {
			List<byte[]> data = jedis.lrange(keyToBytes(key), start, end);
			if (data != null) {
				return valueListFromBytesList(data);
			} else {
				return new ArrayList<byte[]>(0);
			}
		}
		finally {close(jedis);}
	}
	
	/**
	 * 对一个列表进行修剪(trim)，就是说，让列表只保留指定区间内的元素，不在指定区间之内的元素都将被删除。
	 * 举个例子，执行命令 LTRIM list 0 2 ，表示只保留列表 list 的前三个元素，其余元素全部删除。
	 * 下标(index)参数 start 和 stop 都以 0 为底，也就是说，以 0 表示列表的第一个元素，以 1 表示列表的第二个元素，以此类推。
	 * 你也可以使用负数下标，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推。
	 * 当 key 不是列表类型时，返回一个错误。
	 */
	public String ltrim(Object key, long start, long end) {
		Jedis jedis = getJedis();
		try {
			return jedis.ltrim(keyToBytes(key), start, end);
		}
		finally {close(jedis);}
	}
	
	/**
	 * 移除并返回列表 key 的尾元素。
	 */
	@SuppressWarnings("unchecked")
	public <T> T rpop(Object key) {
		Jedis jedis = getJedis();
		try {
			return (T)valueFromBytes(jedis.rpop(keyToBytes(key)));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 命令 RPOPLPUSH 在一个原子时间内，执行以下两个动作：
	 * 将列表 source 中的最后一个元素(尾元素)弹出，并返回给客户端。
	 * 将 source 弹出的元素插入到列表 destination ，作为 destination 列表的的头元素。
	 */
	@SuppressWarnings("unchecked")
	public <T> T rpoplpush(Object srcKey, Object dstKey) {
		Jedis jedis = getJedis();
		try {
			return (T)valueFromBytes(jedis.rpoplpush(keyToBytes(srcKey), keyToBytes(dstKey)));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 将一个或多个值 value 插入到列表 key 的表尾(最右边)。
	 * 如果有多个 value 值，那么各个 value 值按从左到右的顺序依次插入到表尾：比如
	 * 对一个空列表 mylist 执行 RPUSH mylist a b c ，得出的结果列表为 a b c ，
	 * 等同于执行命令 RPUSH mylist a 、 RPUSH mylist b 、 RPUSH mylist c 。
	 * 如果 key 不存在，一个空列表会被创建并执行 RPUSH 操作。
	 * 当 key 存在但不是列表类型时，返回一个错误。
	 */
	public Long rpush(Object key, Object... values) {
		Jedis jedis = getJedis();
		try {
			return jedis.rpush(keyToBytes(key), valuesToBytesArray(values));
		}
		finally {close(jedis);}
	}

	/**
	 * BLPOP 是列表的阻塞式(blocking)弹出原语。
	 * 它是 LPOP 命令的阻塞版本，当给定列表内没有任何元素可供弹出的时候，连接将被 BLPOP 命令阻塞，直到等待超时或发现可弹出元素为止。
	 * 当给定多个 key 参数时，按参数 key 的先后顺序依次检查各个列表，弹出第一个非空列表的头元素。
	 */
	@SuppressWarnings("rawtypes")
	public List blpop(int timeout, Object... keys) {
		Jedis jedis = getJedis();
		try {
			List<byte[]> data = jedis.blpop(timeout, keysToBytesArray(keys));
			return keyValueListFromBytesList(data);
		}
		finally {close(jedis);}
	}
	
	/**
	 * BRPOP 是列表的阻塞式(blocking)弹出原语。
	 * 它是 RPOP 命令的阻塞版本，当给定列表内没有任何元素可供弹出的时候，连接将被 BRPOP 命令阻塞，直到等待超时或发现可弹出元素为止。
	 * 当给定多个 key 参数时，按参数 key 的先后顺序依次检查各个列表，弹出第一个非空列表的尾部元素。
	 * 关于阻塞操作的更多信息，请查看 BLPOP 命令， BRPOP 除了弹出元素的位置和 BLPOP 不同之外，其他表现一致。
	 */
	@SuppressWarnings("rawtypes")
	public List brpop(int timeout, Object... keys) {
		Jedis jedis = getJedis();
		try {
			List<byte[]> data = jedis.brpop(timeout, keysToBytesArray(keys));
			return keyValueListFromBytesList(data);
		}
		finally {close(jedis);}
	}
	
	/**
	 * 使用客户端向 Redis 服务器发送一个 PING ，如果服务器运作正常的话，会返回一个 PONG 。
	 * 通常用于测试与服务器的连接是否仍然生效，或者用于测量延迟值。
	 */
	public String ping() {
		Jedis jedis = getJedis();
		try {
			return jedis.ping();
		}
		finally {close(jedis);}
	}
	
	/**
	 * 将一个或多个 member 元素加入到集合 key 当中，已经存在于集合的 member 元素将被忽略。
	 * 假如 key 不存在，则创建一个只包含 member 元素作成员的集合。
	 * 当 key 不是集合类型时，返回一个错误。
	 */
	public Long sadd(Object key, Object... members) {
		Jedis jedis = getJedis();
		try {
			return jedis.sadd(keyToBytes(key), valuesToBytesArray(members));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 返回集合 key 的基数(集合中元素的数量)。
	 */
	public Long scard(Object key) {
		Jedis jedis = getJedis();
		try {
			return jedis.scard(keyToBytes(key));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 移除并返回集合中的一个随机元素。
	 * 如果只想获取一个随机元素，但不想该元素从集合中被移除的话，可以使用 SRANDMEMBER 命令。
	 */
	@SuppressWarnings("unchecked")
	public <T> T spop(Object key) {
		Jedis jedis = getJedis();
		try {
			return (T)valueFromBytes(jedis.spop(keyToBytes(key)));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 返回集合 key 中的所有成员。
	 * 不存在的 key 被视为空集合。
	 */
	@SuppressWarnings("rawtypes")
	public Set smembers(Object key) {
		Jedis jedis = getJedis();
		try {
			Set<byte[]> data = jedis.smembers(keyToBytes(key));
			Set<Object> result = new HashSet<Object>();
			valueSetFromBytesSet(data, result);
			return result;
		}
		finally {close(jedis);}
	}
	
	/**
	 * 判断 member 元素是否集合 key 的成员。
	 */
	public boolean sismember(Object key, Object member) {
		Jedis jedis = getJedis();
		try {
			return jedis.sismember(keyToBytes(key), valueToBytes(member));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 返回多个集合的交集，多个集合由 keys 指定
	 */
	@SuppressWarnings("rawtypes")
	public Set sinter(Object... keys) {
		Jedis jedis = getJedis();
		try {
			Set<byte[]> data = jedis.sinter(keysToBytesArray(keys));
			Set<Object> result = new HashSet<Object>();
			valueSetFromBytesSet(data, result);
			return result;
		}
		finally {close(jedis);}
	}
	
	/**
	 * 返回集合中的一个随机元素。
	 */
	@SuppressWarnings("unchecked")
	public <T> T srandmember(Object key) {
		Jedis jedis = getJedis();
		try {
			return (T)valueFromBytes(jedis.srandmember(keyToBytes(key)));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 返回集合中的 count 个随机元素。
	 * 从 Redis 2.6 版本开始， SRANDMEMBER 命令接受可选的 count 参数：
	 * 如果 count 为正数，且小于集合基数，那么命令返回一个包含 count 个元素的数组，数组中的元素各不相同。
	 * 如果 count 大于等于集合基数，那么返回整个集合。
	 * 如果 count 为负数，那么命令返回一个数组，数组中的元素可能会重复出现多次，而数组的长度为 count 的绝对值。
	 * 该操作和 SPOP 相似，但 SPOP 将随机元素从集合中移除并返回，而 SRANDMEMBER 则仅仅返回随机元素，而不对集合进行任何改动。
	 */
	@SuppressWarnings("rawtypes")
	public List srandmember(Object key, int count) {
		Jedis jedis = getJedis();
		try {
			List<byte[]> data = jedis.srandmember(keyToBytes(key), count);
			return valueListFromBytesList(data);
		}
		finally {close(jedis);}
	}
	
	/**
	 * 移除集合 key 中的一个或多个 member 元素，不存在的 member 元素会被忽略。
	 */
	public Long srem(Object key, Object... members) {
		Jedis jedis = getJedis();
		try {
			return jedis.srem(keyToBytes(key), valuesToBytesArray(members));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 返回多个集合的并集，多个集合由 keys 指定
	 * 不存在的 key 被视为空集。
	 */
	@SuppressWarnings("rawtypes")
	public Set sunion(Object... keys) {
		Jedis jedis = getJedis();
		try {
			Set<byte[]> data = jedis.sunion(keysToBytesArray(keys));
			Set<Object> result = new HashSet<Object>();
			valueSetFromBytesSet(data, result);
			return result;
		}
		finally {close(jedis);}
	}
	
	/**
	 * 返回一个集合的全部成员，该集合是所有给定集合之间的差集。
	 * 不存在的 key 被视为空集。
	 */
	@SuppressWarnings("rawtypes")
	public Set sdiff(Object... keys) {
		Jedis jedis = getJedis();
		try {
			Set<byte[]> data = jedis.sdiff(keysToBytesArray(keys));
			Set<Object> result = new HashSet<Object>();
			valueSetFromBytesSet(data, result);
			return result;
		}
		finally {close(jedis);}
	}
	
	/**
	 * 将一个或多个 member 元素及其 score 值加入到有序集 key 当中。
	 * 如果某个 member 已经是有序集的成员，那么更新这个 member 的 score 值，
	 * 并通过重新插入这个 member 元素，来保证该 member 在正确的位置上。
	 */
	public Long zadd(Object key, double score, Object member) {
		Jedis jedis = getJedis();
		try {
			return jedis.zadd(keyToBytes(key), score, valueToBytes(member));
		}
		finally {close(jedis);}
	}
	
	public Long zadd(Object key, Map<Object, Double> scoreMembers) {
		Jedis jedis = getJedis();
		try {
			Map<byte[], Double> para = new HashMap<byte[], Double>();
			for (Entry<Object, Double> e : scoreMembers.entrySet())
				para.put(valueToBytes(e.getKey()), e.getValue());	// valueToBytes is important
			return jedis.zadd(keyToBytes(key), para);
		}
		finally {close(jedis);}
	}
	
	/**
	 * 返回有序集 key 的基数。
	 */
	public Long zcard(Object key) {
		Jedis jedis = getJedis();
		try {
			return jedis.zcard(keyToBytes(key));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 返回有序集 key 中， score 值在 min 和 max 之间(默认包括 score 值等于 min 或 max )的成员的数量。
	 * 关于参数 min 和 max 的详细使用方法，请参考 ZRANGEBYSCORE 命令。
	 */
	public Long zcount(Object key, double min, double max) {
		Jedis jedis = getJedis();
		try {
			return jedis.zcount(keyToBytes(key), min, max);
		}
		finally {close(jedis);}
	}
	
	/**
	 * 为有序集 key 的成员 member 的 score 值加上增量 increment 。
	 */
	public Double zincrby(Object key, double score, Object member) {
		Jedis jedis = getJedis();
		try {
			return jedis.zincrby(keyToBytes(key), score, valueToBytes(member));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 返回有序集 key 中，指定区间内的成员。
	 * 其中成员的位置按 score 值递增(从小到大)来排序。
	 * 具有相同 score 值的成员按字典序(lexicographical order )来排列。
	 * 如果你需要成员按 score 值递减(从大到小)来排列，请使用 ZREVRANGE 命令。
	 */
	@SuppressWarnings("rawtypes")
	public Set zrange(Object key, long start, long end) {
		Jedis jedis = getJedis();
		try {
			Set<byte[]> data = jedis.zrange(keyToBytes(key), start, end);
			Set<Object> result = new LinkedHashSet<Object>();	// 有序集合必须 LinkedHashSet
			valueSetFromBytesSet(data, result);
			return result;
		}
		finally {close(jedis);}
	}
	
	/**
	 * 返回有序集 key 中，指定区间内的成员。
	 * 其中成员的位置按 score 值递减(从大到小)来排列。
	 * 具有相同 score 值的成员按字典序的逆序(reverse lexicographical order)排列。
	 * 除了成员按 score 值递减的次序排列这一点外， ZREVRANGE 命令的其他方面和 ZRANGE 命令一样。
	 */
	@SuppressWarnings("rawtypes")
	public Set zrevrange(Object key, long start, long end) {
		Jedis jedis = getJedis();
		try {
			Set<byte[]> data = jedis.zrevrange(keyToBytes(key), start, end);
			Set<Object> result = new LinkedHashSet<Object>();	// 有序集合必须 LinkedHashSet
			valueSetFromBytesSet(data, result);
			return result;
		}
		finally {close(jedis);}
	}
	
	/**
	 * 返回有序集 key 中，所有 score 值介于 min 和 max 之间(包括等于 min 或 max )的成员。
	 * 有序集成员按 score 值递增(从小到大)次序排列。
	 */
	@SuppressWarnings("rawtypes")
	public Set zrangeByScore(Object key, double min, double max) {
		Jedis jedis = getJedis();
		try {
			Set<byte[]> data = jedis.zrangeByScore(keyToBytes(key), min, max);
			Set<Object> result = new LinkedHashSet<Object>();	// 有序集合必须 LinkedHashSet
			valueSetFromBytesSet(data, result);
			return result;
		}
		finally {close(jedis);}
	}
	
	/**
	 * 返回有序集 key 中成员 member 的排名。其中有序集成员按 score 值递增(从小到大)顺序排列。
	 * 排名以 0 为底，也就是说， score 值最小的成员排名为 0 。
	 * 使用 ZREVRANK 命令可以获得成员按 score 值递减(从大到小)排列的排名。
	 */
	public Long zrank(Object key, Object member) {
		Jedis jedis = getJedis();
		try {
			return jedis.zrank(keyToBytes(key), valueToBytes(member));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 返回有序集 key 中成员 member 的排名。其中有序集成员按 score 值递减(从大到小)排序。
	 * 排名以 0 为底，也就是说， score 值最大的成员排名为 0 。
	 * 使用 ZRANK 命令可以获得成员按 score 值递增(从小到大)排列的排名。
	 */
	public Long zrevrank(Object key, Object member) {
		Jedis jedis = getJedis();
		try {
			return jedis.zrevrank(keyToBytes(key), valueToBytes(member));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 移除有序集 key 中的一个或多个成员，不存在的成员将被忽略。
	 * 当 key 存在但不是有序集类型时，返回一个错误。
	 */
	public Long zrem(Object key, Object... members) {
		Jedis jedis = getJedis();
		try {
			return jedis.zrem(keyToBytes(key), valuesToBytesArray(members));
		}
		finally {close(jedis);}
	}
	
	/**
	 * 返回有序集 key 中，成员 member 的 score 值。
	 * 如果 member 元素不是有序集 key 的成员，或 key 不存在，返回 nil 。
	 */
	public Double zscore(Object key, Object member) {
		Jedis jedis = getJedis();
		try {
			return jedis.zscore(keyToBytes(key), valueToBytes(member));
		}
		finally {close(jedis);}
	}
	
	// ---------
	
	protected byte[] keyToBytes(Object key) {
		String keyStr = keyNamingPolicy.getKeyName(key);
		return serializer.keyToBytes(keyStr);
	}

	protected Object keyFromBytes(byte[] bytes) {
		return serializer.keyFromBytes(bytes);
	}
	
	protected byte[][] keysToBytesArray(Object... keys) {
		byte[][] result = new byte[keys.length][];
		for (int i=0; i<result.length; i++)
			result[i] = keyToBytes(keys[i]);
		return result;
	}
	
	protected byte[] fieldToBytes(Object field) {
		return serializer.fieldToBytes(field);
	}
	
	protected Object fieldFromBytes(byte[] bytes) {
		return serializer.fieldFromBytes(bytes);
	}
	
	protected byte[][] fieldsToBytesArray(Object... fieldsArray) {
		byte[][] data = new byte[fieldsArray.length][];
		for (int i=0; i<data.length; i++)
			data[i] = fieldToBytes(fieldsArray[i]);
		return data;
	}
	
	protected void fieldSetFromBytesSet(Set<byte[]> data, Set<Object> result) {
		for (byte[] fieldBytes : data) {
			result.add(fieldFromBytes(fieldBytes));
		}
	}
	
	protected byte[] valueToBytes(Object value) {
		return serializer.valueToBytes(value);
	}
	
	protected Object valueFromBytes(byte[] bytes) {
		return serializer.valueFromBytes(bytes);
	}
	
	protected byte[][] valuesToBytesArray(Object... valuesArray) {
		byte[][] data = new byte[valuesArray.length][];
		for (int i=0; i<data.length; i++)
			data[i] = valueToBytes(valuesArray[i]);
		return data;
	}
	
	protected void valueSetFromBytesSet(Set<byte[]> data, Set<Object> result) {
		for (byte[] valueBytes : data) {
			result.add(valueFromBytes(valueBytes));
		}
	}
	
	@SuppressWarnings("rawtypes")
	protected List valueListFromBytesList(List<byte[]> data) {
		List<Object> result = new ArrayList<Object>();
		for (byte[] d : data)
			result.add(valueFromBytes(d));
		return result;
	}

	protected List keyValueListFromBytesList(List<byte[]> data) {
		List<Object> result = new ArrayList<Object>();
		result.add(keyFromBytes(data.get(0)));
		result.add(valueFromBytes(data.get(1)));
		return result;
	}
	
	// ---------
	
	public String getName() {
		return name;
	}
	
	public ISerializer getSerializer() {
		return serializer;
	}
	
	public IKeyNamingPolicy getKeyNamingPolicy() {
		return keyNamingPolicy;
	}
	
	// ---------
	
	public Jedis getJedis() {
		Jedis jedis = threadLocalJedis.get();
		return jedis != null ? jedis : jedisPool.getResource();
	}
	
	public void close(Jedis jedis) {
		if (threadLocalJedis.get() == null && jedis != null)
			jedis.close();
	}
	
	public Jedis getThreadLocalJedis() {
		return threadLocalJedis.get();
	}
	
	public void setThreadLocalJedis(Jedis jedis) {
		threadLocalJedis.set(jedis);
	}
	
	public void removeThreadLocalJedis() {
		threadLocalJedis.remove();
	}
}






