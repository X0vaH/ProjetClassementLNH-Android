# 420-5C4-ST – Développement mobile avancé

## Exercice 4 – Services avant-plan et arrière-plan

L’exercice suivant permettra de faire évoluer une application afin de permettre la synchronisation des données (classement LNH) par l’entremise d’une tâche récurrente. Une fonctionnalité d’exportation au format JSON sera également ajoutée.

---

## Objectifs pédagogiques

- Planifier une tâche de synchronisation périodique avec **WorkManager** et gérer les erreurs réseau.
- Gérer la permission de notification lorsque nécessaire et démarrer un **service au premier plan** pour exporter les données locales en JSON.
- Respecter la séparation des responsabilités entre l’interface, le ViewModel, le Repository, la base de données et les tâches en arrière-plan.

---

## Modèle de données

Le modèle de données vous est fourni. Il comprend les entités suivantes :

| Entité | Description |
|---|---|
| `EquipeClassementEntity` | Entité regroupant tous les champs nécessaires pour afficher le classement. |
| `SyncMetadataEntity` | Entité permettant de stocker la date / heure de la dernière synchronisation. |

---

## Structure du projet

Le projet de base est structuré de la façon suivante :

| Package | Contenu |
|---|---|
| `business` | Contient les classes du modèle à utiliser par les ViewModels. |
| `data/remote` | DTO, interface Retrofit et configuration du client HTTP. |
| `database/local` | Entités, DAOs et classe Room `@Database`. |
| `database` | Fonctions d’extension pour convertir les DTO en entités. |
| `repository` | Repository responsable de la synchronisation distante et de l’accès aux données locales. |
| `worker` | Le Worker de synchronisation en arrière-plan devrait être créé dans ce package. |
| `service` | Le service en avant-plan responsable de l’exportation devrait être créé dans ce package. |
| `viewmodel` | ViewModel et UiState de l’écran de classement. |
| `ui` | Écran Compose, affichage de l’état et commandes de synchronisation/exportation. |

---

## Fonctionnalités à réaliser

### 1. Synchronisation périodique avec WorkManager

- [x] **a.** Créez un Worker qui récupère les données distantes et met à jour Room.
- [x] **b.** Planifiez une synchronisation périodique (toutes les 4 heures) avec une contrainte de connexion réseau. Cette planification doit se faire au démarrage de l’application.
- [x] **c.** Utilisez un travail unique pour éviter de planifier plusieurs synchronisations périodiques identiques.
- [x] **d.** Retournez un résultat approprié en cas de succès ou d’échec temporaire.

> **Note :** La périodicité de WorkManager est indicative : l’exécution n’est pas garantie à une heure précise.

### 2. Exportation JSON par un service au premier plan

- [x] **a.** Ajoutez un bouton **Exporter** qui démarre un service au premier plan.
- [x] **b.** Le service doit lire les données depuis Room et produire un fichier JSON contenant les équipes et leurs statistiques.
- [x] **c.** Enregistrez le fichier dans le répertoire `Download` de l’appareil.
- [x] **d.** Affichez une notification pendant le traitement et signalez la réussite ou l’échec de l’exportation.
- [x] **e.** Déclarez le service et les permissions nécessaires dans le manifeste, conformément à la version Android ciblée. Demandez `POST_NOTIFICATIONS` sur Android 13 et versions ultérieures lorsque nécessaire.

### 3. BONUS (+1)

- [ ] Modifier le projet pour inclure les logos d’équipe dans la base de données et dans l’affichage.

---

## Contraintes et remise

- L’application doit rester utilisable si l’API est temporairement inaccessible.
- Testez au minimum :
  - une synchronisation réussie;
  - une synchronisation sans réseau;
  - une exportation JSON.
- L’API de la LNH est accessible publiquement, mais elle n’est pas accompagnée d’une documentation officielle complète destinée aux développeurs. Certains développeurs ont cependant produit de la documentation :
  - <https://github.com/pseudo-r/Public-NHL-API>
  - <https://github.com/Zmalski/NHL-API-Reference>

> ⚠️ **Important :** La correction aura lieu de façon interactive en classe au prochain cours. La présence est donc obligatoire.

---

*Département d’informatique, Cégep Saint-Jean-sur-Richelieu*
