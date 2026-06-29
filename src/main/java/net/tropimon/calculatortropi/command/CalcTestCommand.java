package net.tropimon.calculatortropi.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.tropimon.calculatortropi.calc.DamageCalculator;

public class CalcTestCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
            ClientCommandManager.literal("calctest")
                .then(ClientCommandManager.argument("atk", IntegerArgumentType.integer())
                .then(ClientCommandManager.argument("def", IntegerArgumentType.integer())
                .then(ClientCommandManager.argument("puissance", IntegerArgumentType.integer())
                .then(ClientCommandManager.argument("niveau", IntegerArgumentType.integer())
                .then(ClientCommandManager.argument("pvmax", IntegerArgumentType.integer())
                .then(ClientCommandManager.argument("stab", BoolArgumentType.bool())
                .then(ClientCommandManager.argument("efficacite", DoubleArgumentType.doubleArg())
                .then(ClientCommandManager.argument("meteo", DoubleArgumentType.doubleArg())
                .then(ClientCommandManager.argument("critique", BoolArgumentType.bool())
                .then(ClientCommandManager.argument("brulure", BoolArgumentType.bool())
                .executes(CalcTestCommand::executer)
                ))))))))))
        );
    }

    private static int executer(CommandContext<FabricClientCommandSource> contexte) {
        int atk = IntegerArgumentType.getInteger(contexte, "atk");
        int def = IntegerArgumentType.getInteger(contexte, "def");
        int puissance = IntegerArgumentType.getInteger(contexte, "puissance");
        int niveau = IntegerArgumentType.getInteger(contexte, "niveau");
        int pvmax = IntegerArgumentType.getInteger(contexte, "pvmax");
        boolean stab = BoolArgumentType.getBool(contexte, "stab");
        double efficacite = DoubleArgumentType.getDouble(contexte, "efficacite");
        double meteo = DoubleArgumentType.getDouble(contexte, "meteo");
        boolean critique = BoolArgumentType.getBool(contexte, "critique");
        boolean brulure = BoolArgumentType.getBool(contexte, "brulure");

        DamageCalculator.Resultat resultat = DamageCalculator.calculer(
                atk, def, puissance, niveau, pvmax, stab, efficacite, meteo, critique, brulure
        );

        contexte.getSource().sendFeedback(Text.literal(String.format(
                "Degats : %d-%d PV (%.1f%%-%.1f%%)",
                resultat.degatsMin, resultat.degatsMax, resultat.pourcentMin, resultat.pourcentMax
        )));

        return 1;
    }
}
