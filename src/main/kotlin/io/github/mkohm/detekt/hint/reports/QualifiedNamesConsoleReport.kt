package io.github.mkohm.detekt.hint.reports

import io.gitlab.arturbosch.detekt.api.ConsoleReport
import io.gitlab.arturbosch.detekt.api.Detektion

class QualifiedNamesConsoleReport : ConsoleReport() {

    override fun render(detektion: Detektion): String? {
        return qualifiedNamesReport(detektion)
    }
}