package kr.toxicity.hud.hud

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.image.LoadedImage
import kr.toxicity.hud.layout.BackgroundLayout
import kr.toxicity.hud.layout.TextLayout
import kr.toxicity.hud.manager.ConfigManagerImpl
import kr.toxicity.hud.manager.MinecraftManager
import kr.toxicity.hud.manager.TextManager
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.renderer.TextRenderer
import kr.toxicity.hud.shader.GuiLocation
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.shader.ShaderGroup
import kr.toxicity.hud.text.HudTextData
import kr.toxicity.hud.util.*
import net.kyori.adventure.text.Component
import kotlin.math.roundToInt

class HudTextElement(
    parent: HudImpl,
    file: List<String>,
    private val text: TextLayout,
    gui: GuiLocation,
    pixel: ImageLocation
) {

    private val renderer = run {
        val loc = text.location + pixel
        val shader = HudShader(
            gui,
            text.renderScale,
            text.layer,
            text.outline,
            loc.opacity
        )
        val yAxis = loc.y.coerceAtLeast(-HudImpl.ADD_HEIGHT).coerceAtMost(HudImpl.ADD_HEIGHT)
        val group = ShaderGroup(shader, text.text.name, text.scale, yAxis)
        val key = TextManager.getKey(group) ?: run {
            val index2 = ++parent.textIndex
            val array = text.startJson()
            text.text.array.forEach {
                HudImpl.createBit(shader, yAxis) { y ->
                    array.add(jsonObjectOf(
                        "type" to "bitmap",
                        "file" to "$NAME_SPACE_ENCODED:${it.file}",
                        "ascent" to y,
                        "height" to (it.height * text.scale).roundToInt(),
                        "chars" to it.chars
                    ))
                }
            }
            var textIndex = 0xC0000
            val textEncoded = "hud_${parent.name}_text_${index2 + 1}".encodeKey()
            val key = createAdventureKey(textEncoded)
            val imageMap = HashMap<String, WidthComponent>()
            text.text.images.forEach {
                val result = textIndex++.parseChar()
                val imageScale = it.value.scale * text.scale
                val height = (it.value.image.image.height.toDouble() * imageScale).roundToInt()
                val div = height.toDouble() / it.value.image.image.height
                HudImpl.createBit(shader, loc.y + it.value.location.y) { y ->
                    array.add(jsonObjectOf(
                        "type" to "bitmap",
                        "file" to "$NAME_SPACE_ENCODED:${"glyph_${it.key}".encodeKey()}.png",
                        "ascent" to y,
                        "height" to height,
                        "chars" to jsonArrayOf(result)
                    ))
                }
                imageMap[it.key] = it.value.location.x.toSpaceComponent() + WidthComponent(Component.text()
                    .font(key)
                    .content(result)
                    .append(NEGATIVE_ONE_SPACE_COMPONENT.component), (it.value.image.image.width.toDouble() * div).roundToInt())
            }
            if (ConfigManagerImpl.loadMinecraftDefaultTextures) {
                HudImpl.createBit(shader, text.emojiLocation.y) { y ->
                    MinecraftManager.applyAll(array, y, text.emojiScale, key) {
                        textIndex++
                    }.forEach {
                        imageMap[it.key] = text.emojiLocation.x.toSpaceComponent() + it.value
                    }
                }
            }
            val result = HudTextData(
                key,
                imageMap,
                text.background?.let {
                    val backgroundLoc = loc + it.location
                    fun getString(image: LoadedImage, file: String): WidthComponent {
                        val result = textIndex++.parseChar()
                        val height = (image.image.height.toDouble() * text.backgroundScale).roundToInt()
                        val div = height.toDouble() / image.image.height
                        HudImpl.createBit(HudShader(
                            gui,
                            text.renderScale,
                            text.layer - 1,
                            false,
                            backgroundLoc.opacity
                        ), backgroundLoc.y) { y ->
                            array.add(jsonObjectOf(
                                "type" to "bitmap",
                                "file" to "$NAME_SPACE_ENCODED:$file.png",
                                "ascent" to y,
                                "height" to height,
                                "chars" to jsonArrayOf(result)
                            ))
                        }
                        return WidthComponent(Component.text()
                            .font(key)
                            .content(result)
                            .append(NEGATIVE_ONE_SPACE_COMPONENT.component), (image.image.width.toDouble() * div).roundToInt())
                    }
                    BackgroundLayout(
                        backgroundLoc.x,
                        getString(it.left, "background_${it.name}_left".encodeKey()),
                        getString(it.right, "background_${it.name}_right".encodeKey()),
                        getString(it.body, "background_${it.name}_body".encodeKey())
                    )
                }
            )
            PackGenerator.addTask(file + "$textEncoded.json") {
                jsonObjectOf("providers" to array).toByteArray()
            }
            TextManager.setKey(group, result)
            result
        }
        TextRenderer(
            text.text.charWidth,
            text.color,
            key,
            text.pattern,
            text.align,
            text.scale,
            loc.x,
            text.numberEquation,
            text.numberFormat,
            text.disableNumberFormat,
            text.follow,
            text.cancelIfFollowerNotExists,
            text.useLegacyFormat,
            text.legacySerializer,
            text.space,
            text.conditions.and(text.text.conditions)
        )
    }.getText(UpdateEvent.EMPTY)

    fun getText(hudPlayer: HudPlayer): PixelComponent = renderer(hudPlayer)
}