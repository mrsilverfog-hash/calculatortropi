package net.tropimon.calculatortropi.calc;

public class DamageCalculator {

    public static class Resultat {
        public final int degatsMin;
        public final int degatsMax;
        public final double pourcentMin;
        public final double pourcentMax;

        public Resultat(int degatsMin, int degatsMax, double pourcentMin, double pourcentMax) {
            this.degatsMin = degatsMin;
            this.degatsMax = degatsMax;
            this.pourcentMin = pourcentMin;
            this.pourcentMax = pourcentMax;
        }
    }

    public static Resultat calculer(
            int attaque, int defense, int puissance, int niveau, int pvMaxCible,
            boolean stab, double efficaciteType, double meteo, boolean critique, boolean brulure
    ) {
        if (efficaciteType == 0) {
            return new Resultat(0, 0, 0, 0);
        }

        int baseDegats = (((2 * niveau / 5 + 2) * puissance * attaque / defense) / 50) + 2;

        double multiplicateur = 1.0;
        multiplicateur *= meteo;
        if (critique) multiplicateur *= 1.5;
        if (stab) multiplicateur *= 1.5;
        multiplicateur *= efficaciteType;
        if (brulure) multiplicateur *= 0.5;

        int degatsMin = (int) Math.floor(baseDegats * 0.85 * multiplicateur);
        int degatsMax = (int) Math.floor(baseDegats * 1.00 * multiplicateur);

        double pourcentMin = (degatsMin * 100.0) / pvMaxCible;
        double pourcentMax = (degatsMax * 100.0) / pvMaxCible;

        return new Resultat(degatsMin, degatsMax, pourcentMin, pourcentMax);
    }
}
