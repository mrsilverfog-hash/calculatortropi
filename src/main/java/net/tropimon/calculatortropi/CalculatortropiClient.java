package net.tropimon.calculatortropi;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.tropimon.calculatortropi.command.CalcDebugCommand;
import net.tropimon.calculatortropi.command.CalcMoveCommand;
import net.tropimon.calculatortropi.command.CalcOpponentCommand;
import net.tropimon.calculatortropi.command.CalcSpreadCommand;
import net.tropimon.calculatortropi.command.CalcTeamCommand;
import net.tropimon.calculatortropi.command.CalcTestCommand;
import net.tropimon.calculatortropi.database.SpreadDatabase;
import net.tropimon.calculatortropi.hud.CalcHud;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalculatortropiClient implements ClientModInitializer {
    public static final String MOD_ID = "calculatortropi";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("[Calculatortropi] Mod chargé avec succès !");
        SpreadDatabase.charger();
        CalcHud.enregistrer();
        ClientCommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess) -> {
                    CalcTestCommand.register(dispatcher);
                    CalcTeamCommand.register(dispatcher);
                    CalcOpponentCommand.register(dispatcher);
                    CalcSpreadCommand.register(dispatcher);
                    CalcMoveCommand.register(dispatcher);
                    CalcDebugCommand.register(dispatcher);
                }
        );
    }
}
