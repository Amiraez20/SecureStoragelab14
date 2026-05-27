package com.example.storagesecure.prefs;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Gestionnaire de préférences non sensibles (profil, langue, apparence).
 * Utilise MODE_PRIVATE pour garantir l'accès exclusif à l'application.
 */
public final class UserSettings {

    private static final String FILE_KEY = "user_settings_store";
    private static final String FIELD_DISPLAY_NAME = "setting_display_name";
    private static final String FIELD_LOCALE      = "setting_locale";
    private static final String FIELD_APPEARANCE   = "setting_appearance";

    private UserSettings() { /* Empêche l'instanciation */ }

    /**
     * Persiste le profil utilisateur.
     * @param sync true → commit() (synchrone, retourne le résultat),
     *             false → apply() (asynchrone, recommandé pour l'UI).
     */
    public static boolean persist(Context ctx, String displayName,
                                  String locale, String appearance, boolean sync) {

        SharedPreferences sp = ctx.getSharedPreferences(FILE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor writer = sp.edit()
                .putString(FIELD_DISPLAY_NAME, displayName)
                .putString(FIELD_LOCALE, locale)
                .putString(FIELD_APPEARANCE, appearance);

        if (sync) {
            // commit() : bloquant, renvoie true/false
            return writer.commit();
        } else {
            // apply() : non bloquant, pas de retour
            writer.apply();
            return true;
        }
    }

    /**
     * Recharge le profil depuis le disque.
     */
    public static SettingsBundle fetch(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(FILE_KEY, Context.MODE_PRIVATE);
        String displayName = sp.getString(FIELD_DISPLAY_NAME, "");
        String locale      = sp.getString(FIELD_LOCALE, "fr");
        String appearance  = sp.getString(FIELD_APPEARANCE, "system");
        return new SettingsBundle(displayName, locale, appearance);
    }

    /**
     * Supprime toutes les préférences utilisateur.
     */
    public static void wipe(Context ctx) {
        ctx.getSharedPreferences(FILE_KEY, Context.MODE_PRIVATE)
           .edit().clear().apply();
    }

    /** Conteneur immuable pour les 3 champs de préférences. */
    public static final class SettingsBundle {
        public final String displayName;
        public final String locale;
        public final String appearance;

        public SettingsBundle(String displayName, String locale, String appearance) {
            this.displayName = displayName;
            this.locale      = locale;
            this.appearance  = appearance;
        }
    }
}
