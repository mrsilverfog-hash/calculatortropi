package net.tropimon.calculatortropi.cobblemon;

import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;

public class MoveNameResolver {

    // Convertit "Iron Head", "U-turn", "Will-O-Wisp" -> "ironhead", "uturn", "willowisp"
    // (convention probable des identifiants internes Cobblemon/Showdown).
    public static MoveTemplate resoudre(String nomAffiche) {
        String cle = normaliser(nomAffiche);
        return Moves.getByNameOrDummy(cle);
    }

    private static String normaliser(String nom) {
        return nom.toLowerCase()
                .replace(" ", "")
                .replace("-", "")
                .replace("'", "")
                .replace(".", "");
    }
}
