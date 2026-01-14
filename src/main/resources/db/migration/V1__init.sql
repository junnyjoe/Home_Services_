-- Flyway migration: initial schema for Home Services

-- 1. Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    telephone VARCHAR(20),
    role VARCHAR(20) NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Categories table
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    icone VARCHAR(50),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- 3. Provider Profiles table
CREATE TABLE provider_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    bio TEXT,
    competences TEXT, -- JSON string
    quartier VARCHAR(100),
    tarif_horaire NUMERIC(10, 2),
    note_globale DOUBLE PRECISION DEFAULT 0.0,
    nombre_avis INTEGER DEFAULT 0,
    statut VARCHAR(20) NOT NULL DEFAULT 'INCOMPLET',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. Provider Categories (Join table)
CREATE TABLE provider_categories (
    provider_id BIGINT NOT NULL REFERENCES provider_profiles(id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (provider_id, category_id)
);

-- 5. Service Requests table
CREATE TABLE service_requests (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES users(id),
    category_id BIGINT NOT NULL REFERENCES categories(id),
    titre VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    quartier VARCHAR(100) NOT NULL,
    adresse VARCHAR(255),
    budget_min NUMERIC(10, 2),
    budget_max NUMERIC(10, 2),
    date_prestation DATE,
    urgence VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    statut VARCHAR(20) NOT NULL DEFAULT 'BROUILLON',
    nombre_candidatures INTEGER DEFAULT 0,
    selected_provider_id BIGINT REFERENCES users(id),
    expires_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. Applications table
CREATE TABLE applications (
    id BIGSERIAL PRIMARY KEY,
    service_request_id BIGINT NOT NULL REFERENCES service_requests(id) ON DELETE CASCADE,
    provider_id BIGINT NOT NULL REFERENCES users(id),
    message TEXT,
    proposed_price NUMERIC(10, 2),
    proposed_days INTEGER,
    statut VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE',
    client_response VARCHAR(500),
    responded_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (service_request_id, provider_id)
);

-- 7. Messages table
CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    sender_id BIGINT NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 8. Reviews table
CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL UNIQUE REFERENCES applications(id),
    client_id BIGINT NOT NULL REFERENCES users(id),
    provider_id BIGINT NOT NULL REFERENCES users(id),
    note INTEGER NOT NULL CHECK (note >= 1 AND note <= 5),
    commentaire TEXT,
    note_qualite INTEGER CHECK (note_qualite >= 1 AND note_qualite <= 5),
    note_ponctualite INTEGER CHECK (note_ponctualite >= 1 AND note_ponctualite <= 5),
    note_communication INTEGER CHECK (note_communication >= 1 AND note_communication <= 5),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 9. Documents table
CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    provider_id BIGINT NOT NULL REFERENCES users(id),
    type VARCHAR(20) NOT NULL,
    filename VARCHAR(255),
    url VARCHAR(255) NOT NULL,
    statut VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE',
    motif_refus TEXT,
    validated_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Initial categories data
INSERT INTO categories (nom, description, icone) VALUES 
('Ménage', 'Services de nettoyage et entretien de maison', 'broom'),
('Plomberie', 'Réparation de fuites et installations sanitaires', 'faucet'),
('Électricité', 'Travaux électriques et dépannage', 'bolt'),
('Jardinage', 'Entretien d''espaces verts', 'leaf'),
('Bricolage', 'Petites réparations et montage de meubles', 'hammer');
