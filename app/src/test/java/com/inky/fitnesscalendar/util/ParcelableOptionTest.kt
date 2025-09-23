package com.inky.fitnesscalendar.util

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.junit.Assert.assertEquals
import org.junit.Test

class ParcelableOptionTest {
    @Parcelize
    data class Foo(val value: Int) : Parcelable

    @Test
    fun smokeTest() {
        val a = Foo(42)
        val b = Option.Some(a)
        val c: Option<Foo> = Option.None

        assertEquals(b.unwrapOrNull(), Foo(value = 42))
        assertEquals(c.unwrapOrNull(), null)

        assertEquals(b.or { Foo(0) }.value, 42)
        assertEquals(c.or { Foo(0) }.value, 0)
    }
}