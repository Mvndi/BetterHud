package kr.toxicity.hud.api.configuration;

import kr.toxicity.hud.api.compass.Compass;
import kr.toxicity.hud.api.hud.Hud;
import kr.toxicity.hud.api.player.HudPlayer;
import kr.toxicity.hud.api.popup.Popup;
import kr.toxicity.hud.api.update.UpdateEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public record HudObjectType<T extends HudObject>(@NotNull Class<T> clazz, @NotNull String name, @NotNull BiFunction<T, HudPlayer, HudComponentSupplier<T>> function) {
    public static final HudObjectType<Hud> HUD = new HudObjectType<>(Hud.class, "hud", Hud::getComponents);
    public static final HudObjectType<Popup> POPUP = new HudObjectType<>(Popup.class, "popup", (popup, hudPlayer) -> {
        popup.show(UpdateEvent.EMPTY, hudPlayer);
        return HudComponentSupplier.empty(popup);
    });
    public static final HudObjectType<Compass> COMPASS = new HudObjectType<>(
            Compass.class,
            "compass",
            Compass::indicate
    );

    public @NotNull HudComponentSupplier<?> invoke(@NotNull HudObject object, @NotNull HudPlayer hudPlayer) {
        try {
            return function.apply(clazz.cast(object), hudPlayer);
        } catch (Throwable e) {
            return HudComponentSupplier.empty(object);
        }
    }
}
