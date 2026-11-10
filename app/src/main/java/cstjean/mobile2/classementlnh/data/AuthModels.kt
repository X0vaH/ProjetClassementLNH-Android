package cstjean.mobile2.classementlnh.data

data class Profil(val prenom: String, val nom: String, val courriel: String)

class IdentifiantsInvalidesException : Exception("Identifiants invalides")
class SessionExpireeException : Exception("Session expirée")
class ReseauIndisponibleException : Exception("Réseau indisponible")

// Ajout par rapport à la démo : l'énoncé (2d) exige aussi une exception « erreur du serveur »
class ErreurServeurException : Exception("Erreur du serveur")
