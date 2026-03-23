package com.mycompany.gestorcontrasenyas.db;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mycompany.gestorcontrasenyas.utils.Config;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

public class SupabaseAuth {

    private static final String URL      = Config.get("supabase.url");
    private static final String ANON_KEY = Config.get("supabase.anon_key");

    private static byte[] masterKey   = null;
    private static String accessToken = null;
    private static String userId      = null;

    public static void setMasterKey(byte[] key) { masterKey = key; }
    public static byte[] getMasterKey()          { return masterKey; }
    public static String getAccessToken()        { return accessToken; }
    public static String getUserId()             { return userId; }

    /**
     * Limpia todos los datos sensibles de memoria.
     * Llamar siempre al cerrar sesion o al cerrar la app.
     */
    public static void cerrarSesion() {
        // Sobreescribir el array con ceros antes de soltar la referencia.
        // Con String no seria posible porque son inmutables en Java.
        if (masterKey != null) {
            Arrays.fill(masterKey, (byte) 0);
            masterKey = null;
        }
        accessToken = null;
        userId = null;
    }

    public static String login(String email, String password) {
        try {
            // Gson escapa automaticamente caracteres especiales como " y \
            JsonObject obj = new JsonObject();
            obj.addProperty("email", email);
            obj.addProperty("password", password);
            String body = obj.toString();

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL + "/auth/v1/token?grant_type=password"))
                    .header("Content-Type", "application/json")
                    .header("apikey", ANON_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                accessToken = json.get("access_token").getAsString();
                userId = json.getAsJsonObject("user").get("id").getAsString();
                return null;
            } else {
                return "Email o contraseña incorrectos.";
            }

        } catch (Exception e) {
            return "Error de conexión: " + e.getMessage();
        }
    }

    public static String signup(String email, String password) {
        try {
            JsonObject obj = new JsonObject();
            obj.addProperty("email", email);
            obj.addProperty("password", password);
            String body = obj.toString();

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL + "/auth/v1/signup"))
                    .header("Content-Type", "application/json")
                    .header("apikey", ANON_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                if (json.has("access_token") && !json.get("access_token").isJsonNull()) {
                    accessToken = json.get("access_token").getAsString();
                }
                if (json.has("user") && !json.get("user").isJsonNull()) {
                    userId = json.getAsJsonObject("user").get("id").getAsString();
                }
                return null;
            } else if (response.body().contains("weak_password")) {
                return "La contraseña debe tener al menos 6 caracteres.";
            } else if (response.body().contains("already registered")) {
                return "El email ya está en uso.";
            } else {
                return "Error al registrarse: " + response.body();
            }

        } catch (Exception e) {
            return "Error de conexión: " + e.getMessage();
        }
    }

    public static String guardarUsuario(String userId, String salt) {
        try {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", userId);
            obj.addProperty("salt", salt);
            String body = obj.toString();

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL + "/rest/v1/usuarios"))
                    .header("Content-Type", "application/json")
                    .header("apikey", ANON_KEY)
                    .header("Authorization", "Bearer " + accessToken)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201) {
                return null;
            } else {
                return "Error al guardar usuario: " + response.body();
            }

        } catch (Exception e) {
            return "Error de conexión: " + e.getMessage();
        }
    }

    public static String obtenerSalt(String userId) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL + "/rest/v1/usuarios?id=eq." + userId + "&select=salt"))
                    .header("apikey", ANON_KEY)
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
                if (responseBody.equals("[]")) return null;
                JsonArray array = JsonParser.parseString(responseBody).getAsJsonArray();
                return array.get(0).getAsJsonObject().get("salt").getAsString();
            }
            return null;

        } catch (Exception e) {
            return null;
        }
    }
}