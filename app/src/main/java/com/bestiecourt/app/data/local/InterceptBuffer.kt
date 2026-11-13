package com.bestiecourt.app.data.local

import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

data class InterceptEntry(
    val packageName: String,
    val title: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Singleton
class InterceptBuffer @Inject constructor() {
    private val queue = ConcurrentLinkedQueue<InterceptEntry>()

    fun add(pkg: String, title: String, text: String) {
        queue.offer(InterceptEntry(pkg, title, text))
    }

    fun drain(): List<InterceptEntry> {
        val list = mutableListOf<InterceptEntry>()
        while (true) {
            val e = queue.poll() ?: break
            list.add(e)
        }
        return list
    }
}
