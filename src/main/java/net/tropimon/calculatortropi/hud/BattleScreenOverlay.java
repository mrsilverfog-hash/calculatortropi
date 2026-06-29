package net.tropimon.calculatortropi.hud;

import com.cobblemon.mod.common.client.gui.battle.BattleGUI;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;

/**
 * Dessine notre panneau ET l'infobulle de switch APRES que l'écran de
 * combat Cobblemon (BattleGUI) ait fini de se dessiner lui-même - donc
 * par-dessus ses propres éléments (cases d'équipe, boutons...), au lieu
 * d'être recouverts par eux comme avec le HUD classique.
 */
public class BattleScreenOverlay {

    public static void enregistrer() {
        ScreenEvents.AFTER_INIT.register((client, screen, largeur, hauteur) -> {
            if (screen instanceof BattleGUI battleGUI) {
                ScreenEvents.afterRender(screen).register((s, contexte, sourisX, sourisY, delta) -> {
                    MinecraftClient mc = MinecraftClient.getInstance();
                    CalcHud.dessiner(contexte, mc);
                    SwitchTooltip.dessiner(contexte, mc, battleGUI);
                });
            }
        });
    }
}
