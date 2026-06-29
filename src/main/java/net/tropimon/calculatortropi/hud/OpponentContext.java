package net.tropimon.calculatortropi.hud;

import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.client.battle.ClientBattleActor;
import com.cobblemon.mod.common.client.battle.ClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattleSide;
import com.cobblemon.mod.common.pokemon.Species;
import net.minecraft.client.MinecraftClient;
import net.tropimon.calculatortropi.calc.Stat;
import net.tropimon.calculatortropi.calc.StatCalculator;
import net.tropimon.calculatortropi.calc.TypeChart;
import net.tropimon.calculatortropi.cobblemon.TypeMapper;
import net.tropimon.calculatortropi.database.RankedApiClient;
import net.tropimon.calculatortropi.database.SpreadDatabase;
import net.tropimon.calculatortropi.database.SpreadEntry;

import java.util.UUID;

/**
 * Centralise la détection de l'adversaire actif (espèce, spread, stats
 * calculées) ET l'UUID de notre propre Pokémon actif, pour que le panneau
 * principal et l'infobulle de switch utilisent exactement la même logique.
 */
public class OpponentContext {

    public final UUID monUuidActif;
    public final ClientBattlePokemon actifAdverse;
    public final Species espece;
    public final int niveau;
    public final TypeChart.Type type1;
    public final TypeChart.Type type2;
    public final boolean pvExacts;
    public final float pvMaxReel;
    public final double ratioPv;

    public final SpreadEntry spread; // peut être null
    public final boolean spreadEnChargement;

    public final int atk, def, spa, spd;

    private OpponentContext(
            UUID monUuidActif, ClientBattlePokemon actifAdverse, Species espece, int niveau,
            TypeChart.Type type1, TypeChart.Type type2,
            boolean pvExacts, float pvMaxReel, double ratioPv,
            SpreadEntry spread, boolean spreadEnChargement,
            int atk, int def, int spa, int spd
    ) {
        this.monUuidActif = monUuidActif;
        this.actifAdverse = actifAdverse;
        this.espece = espece;
        this.niveau = niveau;
        this.type1 = type1;
        this.type2 = type2;
        this.pvExacts = pvExacts;
        this.pvMaxReel = pvMaxReel;
        this.ratioPv = ratioPv;
        this.spread = spread;
        this.spreadEnChargement = spreadEnChargement;
        this.atk = atk;
        this.def = def;
        this.spa = spa;
        this.spd = spd;
    }

    public int getPvMaxPourCalcul() {
        return (int) pvMaxReel;
    }

    /** Renvoie null si pas en combat exploitable. */
    public static OpponentContext detecter() {
        ClientBattle combat = CobblemonClient.INSTANCE.getBattle();
        if (combat == null) return null;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return null;
        UUID monUuidJoueur = client.player.getUuid();

        // Détection par UUID d'ACTEUR (le joueur), confirmée fiable en duel
        // classique ET en classé via /calcdebug — pas besoin de comparer
        // par UUID de Pokémon, qui s'est révélé moins fiable.
        ClientBattlePokemon monActif = null;
        ClientBattlePokemon actifAdverse = null;

        for (ClientBattleSide cote : combat.getSides()) {
            for (ClientBattleActor acteur : cote.getActors()) {
                for (ActiveClientBattlePokemon actif : acteur.getActivePokemon()) {
                    ClientBattlePokemon bp = actif.getBattlePokemon();
                    if (bp == null) continue;
                    if (acteur.getUuid().equals(monUuidJoueur)) {
                        monActif = bp;
                    } else if (actifAdverse == null) {
                        actifAdverse = bp;
                    }
                }
            }
        }

        if (monActif == null || actifAdverse == null) return null;

        Species espece = actifAdverse.getSpecies();
        int niveau = actifAdverse.getLevel();
        boolean pvExacts = actifAdverse.isHpFlat();
        float pvMaxReel = actifAdverse.getMaxHp();
        double ratioPv = pvExacts
                ? (pvMaxReel > 0 ? actifAdverse.getHpValue() / pvMaxReel : 0)
                : actifAdverse.getHpValue();

        TypeChart.Type type1 = TypeMapper.depuisCobblemon(espece.getPrimaryType());
        TypeChart.Type type2 = espece.getSecondaryType() != null
                ? TypeMapper.depuisCobblemon(espece.getSecondaryType()) : null;

        SpreadEntry spread = RankedApiClient.obtenirSiDisponible(espece.getName());
        if (spread == null) {
            spread = SpreadDatabase.trouver(espece.getName());
        }
        boolean enChargement = spread == null && RankedApiClient.estEnCoursDeChargement(espece.getName());

        int atk = 0, def = 0, spa = 0, spd = 0;
        if (spread != null) {
            def = StatCalculator.calculerStat(espece.getBaseStats().get(Stats.DEFENCE),
                    31, spread.evDef, niveau, spread.nature.getMultiplicateur(Stat.DEF));
            spd = StatCalculator.calculerStat(espece.getBaseStats().get(Stats.SPECIAL_DEFENCE),
                    31, spread.evSpd, niveau, spread.nature.getMultiplicateur(Stat.SPD));
            atk = StatCalculator.calculerStat(espece.getBaseStats().get(Stats.ATTACK),
                    31, spread.evAtk, niveau, spread.nature.getMultiplicateur(Stat.ATK));
            spa = StatCalculator.calculerStat(espece.getBaseStats().get(Stats.SPECIAL_ATTACK),
                    31, spread.evSpa, niveau, spread.nature.getMultiplicateur(Stat.SPA));
        }

        return new OpponentContext(
                monActif.getUuid(), actifAdverse, espece, niveau, type1, type2, pvExacts, pvMaxReel, ratioPv,
                spread, enChargement, atk, def, spa, spd
        );
    }
}
