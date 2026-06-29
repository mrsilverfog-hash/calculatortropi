package net.tropimon.calculatortropi.hud;

import com.cobblemon.mod.common.client.gui.battle.BattleGUI;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleActionSelection;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleSwitchPokemonSelection;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.tropimon.calculatortropi.calc.TypeChart;
import net.tropimon.calculatortropi.cobblemon.TypeMapper;

import java.util.ArrayList;
import java.util.List;

public class SwitchTooltip {

    private static final int LARGEUR_BADGE = 24;
    private static final int HAUTEUR_BADGE = 10;
    private static final int LARGEUR_PANNEAU = 220;
    private static final int MARGE = 50;

    // Taille réelle d'une case affichée (constantes Cobblemon: SELECT_WIDTH * SCALE)
    private static final float LARGEUR_CASE = 94f * 0.5f;

    public static void dessiner(DrawContext contexte, MinecraftClient client, BattleGUI battleGUI) {
        try {
            BattleActionSelection selection = battleGUI.getCurrentActionSelection();
            if (!(selection instanceof BattleSwitchPokemonSelection switchSelection)) return;

            List<BattleSwitchPokemonSelection.SwitchTile> tuiles = switchSelection.getTiles();
            if (tuiles.isEmpty()) return;

            // On calcule la position réelle du groupe de cases visibles
            // (et non celle du conteneur invisible, qui couvre tout l'écran),
            // pour ancrer notre panneau juste à droite, une fois pour toutes.
            float xDroite = Float.MIN_VALUE;
            float yHaut = Float.MAX_VALUE;
            for (BattleSwitchPokemonSelection.SwitchTile tuile : tuiles) {
                xDroite = Math.max(xDroite, tuile.getX() + LARGEUR_CASE);
                yHaut = Math.min(yHaut, tuile.getY());
            }

            int xPanneau = (int) xDroite + MARGE;
            int yPanneau = (int) yHaut;

            OpponentContext ctx = OpponentContext.detecter();

            double sourisX = client.mouse.getX() * client.getWindow().getScaledWidth() / client.getWindow().getWidth();
            double sourisY = client.mouse.getY() * client.getWindow().getScaledHeight() / client.getWindow().getHeight();

            Pokemon survole = null;
            for (BattleSwitchPokemonSelection.SwitchTile tuile : tuiles) {
                if (tuile.isHovered(sourisX, sourisY)) {
                    survole = tuile.getPokemon();
                    break;
                }
            }

            if (ctx == null || ctx.spread == null) return;

            if (survole != null) {
                dessinerMatchup(contexte, client, survole, ctx, xPanneau, yPanneau);
            }

        } catch (Exception e) {
            String erreur = "Erreur SwitchTooltip: " + e;
            contexte.fill(8, 320, 8 + client.textRenderer.getWidth(erreur) + 8, 335, 0xEEFF0000);
            contexte.drawText(client.textRenderer, erreur, 12, 323, 0xFFFFFFFF, false);
        }
    }

    private static void dessinerMatchup(
            DrawContext contexte, MinecraftClient client, Pokemon candidat,
            OpponentContext ctx, int x, int y
    ) {
        TypeChart.Type typeP1 = TypeMapper.depuisCobblemon(candidat.getPrimaryType());
        TypeChart.Type typeP2 = candidat.getSecondaryType() != null
                ? TypeMapper.depuisCobblemon(candidat.getSecondaryType()) : null;

        List<Matchup.Ligne> versAdversaire = Matchup.versCible(
                candidat, typeP1, typeP2, ctx.def, ctx.spd, ctx.getPvMaxPourCalcul(), ctx.type1, ctx.type2
        );
        List<Matchup.Ligne> depuisAdversaire = Matchup.depuisAdversaire(
                ctx.spread.topMoves, ctx.atk, ctx.spa, ctx.niveau, ctx.type1, ctx.type2,
                candidat, typeP1, typeP2
        );

        List<String> texte = new ArrayList<>();
        List<Integer> couleur = new ArrayList<>();
        List<TypeChart.Type> typeLigne = new ArrayList<>();

        texte.add(candidat.getSpecies().getName() + " (Nv." + candidat.getLevel() + ")");
        couleur.add(0xFFFFFFFF);
        typeLigne.add(null);

        texte.add("Contre " + ctx.espece.getName() + " :");
        couleur.add(0xFF55FFFF);
        typeLigne.add(null);

        for (Matchup.Ligne l : versAdversaire) {
            texte.add(l.nom() + "  " + l.resultat());
            couleur.add(l.couleurResultat());
            typeLigne.add(l.type());
        }

        texte.add("Subirait :");
        couleur.add(0xFF55FFFF);
        typeLigne.add(null);

        for (Matchup.Ligne l : depuisAdversaire) {
            texte.add(l.nom() + "  " + l.resultat());
            couleur.add(l.couleurResultat());
            typeLigne.add(l.type());
        }

        int largeurMax = 0;
        for (int i = 0; i < texte.size(); i++) {
            int largeur = client.textRenderer.getWidth(texte.get(i)) + (typeLigne.get(i) != null ? LARGEUR_BADGE + 4 : 0);
            largeurMax = Math.max(largeurMax, largeur);
        }

        int largeurBoite = Math.max(LARGEUR_PANNEAU, largeurMax + 12);
        int hauteurLigne = 11;
        int hauteurBoite = texte.size() * hauteurLigne + 8;

        contexte.fill(x, y, x + largeurBoite, y + hauteurBoite, 0xEE000000);

        int curseurY = y + 4;
        for (int i = 0; i < texte.size(); i++) {
            int curseurX = x + 6;
            TypeChart.Type type = typeLigne.get(i);
            if (type != null) {
                contexte.fill(curseurX, curseurY, curseurX + LARGEUR_BADGE, curseurY + HAUTEUR_BADGE, TypeColors.getCouleurFond(type));
                String abrev = TypeColors.getAbreviation(type);
                int largeurTexte = client.textRenderer.getWidth(abrev);
                contexte.drawText(client.textRenderer, abrev,
                        curseurX + (LARGEUR_BADGE - largeurTexte) / 2, curseurY + 1, TypeColors.getCouleurTexte(type), false);
                curseurX += LARGEUR_BADGE + 4;
            }
            contexte.drawTextWithShadow(client.textRenderer, texte.get(i), curseurX, curseurY + 1, couleur.get(i));
            curseurY += hauteurLigne;
        }
    }
}
