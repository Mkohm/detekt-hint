package io.github.mkohm.detekt.hint.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
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

class LackOfCohesionOfMethodsRule(config: Config = Config.empty) : Rule(config) {
    private lateinit var publicMethods: List<KtNamedFunction>
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

    override fun visitClass(klass: KtClass) {
        if (klass.isInterface() || klass.isEnum() || klass.isInner()) return

        mf_sum = 0
        publicMethods = getPublicMethods(klass)
        val publicMethodsCount = publicMethods.size
        val propertyCount = getPropertyCount(klass)

        super.visitClass(klass)

        if (publicMethodsCount == 0 || propertyCount == 0) return

        println("properties: $propertyCount, methods: $publicMethodsCount, mf: $mf_sum")
        val lcom = 1 - (mf_sum.toDouble() / (publicMethodsCount * propertyCount))
        println("Class ${klass.name} has LCOM: $lcom")
        if (lcom > thresholdValue) {
            report(
                CodeSmell(issue, Entity.from(klass), "${klass.name} have a too high LCOM value: $lcom")
            )
        }
    }

   // todo: include properties from primary constructor
    override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)
        if (propertyIsDeclaredOutsideClass(property)) return



        for (publicMethod in publicMethods) {
            val referenceExpression = getReferencesOfProperty(publicMethod, property)

            if (referenceExpression.isNotEmpty()) {
                mf_sum++
                continue
            }

            // Fetch all methods that is called from this public function, recursively.
            val allCalleesFromThisPublicMethod = getCallees(property, publicMethod, arrayListOf(publicMethod))

            // We look for references of the property, and as soon as we find it, we increase mf_sum and break so that we can look for references in other public methods.
            for (privateMethod in allCalleesFromThisPublicMethod) {

                // Get references in this private function
                val references = getReferencesOfProperty(privateMethod, property)

                if (references.isNotEmpty()) {
                    mf_sum++
                    break
                }
            }
        }
    }

    private fun propertyIsDeclaredOutsideClass(property: KtProperty) =
        property.containingClass() == null

    private fun getPublicMethods(klass: KtClass): List<KtNamedFunction> {
        return klass.collectDescendantsOfType<KtNamedFunction> { it.isPublic }
    }

    private fun getPropertyCount(klass: KtClass): Int {
        return klass.getProperties().size
    }

    private fun getReferencesOfProperty(
        privateMethod: KtNamedFunction,
        property: KtProperty
    ): List<KtReferenceExpression> {
        return privateMethod.bodyExpression?.collectDescendantsOfType { reference ->
            isReferenceOfPropertyClass(reference, property) && reference.text == property.name
        } ?: arrayListOf()
    }

    /**
     * We need to ensure that the found reference not only have the same name as the property, but if it is a member of the correct class as well.
     */
    private fun isReferenceOfPropertyClass(
        reference: KtReferenceExpression,
        property: KtProperty
    ) =
        reference.getResolvedCall(bindingContext)?.resultingDescriptor?.containingDeclaration?.name.toString() == property.containingClass()?.name

    private fun isCalleeInPropertyClass(
        callee: KtNamedFunction,
        property: KtProperty
    ) =
        callee.containingClass()?.name == property.containingClass()?.name

    private fun getCallees(
        property: KtProperty,
        publicMethod: KtNamedFunction,
        foundCallees: List<KtNamedFunction>
    ): ArrayList<KtNamedFunction> {

        // If we have any call expressions inside the parent function with the same name as the parent function - There are some possibilities;
        // 1. calling another function with the same name, but with different signature
        // 2. calling the supermethod
        // 3. calles itself - we have recursion.
        // Todo: create good test cases that will cover these different edge-cases

        val callees =
            ArrayList(publicMethod.collectDescendantsOfType<KtCallExpression>()
                .mapNotNull {
                    try {
                        it.getResolvedCall(bindingContext)?.resultingDescriptor?.findPsi() as KtNamedFunction
                    } catch (e: java.lang.ClassCastException) {
                        null
                    }
                }.filter {
                    !foundCallees.contains(it) && isCalleeInPropertyClass(
                        it,
                        property
                    ) // should include test that this function is defined in the property class. For the test, the bindingcontext will not resolve other classes, but that could change in a real context.
                }.distinct().ifEmpty {
                    return arrayListOf()
                })

        callees.addAll(foundCallees)


        for (callee in callees) {
            callees + getCallees(property, callee, callees)
        }

        return callees
    }
}
