package net.tropimon.calculatortropi.command;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.client.battle.ClientBattleActor;
import com.cobblemon.mod.common.client.battle.ClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattleSide;
import com.cobblemon.mod.common.pokemon.Species;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.tropimon.calculatortropi.calc.TypeChart;
import net.tropimon.calculatortropi.cobblemon.TypeMapper;

import java.util.UUID;

public class CalcOpponentCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
            ClientCommandManager.literal("calcopponent")
                .executes(CalcOpponentCommand::executer)
        );
    }

    private static int executer(CommandContext<FabricClientCommandSource> contexte) {
        ClientBattle combat = CobblemonClient.INSTANCE.getBattle();

        if (combat == null) {
            contexte.getSource().sendFeedback(Text.literal("Tu n'es pas en combat."));
            return 1;
        }

        UUID monUuid = MinecraftClient.getInstance().player.getUuid();
        boolean trouve = false;

        for (ClientBattleSide cote : combat.getSides()) {
            for (ClientBattleActor acteur : cote.getActors()) {
                if (acteur.getUuid().equals(monUuid)) {
                    continue; // c'est nous, on saute
                }

                for (ActiveClientBattlePokemon actif : acteur.getActivePokemon()) {
                    ClientBattlePokemon pokemonAdverse = actif.getBattlePokemon();
                    if (pokemonAdverse == null) continue;

                    trouve = true;
                    Species espece = pokemonAdverse.getSpecies();
                    int niveau = pokemonAdverse.getLevel();
                    float pvMax = pokemonAdverse.getMaxHp();
                    boolean pvExacts = pokemonAdverse.isHpFlat();

                    String texteVie;
                    if (pvExacts) {
                        texteVie = String.format("%.0f/%.0f PV", pokemonAdverse.getHpValue(), pvMax);
                    } else {
                        double pourcentage = pokemonAdverse.getHpValue() * 100.0;
                        double pvEstimes = pokemonAdverse.getHpValue() * pvMax;
                        texteVie = String.format("~%.0f/%.0f PV (estimé, %.0f%% affiché)", pvEstimes, pvMax, pourcentage);
                    }

                    TypeChart.Type type1 = TypeMapper.depuisCobblemon(espece.getPrimaryType());
                    TypeChart.Type type2 = espece.getSecondaryType() != null
                            ? TypeMapper.depuisCobblemon(espece.getSecondaryType())
                            : null;
                    String typesTexte = (type2 != null) ? (type1 + "/" + type2) : type1.toString();

                    contexte.getSource().sendFeedback(Text.literal(String.format(
                            "Adversaire détecté : %s (Nv.%d) [%s] %s",
                            espece.getName(), niveau, typesTexte, texteVie
                    )));
                }
            }
        }

        if (!trouve) {
            contexte.getSource().sendFeedback(Text.literal("Aucun Pokémon adverse actif détecté."));
        }

        return 1;
    }
}
