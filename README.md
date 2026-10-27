# Kaya Chat

Kaya Chat est une application de messagerie instantanée Java en réseau, composée d'un serveur et de plusieurs clients graphiques Swing. Elle permet aux utilisateurs de discuter en mode public, en message privé et dans des salons de discussion.

## Fonctionnalités

- Serveur multi-utilisateurs
- Messagerie globale (broadcast)
- Messages privés entre utilisateurs
- Salons de discussion avec création, jointure et départ
- Liste des utilisateurs connectés
- Liste des salons disponibles
- Interface graphique Swing pour les clients

## Structure du projet

- `ChatServer.java` : serveur central gérant les connexions, les salons et les messages
- `ChatClient.java` : client graphique qui permet de se connecter, envoyer des messages et gérer les salons
- `Message.java` : structure des messages échangés entre le client et le serveur

## Prérequis

- Java JDK 8 ou plus récent
- Un terminal ou un IDE Java (Eclipse, IntelliJ IDEA, VS Code, etc.)

## Lancer le projet

### 1. Compiler le projet

Depuis le dossier du projet :

```bash
javac *.java
```

### 2. Démarrer le serveur

```bash
java ChatServer
```

Le serveur écoute par défaut sur le port `12345`.

### 3. Lancer un ou plusieurs clients

Dans un autre terminal :

```bash
java ChatClient
```

Une boîte de dialogue s'ouvre alors pour demander :

- l'adresse du serveur (`localhost` par défaut)
- le port (`12345`)
- le nom d'utilisateur

## Utilisation

- Envoyer un message à tous : sélectionnez `All` dans la liste de destinataires
- Envoyer un message privé : double-cliquez sur un utilisateur dans la liste
- Créer un salon : utilisez le bouton `Create`
- Rejoindre un salon : sélectionnez un salon puis cliquez sur `Join`
- Quitter un salon : sélectionnez un salon puis cliquez sur `Leave`

## Remarques

- Le serveur doit être démarré avant les clients.
- Les clients se connectent au serveur via `localhost` sur le port `12345` par défaut.
- Le projet utilise la sérialisation Java pour transmettre les objets `Message` entre le client et le serveur.

## Exemple de scénario

1. Démarrer `ChatServer`
2. Ouvrir deux ou plusieurs instances de `ChatClient`
3. Connecter chaque client avec un pseudo différent
4. Envoyer des messages globaux ou privés
5. Créer et rejoindre des salons pour communiquer en groupe

## Licence

Aucune licence spécifique n'est définie dans ce projet pour le moment.
