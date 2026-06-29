package net.tropimon.calculatortropi.hud;

import com.cobblemon.mod.common.client.gui.battle.BattleGUI;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleActionSelection;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleSwitchPokemonSelection;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import net.tropimon.calculatortropi.calc.TypeChart;
import net.tropimon.calculatortropi.cobblemon.TypeMapper;

import java.util.ArrayList;
import java.util.List;

public class SwitchTooltip {

    private static final int LARGEUR_BADGE = 24;
    private static final int HAUTEUR_BADGE = 10;

    public static void enregistrer() {
        HudRenderCallback.EVENT.register(SwitchTooltip::dessiner);
    }

    private static void dessiner(DrawContext contexte, RenderTickCounter compteur) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!(client.currentScreen instanceof BattleGUI battleGUI)) return;

        BattleActionSelection selection = battleGUI.getCurrentActionSelection();
        if (!(selection instanceof BattleSwitchPokemonSelection switchSelection)) return;

        OpponentContext ctx = OpponentContext.detecter();
        if (ctx == null || ctx.spread == null) return;

        Window fenetre = client.getWindow();
        double sourisX = client.mouse.getX() * fenetre.getScaledWidth() / fenetre.getWidth();
        double sourisY = client.mouse.getY() * fenetre.getScaledHeight() / fenetre.getHeight();

        for (BattleSwitchPokemonSelection.SwitchTile tuile : switchSelection.getTiles()) {
            if (!tuile.isHovered(sourisX, sourisY)) continue;

            Pokemon candidat = tuile.getPokemon();
            if (candidat == null) continue;

            dessinerTooltip(contexte, client, candidat, ctx, sourisX, sourisY);
            return;
        }
    }

    private static void dessinerTooltip(
            DrawContext contexte, MinecraftClient client, Pokemon candidat,
            OpponentContext ctx, double sourisX, double sourisY
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

        int largeurBoite = largeurMax + 12;
        int hauteurLigne = 11;
        int hauteurBoite = texte.size() * hauteurLigne + 8;

        int x = (int) sourisX + 14;
        int y = (int) sourisY;

        int largeurEcran = client.getWindow().getScaledWidth();
        int hauteurEcran = client.getWindow().getScaledHeight();
        if (x + largeurBoite > largeurEcran) x = largeurEcran - largeurBoite - 4;
        if (y + hauteurBoite > hauteurEcran) y = hauteurEcran - hauteurBoite - 4;

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
