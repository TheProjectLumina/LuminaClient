package com.project.lumina.client.game.utils.constants

import org.cloudburstmc.math.vector.Vector3i

enum class EnumFacing(val opposite: Int, val horizontalIndex: Int, val humanized: String, val unitVector: Vector3i) {
    DOWN(1, -1, "down", Vector3i.from(0, -1, 0)),
    UP( 0, -1, "up", Vector3i.from(0, 1, 0)),
    NORTH(3, 2, "north", Vector3i.from(0, 0, -1)),
    SOUTH(2, 0, "south", Vector3i.from(0, 0, 1)),
    WEST(5, 1, "west", Vector3i.from(-1, 0, 0)),
    EAST(4, 3, "east", Vector3i.from(1, 0, 0))
}