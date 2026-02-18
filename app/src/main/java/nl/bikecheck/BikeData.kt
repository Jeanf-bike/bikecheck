package nl.bikecheck

import nl.bikecheck.model.ChecklistItem

object BikeData {

    val checklists: Map<String, List<ChecklistItem>> = mapOf(
        "Ultimate" to listOf(
            ChecklistItem("u1",  "Raceschoenen"),
            ChecklistItem("u2",  "Twee bidons"),
            ChecklistItem("u3",  "Reparatietas en bandje"),
            ChecklistItem("u4",  "Wahoo groot"),
            ChecklistItem("u5",  "Handschoenen"),
            ChecklistItem("u6",  "Bril"),
            ChecklistItem("u7",  "Muts"),
            ChecklistItem("u8",  "Overschoenen"),
            ChecklistItem("u9",  "Eten"),
            ChecklistItem("u10", "Verlichting")
        ),
        "Atalaya" to listOf(
            ChecklistItem("a1",  "Klikschoen of Flatschoen"),
            ChecklistItem("a2",  "Twee bidons"),
            ChecklistItem("a3",  "Reparatiebidon"),
            ChecklistItem("a4",  "Batterij"),
            ChecklistItem("a5",  "Wahoo groot"),
            ChecklistItem("a6",  "Handschoenen"),
            ChecklistItem("a7",  "Bril"),
            ChecklistItem("a8",  "Muts"),
            ChecklistItem("a9",  "Overschoenen"),
            ChecklistItem("a10", "Eten"),
            ChecklistItem("a11", "Verlichting")
        ),
        "Neuron" to listOf(
            ChecklistItem("n1",  "Klikschoen of Flatschoen"),
            ChecklistItem("n2",  "Één bidon of drinkzak"),
            ChecklistItem("n3",  "Reparatietas"),
            ChecklistItem("n4",  "Batterij"),
            ChecklistItem("n5",  "Wahoo groot"),
            ChecklistItem("n6",  "Handschoenen"),
            ChecklistItem("n7",  "Bril"),
            ChecklistItem("n8",  "Muts"),
            ChecklistItem("n9",  "Overschoenen"),
            ChecklistItem("n10", "Eten"),
            ChecklistItem("n11", "Verlichting")
        ),
        "Exceed" to listOf(
            ChecklistItem("e1",  "Klikschoenen"),
            ChecklistItem("e2",  "Twee bidons"),
            ChecklistItem("e3",  "Reparatietas"),
            ChecklistItem("e4",  "Wahoo klein"),
            ChecklistItem("e5",  "Handschoenen"),
            ChecklistItem("e6",  "Bril"),
            ChecklistItem("e7",  "Muts"),
            ChecklistItem("e8",  "Overschoenen"),
            ChecklistItem("e9",  "Eten"),
            ChecklistItem("e10", "Verlichting")
        )
    )

    fun defaultItemsFor(bikeName: String): MutableList<ChecklistItem> =
        checklists[bikeName]?.map { it.copy() }?.toMutableList() ?: mutableListOf()

    /**
     * Weergerelateerde extra items.
     * UV >= 3 (matig of hoger) → zonnebescherming toevoegen.
     * Temp < 5°C             → warme uitrusting toevoegen.
     */
    fun conditionalItems(tempCelsius: Double, uvIndex: Double): List<ChecklistItem> {
        val items = mutableListOf<ChecklistItem>()

        if (uvIndex >= 3.0) {
            items += ChecklistItem("weather_sunscreen", "Zonnebrand",   weatherTag = "☀️ UV")
            items += ChecklistItem("weather_lipbalm",   "Lipbalsem",    weatherTag = "☀️ UV")
        }

        if (tempCelsius < 5.0) {
            items += ChecklistItem("weather_warm_hat",     "Muts",                 weatherTag = "🥶 Koud")
            items += ChecklistItem("weather_warm_gloves",  "Warme handschoenen",   weatherTag = "🥶 Koud")
            items += ChecklistItem("weather_warm_overshoes","Overschoenen",         weatherTag = "🥶 Koud")
        }

        return items
    }
}
