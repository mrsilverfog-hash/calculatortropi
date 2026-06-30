package net.tropimon.calculatortropi.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class CalcScreenCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
            ClientCommandManager.literal("calcscreen")
                .executes(CalcScreenCommand::executer)
        );
    }

    private static int executer(CommandContext<FabricClientCommandSource> contexte) {
        var ecran = MinecraftClient.getInstance().currentScreen;

        if (ecran == null) {
            contexte.getSource().sendFeedback(Text.literal("Aucun écran ouvert actuellement (currentScreen == null)."));
        } else {
            contexte.getSource().sendFeedback(Text.literal(
                    "Écran actuel: " + ecran.getClass().getName()
            ));
        }

        return 1;
    }
}
