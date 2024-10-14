package kr.toxicity.hud.equation

import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.layout.LayoutAnimationType
import kotlin.math.roundToInt

class AnimationLocation(
    val type: LayoutAnimationType,
    val location: List<ImageLocation>
) {
    companion object {
        val zero = AnimationLocation(LayoutAnimationType.LOOP, listOf(ImageLocation.zero))
    }
    constructor(
        type: LayoutAnimationType,
        duration: Int,
        imageEquation: EquationTriple
    ): this(
        type,
        (0..<duration).map {
            val d = it.toDouble()
            ImageLocation(
                imageEquation.x.evaluate(d).roundToInt(),
                imageEquation.y.evaluate(d).roundToInt(),
                imageEquation.opacity.evaluate(d)
            )
        }
    )

    constructor(section: YamlObject): this(
        section.get("type")?.asString()?.let {
            LayoutAnimationType.valueOf(it.uppercase())
        } ?: LayoutAnimationType.LOOP,
        section.getAsInt("duration", 20).coerceAtLeast(1),
        EquationTriple(section)
    )
}