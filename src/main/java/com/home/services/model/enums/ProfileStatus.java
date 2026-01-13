package com.home.services.model.enums;

/**
 * Statut de vérification du profil prestataire
 */
public enum ProfileStatus {
    EN_ATTENTE, // Documents soumis, en attente de validation
    VERIFIE, // Profil vérifié par l'admin
    REJETE, // Documents refusés
    INCOMPLET // Documents manquants
}
