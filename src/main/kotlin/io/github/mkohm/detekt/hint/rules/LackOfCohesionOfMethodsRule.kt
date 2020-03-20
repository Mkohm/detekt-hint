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
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.containingClass
import org.jetbrains.kotlin.psi.psiUtil.isProtected
import org.jetbrains.kotlin.psi.psiUtil.isPublic
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall

/**
 * A rule that notifies if there is too much lack of cohesion. Remember to configure it correctly in the detekt.yml.
 */
class LackOfCohesionOfMethodsRule(config: Config = Config.empty) : Rule(config) {

    private val memoizedResults = mutableMapOf<KtExpression, List<KtExpression>>()

    override val issue = Issue(
        javaClass.simpleName,
        Severity.CodeSmell,
        "This rule reports a file with low LCOM value.",
        Debt.TWENTY_MINS
    )

    private val thresholdValue =
        valueOrNull<String>("threshold")?.toDouble() ?: error("You must specify a threshold value in detekt.yml")

    // The number of properties in the class. Includes both properties that comes from the primary constructor and that is declared directly in the class.
    private var propertyCount: Int = 0

    // referencesCount = For each field in the class, you count the methods that reference it, and then you add all of those up across all fields.
    private var referencesCount = 0

    override fun visitNamedDeclaration(declaration: KtNamedDeclaration) {
        super.visitNamedDeclaration(declaration)

        if ((propertyIsMember(declaration) || isPropertyFromPrimaryConstructor(declaration)) && hasContainingClass(declaration)) {
            propertyCount++

            val containingClass = declaration.containingClass()!!

            // todo: handle protected etc methods as well
            searchForReferences(declaration, containingClass)
        }
    }

    private fun hasContainingClass(declaration: KtNamedDeclaration) = declaration.containingClass() != null

    private fun propertyIsMember(declaration: KtNamedDeclaration): Boolean {
        return try {
            // We only want to return true if the declaration is a true member of the class we are calculating LCOM for.
            // If a property is declared inside an anonymous class, it is a member, but the fqname will be null, and we can therefore discard it.
            return (declaration as KtProperty).isMember && declaration.fqName != null
        } catch (e: ClassCastException) {
            false
        }
    }

    override fun visitClass(klass: KtClass) {
        // Skip interfaces, enums and inner classes.
        if (klass.isInterface() || klass.isEnum() || klass.isInner()) return

        propertyCount = 0
        referencesCount = 0
        val publicMethods = getPublicMethods(klass)

        /*
         * methodsCount is the number of methods that can reference properties in the class. Since constructors count as methods we have to add those as well.
         *
         * Initializer blocks can reference properties.
         * Primary constructors can initialize properties with the concise Kotlin syntax. We add 1 if the current class uses this syntax.
         * Secondary constructors can reference properties.
         */
        val methodsCount =
            publicMethods.size +
                klass.getAnonymousInitializers().size +
                klass.secondaryConstructors.size +
                if (initializesPropertiesFromPrimaryConstructor(klass)) 1 else 0

        super.visitClass(klass)

        // If there is no methods or properties, there is no point of calculating LCOM, we will therefore return without reporting any value
        if (methodsCount == 0 || propertyCount == 0) {
            println("m: $methodsCount, f:$propertyCount")
            return
        }

        val lcom = 1 - (referencesCount.toDouble() / (methodsCount * propertyCount))
        println("Class ${klass.name} has LCOM: $lcom, properties: $propertyCount, methods: $methodsCount, mf: $referencesCount")
        publicMethods.forEach { println(it.name) }
        if (lcom > thresholdValue) {
            report(
                CodeSmell(issue, Entity.from(klass), "${klass.name} have a too high LCOM value: $lcom")
            )
        }
    }

    private fun initializesPropertiesFromPrimaryConstructor(klass: KtClass): Boolean {
        return klass.primaryConstructorParameters.any { it.hasValOrVar() }
    }

    private fun isPropertyFromPrimaryConstructor(declaration: KtNamedDeclaration) =
        (declaration is KtParameter) && declaration.hasValOrVar()

    private fun searchForReferences(property: KtNamedDeclaration, containingClass: KtClass) {
        // Properties can be referenced from public methods, protected methods?, initializer blocks and in secondary constructors. Referencing of properties in primary constructors will not be counted.
        val expressionsToLookForReferences =
            getPublicMethods(containingClass).mapNotNull { it.bodyExpression } +
                containingClass.getAnonymousInitializers().mapNotNull { it.body } +
                containingClass.secondaryConstructors.mapNotNull { it.bodyExpression }


        for (expression in expressionsToLookForReferences) {
            println("\nLooking for references in ${expression.name}:")
            val referenceExpression = getReferencesOfProperty(expression, property)

            if (referenceExpression.isNotEmpty()) {
                referencesCount++
                println("Found reference of ${property.name} in method ${expression.name}, continuing with next public method.")
                continue
            }

            // Fetch all methods that is called from this public function, recursively.
            val allCalleesFromThisPublicMethod = getCallees(property, expression, arrayListOf(expression))
            println("Callees of ${expression.name}: (size: ${allCalleesFromThisPublicMethod.size})")
            allCalleesFromThisPublicMethod.forEach { println(it.name) }

            // We look for references of the property, and as soon as we find it, we increase mf_sum and break so that we can look for references in other public methods.
            for (privateMethod in allCalleesFromThisPublicMethod) {

                // Get references in this private function
                val references = getReferencesOfProperty(privateMethod, property)

                if (references.isNotEmpty()) {
                    referencesCount++
                    println("Found reference of ${property.name} in private method ${privateMethod.name} called from ${expression.name}")
                    break
                }
            }
        }
    }

    private fun getPublicMethods(klass: KtClass): List<KtNamedFunction> {
        return klass.collectDescendantsOfType<KtNamedFunction> { it.isPublic || it.isProtected()  }
    }

    private fun getReferencesOfProperty(
        expression: KtExpression,
        property: KtNamedDeclaration
    ): List<KtReferenceExpression> {
        return expression.collectDescendantsOfType { reference ->
            isReferenceOfPropertyClass(reference, expression) && reference.text == property.name
        }
    }

    /**
     * We need to ensure that the found reference not only have the same name as the property, but if it is a member of the correct class as well.
     */
    private fun isReferenceOfPropertyClass(
        reference: KtReferenceExpression,
        method: KtExpression
    ) =
        reference.getResolvedCall(bindingContext)?.resultingDescriptor?.containingDeclaration?.name.toString() == method.containingClass()?.name

    private fun isCalleeInPropertyClass(
        callee: KtExpression,
        property: KtNamedDeclaration
    ) =
        callee.containingClass()?.name == property.containingClass()?.name

    private fun getCallees(
        property: KtNamedDeclaration,
        publicMethod: KtExpression,
        foundCallees: List<KtExpression>
    ): ArrayList<KtExpression> {
        val callees =
            ArrayList(publicMethod.collectDescendantsOfType<KtCallExpression>()
                .mapNotNull {
                    try {
                        (it.getResolvedCall(bindingContext)?.resultingDescriptor?.findPsi() as KtNamedFunction).bodyExpression
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
            var newResult = listOf<KtExpression>()
            if (memoizedResults.containsKey(callee)) {
                newResult = memoizedResults[callee]!!
            } else {
                newResult = getCallees(property, callee, callees)
                memoizedResults[callee] = newResult
            }

            callees + newResult
        }

        return callees
    }
}