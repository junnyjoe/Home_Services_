# Home Services - Marketplace Abidjan

Une plateforme de mise en relation entre clients et prestataires de services à domicile à Abidjan.

## 🚀 Fonctionnalités

- **Authentification** : Inscription/Connexion avec JWT
- **Annonces** : Clients publient des demandes de services
- **Candidatures** : Prestataires postulent aux annonces
- **Messagerie** : Chat entre client et prestataire après acceptation
- **Évaluations** : Notes et avis après prestation
- **Documents** : Vérification d'identité des prestataires

## 🛠️ Technologies

### Backend
- Java 17 + Spring Boot 3.x
- Spring Security + JWT
- Spring Data JPA + PostgreSQL
- Flyway (migrations)
- Maven

### Frontend
- HTML5 / CSS3 / JavaScript (Vanilla)
- Design System personnalisé
- Responsive design

## 📋 Prérequis

- Java 17+
- Maven 3.8+
- PostgreSQL 14+ (ou Docker)

## 🏃 Démarrage rapide

### Mode développement (H2)

```bash
# Cloner le projet
git clone https://github.com/your-username/home-services.git
cd home-services

# Lancer avec le profil dev (H2 en mémoire)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

L'application sera accessible sur http://localhost:8080

### Avec Docker

```bash
# Copier le fichier d'environnement
cp .env.example .env
# Éditer .env avec vos valeurs

# Lancer avec Docker Compose
docker-compose up -d

# Voir les logs
docker-compose logs -f app
```

## 🧪 Tests

```bash
# Lancer tous les tests
./mvnw test

# Lancer les tests d'intégration
./mvnw test -Dtest=*IntegrationTest
```

## 📁 Structure du projet

```
src/
├── main/
│   ├── java/com/home/services/
│   │   ├── config/          # Configuration (Security, JWT)
│   │   ├── controller/      # REST Controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── exception/       # Gestionnaire d'exceptions
│   │   ├── model/           # Entités JPA
│   │   ├── repository/      # Repositories JPA
│   │   └── service/         # Services métier
│   └── resources/
│       ├── static/          # Frontend (HTML/CSS/JS)
│       │   ├── css/
│       │   ├── js/
│       │   └── pages/
│       └── db/migration/    # Scripts Flyway
└── test/                    # Tests unitaires et d'intégration
```

## 🔐 Rôles utilisateurs

| Rôle | Description |
|------|-------------|
| CLIENT | Publie des annonces, sélectionne des prestataires |
| PRESTATAIRE | Postule aux annonces, effectue les prestations |
| ADMIN | Valide les documents, gère la plateforme |

## 📡 API Endpoints

### Authentification
- `POST /api/auth/register` - Inscription
- `POST /api/auth/login` - Connexion

### Annonces
- `GET /api/requests` - Liste des annonces publiées
- `POST /api/requests` - Créer une annonce (CLIENT)
- `GET /api/requests/my` - Mes annonces

### Candidatures
- `POST /api/applications` - Postuler (PRESTATAIRE)
- `POST /api/applications/{id}/accept` - Accepter (CLIENT)
- `GET /api/applications/my` - Mes candidatures

### Messages
- `GET /api/messages/conversations` - Liste des conversations
- `POST /api/messages` - Envoyer un message

### Documents
- `POST /api/documents/upload` - Upload document (PRESTATAIRE)
- `POST /api/documents/{id}/validate` - Valider (ADMIN)

## 🌐 Déploiement

### Variables d'environnement

| Variable | Description | Défaut |
|----------|-------------|--------|
| `SPRING_PROFILES_ACTIVE` | Profil actif | `dev` |
| `DB_PASSWORD` | Mot de passe PostgreSQL | - |
| `JWT_SECRET` | Clé secrète JWT (min 256 bits) | - |
| `APP_UPLOAD_DIR` | Dossier uploads | `/app/uploads` |

### Docker

```bash
# Build l'image
docker build -t home-services .

# Lancer avec docker-compose
docker-compose up -d
```

## 📄 Licence

MIT License

## 👥 Auteurs

- **Home Services Team**

---

Made with ❤️ in Abidjan 🇨🇮
