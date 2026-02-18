package nl.bikecheck.model

data class ChecklistItem(
    val id: String,
    val description: String,
    var isChecked: Boolean = false
)
