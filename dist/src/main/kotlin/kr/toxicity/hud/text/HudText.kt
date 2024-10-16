package kr.toxicity.hud.text

import kr.toxicity.hud.configuration.HudConfiguration
import kr.toxicity.hud.image.LocatedImage
import kr.toxicity.hud.placeholder.ConditionBuilder

class HudText(
    override val path: String,
    val name: String,
    val array: List<HudTextArray>,
    val images: Map<String, LocatedImage>,
    val charWidth: Map<Int, CharWidth>,
    val conditions: ConditionBuilder
) : HudConfiguration