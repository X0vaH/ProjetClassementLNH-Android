package cstjean.mobile2.classementlnh.business

data class EquipeUiModel(
    val abbrev: String,
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