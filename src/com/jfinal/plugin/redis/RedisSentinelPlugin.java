package com.jfinal.plugin.redis;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.Protocol;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.IPlugin;
import com.jfinal.plugin.redis.serializer.FstSerializer;
import com.jfinal.plugin.redis.serializer.ISerializer;

public class RedisSentinelPlugin implements IPlugin {

	private String cacheName;

	private String masterName = null;
	private int connectionTimeout = Protocol.DEFAULT_TIMEOUT;
	//下一jedis版本预留字段
	//private int soTimeout = Protocol.DEFAULT_TIMEOUT;
	private String password = null;
	private int database = Protocol.DEFAULT_DATABASE;
	//下一jedis版本预留字段
	//private String clientName = null;
	private Set<String> sentinels = new HashSet<String>();

	private ISerializer serializer;
	private IKeyNamingPolicy keyNamingPolicy;
	private GenericObjectPoolConfig poolConfig;
	

	public RedisSentinelPlugin(String cacheName, String masterName,
			Set<HostAndPort> sentinels, final GenericObjectPoolConfig poolConfig) {
		this(cacheName, masterName, sentinels, poolConfig,
				Protocol.DEFAULT_TIMEOUT, null, Protocol.DEFAULT_DATABASE);
	}

	public RedisSentinelPlugin(String cacheName, String masterName,
			Set<HostAndPort> sentinels) {
		this(cacheName, masterName, sentinels, new GenericObjectPoolConfig(),
				Protocol.DEFAULT_TIMEOUT, null, Protocol.DEFAULT_DATABASE);
	}

	public RedisSentinelPlugin(String cacheName, String masterName,
			Set<HostAndPort> sentinels, String password) {
		this(cacheName, masterName, sentinels, new GenericObjectPoolConfig(),
				Protocol.DEFAULT_TIMEOUT, password);
	}

	public RedisSentinelPlugin(String cacheName, String masterName,
			Set<HostAndPort> sentinels, final GenericObjectPoolConfig poolConfig,
			int timeout, final String password) {
		this(cacheName, masterName, sentinels, poolConfig, timeout, password,
				Protocol.DEFAULT_DATABASE);
	}

	public RedisSentinelPlugin(String cacheName, String masterName,
			Set<HostAndPort> sentinels, final GenericObjectPoolConfig poolConfig,
			final int timeout) {
		this(cacheName, masterName, sentinels, poolConfig, timeout, null,
				Protocol.DEFAULT_DATABASE);
	}

	public RedisSentinelPlugin(String cacheName, String masterName,
			Set<HostAndPort> sentinels, final GenericObjectPoolConfig poolConfig,
			final String password) {
		this(cacheName, masterName, sentinels, poolConfig,
				Protocol.DEFAULT_TIMEOUT, password);
	}

	public RedisSentinelPlugin(String cacheName, String masterName,
			Set<HostAndPort> sentinels, final GenericObjectPoolConfig poolConfig,
			int timeout, final String password, final int database) {
		this(cacheName, masterName, sentinels, poolConfig, timeout, timeout,
				password, database);
	}

	public RedisSentinelPlugin(String cacheName, String masterName,
			Set<HostAndPort> sentinels, final GenericObjectPoolConfig poolConfig,
			int timeout, final String password, final int database,
			final String clientName) {
		this(cacheName, masterName, sentinels, poolConfig, timeout, timeout,
				password, database, clientName);
	}

	public RedisSentinelPlugin(String cacheName, String masterName,
			Set<HostAndPort> sentinels, final GenericObjectPoolConfig poolConfig,
			final int timeout, final int soTimeout, final String password,
			final int database) {
		this(cacheName, masterName, sentinels, poolConfig, timeout, soTimeout,
				password, database, null);
	}

	public RedisSentinelPlugin(String cacheName, String masterName,
			Set<HostAndPort> sentinels, final GenericObjectPoolConfig poolConfig,
			final int connectionTimeout, final int soTimeout,
			final String password, final int database, final String clientName) {
		if (StrKit.isBlank(cacheName))
			throw new IllegalArgumentException("cacheName can not be blank.");
		if (StrKit.isBlank(masterName))
			throw new IllegalArgumentException("masterName can not be blank.");
		if (null == sentinels || sentinels.isEmpty())
			throw new IllegalArgumentException("sentinels can not be blank.");
		if (null == poolConfig)
			throw new IllegalArgumentException("poolConfig can not be null.");
		for(HostAndPort hp : sentinels)
		{
			this.sentinels.add(hp.toString());
		}
		this.cacheName = cacheName.trim();
		this.masterName = masterName.trim();
		this.poolConfig = poolConfig;
		this.connectionTimeout = connectionTimeout;
		//this.soTimeout = soTimeout;
		this.password = password;
		this.database = database;
		//this.clientName = clientName;

	}

	public boolean start() {
		JedisSentinelPool jedisSentinelPool =new JedisSentinelPool(masterName, sentinels,
			      poolConfig, connectionTimeout, password, database);
		if (serializer == null)
			serializer = FstSerializer.me;
		if (keyNamingPolicy == null)
			keyNamingPolicy = IKeyNamingPolicy.defaultKeyNamingPolicy;

		Cache cache = new Cache(cacheName, jedisSentinelPool, serializer,
				keyNamingPolicy);
		Redis.addCache(cache);
		return true;
	}

	public boolean stop() {
		Cache cache = Redis.removeCache(cacheName);
		if (cache == Redis.mainCache)
			Redis.mainCache = null;
		cache.jedisPool.destroy();
		return true;
	}

	/**
	 * 当RedisSentinelPlugin 提供的设置属性仍然无法满足需求时，通过此方法获取到 JedisPoolConfig 对象，可对 redis
	 * 进行更加细致的配置
	 * 
	 * <pre>
	 * 例如：
	 * redisSentinelPlugin.getJedisPoolConfig().setMaxTotal(100);
	 * </pre>
	 */
	public GenericObjectPoolConfig getGenericObjectPoolConfig() {
		return poolConfig;
	}

	// ---------

	public void setSerializer(ISerializer serializer) {
		this.serializer = serializer;
	}

	public void setKeyNamingPolicy(IKeyNamingPolicy keyNamingPolicy) {
		this.keyNamingPolicy = keyNamingPolicy;
	}

	// ---------

	public void setTestWhileIdle(boolean testWhileIdle) {
		poolConfig.setTestWhileIdle(testWhileIdle);
	}

	public void setMinEvictableIdleTimeMillis(int minEvictableIdleTimeMillis) {
		poolConfig
				.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
	}

	public void setTimeBetweenEvictionRunsMillis(
			int timeBetweenEvictionRunsMillis) {
		poolConfig
				.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
	}

	public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
		poolConfig.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
	}

	//提供懒初始化初始数据库,避免太多构造器
	public void setDatabase(int database) {
		this.database = database;
	}
	
	
	

}
