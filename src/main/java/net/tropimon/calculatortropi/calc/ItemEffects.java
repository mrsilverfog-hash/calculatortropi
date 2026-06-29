package net.tropimon.calculatortropi.calc;

import java.util.HashMap;
import java.util.Map;

// Pas encore branché dans la commande de test - servira quand on assemblera
// un Pokémon complet (stat + objet) à l'étape de l'écran/UI.
public class ItemEffects {

    public enum Cible { ATK, SPA, SPE }

    private static final Map<String, Cible> CIBLE_BOOST = new HashMap<>();
    private static final Map<String, Double> DEGATS_FIXE = new HashMap<>();

    static {
        CIBLE_BOOST.put("choice band", Cible.ATK);
        CIBLE_BOOST.put("choice specs", Cible.SPA);
        CIBLE_BOOST.put("choice scarf", Cible.SPE);

        DEGATS_FIXE.put("life orb", 1.3);
    }

    public static double getMultiplicateurStat(String objet, Cible stat) {
        if (objet == null) return 1.0;
        Cible cible = CIBLE_BOOST.get(objet.toLowerCase());
        return (cible == stat) ? 1.5 : 1.0;
    }

    public static double getMultiplicateurDegats(String objet, boolean superEfficace) {
        if (objet == null) return 1.0;
        String key = objet.toLowerCase();
        if (key.equals("expert belt")) return superEfficace ? 1.2 : 1.0;
        return DEGATS_FIXE.getOrDefault(key, 1.0);
    }
}
