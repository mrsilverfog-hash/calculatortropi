package net.tropimon.calculatortropi.database;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.tropimon.calculatortropi.CalculatortropiClient;
import net.tropimon.calculatortropi.calc.Nature;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RankedApiClient {

    // A mettre a jour a chaque nouvelle saison du ranked Tropimon
    private static final String SAISON = "Season 4";
    private static final String FORMAT = "SINGLES";

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    // On garde en mémoire ce qu'on a déjà demandé pour ne pas re-appeler
    // l'API à chaque /calcopponent sur le même Pokémon dans la session.
    private static final Map<String, SpreadEntry> CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> ECHECS_CONNUS = new ConcurrentHashMap<>();

    public static SpreadEntry obtenir(String nomEspece) {
        String cle = nomEspece.toLowerCase();

        if (CACHE.containsKey(cle)) {
            return CACHE.get(cle);
        }
        if (ECHECS_CONNUS.containsKey(cle)) {
            return null;
        }

        try {
            String url = "https://rankedapi.tropimon.fr/api/species"
                    + "?season=" + URLEncoder.encode(SAISON, StandardCharsets.UTF_8)
                    + "&format=" + FORMAT
                    + "&tier=all"
                    + "&name=" + URLEncoder.encode(nomEspece, StandardCharsets.UTF_8);

            HttpRequest requete = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> reponse = CLIENT.send(requete, HttpResponse.BodyHandlers.ofString());

            if (reponse.statusCode() != 200) {
                CalculatortropiClient.LOGGER.warn("[Calculatortropi] API ranked code " + reponse.statusCode() + " pour " + nomEspece);
                ECHECS_CONNUS.put(cle, true);
                return null;
            }

            SpreadEntry entree = analyser(reponse.body());
            if (entree != null) {
                CACHE.put(cle, entree);
            } else {
                ECHECS_CONNUS.put(cle, true);
            }
            return entree;

        } catch (IOException | InterruptedException | RuntimeException e) {
            CalculatortropiClient.LOGGER.warn("[Calculatortropi] Erreur API ranked pour " + nomEspece + " : " + e.getMessage());
            ECHECS_CONNUS.put(cle, true);
            return null;
        }
    }

    private static SpreadEntry analyser(String json) {
        JsonObject racine = JsonParser.parseString(json).getAsJsonObject();

        String objet = trouverCleMax(racine.getAsJsonObject("items"));
        String natureTexte = trouverCleMax(racine.getAsJsonObject("natures"));
        String talent = trouverCleMax(racine.getAsJsonObject("abilities"));
        String spreadTexte = trouverCleMax(racine.getAsJsonObject("spreads"));

        Nature nature = Nature.HARDY;
        if (natureTexte != null) {
            try {
                nature = Nature.valueOf(natureTexte.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        int[] ev = analyserSpread(spreadTexte);

        return new SpreadEntry(objet, nature, ev[0], ev[1], ev[2], ev[3], ev[4], ev[5], talent);
    }

    private static String trouverCleMax(JsonObject objetJson) {
        if (objetJson == null) return null;
        String meilleureCle = null;
        double meilleureValeur = -1;
        for (String cle : objetJson.keySet()) {
            double valeur = objetJson.get(cle).getAsDouble();
            if (valeur > meilleureValeur) {
                meilleureValeur = valeur;
                meilleureCle = cle;
            }
        }
        return meilleureCle;
    }

    // Parse "252 Atk / 6 SpD / 252 Spe" -> [pv, atk, def, spa, spd, spe]
    private static int[] analyserSpread(String texte) {
        int[] ev = new int[6];
        if (texte == null) return ev;

        for (String morceau : texte.split("/")) {
            String[] parties = morceau.trim().split("\\s+");
            if (parties.length != 2) continue;

            int valeur;
            try {
                valeur = Integer.parseInt(parties[0].trim());
            } catch (NumberFormatException e) {
                continue;
            }

            switch (parties[1].trim().toLowerCase()) {
                case "hp" -> ev[0] = valeur;
                case "atk" -> ev[1] = valeur;
                case "def" -> ev[2] = valeur;
                case "spa" -> ev[3] = valeur;
                case "spd" -> ev[4] = valeur;
                case "spe" -> ev[5] = valeur;
                default -> {}
            }
        }
        return ev;
    }
}
