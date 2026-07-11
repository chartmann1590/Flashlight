package com.charles.flashlight.torch

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class TorchTileService : TileService() {
    override fun onStartListening() {
        super.onStartListening()
        TorchController.initialize(this)
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        TorchController.toggleSteady(this)
        updateTile()
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        if (!TorchController.hasTorch(this)) {
            tile.state = Tile.STATE_UNAVAILABLE
        } else {
            tile.state = if (TorchController.isActive.value) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        }
        tile.updateTile()
    }
}
