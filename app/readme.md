# 420-5C4-ST – Développement mobile avancé

*Département d'informatique, Cégep Saint-Jean-sur-Richelieu*

## Exercice 5 – Notifications et Authentification

Cet exercice poursuit l'application de classement de la LNH de l'Exercice 4. Vous allez :

1. rendre ses notifications plus professionnelles;
2. ajouter un compte utilisateur qui s'authentifie auprès d'une API par jeton;
3. permettre à l'application de recevoir des notifications push envoyées depuis un serveur.

> **Projet de départ :** votre solution de l'Exercice 4.

---

## Objectifs pédagogiques

- Organiser les notifications d'une application avec des **canaux distincts** et une **classe dédiée**.
- Notifier l'utilisateur depuis une tâche en arrière-plan **sans l'importuner** : vérifier la permission et ne notifier que lorsque c'est pertinent.
- Utiliser des **`PendingIntent`** pour ouvrir l'application et pour déclencher une action depuis une notification.
- S'authentifier auprès d'une API **par jeton**, transmettre le jeton avec un **intercepteur** et gérer proprement les erreurs d'authentification.
- Intégrer les **notifications push** avec Firebase Cloud Messaging (FCM) : recevoir un message d'un serveur et l'afficher avec les mêmes canaux que les notifications locales.

---

## Modèle de données

Les entités de l'Exercice 4 (`EquipeClassementEntity`, `SyncMetadataEntity`) **ne changent pas**. Vous devez ajouter :

| Élément | Description |
|---|---|
| `Profil` | Prénom, nom et courriel de l'utilisateur connecté. |
| DTO d'authentification | `LoginRequest`, `LoginResponse`, DTO du profil, avec une fonction d'extension vers `Profil`. |
| `SessionManager` | Conserve le jeton en mémoire et expose l'état de la session (`StateFlow`). |

---

## Structure du projet

| Package | Contenu |
|---|---|
| `business` | Classes du modèle utilisées par les ViewModels. |
| `data/remote` | DTO, interfaces Retrofit et configuration des clients HTTP. |
| `database/local` | Entités, DAO et classe Room `@Database`. |
| `database` | Fonctions d'extension pour convertir les DTO en entités. |
| `repository` | Repositories : synchronisation distante, accès aux données locales, authentification. |
| `worker` | Le Worker de synchronisation en arrière-plan. |
| `service` | Les services de l'app (exportation / FCM). |
| `viewmodel` | ViewModels et UiStates des écrans. |
| `ui` | Écrans Compose, affichage de l'état et commandes de synchronisation / exportation. |
| `util` | Le `NotificationHelper`. |

---

## Fonctionnalités à réaliser

### 1. Notifications

