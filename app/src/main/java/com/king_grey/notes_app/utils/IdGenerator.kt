package com.king_grey.notes_app.utils

import java.util.concurrent.atomic.AtomicLong

/** Note ids are time based so they never clash and need no database round trip before the editor opens. */
object IdGenerator {
    private val last = AtomicLong(0L)

    fun newId(): Long {
        val now = System.currentTimeMillis()
        return last.updateAndGet { prev -> if (now > prev) now else prev + 1 }
    }
}
