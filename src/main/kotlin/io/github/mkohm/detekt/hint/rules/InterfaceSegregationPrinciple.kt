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
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtThrowExpression

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

            @Suppress("UnsafeCallOnNullableType")
            // We know that it has a parent since it has the override modifier, therefore the landmine operator is okay.
            val interfaceName = function.fqName?.parent()!!
            report(
                CodeSmell(
                    issue,
                    Entity.from(function),
                    "${function.text} is not implemented an may not be necessary. This is a possible violation of the interface segregation principle. Consider splitting up $interfaceName into smaller interfaces with a single responsibility."
                )
            )
        }
    }

    private fun isUnNecessary(function: KtNamedFunction): Boolean {
        return if (function.hasBlockBody()) function.bodyExpression?.children?.all { it is KtThrowExpression || it is PsiComment || it is LeafPsiElement } ?: false
        else function.bodyExpression is KtThrowExpression || function.bodyExpression?.children?.all { it is KtThrowExpression || it is PsiComment } ?: false
    }
}
