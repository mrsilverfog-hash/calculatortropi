package net.tropimon.calculatortropi.calc;

import java.util.Arrays;

public class TypeChart {

    public enum Type {
        NORMAL, FEU, EAU, ELECTRIK, PLANTE, GLACE, COMBAT, POISON, SOL,
        VOL, PSY, INSECTE, ROCHE, SPECTRE, DRAGON, TENEBRES, ACIER, FEE
    }

    private static final double[][] TABLE = new double[18][18];

    static {
        for (double[] ligne : TABLE) Arrays.fill(ligne, 1.0);

        def(Type.NORMAL, Type.ROCHE, 0.5); def(Type.NORMAL, Type.SPECTRE, 0); def(Type.NORMAL, Type.ACIER, 0.5);

        def(Type.FEU, Type.FEU, 0.5); def(Type.FEU, Type.EAU, 0.5); def(Type.FEU, Type.PLANTE, 2);
        def(Type.FEU, Type.GLACE, 2); def(Type.FEU, Type.INSECTE, 2); def(Type.FEU, Type.ROCHE, 0.5);
        def(Type.FEU, Type.DRAGON, 0.5); def(Type.FEU, Type.ACIER, 2);

        def(Type.EAU, Type.FEU, 2); def(Type.EAU, Type.EAU, 0.5); def(Type.EAU, Type.PLANTE, 0.5);
        def(Type.EAU, Type.SOL, 2); def(Type.EAU, Type.ROCHE, 2); def(Type.EAU, Type.DRAGON, 0.5);

        def(Type.ELECTRIK, Type.EAU, 2); def(Type.ELECTRIK, Type.ELECTRIK, 0.5); def(Type.ELECTRIK, Type.PLANTE, 0.5);
        def(Type.ELECTRIK, Type.SOL, 0); def(Type.ELECTRIK, Type.VOL, 2); def(Type.ELECTRIK, Type.DRAGON, 0.5);

        def(Type.PLANTE, Type.FEU, 0.5); def(Type.PLANTE, Type.EAU, 2); def(Type.PLANTE, Type.PLANTE, 0.5);
        def(Type.PLANTE, Type.POISON, 0.5); def(Type.PLANTE, Type.SOL, 2); def(Type.PLANTE, Type.VOL, 0.5);
        def(Type.PLANTE, Type.INSECTE, 0.5); def(Type.PLANTE, Type.ROCHE, 2); def(Type.PLANTE, Type.DRAGON, 0.5);
        def(Type.PLANTE, Type.ACIER, 0.5);

        def(Type.GLACE, Type.FEU, 0.5); def(Type.GLACE, Type.EAU, 0.5); def(Type.GLACE, Type.PLANTE, 2);
        def(Type.GLACE, Type.GLACE, 0.5); def(Type.GLACE, Type.SOL, 2); def(Type.GLACE, Type.VOL, 2);
        def(Type.GLACE, Type.DRAGON, 2); def(Type.GLACE, Type.ACIER, 0.5);

        def(Type.COMBAT, Type.NORMAL, 2); def(Type.COMBAT, Type.GLACE, 2); def(Type.COMBAT, Type.POISON, 0.5);
        def(Type.COMBAT, Type.VOL, 0.5); def(Type.COMBAT, Type.PSY, 0.5); def(Type.COMBAT, Type.INSECTE, 0.5);
        def(Type.COMBAT, Type.ROCHE, 2); def(Type.COMBAT, Type.SPECTRE, 0); def(Type.COMBAT, Type.TENEBRES, 2);
        def(Type.COMBAT, Type.ACIER, 2); def(Type.COMBAT, Type.FEE, 0.5);

        def(Type.POISON, Type.PLANTE, 2); def(Type.POISON, Type.POISON, 0.5); def(Type.POISON, Type.SOL, 0.5);
        def(Type.POISON, Type.ROCHE, 0.5); def(Type.POISON, Type.SPECTRE, 0.5); def(Type.POISON, Type.ACIER, 0);
        def(Type.POISON, Type.FEE, 2);

        def(Type.SOL, Type.FEU, 2); def(Type.SOL, Type.ELECTRIK, 2); def(Type.SOL, Type.PLANTE, 0.5);
        def(Type.SOL, Type.POISON, 2); def(Type.SOL, Type.VOL, 0); def(Type.SOL, Type.INSECTE, 0.5);
        def(Type.SOL, Type.ROCHE, 2); def(Type.SOL, Type.ACIER, 2);

        def(Type.VOL, Type.ELECTRIK, 0.5); def(Type.VOL, Type.PLANTE, 2); def(Type.VOL, Type.COMBAT, 2);
        def(Type.VOL, Type.INSECTE, 2); def(Type.VOL, Type.ROCHE, 0.5); def(Type.VOL, Type.ACIER, 0.5);

        def(Type.PSY, Type.COMBAT, 2); def(Type.PSY, Type.POISON, 2); def(Type.PSY, Type.PSY, 0.5);
        def(Type.PSY, Type.TENEBRES, 0); def(Type.PSY, Type.ACIER, 0.5);

        def(Type.INSECTE, Type.FEU, 0.5); def(Type.INSECTE, Type.PLANTE, 2); def(Type.INSECTE, Type.COMBAT, 0.5);
        def(Type.INSECTE, Type.POISON, 0.5); def(Type.INSECTE, Type.VOL, 0.5); def(Type.INSECTE, Type.PSY, 2);
        def(Type.INSECTE, Type.SPECTRE, 0.5); def(Type.INSECTE, Type.TENEBRES, 2); def(Type.INSECTE, Type.ACIER, 0.5);
        def(Type.INSECTE, Type.FEE, 0.5);

        def(Type.ROCHE, Type.FEU, 2); def(Type.ROCHE, Type.GLACE, 2); def(Type.ROCHE, Type.COMBAT, 0.5);
        def(Type.ROCHE, Type.SOL, 0.5); def(Type.ROCHE, Type.VOL, 2); def(Type.ROCHE, Type.INSECTE, 2);
        def(Type.ROCHE, Type.ACIER, 0.5);

        def(Type.SPECTRE, Type.NORMAL, 0); def(Type.SPECTRE, Type.PSY, 2); def(Type.SPECTRE, Type.SPECTRE, 2);
        def(Type.SPECTRE, Type.TENEBRES, 0.5);

        def(Type.DRAGON, Type.DRAGON, 2); def(Type.DRAGON, Type.ACIER, 0.5); def(Type.DRAGON, Type.FEE, 0);

        def(Type.TENEBRES, Type.COMBAT, 0.5); def(Type.TENEBRES, Type.PSY, 2); def(Type.TENEBRES, Type.SPECTRE, 2);
        def(Type.TENEBRES, Type.TENEBRES, 0.5); def(Type.TENEBRES, Type.FEE, 0.5);

        def(Type.ACIER, Type.FEU, 0.5); def(Type.ACIER, Type.EAU, 0.5); def(Type.ACIER, Type.ELECTRIK, 0.5);
        def(Type.ACIER, Type.GLACE, 2); def(Type.ACIER, Type.ROCHE, 2); def(Type.ACIER, Type.ACIER, 0.5);
        def(Type.ACIER, Type.FEE, 2);

        def(Type.FEE, Type.FEU, 0.5); def(Type.FEE, Type.COMBAT, 2); def(Type.FEE, Type.POISON, 0.5);
        def(Type.FEE, Type.DRAGON, 2); def(Type.FEE, Type.TENEBRES, 2); def(Type.FEE, Type.ACIER, 0.5);
    }

    private static void def(Type attaque, Type defense, double valeur) {
        TABLE[attaque.ordinal()][defense.ordinal()] = valeur;
    }

    public static double getMultiplicateur(Type attaque, Type defense1, Type defense2) {
        double m = TABLE[attaque.ordinal()][defense1.ordinal()];
        if (defense2 != null) {
            m *= TABLE[attaque.ordinal()][defense2.ordinal()];
        }
        return m;
    }
}
