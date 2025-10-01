package com.project.lumina.client.game.event

import com.project.lumina.client.constructors.NetBound
import com.project.lumina.client.game.inventory.AbstractInventory

class EventInventorySlotUpdate(
    session: NetBound,
    val inventory: AbstractInventory,
    val slot: Int
) : GameEvent(session, "InventorySlotUpdate")
