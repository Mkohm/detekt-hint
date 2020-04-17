package io.github.mkohm.detekt.hint.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.com.intellij.psi.PsiComment
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.js.resolve.diagnostics.findPsi
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtThrowExpression
import org.jetbrains.kotlin.psi.psiUtil.containingClass
import org.jetbrains.kotlin.psi.synthetics.findClassDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameUnsafe
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperInterfaces

/**
 * Interface segregation principle rule
 */
class InterfaceSegregationPrinciple(config: Config = Config.empty) : Rule(config) {
    override val issue = Issue(
        javaClass.simpleName,
        Severity.CodeSmell,
        "This rule reports classes that violates the Interface Segregation Principle.",
        Debt.TWENTY_MINS
    )

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)

        if (function.hasModifier(KtTokens.OVERRIDE_KEYWORD) && isUnNecessary(function)) {

            val interfaceName = getInterfaceName(function)
            report(
                CodeSmell(
                    issue,
                    Entity.from(function),
                    "${function.name} is not implemented an may not be necessary. This is a possible violation of the interface segregation principle. Consider splitting up$interfaceName into smaller interfaces with a single responsibility."
                )
            )
        }
    }

    private fun getInterfaceName(function: KtNamedFunction): Any {
        val implementedInterfaces = function.containingClass()?.findClassDescriptor(bindingContext)?.getSuperInterfaces()
        val interfaceOfOverriddenFunction = implementedInterfaces?.find { classDescriptor ->
            interfaceThatHasFunctionWithSameName(classDescriptor, function)
        }
        val interfaceNameOfOverriddenFunction = interfaceOfOverriddenFunction?.fqNameOrNull()

        return if (interfaceNameOfOverriddenFunction == null) "" else " `$interfaceOfOverriddenFunction`"
    }

    private fun interfaceThatHasFunctionWithSameName(
        classDescriptor: ClassDescriptor,
        function: KtNamedFunction
    ) = (classDescriptor.findPsi() as KtClass).body?.functions?.any { it.name == function.name } ?: false

    private fun isUnNecessary(function: KtNamedFunction): Boolean {
        return if (function.hasBlockBody()) {
            function.bodyExpression?.children?.all { it is KtThrowExpression || it is PsiComment || it is LeafPsiElement } ?: false
        } else {
            function.bodyExpression is KtThrowExpression || (hasChildren(function) && allChildrenIsThrowOrComment(function))
        }
    }

    private fun allChildrenIsThrowOrComment(function: KtNamedFunction) =
        function.bodyExpression?.children?.all { it is KtThrowExpression || it is PsiComment } ?: false

    private fun hasChildren(function: KtNamedFunction): Boolean = function.bodyExpression?.children?.isNotEmpty() ?: false
}
