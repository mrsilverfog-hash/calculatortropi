package net.tropimon.calculatortropi.calc;

public class StatCalculator {

    public static int calculerPV(int base, int iv, int ev, int niveau) {
        return ((2 * base + iv + (ev / 4)) * niveau) / 100 + niveau + 10;
    }

    public static int calculerStat(int base, int iv, int ev, int niveau, double multiplicateurNature) {
        int avantNature = ((2 * base + iv + (ev / 4)) * niveau) / 100 + 5;
        return (int) Math.floor(avantNature * multiplicateurNature);
    }
}
