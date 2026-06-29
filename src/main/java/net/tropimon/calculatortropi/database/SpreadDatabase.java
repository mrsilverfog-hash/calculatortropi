package net.tropimon.calculatortropi.database;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.tropimon.calculatortropi.CalculatortropiClient;
import net.tropimon.calculatortropi.calc.Nature;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class SpreadDatabase {

    private static final Map<String, SpreadEntry> ENTREES = new HashMap<>();
    private static boolean chargee = false;

    public static void charger() {
        if (chargee) return;
        chargee = true;

        try (InputStream flux = SpreadDatabase.class.getClassLoader()
                .getResourceAsStream("calculatortropi/spreads.json")) {

            if (flux == null) {
                CalculatortropiClient.LOGGER.warn("[Calculatortropi] spreads.json introuvable dans les ressources.");
                return;
            }

            JsonObject racine = JsonParser.parseReader(
                    new InputStreamReader(flux, StandardCharsets.UTF_8)
            ).getAsJsonObject();

            for (String nomEspece : racine.keySet()) {
                JsonObject entree = racine.getAsJsonObject(nomEspece);

                String objet = entree.has("objet") ? entree.get("objet").getAsString() : null;
                String natureTexte = entree.has("nature") ? entree.get("nature").getAsString() : "HARDY";
                Nature nature;
                try {
                    nature = Nature.valueOf(natureTexte.toUpperCase());
                } catch (IllegalArgumentException e) {
                    nature = Nature.HARDY;
                }

                JsonObject ev = entree.has("ev") ? entree.getAsJsonObject("ev") : new JsonObject();
                int evPv = ev.has("pv") ? ev.get("pv").getAsInt() : 0;
                int evAtk = ev.has("atk") ? ev.get("atk").getAsInt() : 0;
                int evDef = ev.has("def") ? ev.get("def").getAsInt() : 0;
                int evSpa = ev.has("spa") ? ev.get("spa").getAsInt() : 0;
                int evSpd = ev.has("spd") ? ev.get("spd").getAsInt() : 0;
                int evSpe = ev.has("spe") ? ev.get("spe").getAsInt() : 0;

                String talent = entree.has("talent") ? entree.get("talent").getAsString() : null;

                ENTREES.put(nomEspece.toLowerCase(),
                        new SpreadEntry(objet, nature, evPv, evAtk, evDef, evSpa, evSpd, evSpe, talent));
            }

            CalculatortropiClient.LOGGER.info("[Calculatortropi] " + ENTREES.size() + " spreads chargés depuis spreads.json");

        } catch (IOException | RuntimeException e) {
            CalculatortropiClient.LOGGER.error("[Calculatortropi] Erreur de chargement de spreads.json", e);
        }
    }

    public static SpreadEntry trouver(String nomEspece) {
        if (!chargee) charger();
        return ENTREES.get(nomEspece.toLowerCase());
    }
}
