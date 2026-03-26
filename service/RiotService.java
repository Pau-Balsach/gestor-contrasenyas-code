package com.mycompany.gestorcontrasenyas.service;

import com.mycompany.gestorcontrasenyas.utils.Config;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RiotService {

    private static final String HENRIK_KEY = Config.get("henrik.api_key");
    private static final String API_KEY    = Config.get("riot.api_key");
    private static final String ROUTING    = "europe";
    private static final String REGION     = "euw1";

    public static String obtenerPuuid(String riotId) {
        try {
            String[] partes = riotId.split("#");
            if (partes.length != 2) return null;

            String gameName = java.net.URLEncoder.encode(partes[0], "UTF-8");
            String tagLine  = java.net.URLEncoder.encode(partes[1], "UTF-8");

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://" + ROUTING + ".api.riotgames.com/riot/account/v1/accounts/by-riot-id/" + gameName + "/" + tagLine))
                    .header("X-Riot-Token", API_KEY)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) return extractJson(response.body(), "puuid");
            return null;

        } catch (Exception e) {
            System.out.println("Error obteniendo puuid.");
            return null;
        }
    }

    /**
     * Obtiene el rango de Valorant usando el endpoint v3 de HenrikDev.
     * Si el rango actual es Unrated, busca en el historial de temporadas
     * el rango mas reciente conocido y lo devuelve con la temporada entre parentesis.
     */
    public static String obtenerRangoValorant(String riotId) {
        try {
            String[] partes = riotId.split("#");
            if (partes.length != 2) return "Sin rango";

            String gameName = java.net.URLEncoder.encode(partes[0], "UTF-8");
            String tagLine  = java.net.URLEncoder.encode(partes[1], "UTF-8");

            HttpClient client = HttpClient.newHttpClient();

            // 1. v3/mmr
            HttpRequest req1 = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.henrikdev.xyz/valorant/v3/mmr/eu/pc/" + gameName + "/" + tagLine))
                    .header("Accept", "application/json")
                    .header("Authorization", HENRIK_KEY)
                    .GET().build();

            HttpResponse<String> res1 = client.send(req1, HttpResponse.BodyHandlers.ofString());
            System.out.println("[DEBUG MMR-V3] " + riotId + " HTTP=" + res1.statusCode());
            System.out.println("[DEBUG MMR-V3 BODY] " + res1.body());

            if (res1.statusCode() == 200) {
                com.google.gson.JsonObject root = com.google.gson.JsonParser.parseString(res1.body()).getAsJsonObject();
                System.out.println("[DEBUG MMR-V3 KEYS] " + root.keySet());

                if (root.has("data") && !root.get("data").isJsonNull()) {
                    com.google.gson.JsonObject data = root.getAsJsonObject("data");
                    System.out.println("[DEBUG MMR-V3 DATA KEYS] " + data.keySet());

                    // current
                    int curId = -1; String curName = null;
                    if (data.has("current") && !data.get("current").isJsonNull()) {
                        com.google.gson.JsonObject cur = data.getAsJsonObject("current");
                        System.out.println("[DEBUG current] " + cur);
                        if (cur.has("tier") && !cur.get("tier").isJsonNull()) {
                            com.google.gson.JsonObject t = cur.getAsJsonObject("tier");
                            curId = t.has("id") ? t.get("id").getAsInt() : -1;
                            curName = t.has("name") ? t.get("name").getAsString() : null;
                        }
                    }
                    System.out.println("[DEBUG 1] current → id=" + curId + " name=" + curName);
                    if (esRangoValido(curId, curName)) return curName;

                    // seasonal
                    if (data.has("seasonal") && !data.get("seasonal").isJsonNull()) {
                        com.google.gson.JsonArray seasonal = data.getAsJsonArray("seasonal");
                        System.out.println("[DEBUG 2] seasonal.size=" + seasonal.size());
                        for (int i = seasonal.size() - 1; i >= 0; i--) {
                            com.google.gson.JsonObject s = seasonal.get(i).getAsJsonObject();
                            System.out.println("[DEBUG 2] seasonal[" + i + "] keys=" + s.keySet() + " val=" + s);
                            String etq = obtenerEtiquetaTemporada(s);

                            if (s.has("end_tier") && !s.get("end_tier").isJsonNull()) {
                                com.google.gson.JsonObject et = s.getAsJsonObject("end_tier");
                                int etId = et.has("id") ? et.get("id").getAsInt() : -1;
                                String etName = et.has("name") ? et.get("name").getAsString() : null;
                                System.out.println("[DEBUG 2a] end_tier id=" + etId + " name=" + etName);
                                if (esRangoValido(etId, etName)) return etName + etq;
                            } else {
                                System.out.println("[DEBUG 2a] end_tier missing");
                            }

                            if (s.has("act_wins") && !s.get("act_wins").isJsonNull()) {
                                com.google.gson.JsonArray aw = s.getAsJsonArray("act_wins");
                                System.out.println("[DEBUG 2b] act_wins.size=" + aw.size());
                                int bestId = -1; String bestName = null;
                                for (com.google.gson.JsonElement we : aw) {
                                    com.google.gson.JsonObject w = we.getAsJsonObject();
                                    int wId = w.has("id") ? w.get("id").getAsInt() : -1;
                                    String wName = w.has("name") ? w.get("name").getAsString() : null;
                                    System.out.println("[DEBUG 2b] act_win id=" + wId + " name=" + wName);
                                    if (esRangoValido(wId, wName) && wId > bestId) { bestId = wId; bestName = wName; }
                                }
                                if (bestName != null) return bestName + etq;
                            } else {
                                System.out.println("[DEBUG 2b] act_wins missing");
                            }
                        }
                    } else {
                        System.out.println("[DEBUG 2] seasonal missing/null");
                    }

                    // peak
                    if (data.has("peak") && !data.get("peak").isJsonNull()) {
                        com.google.gson.JsonObject peak = data.getAsJsonObject("peak");
                        System.out.println("[DEBUG 3] peak=" + peak);
                        if (peak.has("tier") && !peak.get("tier").isJsonNull()) {
                            com.google.gson.JsonObject t = peak.getAsJsonObject("tier");
                            int pId = t.has("id") ? t.get("id").getAsInt() : -1;
                            String pName = t.has("name") ? t.get("name").getAsString() : null;
                            System.out.println("[DEBUG 3] peak.tier id=" + pId + " name=" + pName);
                            if (esRangoValido(pId, pName)) {
                                String ps = "";
                                if (peak.has("season") && !peak.get("season").isJsonNull()) {
                                    com.google.gson.JsonObject season = peak.getAsJsonObject("season");
                                    if (season.has("short") && !season.get("short").isJsonNull())
                                        ps = " (" + season.get("short").getAsString() + ")";
                                }
                                return pName + ps;
                            }
                        }
                    } else {
                        System.out.println("[DEBUG 3] peak missing/null");
                    }
                }
            }

            // 4. mmr-history v2
            HttpRequest req2 = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.henrikdev.xyz/valorant/v2/mmr-history/eu/pc/" + gameName + "/" + tagLine))
                    .header("Accept", "application/json")
                    .header("Authorization", HENRIK_KEY)
                    .GET().build();

            HttpResponse<String> res2 = client.send(req2, HttpResponse.BodyHandlers.ofString());
            System.out.println("[DEBUG MMR-HIST] " + riotId + " HTTP=" + res2.statusCode());
            System.out.println("[DEBUG MMR-HIST BODY] " + res2.body());

            if (res2.statusCode() == 200) {
                com.google.gson.JsonObject hRoot = com.google.gson.JsonParser.parseString(res2.body()).getAsJsonObject();
                System.out.println("[DEBUG MMR-HIST KEYS] " + hRoot.keySet());
                if (hRoot.has("data") && !hRoot.get("data").isJsonNull()) {
                    com.google.gson.JsonObject hData = hRoot.getAsJsonObject("data");
                    System.out.println("[DEBUG MMR-HIST DATA KEYS] " + hData.keySet());
                    if (hData.has("history") && !hData.get("history").isJsonNull()) {
                        com.google.gson.JsonArray hist = hData.getAsJsonArray("history");
                        System.out.println("[DEBUG 4] history.size=" + hist.size());
                        if (hist.size() > 0) {
                            com.google.gson.JsonObject h0 = hist.get(0).getAsJsonObject();
                            System.out.println("[DEBUG 4] history[0]=" + h0);
                            if (h0.has("tier") && !h0.get("tier").isJsonNull()) {
                                com.google.gson.JsonObject t = h0.getAsJsonObject("tier");
                                int hId = t.has("id") ? t.get("id").getAsInt() : -1;
                                String hName = t.has("name") ? t.get("name").getAsString() : null;
                                System.out.println("[DEBUG 4] history[0].tier id=" + hId + " name=" + hName);
                                if (esRangoValido(hId, hName)) {
                                    String etq = "";
                                    if (h0.has("season") && !h0.get("season").isJsonNull()) {
                                        com.google.gson.JsonObject season = h0.getAsJsonObject("season");
                                        if (season.has("short") && !season.get("short").isJsonNull())
                                            etq = " (" + season.get("short").getAsString() + ")";
                                    }
                                    return hName + etq;
                                }
                            } else {
                                System.out.println("[DEBUG 4] history[0] tier missing. Keys=" + h0.keySet());
                            }
                        }
                    } else {
                        System.out.println("[DEBUG 4] history key missing. hData keys=" + hData.keySet());
                    }
                }
            }

            System.out.println("[DEBUG FINAL] " + riotId + " → Sin rango");
            return "Sin rango";

        } catch (Exception e) {
            System.out.println("[DEBUG EXCEPCION] " + e.getMessage());
            e.printStackTrace();
            return "Error";
        }
    }

    private static boolean esRangoValido(int id, String nombre) {
        if (id <= 1 || nombre == null) return false;
        return !"Unrated".equalsIgnoreCase(nombre);
    }

    private static String obtenerEtiquetaTemporada(com.google.gson.JsonObject temporada) {
        if (!temporada.has("season") || temporada.get("season").isJsonNull()) return "";
        com.google.gson.JsonObject season = temporada.getAsJsonObject("season");
        if (!season.has("short") || season.get("short").isJsonNull()) return "";
        return " (" + season.get("short").getAsString() + ")";
    }


    public static String obtenerRangoLol(String puuid) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://" + REGION + ".api.riotgames.com/lol/league/v4/entries/by-puuid/" + puuid))
                    .header("X-Riot-Token", API_KEY)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String body = response.body();
                if (body.equals("[]")) return "Sin rango";
                String tier = extractJson(body, "tier");
                String rank = extractJson(body, "rank");
                String lp   = extractJson(body, "leaguePoints");
                if (tier != null) return tier + " " + rank + " - " + lp + " LP";
            }
            return "Sin rango";

        } catch (Exception e) {
            System.out.println("Error obteniendo rango de LoL.");
            return "Error";
        }
    }

    private static String extractJson(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) {
            search = "\"" + key + "\":";
            start = json.indexOf(search);
            if (start == -1) return null;
            start += search.length();
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            return json.substring(start, end).trim();
        }
        start += search.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
}
