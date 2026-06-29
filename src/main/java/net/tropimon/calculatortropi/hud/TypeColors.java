package net.tropimon.calculatortropi.hud;

import net.tropimon.calculatortropi.calc.TypeChart;

import java.util.HashMap;
import java.util.Map;

public class TypeColors {

    private static final Map<TypeChart.Type, Integer> COULEURS = new HashMap<>();

    static {
        COULEURS.put(TypeChart.Type.NORMAL, 0xFFA8A878);
        COULEURS.put(TypeChart.Type.FEU, 0xFFF08030);
        COULEURS.put(TypeChart.Type.EAU, 0xFF6890F0);
        COULEURS.put(TypeChart.Type.ELECTRIK, 0xFFF8D030);
        COULEURS.put(TypeChart.Type.PLANTE, 0xFF78C850);
        COULEURS.put(TypeChart.Type.GLACE, 0xFF98D8D8);
        COULEURS.put(TypeChart.Type.COMBAT, 0xFFC03028);
        COULEURS.put(TypeChart.Type.POISON, 0xFFA040A0);
        COULEURS.put(TypeChart.Type.SOL, 0xFFE0C068);
        COULEURS.put(TypeChart.Type.VOL, 0xFFA890F0);
        COULEURS.put(TypeChart.Type.PSY, 0xFFF85888);
        COULEURS.put(TypeChart.Type.INSECTE, 0xFFA8B820);
        COULEURS.put(TypeChart.Type.ROCHE, 0xFFB8A038);
        COULEURS.put(TypeChart.Type.SPECTRE, 0xFF705898);
        COULEURS.put(TypeChart.Type.DRAGON, 0xFF7038F8);
        COULEURS.put(TypeChart.Type.TENEBRES, 0xFF705848);
        COULEURS.put(TypeChart.Type.ACIER, 0xFFB8B8D0);
        COULEURS.put(TypeChart.Type.FEE, 0xFFEE99AC);
    }

    public static int getCouleurFond(TypeChart.Type type) {
        return COULEURS.getOrDefault(type, 0xFF808080);
    }

    public static String getAbreviation(TypeChart.Type type) {
        return type.name().substring(0, Math.min(3, type.name().length()));
    }

    public static int getCouleurTexte(TypeChart.Type type) {
        int couleur = getCouleurFond(type);
        int r = (couleur >> 16) & 0xFF;
        int g = (couleur >> 8) & 0xFF;
        int b = couleur & 0xFF;
        double luminance = (0.299 * r + 0.587 * g + 0.114 * b);
        return luminance > 150 ? 0xFF000000 : 0xFFFFFFFF;
    }
}
