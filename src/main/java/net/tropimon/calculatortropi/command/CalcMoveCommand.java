package net.tropimon.calculatortropi.command;

import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.tropimon.calculatortropi.calc.TypeChart;
import net.tropimon.calculatortropi.cobblemon.MoveNameResolver;
import net.tropimon.calculatortropi.cobblemon.TypeMapper;

public class CalcMoveCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
            ClientCommandManager.literal("calcmove")
                .then(ClientCommandManager.argument("nom", StringArgumentType.greedyString())
                .executes(CalcMoveCommand::executer))
        );
    }

    private static int executer(CommandContext<FabricClientCommandSource> contexte) {
        String nom = StringArgumentType.getString(contexte, "nom");
        MoveTemplate capacite = MoveNameResolver.resoudre(nom);

        TypeChart.Type type = TypeMapper.depuisCobblemon(capacite.getElementalType());

        contexte.getSource().sendFeedback(Text.literal(String.format(
                "\"%s\" -> id interne: %s | Type: %s | Catégorie: %s | Puissance: %.0f | Précision: %.0f%% | PP: %d",
                nom, capacite.getName(), type, capacite.getDamageCategory().getName(),
                capacite.getPower(), capacite.getAccuracy(), capacite.getPp()
        )));

        return 1;
    }
}
