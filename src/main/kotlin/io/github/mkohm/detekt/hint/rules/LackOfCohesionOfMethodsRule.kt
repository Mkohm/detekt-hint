package io.github.mkohm.detekt.hint.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.ThresholdRule
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty

class LackOfCohesionOfMethodsRule(config: Config = Config.empty) : ThresholdRule(config, 8) {
    override val issue = Issue(
        javaClass.simpleName,
        Severity.CodeSmell,
        "This rule reports a file with low LCOM value.",
        Debt.TWENTY_MINS
    )

    private var methodsCount: Int = 0
    private var propertiesTimesReferencesMap = mutableMapOf<KtProperty, Int>()

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        methodsCount++
    }

    override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)

        val timesPropertyIsReferenced = property.accessors.size

        propertiesTimesReferencesMap[property] = timesPropertyIsReferenced
    }

    override fun postVisit(root: KtFile) {
        super.postVisit(root)

        // calculate some stuff.

        val m = methodsCount
        val f = propertiesTimesReferencesMap.keys.size
        val mf_sum = propertiesTimesReferencesMap.values.sum()

        val lcom = 1 - (mf_sum / m * f)
        println(lcom)
    }
}

// remember to test a file with multiple classes to see if the calculation is correct.

