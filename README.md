# StorageSecure – Lab Persistance Locale Android (Java)

## Aperçu

Application Android (Java) démontrant les mécanismes de **persistance locale sécurisée** :
SharedPreferences, EncryptedSharedPreferences, fichiers internes (texte UTF-8 / JSON),
cache temporaire (`cacheDir`) et stockage externe app-specific.

L'application fonctionne **intégralement hors ligne**.

---

## Pré-requis

| Élément            | Version minimale          |
|--------------------|---------------------------|
| Android Studio     | Hedgehog ou supérieur     |
| SDK minimum        | API 24 (Android 7.0)     |
| Langage            | Java 11                  |
| Dépendance crypto  | `androidx.security:security-crypto:1.1.0-alpha06` |

> La dépendance Security Crypto est déclarée dans le catalogue de versions (`gradle/libs.versions.toml`).

---

## Architecture du projet

```
com.example.storagesecure
├── ui/
│   └── DashboardActivity.java      ← Écran principal (activité unique)
├── prefs/
│   ├── UserSettings.java            ← Préférences non sensibles (nom, locale, apparence)
│   └── VaultPrefs.java              ← Stockage chiffré (token API) via EncryptedSharedPreferences
├── files/
│   ├── InternalTextManager.java     ← Lecture/écriture fichiers texte UTF-8 (stockage interne)
│   └── LearnersJsonManager.java     ← Sérialisation/désérialisation JSON (org.json)
├── cache/
│   └── TempCacheManager.java        ← Cache temporaire (cacheDir) + purge
├── external/
│   └── AppExternalStorage.java      ← Export vers stockage externe app-specific
└── model/
    └── Learner.java                 ← Modèle de données (apprenant)
```

---

## Fonctionnalités par tâche

### Tâche 1 – Création du projet + dépendances
- Projet Empty Views Activity, Java, min SDK 24.
- Dépendance `androidx.security:security-crypto:1.1.0-alpha06` ajoutée dans `libs.versions.toml` et `build.gradle.kts`.

### Tâche 2 – SharedPreferences (lecture/écriture)
- Classe `UserSettings` : persistance de `displayName`, `locale`, `appearance`.
- `persist()` supporte `apply()` (asynchrone) ou `commit()` (synchrone).
- `fetch()` recharge les valeurs avec défauts cohérents.
- `wipe()` supprime toutes les préférences.
- `MODE_PRIVATE` garantit l'isolation des données.

### Tâche 3 – EncryptedSharedPreferences (sécurité)
- Classe `VaultPrefs` : stockage chiffré du token API.
- `MasterKey` (AES256_GCM) s'appuie sur le KeyStore matériel.
- Clés chiffrées (AES256_SIV) + valeurs chiffrées (AES256_GCM).
- **Le token n'est jamais loggé ni affiché en clair** (seule sa longueur est visible).

### Tâche 4 – Fichiers internes (texte + JSON)
- `InternalTextManager` : écriture/lecture UTF-8 via `openFileOutput`/`openFileInput`.
- `LearnersJsonManager` : sérialisation d'une `List<Learner>` en JSON (`org.json`).
- Fichier absent ou corrompu → liste vide retournée (aucun crash).
- Vérifiable via Device File Explorer : `/data/data/<package>/files/`.

### Tâche 5 – Cache temporaire
- `TempCacheManager` : écriture/lecture dans `cacheDir`.
- `purgeAll()` supprime manuellement tous les fichiers en cache.
- Le cache est réservé aux données régénérables.

### Tâche 6 – Stockage externe app-specific
- `AppExternalStorage` : export/import vers `getExternalFilesDir(null)`.
- Aucune permission runtime nécessaire.
- Chemin absolu retourné pour vérification.

---

## Écran principal (fil rouge)

L'activité `DashboardActivity` propose un écran unique avec :

