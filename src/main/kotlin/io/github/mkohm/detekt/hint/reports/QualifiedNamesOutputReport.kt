package io.github.mkohm.detekt.hint.reports

import io.gitlab.arturbosch.detekt.api.Detektion
import io.gitlab.arturbosch.detekt.api.OutputReport

class QualifiedNamesOutputReport : OutputReport() {

    var fileName: String = "fqNames"
    override val ending: String = "txt"

    override fun render(detektion: Detektion): String? {
        return qualifiedNamesReport(detektion)
    }
}
