package com.example.storagesecure.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

/**
 * Stockage chiffré de données sensibles (token API, clé secrète…).
 * Les clés ET les valeurs sont chiffrées sur disque via le Keystore Android.
 *
 * RÈGLES :
 *  - Ne jamais logger le contenu du token.
 *  - Ne pas afficher le token en clair à l'écran.
 *  - Gérer les exceptions sans exposer le secret.
 */
public final class VaultPrefs {

    private static final String VAULT_FILE  = "vault_encrypted_prefs";
    private static final String FIELD_TOKEN = "vault_api_token";

    private VaultPrefs() { /* Empêche l'instanciation */ }

    /**
     * Crée l'instance SharedPreferences chiffrée.
     * MasterKey s'appuie sur le KeyStore matériel du device.
     */
    private static SharedPreferences openVault(Context ctx) throws Exception {
        MasterKey masterKey = new MasterKey.Builder(ctx)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        return EncryptedSharedPreferences.create(
                ctx,
                VAULT_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    /** Persiste le token chiffré. Ne pas logger le contenu. */
    public static void storeToken(Context ctx, String token) throws Exception {
        openVault(ctx).edit().putString(FIELD_TOKEN, token).apply();
    }

    /** Récupère le token déchiffré. */
    public static String retrieveToken(Context ctx) throws Exception {
        return openVault(ctx).getString(FIELD_TOKEN, "");
    }

    /** Efface l'intégralité du coffre-fort chiffré. */
    public static void wipe(Context ctx) throws Exception {
        openVault(ctx).edit().clear().apply();
    }
}
