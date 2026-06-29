package net.tropimon.calculatortropi.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.tropimon.calculatortropi.database.SpreadDatabase;
import net.tropimon.calculatortropi.database.SpreadEntry;

public class CalcSpreadCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
            ClientCommandManager.literal("calcspread")
                .then(ClientCommandManager.argument("espece", StringArgumentType.greedyString())
                .executes(CalcSpreadCommand::executer))
        );
    }

    private static int executer(CommandContext<FabricClientCommandSource> contexte) {
        String espece = StringArgumentType.getString(contexte, "espece");
        SpreadEntry entree = SpreadDatabase.trouver(espece);

        if (entree == null) {
            contexte.getSource().sendFeedback(Text.literal(
                    "Aucune spread trouvée pour \"" + espece + "\" dans la base de données."
            ));
            return 1;
        }

        contexte.getSource().sendFeedback(Text.literal(String.format(
                "%s -> Objet: %s | Nature: %s | EV PV:%d Atk:%d Def:%d SpA:%d SpD:%d Spe:%d | Talent: %s",
                espece, entree.objet, entree.nature, entree.evPv, entree.evAtk, entree.evDef,
                entree.evSpa, entree.evSpd, entree.evSpe, entree.talent
        )));

        return 1;
    }
}
