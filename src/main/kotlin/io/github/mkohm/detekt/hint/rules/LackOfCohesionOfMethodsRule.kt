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
import org.jetbrains.kotlin.psi.psiUtil.isPublic

class LackOfCohesionOfMethodsRule(config: Config = Config.empty) : ThresholdRule(config, 0) {
    private var currentPublicFunction: KtNamedFunction? = null

    override val issue = Issue(
        javaClass.simpleName,
        Severity.CodeSmell,
        "This rule reports a file with low LCOM value.",
        Debt.TWENTY_MINS
    )

    private val thresholdValue = valueOrNull<String>("threshold")?.toDouble() ?: error("You must specify a threshold value in detekt.yml")


    // todo: consider starting at 1
    private var mf_sum = 0
    private var fieldCount = 0
    private var publicMethodsCount = 0
    private var propertyNames = arrayListOf<String>()

    private var publicMethodToFieldReferences = mutableMapOf<KtNamedFunction, List<KtReferenceExpression>>()

    override fun visitClass(klass: KtClass) {
        mf_sum = 0
        fieldCount = 0
        publicMethodsCount = 0
        propertyNames = arrayListOf()
        publicMethodToFieldReferences.clear()

        super.visitClass(klass)

        if (publicMethodsCount == 0 || fieldCount == 0) return

        mf_sum = mf_sum.plus(publicMethodToFieldReferences.toList().sumBy { it.second.size })


        val lcom = 1 - (mf_sum.toDouble() / (publicMethodsCount * fieldCount))
        println("${klass.name} has LCOM value: $lcom")

        if (lcom > thresholdValue) {
            report(
                CodeSmell(issue, Entity.from(klass), "${klass.name} have a too high LCOM value: $lcom")
            )
        }


        // for each property:
           // for each public method
                // check if property is referenced in the public method
                // if not referenced - check private methods
    }

    // Assume that a public function is visited first, and then its private functions?
    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)

        if (function.isPublic) {
            publicMethodsCount++
            currentPublicFunction = function
        }

        // todo: will probably find multiple of the same references in methods with multiple field references.

        val expression = function.bodyExpression as KtExpression

        // How many references to fields there is in the functions expression
        val referenceExpression = expression.collectDescendantsOfType<KtReferenceExpression> {
            propertyNames.contains(
                it.text
            )
        }

        val currentList = publicMethodToFieldReferences[function] ?: arrayListOf()
        val newList = ArrayList(currentList)
        newList.addAll(referenceExpression)
        publicMethodToFieldReferences[function] = newList
        //publicMethodToFieldReferences[function] = referenceExpression
        //.distinctBy { it.name }

        // create a map with {PublicMethod -> NumberOfReferencedProperties}
        // and then for each private method of that public method -> add properties as we find them.


    }

    override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)
        fieldCount++

        property.name?.let { propertyNames.add(it) }
    }
}
