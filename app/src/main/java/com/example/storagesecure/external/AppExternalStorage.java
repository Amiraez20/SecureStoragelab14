package com.example.storagesecure.external;

import android.content.Context;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Stockage externe app-specific (getExternalFilesDir).
 * Aucune permission runtime nécessaire.
 * Ne PAS utiliser pour des données sensibles ; préférer le stockage interne.
 */
public final class AppExternalStorage {

    private AppExternalStorage() {}

    /**
     * Exporte du texte vers le répertoire externe dédié à l'app.
     * @return chemin absolu du fichier créé, ou null si indisponible.
     */
    public static String exportFile(Context ctx, String fileName, String content) throws Exception {
        File baseDir = ctx.getExternalFilesDir(null);
        if (baseDir == null) return null;

        File output = new File(baseDir, fileName);
        java.nio.file.Files.writeString(output.toPath(), content, StandardCharsets.UTF_8);
        return output.getAbsolutePath();
    }

    /** Lit un fichier depuis le répertoire externe app-specific. */
    public static String importFile(Context ctx, String fileName) throws Exception {
        File baseDir = ctx.getExternalFilesDir(null);
        if (baseDir == null) return null;

        File input = new File(baseDir, fileName);
        if (!input.exists()) return null;
        return java.nio.file.Files.readString(input.toPath(), StandardCharsets.UTF_8);
    }

    /** Supprime un fichier du répertoire externe app-specific. */
    public static boolean removeFile(Context ctx, String fileName) {
        File baseDir = ctx.getExternalFilesDir(null);
        if (baseDir == null) return false;
        return new File(baseDir, fileName).delete();
    }
}
