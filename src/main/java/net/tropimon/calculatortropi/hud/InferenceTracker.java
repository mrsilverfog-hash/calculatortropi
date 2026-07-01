package net.tropimon.calculatortropi.hud;

import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.categories.DamageCategories;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.tropimon.calculatortropi.calc.DamageCalculator;
import net.tropimon.calculatortropi.calc.TypeChart;
import net.tropimon.calculatortropi.cobblemon.MoveNameResolver;
import net.tropimon.calculatortropi.cobblemon.TypeMapper;

import java.util.UUID;

/**
 * Observe les dégâts réellement reçus en combat et en déduit les vraies
 * stats offensives de l'adversaire et son objet probable, en comparant
 * les dégâts observés à ceux attendus selon la spread de l'API.
 */
public class InferenceTracker {

    private static UUID dernierAdversaireId = null;
    private static UUID dernierNotreId = null;
    private static int dernierHp = -1;

    // Corrections accumulées (moyenne glissante)
    private static double sommeMultAtk = 0;
    private static int nbObsAtk = 0;
    private static double sommeMultSpa = 0;
    private static int nbObsSpa = 0;

    public static String objetInfere = null;

    public static void reset() {
        dernierAdversaireId = null;
        dernierNotreId = null;
        dernierHp = -1;
        resetCorrections();
    }

    private static void resetCorrections() {
        sommeMultAtk = 0;
        nbObsAtk = 0;
        sommeMultSpa = 0;
        nbObsSpa = 0;
        objetInfere = null;
    }

    public static double getMultiplicateurAtk() {
        return nbObsAtk > 0 ? sommeMultAtk / nbObsAtk : 1.0;
    }

    public static double getMultiplicateurSpa() {
        return nbObsSpa > 0 ? sommeMultSpa / nbObsSpa : 1.0;
    }

    public static boolean aDesCorrections() {
        return nbObsAtk > 0 || nbObsSpa > 0 || objetInfere != null;
    }

    public static int getNbObservations() {
        return nbObsAtk + nbObsSpa;
    }

    public static void mettreAJour(Pokemon monPokemon, OpponentContext ctx) {
        if (ctx == null || ctx.spread == null || monPokemon == null) {
            reset();
            return;
        }

        UUID idAdv = ctx.actifAdverse.getUuid();
        UUID idNous = monPokemon.getUuid();

        // Adversaire changé (switch de sa part) → reset corrections
        if (!idAdv.equals(dernierAdversaireId)) {
            resetCorrections();
            dernierAdversaireId = idAdv;
        }

        // Notre Pokemon changé (on a switché) → reset suivi HP
        if (!idNous.equals(dernierNotreId)) {
            dernierNotreId = idNous;
            dernierHp = monPokemon.getCurrentHealth();
            return;
        }

        int hpActuel = monPokemon.getCurrentHealth();
        int pvMax = monPokemon.getMaxHealth();

        if (hpActuel < dernierHp && dernierHp > 0 && pvMax > 0) {
            analyserDegats(dernierHp - hpActuel, pvMax, monPokemon, ctx);
        }

        dernierHp = hpActuel;
    }

    private static void analyserDegats(int degats, int pvMax, Pokemon cible, OpponentContext ctx) {
        double pctReel = (double) degats / pvMax * 100.0;

        TypeChart.Type typeCible1 = TypeMapper.depuisCobblemon(cible.getPrimaryType());
        TypeChart.Type typeCible2 = cible.getSecondaryType() != null
                ? TypeMapper.depuisCobblemon(cible.getSecondaryType()) : null;

        MoveTemplate meilleur = null;
        boolean meilleurPhysique = false;
        double meilleurEcart = Double.MAX_VALUE;
        double attenduMeilleur = 0;

        // On cherche quelle capacité probable de l'adversaire explique le mieux les dégâts
        for (String nomMove : ctx.spread.topMoves) {
            MoveTemplate g = MoveNameResolver.resoudre(nomMove);
            if (g == null || g.getPower() <= 0) continue;

            boolean physique = g.getDamageCategory() == DamageCategories.INSTANCE.getPHYSICAL();
            // IMPORTANT: on utilise les stats BRUTES (ctx.atk/ctx.spa) pour calculer
            // le ratio, pas les valeurs déjà corrigées — sinon boucle circulaire
            int atk = physique ? ctx.atk : ctx.spa;
            int def = physique ? cible.getDefence() : cible.getSpecialDefence();

            TypeChart.Type typeMove = TypeMapper.depuisCobblemon(g.getElementalType());
            boolean stab = typeMove == ctx.type1 || typeMove == ctx.type2;
            double efficacite = TypeChart.getMultiplicateur(typeMove, typeCible1, typeCible2);
            if (efficacite == 0) continue;

            DamageCalculator.Resultat r = DamageCalculator.calculer(
                    atk, def, (int) g.getPower(), ctx.niveau, pvMax, stab, efficacite, 1.0, false, false
            );

            double attendu = (r.pourcentMin + r.pourcentMax) / 2.0;
            double ecart = Math.abs(pctReel - attendu);

            if (ecart < meilleurEcart) {
                meilleurEcart = ecart;
                meilleur = g;
                meilleurPhysique = physique;
                attenduMeilleur = attendu;
            }
        }

        // Rejeter si aucune capacité ne correspond (dégâts de statut, pièges, etc.)
        if (meilleur == null || attenduMeilleur <= 0) return;
        if (meilleurEcart > Math.max(20.0, attenduMeilleur * 0.7)) return;

        double ratio = pctReel / attenduMeilleur;

        // Inférence d'objet depuis le ratio réel/attendu
        if (Math.abs(ratio - 1.5) < 0.13) {
            objetInfere = meilleurPhysique ? "choice band" : "choice specs";
        } else if (Math.abs(ratio - 1.3) < 0.12) {
            objetInfere = "life orb";
        }

        // Accumulation de la correction (moyenne glissante pondérée)
        if (meilleurPhysique) {
            sommeMultAtk += ratio;
            nbObsAtk++;
        } else {
            sommeMultSpa += ratio;
            nbObsSpa++;
        }
    }
}
