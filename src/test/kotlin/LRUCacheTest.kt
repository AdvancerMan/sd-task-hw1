import org.junit.Assert
import org.junit.Test

class LRUCacheTest {

    @Test(expected = IllegalArgumentException::class)
    fun testNoCapacity() {
        LRUCacheImpl<String, String>(0, debugMode = true)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNegativeCapacity() {
        LRUCacheImpl<String, String>(-10, debugMode = true)
    }

    @Test
    fun testEmptyCache() {
        val cache = LRUCacheImpl<String, String>(123, debugMode = true)
        Assert.assertEquals(0, cache.size)
        Assert.assertEquals(null, cache["a"])
        Assert.assertEquals(123, cache.capacity)
    }

    @Test
    fun testSetGet() {
        val cache = LRUCacheImpl<String, String>(1, debugMode = true)

        cache["a"] = "b"
        Assert.assertEquals("b", cache["a"])
        Assert.assertEquals(1, cache.size)
    }

    @Test
    fun testNoSetGet() {
        val cache = LRUCacheImpl<String, String>(1, debugMode = true)

        Assert.assertEquals(null, cache["a"])
        Assert.assertEquals(0, cache.size)
    }

    @Test
    fun testReplace() {
        val cache = LRUCacheImpl<String, String>(1, debugMode = true)

        cache["a"] = "b"
        cache["a"] = "c"
        Assert.assertEquals("c", cache["a"])
        Assert.assertEquals(1, cache.size)
    }

    @Test
    fun testTooManySets() {
        val cache = LRUCacheImpl<String, String>(1, debugMode = true)

        cache["a"] = "b"
        cache["c"] = "d"
        Assert.assertEquals(null, cache["a"])
        Assert.assertEquals("d", cache["c"])
        Assert.assertEquals(1, cache.size)
    }

    @Test
    fun testGetPrioritizesElement() {
        val cache = LRUCacheImpl<String, String>(2, debugMode = true)

        cache["a"] = "b"
        cache["c"] = "d"
        Assert.assertEquals(2, cache.size)

        Assert.assertNotNull(cache["a"])
        cache["e"] = cache["a"]!!

        Assert.assertEquals("b", cache["a"])
        Assert.assertEquals(null, cache["c"])
        Assert.assertEquals(cache["a"], cache["e"])
        Assert.assertEquals(2, cache.size)
    }

    @Test
    fun testPrioritizeElementInMiddle() {
        val cache = LRUCacheImpl<String, String>(3, debugMode = true)

        cache["a"] = "b"
        cache["c"] = "d"
        cache["e"] = "f"
        Assert.assertEquals(3, cache.size)

        Assert.assertNotNull(cache["c"])
        cache["g"] = cache["c"]!!
        cache["h"] = "i"

        Assert.assertEquals(null, cache["a"])
        Assert.assertEquals("d", cache["c"])
        Assert.assertEquals(null, cache["e"])
        Assert.assertEquals(cache["c"], cache["g"])
        Assert.assertEquals("i", cache["h"])
        Assert.assertEquals(3, cache.size)
    }
}
