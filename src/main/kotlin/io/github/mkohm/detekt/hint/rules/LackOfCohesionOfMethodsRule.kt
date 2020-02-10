package io.github.mkohm.detekt.hint.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.ThresholdRule
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

class LackOfCohesionOfMethodsRule(config: Config = Config.empty) : ThresholdRule(config, 0) {
    override val issue = Issue(
        javaClass.simpleName,
        Severity.CodeSmell,
        "This rule reports a file with low LCOM value.",
        Debt.TWENTY_MINS
    )

    // todo: consider starting at 1
    private var mf_sum = 0
    private var f = 0
    private var methodsCount = 0
    private var propertyNames = arrayListOf<String>()

    override fun visitClass(klass: KtClass) {
        mf_sum = 0
        f = 0
        methodsCount = 0
        propertyNames = arrayListOf<String>()

        super.visitClass(klass)

        val lcom = 1 - (mf_sum.toDouble() / (methodsCount * f))
        println(lcom)

        if (lcom > threshold) {
            report(
                CodeSmell(issue, Entity.from(klass), "This class have a too high LCOM value: $lcom")
            )
        }
    }



    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        methodsCount++

        val reference_expression =
            (function.bodyExpression as KtExpression).collectDescendantsOfType<KtReferenceExpression> {
                propertyNames.contains(
                    it.text
                )
            }

        mf_sum = mf_sum.plus(reference_expression.size)
    }

    override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)
        f++

        property.name?.let { propertyNames.add(it) }
    }


}

// remember to test a file with multiple classes to see if the calculation is correct.