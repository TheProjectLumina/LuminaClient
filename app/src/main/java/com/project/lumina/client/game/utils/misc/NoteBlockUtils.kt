package com.project.lumina.client.game.utils.misc

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import java.io.InputStream
import kotlin.math.floor


object NoteBlockUtils {

    class Song {
        val ticks = mutableMapOf<Int, MutableList<Note>>()
        var maxTicks = 0
            private set

        fun computeMaxTicks() {
            var maxTicks = 0
            ticks.keys.forEach {
                if (it > maxTicks) {
                    maxTicks = it
                }
            }
            this.maxTicks = maxTicks
        }

        fun readNbs(inputIn: InputStream) {
			val buf = Unpooled.wrappedBuffer(inputIn.readBytes())
            val length = buf.readShortLE()

            var nbsversion = 0
            if (length.toInt() == 0) {
                nbsversion = buf.readUnsignedByte().toInt()

					buf.readUnsignedByte().toInt()
                if (nbsversion >= 3) {

						buf.readShortLE()
                }
            }
            buf.readShortLE()
			buf.readString()
			buf.readString()
			buf.readString()
			buf.readString()
            val speed = buf.readShortLE() / 100f
            buf.readBoolean()
            buf.readByte()
            buf.readByte()
            buf.readIntLE()
            buf.readIntLE()
            buf.readIntLE()
            buf.readIntLE()
            buf.readIntLE()
            buf.readString()
            if (nbsversion >= 4) {
                buf.readByte()
                buf.readByte()
                buf.readShortLE()
            }
            var tick: Short = -1
            while (true) {
                val jumpTicks = buf.readShortLE()
                if (jumpTicks.toInt() == 0) {
                    break
                }
                tick = (tick + jumpTicks).toShort()
                while (true) {
                    val jumpLayers = buf.readShortLE()
                    if (jumpLayers.toInt() == 0) {
                        break
                    }
                    val instrument = buf.readUnsignedByte()
                    val key = (buf.readUnsignedByte() - 33).toByte()
                    if (nbsversion >= 4) {
                        buf.readByte()
                        buf.readByte()
                        buf.readShortLE()
                    }
                    val realTick = floor(tick * 20f / speed).toInt()
                    (ticks[realTick] ?: mutableListOf<Note>().also { ticks[realTick] = it })
                        .add(Note(Instrument.values()[instrument.toInt()], key))
                }
            }
        }
    }

    data class Note(val inst: Instrument, val key: Byte)

    enum class Instrument {
        PIANO,
        BASS_DRUM,
        DRUM,
        STICKS,
        BASS,
        GLOCKENSPIEL,
        FLUTE,
        CHIME,
        GUITAR,
        XYLOPHONE,
        VIBRAPHONE,
        COW_BELL,
        DIDGERIDOO,
        SQUARE_WAVE,
        BANJO,
        ELECTRIC_PIANO;
    }

    private fun ByteBuf.readString(): String {
        var length = readIntLE()
        val builder = StringBuilder(length)
        while (length > 0) {
            var c = Char(readByte().toUShort())
            if (c == 0x0D.toChar()) {
                c = ' '
            }
            builder.append(c)
            --length
        }
        return builder.toString()
    }
}
