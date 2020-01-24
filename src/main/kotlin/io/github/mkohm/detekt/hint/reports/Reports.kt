package io.github.mkohm.detekt.hint.reports

import io.gitlab.arturbosch.detekt.api.Detektion
import io.github.mkohm.detekt.hint.processors.fqNamesKey

public fun



    qualifiedNamesReport(detektion: Detektion):




    String? {
    val fqNames = detektion.getData(fqNamesKey)
    println("fqNames: $fqNames")
    if (fqNames.isNullOrEmpty()) return null

    return with(StringBuilder()) {
        for (name in fqNames) {
            append("$name\n")
        }
        toString()
    }
}
