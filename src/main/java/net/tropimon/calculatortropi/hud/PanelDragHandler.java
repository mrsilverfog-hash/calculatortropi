package net.tropimon.calculatortropi.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;

public class PanelDragHandler {

    private static int panelX = 8;
    private static int panelY = 90;

    private static boolean enTrainDeDeplacer = false;
    private static boolean boutonAppuyePrecedemment = false;
    private static double decalageX;
    private static double decalageY;

    public static int getX() {
        return panelX;
    }

    public static int getY() {
        return panelY;
    }

    /**
     * A appeler une fois par frame, juste avant de dessiner le panneau.
     */
    public static void mettreAJour(MinecraftClient client, int largeurPanneau, int hauteurPanneau) {
        Window fenetre = client.getWindow();
        long handle = fenetre.getHandle();

        boolean boutonAppuye = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

        double brutX = client.mouse.getX();
        double brutY = client.mouse.getY();
        double sourisX = brutX * fenetre.getScaledWidth() / fenetre.getWidth();
        double sourisY = brutY * fenetre.getScaledHeight() / fenetre.getHeight();

        if (boutonAppuye && !boutonAppuyePrecedemment) {
            boolean dansLePanneau = sourisX >= panelX && sourisX <= panelX + largeurPanneau
                    && sourisY >= panelY && sourisY <= panelY + hauteurPanneau;
            if (dansLePanneau) {
                enTrainDeDeplacer = true;
                decalageX = sourisX - panelX;
                decalageY = sourisY - panelY;
            }
        }

        if (!boutonAppuye) {
            enTrainDeDeplacer = false;
        }

        if (enTrainDeDeplacer) {
            panelX = (int) Math.round(sourisX - decalageX);
            panelY = (int) Math.round(sourisY - decalageY);

            panelX = Math.max(0, Math.min(panelX, fenetre.getScaledWidth() - largeurPanneau));
            panelY = Math.max(0, Math.min(panelY, fenetre.getScaledHeight() - hauteurPanneau));
        }

        boutonAppuyePrecedemment = boutonAppuye;
    }
}
