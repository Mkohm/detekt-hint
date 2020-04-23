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
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall

/**
 * A rule that notifies if there is too much lack of cohesion. Remember to configure it correctly in the detekt.yml.
 *
 * LCOM for a class will range between 0 and 1, with 0 being totally cohesive and 1 being totally non-cohesive.
 * This makes sense since a low “lack of cohesion” score would mean a lot of cohesion.
 *
 * For each property in the class, you count the methods that reference it, and then you add all of those up across all properties. This value is called referencesCount.
 * You then divide that by the count of methods times the count of properties, and you subtract the result from one.
 *
 * LCOM = 1 - referencesCount / ( methodsCount * propertyCount)
 */
class LackOfCohesionMethods(config: Config = Config.empty) : Rule(config) {

    override val issue = Issue(
        javaClass.simpleName,
        Severity.CodeSmell,
        "This rule reports a file with low LCOM value.",
        Debt.TWENTY_MINS
    )

    private val memoizedResults = mutableMapOf<KtExpression, List<KtExpression>>()

    private var propertyCount: Int = 0
    private var referencesCount = 0

    override fun visitNamedDeclaration(declaration: KtNamedDeclaration) {
        super.visitNamedDeclaration(declaration)
        if (bindingContext == BindingContext.EMPTY) return

        if ((propertyIsMember(declaration) || isPropertyInitializedInPrimaryConstructor(declaration)) && hasContainingClass(declaration)) {
            propertyCount++

            // We know that the containing class exist, because of the above check hasContainingClass().
            // Landmine operator is therefore okay.
            @Suppress("UnsafeCallOnNullableType")
            val containingClass = declaration.containingClass()!!
            searchForReferences(declaration, containingClass)
        }
    }

    override fun visitClass(klass: KtClass) {
        // Skip interfaces, enums and inner classes.
        if (klass.isInterface() || klass.isEnum() || klass.isInner()) return

        propertyCount = 0
        referencesCount = 0
        val publicAndProtectedMethods = getPublicAndProtectedMethods(klass)

        /*
         * methodsCount is the number of methods that can reference properties in the class. Since constructors count as methods we have to add those as well.
         *
         * Initializer blocks can reference properties.
         * Primary constructors can initialize properties with the concise Kotlin syntax. We add 1 if the current class uses this syntax.
         * Secondary constructors can reference properties.
         */
        val methodsCount =
            publicAndProtectedMethods.size +
                klass.getAnonymousInitializers().size +
                klass.secondaryConstructors.size +
                if (initializesPropertiesFromPrimaryConstructor(klass)) 1 else 0

        super.visitClass(klass)

        // If there is no methods or properties, there is no point of calculating LCOM, we will therefore return without reporting any value
        if (methodsCount == 0 || propertyCount == 0) {
            return
        }

        val lcom = calculateLCOMvalue(methodsCount, propertyCount, referencesCount)
        val thresholdValue = valueOrNull<String>("threshold")?.toDouble() ?: error("You must specify a threshold value in detekt.yml")
        if (lcom >= thresholdValue) {
            report(
                CodeSmell(issue, Entity.from(klass), "${klass.name} have a too high LCOM value: $lcom. Number of methods: $methodsCount, number of properties: $propertyCount, number of references: $referencesCount")
            )
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

    private fun calculateLCOMvalue(
        methodsCount: Int,
        propertyCount: Int,
        referencesCount: Int
    ) = 1 - (referencesCount.toDouble() / (methodsCount * propertyCount))

    private fun initializesPropertiesFromPrimaryConstructor(klass: KtClass): Boolean {
        return klass.primaryConstructorParameters.any { it.hasValOrVar() }
    }

    private fun isPropertyInitializedInPrimaryConstructor(declaration: KtNamedDeclaration) =
        (declaration is KtParameter) && declaration.hasValOrVar()

    private fun searchForReferences(property: KtNamedDeclaration, containingClass: KtClass) {
        // Properties can be referenced from public methods, protected methods?, initializer blocks and in secondary constructors.
        val expressionsToLookForReferences =
            getPublicAndProtectedMethods(containingClass).mapNotNull { it.bodyExpression } +
                containingClass.getAnonymousInitializers().mapNotNull { it.body } +
                containingClass.secondaryConstructors.mapNotNull { it.bodyExpression }

        // If the property is initialized in the primary constructor it is automatically counted as a reference.
        if (isPropertyInitializedInPrimaryConstructor(property)) {
            referencesCount++
        }

        for (expression in expressionsToLookForReferences) {
            val referenceExpression = getReferencesOfProperty(expression, property)

            if (referenceExpression.isNotEmpty()) {
                referencesCount++
                continue
            }

            // Fetch all expressions that is reachable from the current expression (public method), recursively.
            val allReachableExpressionsFromPublicMethod = getReachableExpressions(property, expression, arrayListOf(expression))

            // We look for references of the property, and as soon as we find it, we increase mf_sum and break so that we can look for references in other public methods.
            for (subExpression in allReachableExpressionsFromPublicMethod) {

                // Get references in this private function
                val references = getReferencesOfProperty(subExpression, property)

                if (references.isNotEmpty()) {
                    referencesCount++
                    break
                }
            }
        }
    }

    private fun getPublicAndProtectedMethods(klass: KtClass): List<KtNamedFunction> {
        return klass.collectDescendantsOfType<KtNamedFunction> { it.isPublic || it.isProtected() }
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
        expression: KtExpression
    ) =
        reference.getResolvedCall(bindingContext)?.resultingDescriptor?.containingDeclaration?.name.toString() == expression.containingClass()?.name

    private fun isCalleeInPropertyClass(
        callee: KtExpression,
        property: KtNamedDeclaration
    ) =
        callee.containingClass()?.name == property.containingClass()?.name

    /*
    * From public, protected, initializer methods etc. Calls to private methods can exist.
    * We need to find all such calls and look for references there as well.
    */
    private fun getReachableExpressions(
        property: KtNamedDeclaration,
        expression: KtExpression,
        foundExpressions: List<KtExpression>
    ): ArrayList<KtExpression> {
        val reachableExpressionsFromThisExpressions =
            ArrayList(expression.collectDescendantsOfType<KtCallExpression>()
                .mapNotNull {
                    try {
                        (it.getResolvedCall(bindingContext)?.resultingDescriptor?.findPsi() as KtNamedFunction).bodyExpression
                    } catch (e: java.lang.ClassCastException) {
                        null
                    }
                }.filter {
                    !foundExpressions.contains(it) && isCalleeInPropertyClass(
                        it,
                        property
                    )
                }.distinct().ifEmpty {
                    return arrayListOf()
                })

        reachableExpressionsFromThisExpressions.addAll(foundExpressions)

        // We need to dig further down into all the found expressions.
        // There may be an unlimited amount of reachable expressions inside found expression.
        // For performance reasons, the results are memoized.
        for (reachableExpression in reachableExpressionsFromThisExpressions) {

            // Memoize the results of the calculation to speed things up
            var newResult = listOf<KtExpression>()
            if (memoizedResults.containsKey(reachableExpression)) {

                // After each call we put the result into the map, we therefore know that the key exists.
                // Landmine operator is therefore okay in this case.
                @Suppress("UnsafeCallOnNullableType")
                newResult = memoizedResults[reachableExpression]!!
            } else {
                newResult = getReachableExpressions(property, reachableExpression, reachableExpressionsFromThisExpressions)
                memoizedResults[reachableExpression] = newResult
            }

            reachableExpressionsFromThisExpressions + newResult
        }

        return reachableExpressionsFromThisExpressions
    }
}
