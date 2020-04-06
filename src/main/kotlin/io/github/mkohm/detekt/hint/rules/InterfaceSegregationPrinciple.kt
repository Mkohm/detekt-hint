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
            report(
                CodeSmell(
                    issue,
                    Entity.from(function),
                    "${function.text} is not implemented an may not be neccesary. This is a possible violation of the interface segregation principle."
                )
            )
        }
    }

    private fun isUnNecessary(function: KtNamedFunction): Boolean {
        return if (function.hasBlockBody()) function.bodyExpression?.children?.all { it is KtThrowExpression || it is PsiComment || it is LeafPsiElement } ?: false
        else function.bodyExpression is KtThrowExpression || function.bodyExpression?.children?.all { it is KtThrowExpression || it is PsiComment } ?: false
    }
}
