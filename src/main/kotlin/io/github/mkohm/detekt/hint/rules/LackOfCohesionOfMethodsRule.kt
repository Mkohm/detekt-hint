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
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.containingClass
import org.jetbrains.kotlin.psi.psiUtil.isPublic
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall

/**
 * A rule that notifies if there is too much lack of cohesion.
 */
class LackOfCohesionOfMethodsRule(config: Config = Config.empty) : Rule(config) {

    val cachedResult = mutableMapOf<KtNamedFunction, List<KtNamedFunction>>()

    override val issue = Issue(
        javaClass.simpleName,
        Severity.CodeSmell,
        "This rule reports a file with low LCOM value.",
        Debt.TWENTY_MINS
    )
    private val thresholdValue =
        valueOrNull<String>("threshold")?.toDouble() ?: error("You must specify a threshold value in detekt.yml")

    /**
     * The number of properties in the class. Includes both properties that comes
     */
    private var propertyCount: Int = 0
    private lateinit var publicMethods: List<KtNamedFunction>
    // todo: consider starting at 1
    private var mf_sum = 0

    override fun visitNamedDeclaration(declaration: KtNamedDeclaration) {
        super.visitNamedDeclaration(declaration)

        if ((propertyIsMember(declaration) || isPropertyFromPrimaryConstructor(declaration)) && hasContainingClass(declaration)) {
            propertyCount++

            val containingClass = declaration.containingClass()!!
            searchForReferencesInPublicMethods(declaration, containingClass)
        }
    }

    private fun hasContainingClass(declaration: KtNamedDeclaration) = declaration.containingClass() != null

    private fun propertyIsMember(declaration: KtNamedDeclaration): Boolean {
        return try {
            // We only want to return true if the declaration is a true member of the class.
            // If a property is declared inside an anonymous class, it is a member, but the fqname will be null, and we can therefore discard it.
            return (declaration as KtProperty).isMember && declaration.fqName != null
        } catch (e: ClassCastException) {
            false
        }
    }

    override fun visitClass(klass: KtClass) {
        if (klass.isInterface() || klass.isEnum() || klass.isInner()) return

        propertyCount = 0
        mf_sum = 0
        publicMethods = getPublicMethods(klass)
        val publicMethodsCount = publicMethods.size

        super.visitClass(klass)

        if (publicMethodsCount == 0 || propertyCount == 0) return

        val lcom = 1 - (mf_sum.toDouble() / (publicMethodsCount * propertyCount))
        println("Class ${klass.name} has LCOM: $lcom, properties: $propertyCount, methods: $publicMethodsCount, mf: $mf_sum")
        publicMethods.forEach { println(it.name) }
        if (lcom > thresholdValue) {
            report(
                CodeSmell(issue, Entity.from(klass), "${klass.name} have a too high LCOM value: $lcom")
            )
        }
    }

    private fun isPropertyFromPrimaryConstructor(declaration: KtNamedDeclaration) =
        (declaration is KtParameter) && declaration.hasValOrVar()

    private fun searchForReferencesInPublicMethods(property: KtNamedDeclaration, containingClass: KtClass) {
        val publicMethods = getPublicMethods(containingClass)
        for (publicMethod in publicMethods) {
            println("\nLooking for references in ${publicMethod.name}:")
            val referenceExpression = getReferencesOfProperty(publicMethod, property)

            if (referenceExpression.isNotEmpty()) {
                mf_sum++
                println("Found reference of ${property.name} in method ${publicMethod.name}, continuing with next public method.")
                continue
            }

            // Fetch all methods that is called from this public function, recursively.
            val allCalleesFromThisPublicMethod = getCallees(property, publicMethod, arrayListOf(publicMethod))
            println("Callees of ${publicMethod.name}: (size: ${allCalleesFromThisPublicMethod.size})")
            allCalleesFromThisPublicMethod.forEach { println(it.name) }

            // We look for references of the property, and as soon as we find it, we increase mf_sum and break so that we can look for references in other public methods.
            for (privateMethod in allCalleesFromThisPublicMethod) {

                // Get references in this private function
                val references = getReferencesOfProperty(privateMethod, property)

                if (references.isNotEmpty()) {
                    mf_sum++
                    println("Found reference of ${property.name} in private method ${privateMethod.name} called from ${publicMethod.name}")

                    break
                }
            }
        }
    }

    private fun getPublicMethods(klass: KtClass): List<KtNamedFunction> {
        return klass.collectDescendantsOfType<KtNamedFunction> { it.isPublic }
    }

    private fun getReferencesOfProperty(
        method: KtNamedFunction,
        property: KtNamedDeclaration
    ): List<KtReferenceExpression> {
        return method.bodyExpression?.collectDescendantsOfType { reference ->
            isReferenceOfPropertyClass(reference, method) && reference.text == property.name
        } ?: arrayListOf()
    }

    /**
     * We need to ensure that the found reference not only have the same name as the property, but if it is a member of the correct class as well.
     */
    private fun isReferenceOfPropertyClass(
        reference: KtReferenceExpression,
        method: KtNamedFunction
    ) =
        reference.getResolvedCall(bindingContext)?.resultingDescriptor?.containingDeclaration?.name.toString() == method.containingClass()?.name

    private fun isCalleeInPropertyClass(
        callee: KtNamedFunction,
        property: KtNamedDeclaration
    ) =
        callee.containingClass()?.name == property.containingClass()?.name

    private fun getCallees(
        property: KtNamedDeclaration,
        publicMethod: KtNamedFunction,
        foundCallees: List<KtNamedFunction>
    ): ArrayList<KtNamedFunction> {
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

            // Memoize the results of the calculation to speed things up
            var newResult = listOf<KtNamedFunction>()
            if (cachedResult.containsKey(callee)) {
                newResult = cachedResult[callee]!!
            } else {
                newResult = getCallees(property, callee, callees)
                cachedResult[callee] = newResult
            }

            callees + newResult
        }

        return callees
    }
}