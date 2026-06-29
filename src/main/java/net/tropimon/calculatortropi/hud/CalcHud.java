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

    private static final int LARGEUR_PANNEAU = 250;
    private static final int LARGEUR_BADGE = 24;
    private static final int HAUTEUR_BADGE = 10;
    private static final int LARGEUR_BARRE_VIE = 110;
    private static final int HAUTEUR_BARRE_VIE = 6;

    // --- Rangées du panneau ---
    private interface Rang {}

    private record RangTexte(String texte, int couleur) implements Rang {}
    private record RangNomTypes(String nom, TypeChart.Type type1, TypeChart.Type type2) implements Rang {}
    private record RangBarreVie(double ratio, String texteDroite) implements Rang {}
    private record RangMove(TypeChart.Type type, String nom, String resultat, int couleurResultat) implements Rang {}
    private record RangEspace() implements Rang {}

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

        Pokemon monPokemonComplet = null;
        for (Pokemon p : CobblemonClient.INSTANCE.getStorage().getParty()) {
            if (p != null && p.getUuid().equals(monActif.getUuid())) {
                monPokemonComplet = p;
                break;
            }
        }
        if (monPokemonComplet == null) return;

        List<Rang> rangs = new ArrayList<>();
        rangs.add(new RangTexte("=== Calculatortropi ===", 0xFF55FFFF));

        // --- Notre Pokémon ---
        TypeChart.Type typeNous1 = TypeMapper.depuisCobblemon(monPokemonComplet.getPrimaryType());
        TypeChart.Type typeNous2 = monPokemonComplet.getSecondaryType() != null
                ? TypeMapper.depuisCobblemon(monPokemonComplet.getSecondaryType()) : null;

        rangs.add(new RangNomTypes(
                monPokemonComplet.getSpecies().getName() + " (Nv." + monPokemonComplet.getLevel() + ")",
                typeNous1, typeNous2
        ));
        double ratioNous = monPokemonComplet.getMaxHealth() > 0
                ? (double) monPokemonComplet.getCurrentHealth() / monPokemonComplet.getMaxHealth() : 0;
        rangs.add(new RangBarreVie(ratioNous,
                monPokemonComplet.getCurrentHealth() + "/" + monPokemonComplet.getMaxHealth() + " PV"));

        rangs.add(new RangTexte("contre", 0xFFAAAAAA));

        // --- Adversaire ---
        Species especeAdverse = actifAdverse.getSpecies();
        int niveauAdverse = actifAdverse.getLevel();
        boolean pvExacts = actifAdverse.isHpFlat();
        float pvMaxReel = actifAdverse.getMaxHp();
        double ratioAdverse = pvExacts
                ? (pvMaxReel > 0 ? actifAdverse.getHpValue() / pvMaxReel : 0)
                : actifAdverse.getHpValue();

        TypeChart.Type typeAdv1 = TypeMapper.depuisCobblemon(especeAdverse.getPrimaryType());
        TypeChart.Type typeAdv2 = especeAdverse.getSecondaryType() != null
                ? TypeMapper.depuisCobblemon(especeAdverse.getSecondaryType()) : null;

        rangs.add(new RangNomTypes(
                especeAdverse.getName() + " (Nv." + niveauAdverse + ")",
                typeAdv1, typeAdv2
        ));

        String texteVieAdverse = pvExacts
                ? String.format("%.0f/%.0f PV", actifAdverse.getHpValue(), pvMaxReel)
                : String.format("~%.0f/%.0f PV (est.)", ratioAdverse * pvMaxReel, pvMaxReel);
        rangs.add(new RangBarreVie(ratioAdverse, texteVieAdverse));

        SpreadEntry spread = RankedApiClient.obtenirSiDisponible(especeAdverse.getName());
        if (spread == null) {
            spread = SpreadDatabase.trouver(especeAdverse.getName());
        }

        rangs.add(new RangEspace());

        if (spread == null) {
            if (RankedApiClient.estEnCoursDeChargement(especeAdverse.getName())) {
                rangs.add(new RangTexte("Recherche de la spread adverse...", 0xFFAAAAAA));
            } else {
                rangs.add(new RangTexte("Spread adverse inconnue.", 0xFFAAAAAA));
            }
        } else {
            rangs.add(new RangTexte("Objet: " + spread.objet + " | Talent: " + spread.talent, 0xFFAAAAAA));
            rangs.add(new RangEspace());

            int defAdv = StatCalculator.calculerStat(especeAdverse.getBaseStats().get(Stats.DEFENCE),
                    31, spread.evDef, niveauAdverse, spread.nature.getMultiplicateur(Stat.DEF));
            int spdAdv = StatCalculator.calculerStat(especeAdverse.getBaseStats().get(Stats.SPECIAL_DEFENCE),
                    31, spread.evSpd, niveauAdverse, spread.nature.getMultiplicateur(Stat.SPD));
            int atkAdv = StatCalculator.calculerStat(especeAdverse.getBaseStats().get(Stats.ATTACK),
                    31, spread.evAtk, niveauAdverse, spread.nature.getMultiplicateur(Stat.ATK));
            int spaAdv = StatCalculator.calculerStat(especeAdverse.getBaseStats().get(Stats.SPECIAL_ATTACK),
                    31, spread.evSpa, niveauAdverse, spread.nature.getMultiplicateur(Stat.SPA));
            int pvMaxAdvPourCalcul = (int) pvMaxReel;

            rangs.add(new RangTexte("Vos capacités -> adversaire", 0xFF55FFFF));
            for (Move m : monPokemonComplet.getMoveSet()) {
                if (m == null || m.getPower() <= 0) continue;
                rangs.add(construireRangMove(
                        m.getDisplayName().getString(), m.getPower(), m.getType(), m.getDamageCategory(),
                        monPokemonComplet.getAttack(), monPokemonComplet.getSpecialAttack(), monPokemonComplet.getLevel(),
                        defAdv, spdAdv, pvMaxAdvPourCalcul, typeNous1, typeNous2, typeAdv1, typeAdv2
                ));
            }

            rangs.add(new RangEspace());
            rangs.add(new RangTexte("Attaques probables adverses -> vous", 0xFF55FFFF));
            for (String nomMove : spread.topMoves) {
                MoveTemplate gabarit = MoveNameResolver.resoudre(nomMove);
                if (gabarit == null || gabarit.getPower() <= 0) continue;
                rangs.add(construireRangMove(
                        nomMove, gabarit.getPower(), gabarit.getElementalType(), gabarit.getDamageCategory(),
                        atkAdv, spaAdv, niveauAdverse,
                        monPokemonComplet.getDefence(), monPokemonComplet.getSpecialDefence(), monPokemonComplet.getMaxHealth(),
                        typeAdv1, typeAdv2, typeNous1, typeNous2
                ));
            }
        }

        dessinerPanneau(contexte, client, rangs);
    }

    private static RangMove construireRangMove(
            String nomMove, double puissance,
            ElementalType typeMoveCobblemon, DamageCategory categorie,
            int attaquePhysique, int attaqueSpeciale, int niveauAttaquant,
            int defensePhysiqueCible, int defenseSpecialeCible, int pvMaxCible,
            TypeChart.Type typeAttaquant1, TypeChart.Type typeAttaquant2,
            TypeChart.Type typeCible1, TypeChart.Type typeCible2
    ) {
        boolean physique = categorie == DamageCategories.INSTANCE.getPHYSICAL();
        int attaque = physique ? attaquePhysique : attaqueSpeciale;
        int defense = physique ? defensePhysiqueCible : defenseSpecialeCible;

        TypeChart.Type typeMove = TypeMapper.depuisCobblemon(typeMoveCobblemon);
        boolean stab = typeMove == typeAttaquant1 || typeMove == typeAttaquant2;
        double efficacite = TypeChart.getMultiplicateur(typeMove, typeCible1, typeCible2);

        if (efficacite == 0 || pvMaxCible <= 0) {
            return new RangMove(typeMove, nomMove, "aucun effet", 0xFF888888);
        }

        DamageCalculator.Resultat resultat = DamageCalculator.calculer(
                attaque, defense, (int) puissance, niveauAttaquant, pvMaxCible, stab, efficacite, 1.0, false, false
        );

        String texteResultat = String.format("%.0f-%.0f%%", resultat.pourcentMin, resultat.pourcentMax);
        int couleur = couleurSeverite(resultat.pourcentMin, resultat.pourcentMax);

        return new RangMove(typeMove, nomMove, texteResultat, couleur);
    }

    private static int couleurSeverite(double pourcentMin, double pourcentMax) {
        if (pourcentMin >= 100) return 0xFFFF5555;
        if (pourcentMax >= 100) return 0xFFFFAA00;
        if (pourcentMax >= 50) return 0xFFFFFFFF;
        return 0xFFAAAAAA;
    }

    private static int couleurBarreVie(double ratio) {
        if (ratio > 0.5) return 0xFF55DD55;
        if (ratio > 0.2) return 0xFFFFAA00;
        return 0xFFFF5555;
    }

    private static int hauteurDe(Rang rang) {
        if (rang instanceof RangTexte) return 11;
        if (rang instanceof RangNomTypes) return 13;
        if (rang instanceof RangBarreVie) return 12;
        if (rang instanceof RangMove) return 11;
        if (rang instanceof RangEspace) return 5;
        return 11;
    }

    private static void dessinerPanneau(DrawContext contexte, MinecraftClient client, List<Rang> rangs) {
        int x = 8;
        int y = 8;
        int largeurTexteDisponible = LARGEUR_PANNEAU - 12 - LARGEUR_BADGE * 2 - 6;

        int hauteurPanneau = 8;
        for (Rang rang : rangs) hauteurPanneau += hauteurDe(rang);

        contexte.fill(x, y, x + LARGEUR_PANNEAU, y + hauteurPanneau, 0xCC000000);

        int curseurY = y + 4;
        for (Rang rang : rangs) {
            int curseurX = x + 6;

            if (rang instanceof RangTexte rt) {
                String texte = client.textRenderer.trimToWidth(rt.texte(), LARGEUR_PANNEAU - 12);
                contexte.drawTextWithShadow(client.textRenderer, texte, curseurX, curseurY, rt.couleur());

            } else if (rang instanceof RangNomTypes rn) {
                String nom = client.textRenderer.trimToWidth(rn.nom(), largeurTexteDisponible);
                contexte.drawTextWithShadow(client.textRenderer, nom, curseurX, curseurY + 1, 0xFFFFFFFF);
                int xBadge = x + LARGEUR_PANNEAU - 6 - LARGEUR_BADGE;
                if (rn.type2() != null) {
                    dessinerBadgeType(contexte, client, xBadge, curseurY, rn.type2());
                    xBadge -= LARGEUR_BADGE + 3;
                }
                dessinerBadgeType(contexte, client, xBadge, curseurY, rn.type1());

            } else if (rang instanceof RangBarreVie rb) {
                int yBarre = curseurY + 2;
                contexte.fill(curseurX, yBarre, curseurX + LARGEUR_BARRE_VIE, yBarre + HAUTEUR_BARRE_VIE, 0xFF3A3A3A);
                int largeurRemplie = (int) (LARGEUR_BARRE_VIE * Math.max(0, Math.min(1, rb.ratio())));
                contexte.fill(curseurX, yBarre, curseurX + largeurRemplie, yBarre + HAUTEUR_BARRE_VIE, couleurBarreVie(rb.ratio()));
                contexte.drawTextWithShadow(client.textRenderer, rb.texteDroite(),
                        curseurX + LARGEUR_BARRE_VIE + 6, curseurY + 1, 0xFFDDDDDD);

            } else if (rang instanceof RangMove rm) {
                dessinerBadgeType(contexte, client, curseurX, curseurY, rm.type());
                int xTexte = curseurX + LARGEUR_BADGE + 4;
                String nom = client.textRenderer.trimToWidth(rm.nom(), largeurTexteDisponible - 40);
                contexte.drawTextWithShadow(client.textRenderer, nom, xTexte, curseurY + 1, 0xFFEEEEEE);
                String resultat = rm.resultat();
                int largeurResultat = client.textRenderer.getWidth(resultat);
                contexte.drawTextWithShadow(client.textRenderer, resultat,
                        x + LARGEUR_PANNEAU - 6 - largeurResultat, curseurY + 1, rm.couleurResultat());
            }
            // RangEspace : rien à dessiner, juste l'espace

            curseurY += hauteurDe(rang);
        }
    }

    private static void dessinerBadgeType(DrawContext contexte, MinecraftClient client, int x, int y, TypeChart.Type type) {
        contexte.fill(x, y, x + LARGEUR_BADGE, y + HAUTEUR_BADGE, TypeColors.getCouleurFond(type));
        String abrev = TypeColors.getAbreviation(type);
        int largeurTexte = client.textRenderer.getWidth(abrev);
        int xTexte = x + (LARGEUR_BADGE - largeurTexte) / 2;
        contexte.drawText(client.textRenderer, abrev, xTexte, y + 1, TypeColors.getCouleurTexte(type), false);
    }
}
