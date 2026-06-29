package net.tropimon.calculatortropi.command;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.storage.ClientStorageManager;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.tropimon.calculatortropi.calc.TypeChart;
import net.tropimon.calculatortropi.cobblemon.TypeMapper;

public class CalcTeamCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
            ClientCommandManager.literal("calcteam")
                .executes(CalcTeamCommand::executer)
        );
    }

    private static int executer(CommandContext<FabricClientCommandSource> contexte) {
        ClientStorageManager storage = CobblemonClient.INSTANCE.getStorage();

        boolean trouve = false;
        for (Pokemon pokemon : storage.getParty()) {
            if (pokemon == null) continue;
            trouve = true;

            Species espece = pokemon.getSpecies();

            TypeChart.Type type1 = TypeMapper.depuisCobblemon(espece.getPrimaryType());
            TypeChart.Type type2 = espece.getSecondaryType() != null
                    ? TypeMapper.depuisCobblemon(espece.getSecondaryType())
                    : null;
            String typesTexte = (type2 != null) ? (type1 + "/" + type2) : type1.toString();

            ItemStack objet = pokemon.heldItem();
            String nomObjet = objet.isEmpty()
                    ? "Aucun"
                    : Registries.ITEM.getId(objet.getItem()).getPath().replace("_", " ");

            contexte.getSource().sendFeedback(Text.literal(String.format(
                    "%s (Nv.%d) [%s] PV:%d Atk:%d Def:%d SpA:%d SpD:%d Spe:%d | Objet: %s | Nature: %s | Talent: %s",
                    espece.getName(), pokemon.getLevel(), typesTexte,
                    pokemon.getMaxHealth(), pokemon.getAttack(), pokemon.getDefence(),
                    pokemon.getSpecialAttack(), pokemon.getSpecialDefence(), pokemon.getSpeed(),
                    nomObjet, pokemon.getNature().getDisplayName(), pokemon.getAbility().getName()
            )));
        }

        if (!trouve) {
            contexte.getSource().sendFeedback(Text.literal("Aucun Pokémon trouvé dans ton équipe."));
        }

        return 1;
    }
}
