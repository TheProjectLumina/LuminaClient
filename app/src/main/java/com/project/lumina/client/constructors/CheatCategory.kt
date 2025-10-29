package com.project.lumina.client.constructors

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.project.lumina.client.R

enum class CheatCategory(
    @DrawableRes val iconResId: Int,
    @StringRes val labelResId: Int
) {
    Home(
        iconResId = ir.alirezaivaz.tablericons.R.drawable.ic_home,
        labelResId = R.string.home
    ),
    Combat(
        iconResId = R.drawable.ic_sword_cross_black_24dp,
        labelResId = R.string.combat
    ),
    Motion(
        iconResId = R.drawable.ic_run_black_24dp,
        labelResId = R.string.motion
    ),
    World(
        iconResId = R.drawable.ic_cube_outline_black_24dp,
                labelResId = R.string.world
    ),
    Visual(
        iconResId = R.drawable.ic_camera_outline_black_24dp,
        labelResId = R.string.visual
    ),
    Misc(
        iconResId = R.drawable.ic_key_variant_black_24dp,
        labelResId = R.string.misc
    ),
    Chat(
        iconResId = R.drawable.messages_24,
        labelResId = R.string.chat
    ),
    Config(
        iconResId = R.drawable.ic_cloud_upload_black_24dp,
        labelResId = R.string.config
    )
}