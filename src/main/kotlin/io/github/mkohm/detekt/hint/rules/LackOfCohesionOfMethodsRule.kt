package io.github.mkohm.detekt.hint.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.ThresholdRule
import org.jetbrains.kotlin.js.resolve.diagnostics.findPsi
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.containingClass
import org.jetbrains.kotlin.psi.psiUtil.isPublic
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall

class LackOfCohesionOfMethodsRule(config: Config = Config.empty) : ThresholdRule(config, 0) {
    private var currentPublicFunction: KtNamedFunction? = null

    override val issue = Issue(
        javaClass.simpleName,
        Severity.CodeSmell,
        "This rule reports a file with low LCOM value.",
        Debt.TWENTY_MINS
    )

    private val thresholdValue =
        valueOrNull<String>("threshold")?.toDouble() ?: error("You must specify a threshold value in detekt.yml")

    // todo: consider starting at 1
    private var mf_sum = 0
    private var fieldCount = 0
    private var publicMethodsCount = 0

    override fun visitClass(klass: KtClass) {
        mf_sum = 0
        fieldCount = 0
        publicMethodsCount = 0

        super.visitClass(klass)

        if (publicMethodsCount == 0 || fieldCount == 0) return

        val lcom = 1 - (mf_sum.toDouble() / (publicMethodsCount * fieldCount))
        if (lcom > thresholdValue) {
            report(
                CodeSmell(issue, Entity.from(klass), "${klass.name} have a too high LCOM value: $lcom")
            )
        }
        //    mf_sum = mf_sum.plus(publicMethodToFieldReferences.toList().sumBy { it.second.size })

        //    println("${klass.name} has LCOM value: $lcom")
    }

    override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)
        fieldCount++
        val publicMethods =
            property.containingClass()?.collectDescendantsOfType<KtNamedFunction> { it.isPublic } ?: arrayListOf()
        publicMethodsCount = publicMethods.size

        for (publicMethod in publicMethods) {
            val referenceExpression = publicMethod.bodyExpression?.collectDescendantsOfType<KtReferenceExpression> {
                it.text == property.name
            } ?: arrayListOf()

            if (referenceExpression.isNotEmpty()) {
                mf_sum++
                return
            }

            if (referenceExpression.isEmpty()) {
                // look in all local methods that is called from this public function, recursively
                val allCalleesFromThisPublicMethod = getCallees(publicMethod)
                val a = 1

                // val allPrivateMethodsOfThisPublicMethod = allPrivateMethods.filter { it.name.toString() == publicMethod.findDescendantOfType<KtCallExpression>(it.name) }


                 for (privateMethod in allCalleesFromThisPublicMethod) {
                     val referenceExpression = privateMethod.bodyExpression?.collectDescendantsOfType<KtReferenceExpression> {
                         it.text == property.name
                     } ?: arrayListOf()

                     if (referenceExpression.isNotEmpty()) {
                         mf_sum ++
                         return
                     }
                 }
            }
        }

        // for each public method
        // check if property is referenced in the public method
        // if not referenced - check private methods
    }

    private fun getCallees(publicMethod: KtNamedFunction): List<KtNamedFunction> {
        val doesNotHaveDecendants =
            ArrayList(publicMethod.collectDescendantsOfType<KtCallExpression>().map { it.getResolvedCall(bindingContext)?.resultingDescriptor?.findPsi() as KtNamedFunction }).size == 0

        if (doesNotHaveDecendants) {
            return arrayListOf<KtNamedFunction>()
        }
        val callees = ArrayList(publicMethod.collectDescendantsOfType<KtCallExpression>().map {
            it.getResolvedCall(bindingContext)?.resultingDescriptor?.findPsi() as KtNamedFunction
        })

        for (callee in callees) {
            return callees + getCallees(callee)
        }

        return listOf<KtNamedFunction>()
    }

}
