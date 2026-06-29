package net.tropimon.calculatortropi.calc;

// Noms en anglais pour l'instant (logique interne). Le mapping FR -> nature
// sera ajouté côté UI, en réutilisant le même principe que FrenchNames.java
// de TropiTracker, pour ne pas inventer de traductions ici.
public enum Nature {
    HARDY(null, null),
    LONELY(Stat.ATK, Stat.DEF),
    BRAVE(Stat.ATK, Stat.SPE),
    ADAMANT(Stat.ATK, Stat.SPA),
    NAUGHTY(Stat.ATK, Stat.SPD),
    BOLD(Stat.DEF, Stat.ATK),
    DOCILE(null, null),
    RELAXED(Stat.DEF, Stat.SPE),
    IMPISH(Stat.DEF, Stat.SPA),
    LAX(Stat.DEF, Stat.SPD),
    TIMID(Stat.SPE, Stat.ATK),
    HASTY(Stat.SPE, Stat.DEF),
    SERIOUS(null, null),
    JOLLY(Stat.SPE, Stat.SPA),
    NAIVE(Stat.SPE, Stat.SPD),
    MODEST(Stat.SPA, Stat.ATK),
    MILD(Stat.SPA, Stat.DEF),
    QUIET(Stat.SPA, Stat.SPE),
    BASHFUL(null, null),
    RASH(Stat.SPA, Stat.SPD),
    CALM(Stat.SPD, Stat.ATK),
    GENTLE(Stat.SPD, Stat.DEF),
    SASSY(Stat.SPD, Stat.SPE),
    CAREFUL(Stat.SPD, Stat.SPA),
    QUIRKY(null, null);

    public final Stat boostee;
    public final Stat baissee;

    Nature(Stat boostee, Stat baissee) {
        this.boostee = boostee;
        this.baissee = baissee;
    }

    public double getMultiplicateur(Stat stat) {
        if (stat == boostee) return 1.1;
        if (stat == baissee) return 0.9;
        return 1.0;
    }
}