**a. `NotificationHelper`**
Créez une classe `NotificationHelper` qui centralise la création des canaux, la construction et l'affichage des notifications.
- Les canaux sont créés **une seule fois, au démarrage** de l'application.
- Prévoyez **au minimum deux canaux**, avec des niveaux d'importance justifiés par un commentaire dans le code :
  - un canal **Exportation** (progression et résultat de l'exportation);
  - un canal **Classement** (mises à jour du classement).
- Le service d'exportation de l'Exercice 4 doit utiliser ce `NotificationHelper`, **sans créer son propre canal**.

**b. Notification de changement de classement**
À la fin d'une synchronisation réussie, le Worker compare le classement **avant et après** la mise à jour de Room.
- Si **au moins une équipe a changé de position**, affichez une notification sur le canal Classement.
- Elle indique le **nombre d'équipes** qui ont changé de position et, si elle a changé, la **nouvelle équipe en tête**.
- **Aucune notification** ne doit s'afficher si rien n'a changé.

**c. Vérifier la permission**
Un Worker ne peut pas demander de permission. Avant d'afficher une notification, vérifiez que les notifications sont autorisées; sinon, n'affichez rien — **sans erreur et sans faire échouer le Worker**.

**d. Ouvrir l'application**
Toucher la notification de classement **ouvre l'application** (`PendingIntent`). La notification **disparaît** lorsqu'on la touche.

**e. Action « Réessayer »**
Si l'exportation JSON échoue, la notification d'échec comporte un bouton d'action **« Réessayer »** qui relance le service d'exportation au moyen d'un `PendingIntent`.

### 2. Authentification

- **a.** Implémentez la connexion avec [DummyJSON](https://dummyjson.com/docs/auth). Le jeton reçu est conservé dans le `SessionManager`.
- **b.** Définissez une interface `AuthApiService` qui définit les méthodes d'accès de l'API. Implémentez aussi les DTO requis.
- **c.** Définissez une **nouvelle instance Retrofit** pour communiquer avec l'API d'authentification.
- **d.** Définissez une classe `ApiAuthRepository` et son implémentation. Le dépôt traduit les erreurs de Retrofit en **vos propres exceptions** :
  - identifiants invalides;
  - session expirée;
  - réseau indisponible;
  - erreur du serveur.
- **e.** Ajoutez un **intercepteur OkHttp** qui ajoute l'en-tête `Authorization: Bearer <jeton>` aux requêtes sortantes. Il doit être branché sur un **client distinct**, utilisé uniquement pour l'API d'authentification. **Le client de la LNH ne reçoit jamais le jeton.**
- **f.** Créez les UiStates et les ViewModels nécessaires pour les écrans de connexion et de profil.
- **g.** Ajoutez les écrans **Login** et **Profil** et intégrez-les correctement à l'application.

### 3. Notifications push

- **a.** Créez un projet Firebase et suivez les étapes pour configurer votre projet.
- **b.** Créez le service permettant de recevoir des messages.
- **c.** À la réception d'un message, affichez la notification.
- **d.** Vérifiez le bon fonctionnement en envoyant un message depuis la **Console Firebase**.

---

## Contraintes et remise

- L'application doit **rester utilisable** si l'API de la LNH ou le service d'authentification sont temporairement inaccessibles.
- Testez au minimum, **sur Android 13 ou plus récent** :
  - [ ] Une synchronisation **avec** changement de classement
    *(astuce : modifiez les données d'une équipe dans Room avec le Database Inspector, puis déclenchez une synchronisation avec un bouton temporaire « Synchroniser maintenant » qui utilise une tâche unique)*.
  - [ ] Une synchronisation **sans** changement (aucune notification).
  - [ ] Une synchronisation avec les notifications **désactivées** (aucun plantage).
  - [ ] Un **échec d'exportation** et l'action « Réessayer ».
  - [ ] Une connexion **réussie**, une connexion **refusée** et une connexion **sans réseau**.
  - [ ] Un **message de test** envoyé depuis la console Firebase.

> ⚠️ **Important :** la correction aura lieu de façon interactive en classe au prochain cours. La présence est donc obligatoire.

---

## Notes de réalisation (comment tester)

| Test | Comment faire |
|---|---|
| Sync avec changement | Database Inspector → table `classement` → modifier les `points` d'une équipe (ex. mettre 999). Puis bouton **« Sync maintenant »** (tâche unique WorkManager). Une notification « Classement » apparaît. |
| Sync sans changement | Appuyer de nouveau sur **« Sync maintenant »** : aucune notification. |
| Notifications désactivées | Paramètres → Applications → Classement LNH → Notifications : désactiver, puis **« Sync maintenant »**. Rien ne s'affiche, le Worker réussit (voir Logcat `SyncClassementWorker`). |
| Échec d'exportation | Bouton **« Export (échec) »** : l'exportation échoue volontairement. La notification d'échec offre **« Réessayer »**, qui relance une exportation normale. |
| Connexion | L'app démarre sur l'écran de connexion. Réussie : `emilys` / `emilyspass`. Refusée : mauvais mot de passe. Sans réseau : mode avion. Onglet **Profil** → « Se déconnecter » ramène à la connexion. |
| Push Firebase | Voir la section ci-dessous. |

### Configuration Firebase

1. Dans la [Console Firebase](https://console.firebase.google.com/), ajouter une application Android avec le package **`cstjean.mobile2.classementlnh`**.
2. Télécharger `google-services.json` et le placer dans le dossier **`app/`**.
3. Synchroniser Gradle. Le plugin `google-services` n'est appliqué que si ce fichier existe, ce qui permet au projet de compiler sans lui.
4. Lancer l'app, accepter la permission de notification et copier le jeton affiché dans Logcat (filtre `FCM_TOKEN`).
5. Console Firebase → **Messaging** → *Créer une campagne* → *Messages Firebase Notifications* → **Envoyer un message de test** → coller le jeton → **Tester**.
