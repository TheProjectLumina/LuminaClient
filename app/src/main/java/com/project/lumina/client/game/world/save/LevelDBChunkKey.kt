package com.project.lumina.client.game.world.save

object LevelDBChunkKey {

    enum class Key(val id: Int) {
        VERSION(0x2c),
        OLD_VERSION(0x76),
        DATA_2D(0x2d),
        DATA_3D(0x2b),
        SUB_CHUNK_DATA(0x2f),
        BLOCK_ENTITIES(0x31),
        ENTITIES(0x32),
        FINALIZATION(0x36);

        fun getKey(x: Int, z: Int, dimension: Int = 0, extra: Int? = null): ByteArray {
            return if (dimension == 0 && extra == null) {
                byteArrayOf(
                    x.toByte(), (x shr 8).toByte(), (x shr 16).toByte(), (x shr 24).toByte(),
                    z.toByte(), (z shr 8).toByte(), (z shr 16).toByte(), (z shr 24).toByte(),
                    id.toByte()
                )
            } else if (extra != null) {
                byteArrayOf(
                    x.toByte(), (x shr 8).toByte(), (x shr 16).toByte(), (x shr 24).toByte(),
                    z.toByte(), (z shr 8).toByte(), (z shr 16).toByte(), (z shr 24).toByte(),
                    dimension.toByte(), (dimension shr 8).toByte(), (dimension shr 16).toByte(), (dimension shr 24).toByte(),
                    id.toByte(), extra.toByte()
                )
            } else {
                byteArrayOf(
                    x.toByte(), (x shr 8).toByte(), (x shr 16).toByte(), (x shr 24).toByte(),
                    z.toByte(), (z shr 8).toByte(), (z shr 16).toByte(), (z shr 24).toByte(),
                    dimension.toByte(), (dimension shr 8).toByte(), (dimension shr 16).toByte(), (dimension shr 24).toByte(),
                    id.toByte()
                )
            }
        }
    }
}
