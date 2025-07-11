package com.xaur.util;

import java.util.HashMap;
import java.util.Map;

public class CustomMap<K, V> {


    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2) {

        Map m = new HashMap();
        m.put(k1,v1);
        m.put(k2,v2);
        return m;

    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        Map m = new HashMap();
        m.put(k1,v1);
        m.put(k2,v2);
        m.put(k3,v3);
        return m;

    }
}