| Élément UI                | Rôle                                              |
|---------------------------|----------------------------------------------------|
| `EditText` – Nom affiché  | Saisie du nom utilisateur                          |
| `Spinner` – Locale        | Sélection de la langue (fr / en / ar)              |
| `Switch` – Mode sombre    | Bascule thème clair/sombre                         |
| `EditText` – Token        | Saisie du token (masqué, `textPassword`)           |
| Bouton « Sauvegarder prefs » | Persiste préférences + token chiffré            |
| Bouton « Charger prefs »  | Recharge les valeurs depuis le stockage            |
| Bouton « Sauvegarder JSON » | Écrit `learners.json` + `memo.txt`               |
| Bouton « Charger JSON »   | Relit et affiche les apprenants                    |
| Bouton « Effacer »        | Nettoyage complet (prefs + coffre + fichiers + cache) |
| `TextView` – Résultat     | Affiche le retour de chaque opération              |

---

## Checklist sécurité (10+ points)

| #  | Critère                                                         | Statut |
|----|-----------------------------------------------------------------|--------|
| 1  | Aucun token/mot de passe n'apparaît dans Logcat                 | ✅     |
| 2  | EncryptedSharedPreferences utilisé pour les secrets             | ✅     |
| 3  | `MODE_PRIVATE` utilisé pour fichiers internes et prefs claires  | ✅     |
| 4  | Token masqué à l'écran (`textPassword`)                         | ✅     |
| 5  | Nettoyage complet disponible (prefs + coffre + fichiers + cache)| ✅     |
| 6  | Cache réservé au temporaire régénérable                         | ✅     |
| 7  | Export externe limité à app-specific (pas public)               | ✅     |
| 8  | Exceptions gérées sans fuite d'informations                     | ✅     |
| 9  | Encodage UTF-8 imposé pour fichiers texte                       | ✅     |
| 10 | Token non affiché en clair (longueur uniquement)                | ✅     |
| 11 | Vérification via Device File Explorer possible                  | ✅     |
| 12 | Concept d'expiration de token prévu (délai + invalidation)      | ✅     |

---

## Vérifications

1. **Sync Gradle** : vérifier que la synchronisation réussit sans erreur.
2. **Build** : compiler le projet (`Build > Make Project`).
3. **Lancement** : l'écran principal s'affiche avec les champs et boutons.
4. **Sauvegarder prefs** → redémarrer → **Charger prefs** : valeurs restaurées.
5. **Token** : saisir un token, sauvegarder, redémarrer → `tokenLength > 0`, aucun token en clair dans Logcat.
6. **JSON** : « Sauvegarder JSON » → « Charger JSON » : liste des apprenants affichée.
7. **Device File Explorer** :
   - `/data/data/com.example.storagesecure/files/learners.json`
   - `/data/data/com.example.storagesecure/files/memo.txt`
   - `/data/data/com.example.storagesecure/cache/last_snapshot.txt`
8. **Effacer** : nettoyage complet, tous les champs réinitialisés.

---

## Dépannage

| Problème                                    | Solution                                                             |
|---------------------------------------------|----------------------------------------------------------------------|
| Dépendance Security Crypto indisponible     | Relancer « Sync Project with Gradle Files » ; vérifier le cache local |
| Crash sur EncryptedSharedPreferences        | Tester API 24+ ; vérifier le contexte Activity                       |
| Fichiers internes invisibles                | Lancer l'app au moins une fois ; vérifier le bon package             |
| JSON vide                                   | Re-sauvegarder ; si corrompu → « Effacer » puis re-générer          |
| Logs sensibles détectés                     | Supprimer immédiatement ; remplacer par `tokenLength`                |

---

## Récapitulatif

- **SharedPreferences** : préférences non sensibles (apparence, locale, nom).
- **apply()** non bloquant vs **commit()** bloquant avec retour booléen.
- **EncryptedSharedPreferences + MasterKey** : coffre-fort local basé sur le KeyStore.
- **Fichiers internes** : texte UTF-8 et JSON, sans permissions, `MODE_PRIVATE`.
- **Cache** : temporaire, purgeable manuellement.
- **Externe app-specific** : export contrôlé, sans permissions runtime.
- **Sécurité** : pas de logs sensibles, chiffrement, nettoyage explicite, isolation.

## DEMO

https://github.com/user-attachments/assets/87c2b574-b1fb-440f-912d-dfddf25d04f7


https://github.com/user-attachments/assets/2ce4d2f9-3307-4062-8190-2801398c1c6e


