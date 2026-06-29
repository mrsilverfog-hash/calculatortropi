package net.tropimon.calculatortropi.database;

import net.tropimon.calculatortropi.calc.Nature;

import java.util.Collections;
import java.util.List;

public class SpreadEntry {
    public final String objet;
    public final Nature nature;
    public final int evPv, evAtk, evDef, evSpa, evSpd, evSpe;
    public final String talent;
    public final List<String> topMoves;

    public SpreadEntry(String objet, Nature nature, int evPv, int evAtk, int evDef,
                        int evSpa, int evSpd, int evSpe, String talent) {
        this(objet, nature, evPv, evAtk, evDef, evSpa, evSpd, evSpe, talent, Collections.emptyList());
    }

    public SpreadEntry(String objet, Nature nature, int evPv, int evAtk, int evDef,
                        int evSpa, int evSpd, int evSpe, String talent, List<String> topMoves) {
        this.objet = objet;
        this.nature = nature;
        this.evPv = evPv;
        this.evAtk = evAtk;
        this.evDef = evDef;
        this.evSpa = evSpa;
        this.evSpd = evSpd;
        this.evSpe = evSpe;
        this.talent = talent;
        this.topMoves = topMoves;
    }
}
