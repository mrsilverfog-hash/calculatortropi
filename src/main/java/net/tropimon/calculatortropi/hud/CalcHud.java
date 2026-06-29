package net.tropimon.calculatortropi.hud;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.gui.battle.BattleGUI;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.tropimon.calculatortropi.calc.TypeChart;
import net.tropimon.calculatortropi.cobblemon.TypeMapper;

import java.util.ArrayList;
import java.util.List;

public class CalcHud {

    private static final int LARGEUR_PANNEAU = 300;
    private static final int LARGEUR_BADGE = 24;
    private static final int HAUTEUR_BADGE = 10;
    private static final int LARGEUR_BARRE_VIE = 110;
    private static final int HAUTEUR_BARRE_VIE = 6;

    private interface Rang {}

    private record RangTexte(String texte, int couleur) implements Rang {}
    private record RangNomTypes(String nom, TypeChart.Type type1, TypeChart.Type type2) implements Rang {}
    private record RangBarreVie(double ratio, String texteDroite) implements Rang {}
    private record RangMove(TypeChart.Type type, String nom, String resultat, int couleurResultat) implements Rang {}
    private record RangEspace() implements Rang {}

    public static void enregistrer() {
        HudRenderCallback.EVENT.register(CalcHud::dessinerViaHud);
    }

    private static void dessinerViaHud(DrawContext contexte, RenderTickCounter compteur) {
        MinecraftClient client = MinecraftClient.getInstance();
        // Quand l'écran de combat (menu Combat/Equipe) est ouvert, c'est
        // BattleScreenOverlay qui dessine le panneau APRES cet écran, pour
        // ne pas être recouvert par ses propres éléments (cases d'équipe...).
        if (client.currentScreen instanceof BattleGUI) return;
        dessiner(contexte, client);
    }

    public static void dessiner(DrawContext contexte, MinecraftClient client) {
        if (client.player == null) return;

        OpponentContext ctx = OpponentContext.detecter();
        if (ctx == null) return;

        Pokemon monPokemonComplet = trouverMonActif(ctx);
        if (monPokemonComplet == null) return;

        List<Rang> rangs = construireRangs(ctx, monPokemonComplet);
        dessinerPanneau(contexte, client, rangs);
    }

    private static Pokemon trouverMonActif(OpponentContext ctx) {
        for (Pokemon p : CobblemonClient.INSTANCE.getStorage().getParty()) {
            if (p != null && p.getUuid().equals(ctx.monUuidActif)) {
                return p;
            }
        }
        return null;
    }

    private static List<Rang> construireRangs(OpponentContext ctx, Pokemon monPokemonComplet) {
        List<Rang> rangs = new ArrayList<>();
        rangs.add(new RangTexte("=== Calculatortropi ===", 0xFF55FFFF));

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

        rangs.add(new RangNomTypes(
                ctx.espece.getName() + " (Nv." + ctx.niveau + ")",
                ctx.type1, ctx.type2
        ));

        String texteVieAdverse = ctx.pvExacts
                ? String.format("%.0f/%.0f PV", ctx.actifAdverse.getHpValue(), ctx.pvMaxReel)
                : String.format("~%.0f/%.0f PV (est.)", ctx.ratioPv * ctx.pvMaxReel, ctx.pvMaxReel);
        rangs.add(new RangBarreVie(ctx.ratioPv, texteVieAdverse));

        rangs.add(new RangEspace());

        if (ctx.spread == null) {
            rangs.add(new RangTexte(
                    ctx.spreadEnChargement ? "Recherche de la spread adverse..." : "Spread adverse inconnue.",
                    0xFFAAAAAA
            ));
            return rangs;
        }

        rangs.add(new RangTexte("Objet: " + ctx.spread.objet + " | Talent: " + ctx.spread.talent, 0xFFAAAAAA));
        rangs.add(new RangEspace());

        rangs.add(new RangTexte("Vos capacités -> adversaire", 0xFF55FFFF));
        for (Matchup.Ligne ligne : Matchup.versCible(
                monPokemonComplet, typeNous1, typeNous2,
                ctx.def, ctx.spd, ctx.getPvMaxPourCalcul(), ctx.type1, ctx.type2
        )) {
            rangs.add(new RangMove(ligne.type(), ligne.nom(), ligne.resultat(), ligne.couleurResultat()));
        }

        rangs.add(new RangEspace());
        rangs.add(new RangTexte("Attaques probables adverses -> vous", 0xFF55FFFF));
        for (Matchup.Ligne ligne : Matchup.depuisAdversaire(
                ctx.spread.topMoves, ctx.atk, ctx.spa, ctx.niveau, ctx.type1, ctx.type2,
                monPokemonComplet, typeNous1, typeNous2
        )) {
            rangs.add(new RangMove(ligne.type(), ligne.nom(), ligne.resultat(), ligne.couleurResultat()));
        }

        return rangs;
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
        int largeurTexteDisponible = LARGEUR_PANNEAU - 12 - LARGEUR_BADGE * 2 - 6;

        int hauteurPanneau = 8;
        for (Rang rang : rangs) hauteurPanneau += hauteurDe(rang);

        PanelDragHandler.mettreAJour(client, LARGEUR_PANNEAU, hauteurPanneau);
        int x = PanelDragHandler.getX();
        int y = PanelDragHandler.getY();

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
