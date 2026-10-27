# KayaTravel
Agenda interactif collectif


KayaTravel est une application Java Swing pour la gestion d’événements et de notifications d’une structure touristique ou de voyage. Elle permet notamment de :

- créer un compte et se connecter,
- gérer des utilisateurs avec différents rôles,
- ajouter, modifier et supprimer des événements,
- consulter les notifications,
- commenter les activités et suivre l’activité de la plateforme.

## Fonctionnalités principales

- Authentification utilisateur
- Inscription avec validation de mot de passe et email
- Gestion des rôles : admin / employé / autre type d’utilisateur
- Ajout et gestion d’événements
- Notifications dynamiques
- Système de commentaires
- Interface graphique Swing moderne
- Sauvegarde locale des données dans des fichiers .dat

## Stack technique

- Java 21
- Maven
- Swing (interface graphique)
- Sérialisation Java pour le stockage local des données

## Prérequis

- JDK 21 ou plus
- Maven installé
- Windows, Linux ou macOS

## Cloner et lancer le projet

```bash
git clone <url-du-projet>
cd KAYATRAVEL
mvn clean package
java -jar target/kayatravel-ihm-0.1.0.jar
```

## Lancement rapide sous Windows

Un script est aussi fourni :

```bat
KayaTravel.bat
```

Il démarre directement le fichier JAR généré dans le dossier `target`.

## Comptes par défaut

L’application crée automatiquement des comptes d’exemple s’ils n’existent pas :

- Admin : `admin@kaya.com` / `Admin123`
- Employé : `emp@kaya.com` / `Employe123`

## Structure du projet

```text
KAYATRAVEL/
├── src/
│   └── main/
│       └── java/
│           ├── Main.java
│           ├── controller/
│           ├── model/
│           └── view/
├── pom.xml
├── KayaTravel.bat
├── target/
├── users.dat
├── events.dat
├── notifications.dat
├── comments.dat
└── README.md
```

## Données enregistrées

Les données sont stockées localement dans le dossier du projet sous forme de fichiers :

- `users.dat`
- `events.dat`
- `notifications.dat`
- `comments.dat`

## Commandes Maven utiles

```bash
mvn compile
mvn test
mvn package
```

## Notes

- Le projet est une application desktop locale, donc les données sont enregistrées sur le disque local.
- Le point d’entrée principal est la classe `Main`.
- Le fichier de configuration Maven exige Java 21.

## Licence

Ce projet est fourni sans licence spécifique dans le dépôt actuel.
