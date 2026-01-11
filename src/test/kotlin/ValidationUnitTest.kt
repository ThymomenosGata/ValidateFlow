import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.wordy.validate.flow.ValidatorResult
import org.wordy.validate.flow.validateParallel
import org.wordy.validate.flow.validateReactive
import org.wordy.validate.flow.validateReactiveLatest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ValidationUnitTest {
    @Test
    fun `validation should be cancelled when new data arrives`() = runTest {
        val input = MutableSharedFlow<String>()

        input.validateReactiveLatest {
            validateField({ this }) {
                checkAsync("Error", "ERR_TEST") {
                    delay(1000)
                    it.length > 3
                }
            }
        }.test {
            input.emit("A")
            delay(500)
            input.emit("ABCD")

            val result = awaitItem()
            assertTrue(result.isValid)
            expectNoEvents()
        }

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `validation no cancelled when new data arrives`() = runTest {
        val input = MutableSharedFlow<String>(replay = 2)
        val results = mutableListOf<ValidatorResult>()

        val job = backgroundScope.launch {
            input.validateReactive {
                validateField({ this }) {
                    checkAsync("Error", "ERR_TEST") {
                        delay(1000)
                        it.length > 3
                    }
                }
            }.collect {
                results.add(it)
            }
        }

        input.emit("A")
        delay(2000)
        input.emit("ABCD")
        delay(2000)


        assertEquals(2, results.size)
        assertEquals(false, results[0].isValid)
        assertTrue(results[1].isValid)

        job.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `validateParallel should process items concurrently`() = runTest {
        val input = MutableSharedFlow<String>(replay = 3)
        val results = mutableListOf<String>()

        val job = backgroundScope.launch {
            input.validateParallel(concurrency = 3) {
                validateField({ this }) {
                    checkAsync("Error", "ERR_TEST") { value ->
                        val delayTime = if (value == "Slow") 2000L else 500L
                        delay(delayTime)
                        results.add(value)
                        true
                    }
                }
            }.collect {}
        }

        input.emit("Slow")
        input.emit("Fast1")
        input.emit("Fast2")
        advanceTimeBy(2500)

        assertEquals(3, results.size)
        assertEquals("Fast1", results[0])
        assertEquals("Fast2", results[1])
        assertEquals("Slow", results[2])
        assertEquals(2500, currentTime)

        job.cancel()

    }
}