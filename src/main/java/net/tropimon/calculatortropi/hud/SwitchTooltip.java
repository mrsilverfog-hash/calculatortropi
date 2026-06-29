package net.tropimon.calculatortropi.hud;

import com.cobblemon.mod.common.client.gui.battle.BattleGUI;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleActionSelection;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleSwitchPokemonSelection;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.tropimon.calculatortropi.calc.TypeChart;
import net.tropimon.calculatortropi.cobblemon.TypeMapper;

import java.util.ArrayList;
import java.util.List;

public class SwitchTooltip {

    private static final int LARGEUR_BADGE = 24;
    private static final int HAUTEUR_BADGE = 10;
    private static final int LARGEUR_PANNEAU = 220;
    private static final int MARGE = 8;

    public static void dessiner(DrawContext contexte, MinecraftClient client, BattleGUI battleGUI) {
        try {
            BattleActionSelection selection = battleGUI.getCurrentActionSelection();
            if (!(selection instanceof BattleSwitchPokemonSelection switchSelection)) return;

            OpponentContext ctx = OpponentContext.detecter();

            int xPanneau = switchSelection.getX() + switchSelection.getWidth() + MARGE;
            int yPanneau = switchSelection.getY();

            // --- BANNIERE DEBUG TEMPORAIRE (à retirer une fois le souci identifié) ---
            String debug = String.format(
                    "DEBUG widget x=%d y=%d w=%d h=%d | ctx=%s spread=%s",
                    switchSelection.getX(), switchSelection.getY(),
                    switchSelection.getWidth(), switchSelection.getHeight(),
                    ctx != null, ctx != null && ctx.spread != null
            );
            contexte.fill(8, 300, 8 + client.textRenderer.getWidth(debug) + 8, 315, 0xEEFFAA00);
            contexte.drawText(client.textRenderer, debug, 12, 303, 0xFF000000, false);
            // --- FIN BANNIERE DEBUG ---

            Pokemon survole = null;
            for (BattleSwitchPokemonSelection.SwitchTile tuile : switchSelection.getTiles()) {
                if (tuile.isHovered(
                        client.mouse.getX() * client.getWindow().getScaledWidth() / client.getWindow().getWidth(),
                        client.mouse.getY() * client.getWindow().getScaledHeight() / client.getWindow().getHeight()
                )) {
                    survole = tuile.getPokemon();
                    break;
                }
            }

            if (ctx == null || ctx.spread == null) {
                dessinerPlaceholder(contexte, client, xPanneau, yPanneau, "Pas de spread adverse disponible.");
                return;
            }

            if (survole == null) {
                dessinerPlaceholder(contexte, client, xPanneau, yPanneau, "Survole un Pokémon...");
            } else {
                dessinerMatchup(contexte, client, survole, ctx, xPanneau, yPanneau);
            }

        } catch (Exception e) {
            String erreur = "Erreur SwitchTooltip: " + e;
            contexte.fill(8, 320, 8 + client.textRenderer.getWidth(erreur) + 8, 335, 0xEEFF0000);
            contexte.drawText(client.textRenderer, erreur, 12, 323, 0xFFFFFFFF, false);
        }
    }

    private static void dessinerPlaceholder(DrawContext contexte, MinecraftClient client, int x, int y, String texte) {
        int largeur = Math.max(LARGEUR_PANNEAU, client.textRenderer.getWidth(texte) + 12);
        contexte.fill(x, y, x + largeur, y + 19, 0xEE000000);
        contexte.drawTextWithShadow(client.textRenderer, texte, x + 6, y + 6, 0xFFAAAAAA);
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
