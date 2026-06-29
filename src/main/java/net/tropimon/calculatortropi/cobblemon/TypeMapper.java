package net.tropimon.calculatortropi.cobblemon;

import com.cobblemon.mod.common.api.types.ElementalType;
import net.tropimon.calculatortropi.calc.TypeChart;

public class TypeMapper {

    public static TypeChart.Type depuisCobblemon(ElementalType type) {
        return switch (type.getName().toLowerCase()) {
            case "normal" -> TypeChart.Type.NORMAL;
            case "fire" -> TypeChart.Type.FEU;
            case "water" -> TypeChart.Type.EAU;
            case "electric" -> TypeChart.Type.ELECTRIK;
            case "grass" -> TypeChart.Type.PLANTE;
            case "ice" -> TypeChart.Type.GLACE;
            case "fighting" -> TypeChart.Type.COMBAT;
            case "poison" -> TypeChart.Type.POISON;
            case "ground" -> TypeChart.Type.SOL;
            case "flying" -> TypeChart.Type.VOL;
            case "psychic" -> TypeChart.Type.PSY;
            case "bug" -> TypeChart.Type.INSECTE;
            case "rock" -> TypeChart.Type.ROCHE;
            case "ghost" -> TypeChart.Type.SPECTRE;
            case "dragon" -> TypeChart.Type.DRAGON;
            case "dark" -> TypeChart.Type.TENEBRES;
            case "steel" -> TypeChart.Type.ACIER;
            case "fairy" -> TypeChart.Type.FEE;
            default -> TypeChart.Type.NORMAL;
        };
    }
}
