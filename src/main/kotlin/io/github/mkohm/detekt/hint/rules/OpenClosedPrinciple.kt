package io.github.mkohm.detekt.hint.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtIsExpression
import org.jetbrains.kotlin.psi.KtWhenEntry
import org.jetbrains.kotlin.psi.KtWhenExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.resolve.calls.callUtil.getType
import org.jetbrains.kotlin.types.typeUtil.isEnum

/**
 * Open closed principle rule. Only supports catching the easiest cases. Not complex when expressions, with type checking and use of enums.
 *
 * Supported:
 *  when (enum) {
 *      Color.RED -> ...
 *      Color.Blue -> ...
 *      ...
 *  }
 *
 *  when {
 *      a is Square -> ...
 *      b is Circle -> ...
 *      ...
 *  }
 *
 */
class OpenClosedPrinciple(config: Config = Config.empty) : Rule(config) {
    override val issue = Issue(
        javaClass.simpleName,
        Severity.CodeSmell,
        "This rule reports use of switching on enums and classes, which may be a sign of violation the open closed principle.",
        Debt.TWENTY_MINS
    )

    override fun visitWhenExpression(expression: KtWhenExpression) {
        super.visitWhenExpression(expression)

        when {
            isEnumWhenExpression(expression) -> reportEnumSmell(expression)
            isTypeCheckExpression(expression) -> reportTypeCheckSmell(expression)
        }
    }

    private fun reportTypeCheckSmell(expression: KtWhenExpression) {
        val classes = getClassNames(expression)

        report(
            CodeSmell(
                issue,
                Entity.from(expression),
                "Type checking is a sign of violating the Open-Closed Principle. Consider introducing an abstraction (interface) for $classes, with new implementations of the interface for every class."
            )
        )
    }

    private fun reportEnumSmell(expression: KtWhenExpression) {
        val enum = getEnumName(expression)
        report(
            CodeSmell(
                issue,
                Entity.from(expression),
                "Switching on enum values is a common sign of violation the Open-Closed Principle. Consider introducing an abstraction (interface) for `$enum`, with new implementations of the interface for every value."
            )
        )
    }

    private fun getClassNames(expression: KtWhenExpression): String? {
        val allClasses = expression.collectDescendantsOfType<KtIsExpression>().map { it.typeReference }
        return allClasses.map { "`${it?.text}`" }.reduceRight { ktTypeReference, acc -> "$acc, $ktTypeReference" }
    }

    private fun getEnumName(expression: KtWhenExpression): String = expression.subjectExpression?.getType(bindingContext).toString()

    private fun isTypeCheckExpression(expression: KtWhenExpression): Boolean = entriesContainsIsExpression(expression)

    private fun entriesContainsIsExpression(expression: KtWhenExpression): Boolean =
        numberOfIsExpression(expression) > 0 && numberOfIsExpression(expression) >= (expression.entries.count() - 1)

    private fun numberOfIsExpression(expression: KtWhenExpression) = expression.entries.count { entry -> hasExactlyOneIsExpression(entry) }

    private fun hasExactlyOneIsExpression(whenEntry: KtWhenEntry): Boolean = whenEntry.collectDescendantsOfType<KtIsExpression>().count() == 1

    private fun isEnumWhenExpression(expression: KtWhenExpression): Boolean = subjectExpressionIsEnum(expression)

    private fun subjectExpressionIsEnum(expression: KtWhenExpression): Boolean = expression.subjectExpression?.getType(bindingContext)?.isEnum() ?: false
}