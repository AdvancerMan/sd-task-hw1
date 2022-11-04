interface LRUCache<K, V> {
    val capacity: Int

    val size: Int

    operator fun get(key: K): V?

    operator fun set(key: K, value: V)
}
