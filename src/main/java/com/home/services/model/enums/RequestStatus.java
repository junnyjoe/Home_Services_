package com.home.services.model.enums;

/**
 * Statut d'une annonce de service
 */
public enum RequestStatus {
    BROUILLON, // Non publié
    PUBLIEE, // Visible par les prestataires
    EN_COURS, // Un prestataire a été sélectionné
    TERMINEE, // Service effectué
    ANNULEE // Annulée par le client
}
