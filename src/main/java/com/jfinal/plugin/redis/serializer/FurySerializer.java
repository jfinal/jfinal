package com.jfinal.plugin.redis.serializer;

import io.fury.Fury;
import io.fury.ThreadSafeFury;
import io.fury.config.Language;
import redis.clients.jedis.util.SafeEncoder;

/**
 * FurySerializer
 */
public class FurySerializer implements ISerializer {

    private static ThreadSafeFury fury;

    static {
        fury = Fury.builder()
                .withLanguage(Language.JAVA)
                .withRefTracking(true)
                .requireClassRegistration(false)
                .withNumberCompressed(false)
                // .withAsyncCompilation(true)
                .buildThreadSafeFury();
                // .withCompatibleMode(CompatibleMode.SCHEMA_CONSISTENT)
                // .buildThreadSafeFuryPool(8, 32, 5, TimeUnit.MINUTES);
                // .buildThreadLocalFury();
    }

    @Override
    public byte[] keyToBytes(String key) {
        return SafeEncoder.encode(key);
    }

    @Override
    public String keyFromBytes(byte[] bytes) {
        return SafeEncoder.encode(bytes);
    }

    @Override
    public byte[] fieldToBytes(Object field) {
        return SafeEncoder.encode(field.toString());
    }

    @Override
    public Object fieldFromBytes(byte[] bytes) {
        return SafeEncoder.encode(bytes);
    }

    @Override
    public byte[] valueToBytes(Object value) {
        return value != null ? fury.serialize(value) : null;
    }

    @Override
    public Object valueFromBytes(byte[] bytes) {
        return bytes != null && bytes.length != 0 ? fury.deserialize(bytes) : null;
    }
}

