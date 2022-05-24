package com.github.nuguya21.fortress.collection

class Chain<V1, V2> {
    private val m1: MutableMap<V1, V2> = mutableMapOf()
    private val m2: MutableMap<V2, V1> = mutableMapOf()

    fun plus(v1: V1, v2: V2) {
        if (v1 == v2) return
        m1[v1] = v2
        m2[v2] = v1
    }

    @JvmName("get1")
    operator fun get(v1: V1): V2? {
        return m1[v1]
    }

    operator fun get(v2: V2): V1? {
        return m2[v2]
    }

    @JvmName("remove1")
    fun remove(v1: V1) {
        m2.remove(m1[v1])
        m1.remove(v1)
    }

    fun remove(v2: V2) {
        m1.remove(m2[v2])
        m2.remove(v2)
    }

    fun values1(): Set<V1> {
        return m1.keys
    }

    fun values2(): Set<V2> {
        return m2.keys
    }

    @JvmName("contains1")
    fun contains(v1: V1): Boolean {
        return m1.contains(v1)
    }

    fun contains(v2: V2): Boolean {
        return m2.contains(v2)
    }
}