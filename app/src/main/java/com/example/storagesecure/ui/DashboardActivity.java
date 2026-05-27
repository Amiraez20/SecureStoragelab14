package com.example.storagesecure.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.storagesecure.R;
import com.example.storagesecure.cache.TempCacheManager;
import com.example.storagesecure.files.InternalTextManager;
import com.example.storagesecure.files.LearnersJsonManager;
import com.example.storagesecure.model.Learner;
import com.example.storagesecure.prefs.UserSettings;
import com.example.storagesecure.prefs.VaultPrefs;

import java.util.Arrays;
import java.util.List;

/**
 * Écran principal du lab « Stockage sécurisé ».
 * Regroupe : préférences, coffre chiffré, fichiers internes, cache et nettoyage.
 */
public class DashboardActivity extends AppCompatActivity {

    private static final String LOG_TAG = "StorageSecureLab";
    private final List<String> availableLocales = Arrays.asList("fr", "en", "ar");

    private EditText   etDisplayName;
    private EditText   etSecret;
    private Spinner    spLocale;
    private Switch     swNightMode;
    private TextView   tvOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etDisplayName = findViewById(R.id.etDisplayName);
        etSecret      = findViewById(R.id.etSecret);
        spLocale      = findViewById(R.id.spLocale);
        swNightMode   = findViewById(R.id.swNightMode);
        tvOutput      = findViewById(R.id.tvOutput);

        initLocaleSpinner();

        Button btnPersist = findViewById(R.id.btnPersistSettings);
        Button btnFetch   = findViewById(R.id.btnFetchSettings);
        Button btnSaveJ   = findViewById(R.id.btnPersistJson);
        Button btnLoadJ   = findViewById(R.id.btnFetchJson);
        Button btnWipe    = findViewById(R.id.btnWipeAll);

        btnPersist.setOnClickListener(v -> onPersistSettings());
        btnFetch.setOnClickListener(v   -> onFetchSettings());
        btnSaveJ.setOnClickListener(v   -> onPersistJsonData());
        btnLoadJ.setOnClickListener(v   -> onFetchJsonData());
        btnWipe.setOnClickListener(v    -> onWipeEverything());

        // Chargement initial des préférences au démarrage
        onFetchSettings();
    }

    /* -------- Spinner -------- */

    private void initLocaleSpinner() {
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, availableLocales);
        spLocale.setAdapter(adapter);
    }

    /* -------- Préférences (non sensibles + chiffrées) -------- */

    private void onPersistSettings() {
        String displayName = etDisplayName.getText().toString().trim();
        String locale      = availableLocales.get(Math.max(0, spLocale.getSelectedItemPosition()));
        String appearance  = swNightMode.isChecked() ? "dark" : "light";

        boolean result = UserSettings.persist(this, displayName, locale, appearance, false);

        // Token : stockage chiffré uniquement
        String secret = etSecret.getText().toString();
        if (!secret.isBlank()) {
            try {
                VaultPrefs.storeToken(this, secret);
            } catch (Exception ex) {
                tvOutput.setText("Erreur chiffrement token : " + ex.getMessage());
                return;
            }
        }

        // Log sécurisé : PAS de token en clair
        Log.d(LOG_TAG, "Prefs persistées result=" + result
                + ", displayName=" + displayName
                + ", locale=" + locale
                + ", appearance=" + appearance);

        // Sauvegarde d'un snapshot en cache temporaire
        try {
            TempCacheManager.writeTempFile(this, "last_snapshot.txt",
                    "displayName=" + displayName + ", locale=" + locale + ", appearance=" + appearance);
        } catch (Exception ignored) {}

        tvOutput.setText(
                "Sauvegarde prefs terminée.\n" +
                "displayName=" + displayName + "\n" +
                "locale=" + locale + "\n" +
                "appearance=" + appearance + "\n" +
                "token: stocké chiffré si non vide (non affiché)."
        );
    }

    private void onFetchSettings() {
        UserSettings.SettingsBundle bundle = UserSettings.fetch(this);

        etDisplayName.setText(bundle.displayName);
        swNightMode.setChecked("dark".equals(bundle.appearance));

        int localeIdx = availableLocales.indexOf(bundle.locale);
        spLocale.setSelection(localeIdx >= 0 ? localeIdx : 0);

        int secretLength = 0;
        try {
            String token = VaultPrefs.retrieveToken(this);
            secretLength = (token == null) ? 0 : token.length();
        } catch (Exception ignored) {}

        tvOutput.setText(
                "Chargement prefs terminé.\n" +
                "displayName=" + bundle.displayName + "\n" +
                "locale=" + bundle.locale + "\n" +
                "appearance=" + bundle.appearance + "\n" +
                "tokenLength=" + secretLength
        );

        Log.d(LOG_TAG, "Prefs chargées displayName=" + bundle.displayName
                + ", locale=" + bundle.locale
                + ", appearance=" + bundle.appearance
                + ", tokenLength=" + secretLength);
    }

    /* -------- Fichiers internes (JSON + texte) -------- */

    private void onPersistJsonData() {
        List<Learner> learners = Arrays.asList(
                new Learner(1, "Yasmine",  22),
                new Learner(2, "Mehdi",    23),
                new Learner(3, "Nadia",    20)
        );

        try {
            LearnersJsonManager.persist(this, learners);
            InternalTextManager.writeContent(this, "memo.txt",
                    "Export JSON effectué avec succès (UTF-8).");
        } catch (Exception ex) {
            tvOutput.setText("Erreur sauvegarde JSON : " + ex.getMessage());
            return;
        }

        Log.d(LOG_TAG, "Fichiers internes écrits : learners.json, memo.txt");
        tvOutput.setText("Sauvegarde fichier JSON terminée. learners=" + learners.size());
    }

    private void onFetchJsonData() {
        List<Learner> learners = LearnersJsonManager.retrieve(this);

        String memo;
        try {
            memo = InternalTextManager.readContent(this, "memo.txt");
        } catch (Exception e) {
            memo = "(memo.txt absent)";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Chargement fichier JSON terminé.\n");
        sb.append("memo=").append(memo).append("\n");
        sb.append("learners=").append(learners.size()).append("\n");
        for (Learner l : learners) {
            sb.append(" - id=").append(l.identifier)
              .append(", fullName=").append(l.fullName)
              .append(", age=").append(l.age)
              .append("\n");
        }

        tvOutput.setText(sb.toString());
        Log.d(LOG_TAG, "Fichier JSON chargé : learners=" + learners.size());
    }

    /* -------- Nettoyage complet -------- */

    private void onWipeEverything() {
        UserSettings.wipe(this);

        try {
            VaultPrefs.wipe(this);
        } catch (Exception ignored) {}

        LearnersJsonManager.remove(this);
        InternalTextManager.remove(this, "memo.txt");

        int purgedCount = TempCacheManager.purgeAll(this);

        etDisplayName.setText("");
        etSecret.setText("");
        swNightMode.setChecked(false);
        spLocale.setSelection(0);

        tvOutput.setText(
                "Nettoyage terminé.\n" +
                "user_settings: wipe()\n" +
                "vault_prefs: wipe()\n" +
                "learners.json: supprimé\n" +
                "memo.txt: supprimé\n" +
                "cache purgé: " + purgedCount + " fichier(s)"
        );

        Log.d(LOG_TAG, "Nettoyage complet (aucune donnée sensible loggée).");
    }
}
