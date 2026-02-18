package nl.bikecheck.model

data class Bike(
    val name: String,
    val subtitle: String,
    val subtype: String = "",
    val imageRes: Int,
    val accentColor: Int
)
