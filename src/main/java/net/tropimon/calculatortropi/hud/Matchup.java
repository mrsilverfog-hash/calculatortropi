package net.tropimon.calculatortropi.hud;

import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.categories.DamageCategories;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.tropimon.calculatortropi.calc.DamageCalculator;
import net.tropimon.calculatortropi.calc.TypeChart;
import net.tropimon.calculatortropi.cobblemon.MoveNameResolver;
import net.tropimon.calculatortropi.cobblemon.TypeMapper;

import java.util.ArrayList;
import java.util.List;

/** Calculs de matchup réutilisables, indépendants du rendu. */
public class Matchup {

    public record Ligne(TypeChart.Type type, String nom, double min, double max, String resultat, int couleurResultat) {}

    /** Dégâts de chaque capacité de "attaquant" vers la cible. */
    public static List<Ligne> versCible(
            Pokemon attaquant, TypeChart.Type typeAtt1, TypeChart.Type typeAtt2,
            int defenseCible, int defenseSpecialeCible, int pvMaxCible,
            TypeChart.Type typeCible1, TypeChart.Type typeCible2
    ) {
        List<Ligne> resultat = new ArrayList<>();
        for (Move m : attaquant.getMoveSet()) {
            if (m == null || m.getPower() <= 0) continue;

            boolean physique = m.getDamageCategory() == DamageCategories.INSTANCE.getPHYSICAL();
            int attaque = physique ? attaquant.getAttack() : attaquant.getSpecialAttack();
            int defense = physique ? defenseCible : defenseSpecialeCible;

            TypeChart.Type typeMove = TypeMapper.depuisCobblemon(m.getType());
            boolean stab = typeMove == typeAtt1 || typeMove == typeAtt2;
            double efficacite = TypeChart.getMultiplicateur(typeMove, typeCible1, typeCible2);

            resultat.add(construire(m.getDisplayName().getString(), typeMove, m.getPower(),
                    attaque, defense, attaquant.getLevel(), pvMaxCible, stab, efficacite));
        }
        return resultat;
    }

    /** Dégâts des capacités probables de l'adversaire (par nom) vers "cible". */
    public static List<Ligne> depuisAdversaire(
            List<String> topMoves, int atkAdv, int spaAdv, int niveauAdverse,
            TypeChart.Type typeAdv1, TypeChart.Type typeAdv2,
            Pokemon cible, TypeChart.Type typeCible1, TypeChart.Type typeCible2
    ) {
        List<Ligne> resultat = new ArrayList<>();
        for (String nomMove : topMoves) {
            MoveTemplate gabarit = MoveNameResolver.resoudre(nomMove);
            if (gabarit == null || gabarit.getPower() <= 0) continue;

            boolean physique = gabarit.getDamageCategory() == DamageCategories.INSTANCE.getPHYSICAL();
            int attaque = physique ? atkAdv : spaAdv;
            int defense = physique ? cible.getDefence() : cible.getSpecialDefence();

            TypeChart.Type typeMove = TypeMapper.depuisCobblemon(gabarit.getElementalType());
            boolean stab = typeMove == typeAdv1 || typeMove == typeAdv2;
            double efficacite = TypeChart.getMultiplicateur(typeMove, typeCible1, typeCible2);

            resultat.add(construire(nomMove, typeMove, gabarit.getPower(),
                    attaque, defense, niveauAdverse, cible.getMaxHealth(), stab, efficacite));
        }
        return resultat;
    }

    /** Renvoie la ligne avec le pourcentage max le plus élevé (le "pire/meilleur" coup). */
    public static Ligne meilleure(List<Ligne> lignes) {
        Ligne meilleure = null;
        for (Ligne l : lignes) {
            if (meilleure == null || l.max() > meilleure.max()) {
                meilleure = l;
            }
        }
        return meilleure;
    }

    private static Ligne construire(
            String nom, TypeChart.Type typeMove, double puissance,
            int attaque, int defense, int niveauAttaquant, int pvMaxCible,
            boolean stab, double efficacite
    ) {
        if (efficacite == 0 || pvMaxCible <= 0) {
            return new Ligne(typeMove, nom, 0, 0, "aucun effet", 0xFF888888);
        }

        DamageCalculator.Resultat r = DamageCalculator.calculer(
                attaque, defense, (int) puissance, niveauAttaquant, pvMaxCible, stab, efficacite, 1.0, false, false
        );

        String texte = String.format("%.0f-%.0f%%", r.pourcentMin, r.pourcentMax);
        return new Ligne(typeMove, nom, r.pourcentMin, r.pourcentMax, texte, couleurSeverite(r.pourcentMin, r.pourcentMax));
    }

    public static int couleurSeverite(double pourcentMin, double pourcentMax) {
        if (pourcentMin >= 100) return 0xFFFF5555;
        if (pourcentMax >= 100) return 0xFFFFAA00;
        if (pourcentMax >= 50) return 0xFFFFFFFF;
        return 0xFFAAAAAA;
    }
}
