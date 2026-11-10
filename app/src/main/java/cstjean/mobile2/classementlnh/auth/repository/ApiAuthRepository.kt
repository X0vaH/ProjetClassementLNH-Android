package cstjean.mobile2.classementlnh.auth.repository

import cstjean.mobile2.classementlnh.auth.AuthApiService
import cstjean.mobile2.classementlnh.data.ErreurServeurException
import cstjean.mobile2.classementlnh.data.IdentifiantsInvalidesException
import cstjean.mobile2.classementlnh.data.Profil
import cstjean.mobile2.classementlnh.data.ReseauIndisponibleException
import cstjean.mobile2.classementlnh.data.SessionExpireeException
import cstjean.mobile2.classementlnh.auth.SessionManager
import cstjean.mobile2.classementlnh.auth.dto.LoginRequestDto
import cstjean.mobile2.classementlnh.auth.dto.toModel
import okio.IOException
import retrofit2.HttpException

class ApiAuthRepository(
    private val api: AuthApiService,
    private val session: SessionManager
) {

    suspend fun connexion(identifiant: String, motDePasse: String) {
        try {
            val reponse = api.connexion(LoginRequestDto(identifiant, motDePasse))
            session.ouvrirSession(reponse.accessToken)
        } catch (e: HttpException) {
            // Ajout par rapport à la démo : erreur du serveur (5xx)
            if (e.code() >= 500) throw ErreurServeurException()
            throw IdentifiantsInvalidesException()
        } catch (e: IOException) {
            throw ReseauIndisponibleException()
        }
    }

    suspend fun obtenirProfil(): Profil {
        try {
            return api.profil().toModel()
        } catch (e: HttpException) {
            if (e.code() == 401 || e.code() == 403) throw SessionExpireeException()
            // Ajout par rapport à la démo : erreur du serveur (5xx)
            if (e.code() >= 500) throw ErreurServeurException()
            throw e
        } catch (e: IOException) {
            throw ReseauIndisponibleException()
        }
    }

    fun deconnexion() = session.fermerSession()
}
