class LRUCacheImpl<K, V>(
    override val capacity: Int,
    private val debugMode: Boolean = false,
) : LRUCache<K, V> {

    private val elementsMap: MutableMap<K, Node<K, V>> = HashMap(capacity)

    private var linkedListHead: Node<K, V>? = null
    private var linkedListTail: Node<K, V>? = null

    init {
        if (capacity <= 0) {
            throw IllegalArgumentException("Capacity should be positive, got $capacity")
        }
    }

    private val areAssertionsEnabled: Boolean
        get() = this::class.java.desiredAssertionStatus()

    private fun checkClassInvariant() {
        if (areAssertionsEnabled) {
            assert(size <= capacity)
            if (size > 0) {
                assert(linkedListHead != null)
                assert(linkedListTail != null)
            }

            if (!debugMode) {
                return
            }

            val linkedList = collectLinkedList()
            assert(linkedList.lastOrNull() == linkedListTail)
            assert(linkedList.size == size)

            linkedList
                .zipWithNext()
                .forEach { (first, second) -> assert(second.previous == first) }

            assert(linkedList.sortedBy { it.hashCode() } == elementsMap.values.sortedBy { it.hashCode() })
        }
    }

    private fun collectLinkedList(): List<Node<K, V>> {
        return generateSequence(linkedListHead) { it.next }
            .toList()
    }

    override val size: Int
        get() = elementsMap.size

    override operator fun get(key: K): V? {
        checkClassInvariant()

        return elementsMap[key]
            ?.also { prioritizeNode(it) }
            ?.value
            ?.also { assert(linkedListHead == it) }
            .also { checkClassInvariant() }
    }

    override operator fun set(key: K, value: V) {
        checkClassInvariant()

        val newNode = Node(null, null, key, value)
        elementsMap.put(key, newNode)
            ?.let { removeFromLinkedList(it) }
            ?: ensureSizeFitsCapacity()
        prioritizeNode(newNode)

        assert(linkedListHead == value)
        checkClassInvariant()
    }

    private fun removeFromLinkedList(node: Node<K, V>) {
        if (linkedListHead == node) {
            linkedListHead = node.next
        }
        if (linkedListTail == node) {
            linkedListTail = node.previous
        }

        node.previous?.next = node.next
        node.next?.previous = node.previous

        node.previous = null
        node.next = null

        if (debugMode) {
            assert(node.key !in collectLinkedList().map { it.key }.toSet())
        }
    }

    private fun prioritizeNode(node: Node<K, V>) {
        removeFromLinkedList(node)
        node.next = linkedListHead
        linkedListHead?.previous = node
        linkedListHead = node

        if (linkedListTail == null) {
            linkedListTail = node
        }

        assert(linkedListHead == node)
    }

    private fun ensureSizeFitsCapacity() {
        while (elementsMap.size > capacity) {
            assert(linkedListTail != null)

            val nodeToRemove = linkedListTail!!
            val removedNode = elementsMap.remove(nodeToRemove.key)
            assert(removedNode != null)

            removeFromLinkedList(nodeToRemove)
        }

        assert(size <= capacity)
    }

    private class Node<K, V>(
        var previous: Node<K, V>?,
        var next: Node<K, V>?,
        val key: K,
        val value: V,
    )
}
