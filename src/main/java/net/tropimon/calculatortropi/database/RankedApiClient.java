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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RankedApiClient {

    private static final String SAISON = "Season 4";
    private static final String FORMAT = "SINGLES";

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private static final ExecutorService EXECUTEUR = Executors.newFixedThreadPool(2, runnable -> {
        Thread t = new Thread(runnable, "Calculatortropi-RankedAPI");
        t.setDaemon(true);
        return t;
    });

    private enum Etat { EN_COURS, REUSSI, ECHEC }

    private static final Map<String, Etat> ETATS = new ConcurrentHashMap<>();
    private static final Map<String, SpreadEntry> CACHE = new ConcurrentHashMap<>();

    /**
     * Non bloquant : renvoie ce qu'on a déjà en cache (ou null si pas encore
     * prêt), et déclenche une recherche en tâche de fond si besoin.
     * A utiliser depuis le rendu HUD (60fps), jamais d'appel réseau direct ici.
     */
    public static SpreadEntry obtenirSiDisponible(String nomEspece) {
        String cle = nomEspece.toLowerCase();
        if (ETATS.get(cle) == null) {
            ETATS.put(cle, Etat.EN_COURS);
            EXECUTEUR.submit(() -> rechercher(nomEspece, cle));
        }
        return CACHE.get(cle);
    }

    public static boolean estEnCoursDeChargement(String nomEspece) {
        return ETATS.get(nomEspece.toLowerCase()) == Etat.EN_COURS;
    }

    /** Version bloquante : gardée pour /calcspread, une commande ponctuelle, pas le rendu. */
    public static SpreadEntry obtenir(String nomEspece) {
        String cle = nomEspece.toLowerCase();
        if (CACHE.containsKey(cle)) return CACHE.get(cle);
        if (ETATS.get(cle) == Etat.ECHEC) return null;
        rechercher(nomEspece, cle);
        return CACHE.get(cle);
    }

    private static void rechercher(String nomEspece, String cle) {
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
                ETATS.put(cle, Etat.ECHEC);
                return;
            }

            SpreadEntry entree = analyser(reponse.body());
            if (entree != null) {
                CACHE.put(cle, entree);
                ETATS.put(cle, Etat.REUSSI);
            } else {
                ETATS.put(cle, Etat.ECHEC);
            }

        } catch (IOException | InterruptedException | RuntimeException e) {
            CalculatortropiClient.LOGGER.warn("[Calculatortropi] Erreur API ranked pour " + nomEspece + " : " + e.getMessage());
            ETATS.put(cle, Etat.ECHEC);
        }
    }

    private static SpreadEntry analyser(String json) {
        JsonObject racine = JsonParser.parseString(json).getAsJsonObject();

        String objet = trouverCleMax(racine.getAsJsonObject("items"));
        String natureTexte = trouverCleMax(racine.getAsJsonObject("natures"));
        String talent = trouverCleMax(racine.getAsJsonObject("abilities"));
        String spreadTexte = trouverCleMax(racine.getAsJsonObject("spreads"));
        List<String> topMoves = trouverTopCles(racine.getAsJsonObject("moves"), 6);

        Nature nature = Nature.HARDY;
        if (natureTexte != null) {
            try {
                nature = Nature.valueOf(natureTexte.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        int[] ev = analyserSpread(spreadTexte);

        return new SpreadEntry(objet, nature, ev[0], ev[1], ev[2], ev[3], ev[4], ev[5], talent, topMoves);
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

    private static List<String> trouverTopCles(JsonObject objetJson, int max) {
        List<String> resultat = new ArrayList<>();
        if (objetJson == null) return resultat;

        List<Map.Entry<String, Double>> entrees = new ArrayList<>();
        for (String cle : objetJson.keySet()) {
            entrees.add(Map.entry(cle, objetJson.get(cle).getAsDouble()));
        }
        entrees.sort(Comparator.comparingDouble((Map.Entry<String, Double> e) -> e.getValue()).reversed());

        for (int i = 0; i < Math.min(max, entrees.size()); i++) {
            resultat.add(entrees.get(i).getKey());
        }
        return resultat;
    }

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
