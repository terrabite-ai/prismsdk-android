package ai.terrabite.prism

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** One engine callback fans out to the listener and the flow. */
@OptIn(ExperimentalCoroutinesApi::class)
class PrismDispatcherTest {

    private val location = PrismLocation.from(LocationFixtures.full)

    @After
    fun clear() {
        PrismDispatcher.locationListener = null
        PrismDispatcher.errorListener = null
    }

    @Test
    fun `listener receives the location`() {
        var received: PrismLocation? = null
        PrismDispatcher.locationListener = PrismLocationListener { received = it }

        PrismDispatcher.deliver(location)

        assertEquals(location, received)
    }

    @Test
    fun `a second listener replaces the first, and null clears`() {
        var first = 0
        var second = 0
        PrismDispatcher.locationListener = PrismLocationListener { first++ }
        PrismDispatcher.locationListener = PrismLocationListener { second++ }
        PrismDispatcher.deliver(location)
        PrismDispatcher.locationListener = null
        PrismDispatcher.deliver(location)

        assertEquals(0, first)
        assertEquals(1, second)
    }

    @Test
    fun `flow receives the location`() = runTest(UnconfinedTestDispatcher()) {
        var received: PrismLocation? = null
        val job = launch { received = PrismDispatcher.locations.first() }

        PrismDispatcher.deliver(location)
        job.join()

        assertEquals(location, received)
    }

    @Test
    fun `errors reach the error listener and the error flow`() = runTest(UnconfinedTestDispatcher()) {
        var listened: String? = null
        var flowed: String? = null
        PrismDispatcher.errorListener = PrismErrorListener { listened = it }
        val job = launch { flowed = PrismDispatcher.errors.first() }

        PrismDispatcher.deliverError("Location permission not granted")
        job.join()

        assertEquals("Location permission not granted", listened)
        assertEquals("Location permission not granted", flowed)
    }

    @Test
    fun `delivering with no listener is harmless`() {
        assertNull(PrismDispatcher.locationListener)
        PrismDispatcher.deliver(location)
    }
}
