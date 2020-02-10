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
    private var propertiesTimesReferencesMap = arrayListOf<KtProperty>()

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        methodsCount++


        val references = function.references
      //  val fields = references.filter
    }

    override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)

        val howManyMethodsThatReferenceThisProperty = countReferences()
       // property.accessors

        propertiesTimesReferencesMap.add(property)
    }


    fun countReferences(): Int {

      //  for (method in klass) {
        //    if (method contains property)
        // count++





        return 0
    }

    override fun postVisit(root: KtFile) {
        super.postVisit(root)

        // calculate some stuff.

        val m = methodsCount
       // val f = propertiesTimesReferencesMap.keys.size
       // val mf_sum = propertiesTimesReferencesMap.values.sum()

       // val lcom = 1 - (mf_sum / m * f)
       // println(lcom)
    }
}

// remember to test a file with multiple classes to see if the calculation is correct.

