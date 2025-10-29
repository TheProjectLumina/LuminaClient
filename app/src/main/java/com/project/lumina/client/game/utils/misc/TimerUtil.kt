package com.project.lumina.client.game.utils.misc

class TimerUtil {
    private var lastMS: Long = 0

    var time: Long = 0
        get() = System.nanoTime() / 1000000L

    private var ms = this.currentMS

    init {
        this.time = -1L
    }

    val elapsedTime: Long
        get() = this.currentMS - this.ms

    fun elapsed(milliseconds: Long): Boolean {
        return this.currentMS - this.ms > milliseconds
    }

    fun resetStopWatch() {
        this.ms = this.currentMS
    }

    private val currentMS: Long
        get() = System.nanoTime() / 1000000L

    fun hit(milliseconds: Long): Boolean {
        return (currentMS - lastMS) >= milliseconds
    }

    fun hasReached(milliseconds: Double): Boolean {
        if ((this.currentMS - this.lastMS).toDouble() >= milliseconds) {
            return true
        }
        return false
    }

    fun reset() {
        this.lastMS = this.currentMS
        this.time = System.currentTimeMillis()
    }

    fun delay(delay: Float): Boolean {
        return (time - this.lastMS).toFloat() >= delay
    }

    fun isDelayComplete(delay: Long): Boolean {
        if (System.currentTimeMillis() - this.lastMS > delay) {
            return true
        }
        return false
    }

    fun hasTimePassed(MS: Long): Boolean {
        return System.currentTimeMillis() >= this.time + MS
    }

    fun hasTimeLeft(MS: Long): Long {
        return MS + this.time - System.currentTimeMillis()
    }
}

