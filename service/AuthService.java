package com.mycompany.gestorcontrasenyas.service;

import com.google.gson.JsonObject;
import com.mycompany.gestorcontrasenyas.db.SupabaseAuth;
import com.mycompany.gestorcontrasenyas.utils.Config;
import com.mycompany.gestorcontrasenyas.utils.Pbkdf2;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AuthService {

    public static String login(String email, String password) {
        return SupabaseAuth.login(email, password);
    }

    public static boolean tieneMasterKey(String userId) {
        String salt = SupabaseAuth.obtenerSalt(userId);
        return salt != null;
    }

    public static String configurarMasterKey(String masterPassword) {
        String userId       = SupabaseAuth.getUserId();
        String saltExistente = SupabaseAuth.obtenerSalt(userId);

        try {
            if (saltExistente == null) {
                String salt  = Pbkdf2.generarSalt();
                String error = SupabaseAuth.guardarUsuario(userId, salt);
                if (error != null) return error;
                saltExistente = salt;
            }

            byte[] key = Pbkdf2.derivarKey(masterPassword, saltExistente);
            SupabaseAuth.setMasterKey(key);
            return null;

        } catch (Exception e) {
            return "Error al generar la clave: " + e.getMessage();
        }
    }

    public static String signup(String email, String password) {
        return SupabaseAuth.signup(email, password);
    }

    public static String enviarRecuperacion(String email) {
        try {
            // Gson escapa el email correctamente
            JsonObject obj = new JsonObject();
            obj.addProperty("email", email);
            String body = obj.toString();

            String supabaseUrl = Config.get("supabase.url");
            String anonKey     = Config.get("supabase.anon_key");

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(supabaseUrl + "/auth/v1/recover"))
                    .header("Content-Type", "application/json")
                    .header("apikey", anonKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) return null;
            return "Error al enviar el correo.";

        } catch (Exception e) {
            return "Error de conexión: " + e.getMessage();
        }
    }
}