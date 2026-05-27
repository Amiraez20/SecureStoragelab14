package com.example.storagesecure.cache;

import android.content.Context;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Gestion du cache temporaire (cacheDir).
 * Le cache est réservé aux données régénérables ;
 * le système peut le purger à tout moment.
 */
public final class TempCacheManager {

    private TempCacheManager() {}

    /** Écrit du texte dans un fichier cache. */
    public static void writeTempFile(Context ctx, String fileName, String content) throws Exception {
        File target = new File(ctx.getCacheDir(), fileName);
        java.nio.file.Files.writeString(target.toPath(), content, StandardCharsets.UTF_8);
    }

    /** Lit le contenu d'un fichier cache. Retourne null si absent. */
    public static String readTempFile(Context ctx, String fileName) throws Exception {
        File target = new File(ctx.getCacheDir(), fileName);
        if (!target.exists()) return null;
        return java.nio.file.Files.readString(target.toPath(), StandardCharsets.UTF_8);
    }

    /** Purge l'intégralité du répertoire cache. Retourne le nombre de fichiers supprimés. */
    public static int purgeAll(Context ctx) {
        File[] entries = ctx.getCacheDir().listFiles();
        if (entries == null) return 0;
        int removed = 0;
        for (File f : entries) {
            if (f.delete()) removed++;
        }
        return removed;
    }
}
