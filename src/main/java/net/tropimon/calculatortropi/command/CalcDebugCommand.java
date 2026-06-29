package net.tropimon.calculatortropi.command;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.client.battle.ClientBattleActor;
import com.cobblemon.mod.common.client.battle.ClientBattleSide;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.UUID;

public class CalcDebugCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
            ClientCommandManager.literal("calcdebug")
                .executes(CalcDebugCommand::executer)
        );
    }

    private static int executer(CommandContext<FabricClientCommandSource> contexte) {
        ClientBattle combat = CobblemonClient.INSTANCE.getBattle();

        if (combat == null) {
            envoyer(contexte, "combat == null (aucun combat détecté par le mod)");
            return 1;
        }

        UUID monUuid = MinecraftClient.getInstance().player.getUuid();
        envoyer(contexte, "Mon UUID: " + monUuid);
        envoyer(contexte, "isPvW(): " + combat.isPvW());
        envoyer(contexte, "Nombre de camps (sides): " + combat.getSides().length);

        int numeroCote = 0;
        for (ClientBattleSide cote : combat.getSides()) {
            numeroCote++;
            envoyer(contexte, "--- Camp " + numeroCote + " : " + cote.getActors().size() + " acteur(s) ---");

            for (ClientBattleActor acteur : cote.getActors()) {
                boolean correspond = acteur.getUuid().equals(monUuid);
                envoyer(contexte, String.format(
                        "  Acteur: uuid=%s | type=%s | nom=%s | c'est moi: %s | pokemon actifs: %d",
                        acteur.getUuid(), acteur.getType(), acteur.getDisplayName().getString(),
                        correspond, acteur.getActivePokemon().toString().split(",").length
                ));

                for (ActiveClientBattlePokemon actif : acteur.getActivePokemon()) {
                    if (actif.getBattlePokemon() != null) {
                        envoyer(contexte, "    -> Pokemon actif: " + actif.getBattlePokemon().getSpecies().getName());
                    }
                }
            }
        }

        return 1;
    }

    private static void envoyer(CommandContext<FabricClientCommandSource> contexte, String texte) {
        contexte.getSource().sendFeedback(Text.literal(texte));
    }
}
