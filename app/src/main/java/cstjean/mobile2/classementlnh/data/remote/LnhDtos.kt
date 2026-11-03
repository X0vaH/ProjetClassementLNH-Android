package cstjean.mobile2.classementlnh.data.remote

data class ReponseClassementDto(
    val standings: List<EquipeClassementDto> = emptyList()
)

data class TexteLocaliseDto(
    val default: String = ""
)

data class EquipeClassementDto(
    val teamAbbrev: TexteLocaliseDto = TexteLocaliseDto(),
    val teamName: TexteLocaliseDto = TexteLocaliseDto(),
    val divisionName: String = "",
    val conferenceName: String = "",
    val gamesPlayed: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val otLosses: Int = 0,
    val points: Int = 0,
    val conferenceSequence: Int = 0,
    val divisionSequence: Int = 0
)
