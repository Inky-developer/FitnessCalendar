package com.inky.fitnesscalendar.util.result

import org.junit.Assert.*
import org.junit.Test

class TypedResultTest {
    @Test
    fun testTryScope() {
        val success = tryScope<Int, String> { 1 }
        assertEquals(1.asOk(), success)

        val error = tryScope { raise("An error has occurred") }
        assertEquals("An error has occurred".asErr(), error)
    }

    @Test
    fun testComposition() {
        fun Raises<String>.bar(): Int {
            return 1
        }

        val result = tryScope {
            bar() + 10
        }

        assertEquals(11, result.ok())
    }

    @Test
    fun testScopeBoundary() {
        fun foo(): TypedResult<Int, String> = tryScope {
            5
        }

        fun bar(): TypedResult<Int, String> = tryScope {
            raise("Exception")
        }

        val result = tryScope {
            foo().unwrap() + 1
        }
        assertEquals(6, result.ok())

        val result2 = tryScope {
            bar().unwrap() + 1
        }
        assertEquals("Exception", result2.err())
    }

    @Test
    fun errorHierarchy() {
        abstract class AppError

        class IoError : AppError()
        class ValueError : AppError()

        fun Raises<IoError>.doIo(fail: Boolean): String = if (fail) raise(IoError()) else {
            "Success"
        }

        fun Raises<ValueError>.doSomethingWithInput(input: String): String = input.reversed()

        val result = tryScope {
            doSomethingWithInput(doIo(true))
        }
        assertTrue(result.err() is IoError)

        val result2 = tryScope {
            doSomethingWithInput(doIo(false))
        }
        assertEquals("sseccuS", result2.ok())
    }
}