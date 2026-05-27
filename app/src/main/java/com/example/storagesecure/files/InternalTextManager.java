package com.example.storagesecure.files;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Lecture/écriture de fichiers texte en stockage interne (MODE_PRIVATE).
 * L'encodage UTF-8 est systématiquement imposé.
 */
public final class InternalTextManager {

    private InternalTextManager() {}

    /** Écrit du texte UTF-8 dans le stockage interne privé. */
    public static void writeContent(Context ctx, String fileName, String content) throws Exception {
        try (FileOutputStream fos = ctx.openFileOutput(fileName, Context.MODE_PRIVATE)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    /** Lit un fichier texte UTF-8 depuis le stockage interne. */
    public static String readContent(Context ctx, String fileName) throws Exception {
        try (FileInputStream fis = ctx.openFileInput(fileName)) {
            byte[] raw = fis.readAllBytes();
            return new String(raw, StandardCharsets.UTF_8);
        }
    }

    /** Supprime un fichier interne. */
    public static boolean remove(Context ctx, String fileName) {
        return ctx.deleteFile(fileName);
    }
}
