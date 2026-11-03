package cstjean.mobile2.classementlnh.database.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import cstjean.mobile2.classementlnh.data.remote.EquipeClassementDto

@Entity(tableName = "classement")
data class EquipeClassementEntity(
    @PrimaryKey val abbrev: String,
    val nom: String,
    val division: String,
    val conference: String,
    val matchsJoues: Int,
    val victoires: Int,
    val defaites: Int,
    val defaitesProlongation: Int,
    val points: Int,
    val rangConference: Int,
    val rangDivision: Int
)

fun EquipeClassementDto.toEntity() = EquipeClassementEntity(
    abbrev = teamAbbrev.default,
    nom = teamName.default,
    division = divisionName,
    conference = conferenceName,
    matchsJoues = gamesPlayed,
    victoires = wins,
    defaites = losses,
    defaitesProlongation = otLosses,
    points = points,
    rangConference = conferenceSequence,
    rangDivision = divisionSequence
)
