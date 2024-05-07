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
                .withAsyncCompilation(true)
                .buildThreadSafeFury();
                //.withCompatibleMode(CompatibleMode.SCHEMA_CONSISTENT)
                //.buildThreadSafeFuryPool(8, 32, 5, TimeUnit.MINUTES);
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
        if(value == null){return null;}
        return fury.serialize(value);
    }

    @Override
    public Object valueFromBytes(byte[] bytes) {
        if(bytes == null || bytes.length == 0){return null;}
        return fury.deserialize(bytes);
    }
}
