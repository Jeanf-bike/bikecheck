package nl.bikecheck.model

data class ChecklistCategory(
    val name: String,
    val icon: String,
    val items: MutableList<ChecklistItem>
)
