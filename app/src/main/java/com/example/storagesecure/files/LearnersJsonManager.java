package com.example.storagesecure.files;

import android.content.Context;

import com.example.storagesecure.model.Learner;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Sérialisation / désérialisation d'une liste de {@link Learner} en JSON
 * vers le stockage interne (org.json, aucune dépendance externe).
 */
public final class LearnersJsonManager {

    public static final String DATA_FILE = "learners.json";

    private LearnersJsonManager() {}

    /** Persiste la liste d'apprenants en JSON dans le stockage interne. */
    public static void persist(Context ctx, List<Learner> learners) throws Exception {
        String payload = serializeToJson(learners);
        try (var fos = ctx.openFileOutput(DATA_FILE, Context.MODE_PRIVATE)) {
            fos.write(payload.getBytes(StandardCharsets.UTF_8));
        }
    }

    /** Charge la liste depuis le fichier JSON ; retourne liste vide si absent/corrompu. */
    public static List<Learner> retrieve(Context ctx) {
        try (var fis = ctx.openFileInput(DATA_FILE)) {
            byte[] raw = fis.readAllBytes();
            String payload = new String(raw, StandardCharsets.UTF_8);
            return deserializeFromJson(payload);
        } catch (Exception e) {
            return List.of();
        }
    }

    /** Supprime le fichier JSON du stockage interne. */
    public static boolean remove(Context ctx) {
        return ctx.deleteFile(DATA_FILE);
    }

    /* ---------- Sérialisation privée ---------- */

    private static String serializeToJson(List<Learner> learners) throws Exception {
        JSONArray array = new JSONArray();
        for (Learner l : learners) {
            JSONObject obj = new JSONObject();
            obj.put("identifier", l.identifier);
            obj.put("fullName",   l.fullName);
            obj.put("age",        l.age);
            array.put(obj);
        }
        return array.toString();
    }

    private static List<Learner> deserializeFromJson(String json) throws Exception {
        JSONArray array = new JSONArray(json);
        List<Learner> result = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            result.add(new Learner(
                    obj.getInt("identifier"),
                    obj.getString("fullName"),
                    obj.getInt("age")
            ));
        }
        return result;
    }
}
