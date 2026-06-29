package net.tropimon.calculatortropi.hud;

import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.categories.DamageCategories;
import com.cobblemon.mod.common.api.moves.categories.DamageCategory;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.client.battle.ClientBattleActor;
import com.cobblemon.mod.common.client.battle.ClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattleSide;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.tropimon.calculatortropi.calc.DamageCalculator;
import net.tropimon.calculatortropi.calc.Stat;
import net.tropimon.calculatortropi.calc.StatCalculator;
import net.tropimon.calculatortropi.calc.TypeChart;
import net.tropimon.calculatortropi.cobblemon.MoveNameResolver;
import net.tropimon.calculatortropi.cobblemon.TypeMapper;
import net.tropimon.calculatortropi.database.RankedApiClient;
import net.tropimon.calculatortropi.database.SpreadDatabase;
import net.tropimon.calculatortropi.database.SpreadEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CalcHud {

    public static void enregistrer() {
        HudRenderCallback.EVENT.register(CalcHud::dessiner);
    }

    private static void dessiner(DrawContext contexte, RenderTickCounter compteur) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        ClientBattle combat = CobblemonClient.INSTANCE.getBattle();
        if (combat == null || combat.isPvW()) return;

        UUID monUuid = client.player.getUuid();

        ClientBattlePokemon monActif = null;
        ClientBattlePokemon actifAdverse = null;

        for (ClientBattleSide cote : combat.getSides()) {
            for (ClientBattleActor acteur : cote.getActors()) {
                for (ActiveClientBattlePokemon actif : acteur.getActivePokemon()) {
                    ClientBattlePokemon bp = actif.getBattlePokemon();
                    if (bp == null) continue;
                    if (acteur.getUuid().equals(monUuid)) {
                        monActif = bp;
                    } else {
                        actifAdverse = bp;
                    }
                }
            }
        }

        if (monActif == null || actifAdverse == null) return;

        // On retrouve la fiche complète (vraies IV/EV/capacités) de notre
        // Pokémon actif dans notre party, via son UUID.
        Pokemon monPokemonComplet = null;
        for (Pokemon p : CobblemonClient.INSTANCE.getStorage().getParty()) {
            if (p != null && p.getUuid().equals(monActif.getUuid())) {
                monPokemonComplet = p;
                break;
            }
        }
        if (monPokemonComplet == null) return;

        Species especeAdverse = actifAdverse.getSpecies();
        int niveauAdverse = actifAdverse.getLevel();

        List<String> lignes = new ArrayList<>();
        lignes.add("=== Calculatortropi ===");
        lignes.add(monPokemonComplet.getSpecies().getName() + " (Nv." + monPokemonComplet.getLevel() + ")  vs  "
                + especeAdverse.getName() + " (Nv." + niveauAdverse + ")");

        SpreadEntry spread = RankedApiClient.obtenirSiDisponible(especeAdverse.getName());
        if (spread == null) {
            spread = SpreadDatabase.trouver(especeAdverse.getName());
        }

        if (spread == null) {
            if (RankedApiClient.estEnCoursDeChargement(especeAdverse.getName())) {
                lignes.add("Recherche de la spread adverse...");
            } else {
                lignes.add("Spread adverse inconnue (API et base locale).");
            }
        } else {
            TypeChart.Type typeAdv1 = TypeMapper.depuisCobblemon(especeAdverse.getPrimaryType());
            TypeChart.Type typeAdv2 = especeAdverse.getSecondaryType() != null
                    ? TypeMapper.depuisCobblemon(especeAdverse.getSecondaryType()) : null;

            TypeChart.Type typeNous1 = TypeMapper.depuisCobblemon(monPokemonComplet.getPrimaryType());
            TypeChart.Type typeNous2 = monPokemonComplet.getSecondaryType() != null
                    ? TypeMapper.depuisCobblemon(monPokemonComplet.getSecondaryType()) : null;

            int defAdv = StatCalculator.calculerStat(especeAdverse.getBaseStats().get(Stats.DEFENCE),
                    31, spread.evDef, niveauAdverse, spread.nature.getMultiplicateur(Stat.DEF));
            int spdAdv = StatCalculator.calculerStat(especeAdverse.getBaseStats().get(Stats.SPECIAL_DEFENCE),
                    31, spread.evSpd, niveauAdverse, spread.nature.getMultiplicateur(Stat.SPD));
            int pvMaxAdv = StatCalculator.calculerPV(especeAdverse.getBaseStats().get(Stats.HP),
                    31, spread.evPv, niveauAdverse);

            int atkAdv = StatCalculator.calculerStat(especeAdverse.getBaseStats().get(Stats.ATTACK),
                    31, spread.evAtk, niveauAdverse, spread.nature.getMultiplicateur(Stat.ATK));
            int spaAdv = StatCalculator.calculerStat(especeAdverse.getBaseStats().get(Stats.SPECIAL_ATTACK),
                    31, spread.evSpa, niveauAdverse, spread.nature.getMultiplicateur(Stat.SPA));

            lignes.add("Adversaire (estime) : Objet " + spread.objet + " | Talent " + spread.talent);

            lignes.add("--- Vos capacites -> adversaire ---");
            for (Move m : monPokemonComplet.getMoveSet()) {
                if (m == null) continue;
                ajouterLigneDegats(lignes, m.getDisplayName().getString(), m.getPower(), m.getElementalType(), m.getDamageCategory(),
                        monPokemonComplet.getAttack(), monPokemonComplet.getSpecialAttack(), monPokemonComplet.getLevel(),
                        defAdv, spdAdv, pvMaxAdv, typeNous1, typeNous2, typeAdv1, typeAdv2);
            }

            lignes.add("--- Capacites probables adverses -> vous ---");
            for (String nomMove : spread.topMoves) {
                MoveTemplate gabarit = MoveNameResolver.resoudre(nomMove);
                if (gabarit == null || gabarit.getPower() <= 0) continue;
                ajouterLigneDegats(lignes, nomMove, gabarit.getPower(), gabarit.getElementalType(), gabarit.getDamageCategory(),
                        atkAdv, spaAdv, niveauAdverse,
                        monPokemonComplet.getDefence(), monPokemonComplet.getSpecialDefence(), monPokemonComplet.getMaxHealth(),
                        typeAdv1, typeAdv2, typeNous1, typeNous2);
            }
        }

        dessinerPanneau(contexte, client, lignes);
    }

    private static void ajouterLigneDegats(
            List<String> lignes, String nomMove, double puissance,
            ElementalType typeMoveCobblemon, DamageCategory categorie,
            int attaquePhysique, int attaqueSpeciale, int niveauAttaquant,
            int defensePhysiqueCible, int defenseSpecialeCible, int pvMaxCible,
            TypeChart.Type typeAttaquant1, TypeChart.Type typeAttaquant2,
            TypeChart.Type typeCible1, TypeChart.Type typeCible2
    ) {
        if (puissance <= 0) return;

        boolean physique = categorie == DamageCategories.INSTANCE.getPHYSICAL();
        int attaque = physique ? attaquePhysique : attaqueSpeciale;
        int defense = physique ? defensePhysiqueCible : defenseSpecialeCible;

        TypeChart.Type typeMove = TypeMapper.depuisCobblemon(typeMoveCobblemon);
        boolean stab = typeMove == typeAttaquant1 || typeMove == typeAttaquant2;
        double efficacite = TypeChart.getMultiplicateur(typeMove, typeCible1, typeCible2);

        DamageCalculator.Resultat resultat = DamageCalculator.calculer(
                attaque, defense, (int) puissance, niveauAttaquant, pvMaxCible, stab, efficacite, 1.0, false, false
        );

        if (efficacite == 0) {
            lignes.add(String.format("%s : aucun effet", nomMove));
        } else {
            lignes.add(String.format("%s : %.0f-%.0f%%", nomMove, resultat.pourcentMin, resultat.pourcentMax));
        }
    }

    private static void dessinerPanneau(DrawContext contexte, MinecraftClient client, List<String> lignes) {
        int largeurMax = 0;
        for (String ligne : lignes) {
            largeurMax = Math.max(largeurMax, client.textRenderer.getWidth(ligne));
        }

        int x = 8;
        int y = 8;
        int largeurPanneau = largeurMax + 12;
        int hauteurLigne = 11;
        int hauteurPanneau = lignes.size() * hauteurLigne + 8;

        contexte.fill(x, y, x + largeurPanneau, y + hauteurPanneau, 0xAA000000);

        int yTexte = y + 4;
        for (String ligne : lignes) {
            contexte.drawTextWithShadow(client.textRenderer, ligne, x + 6, yTexte, 0xFFFFFF);
            yTexte += hauteurLigne;
        }
    }
}
