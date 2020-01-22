package io.gitlab.arturbosch.detekt.sample.extensions.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.psiUtil.getSuperNames

class UseCompositionInsteadOfInheritance(config: Config = Config.empty) : Rule(config) {
    override val issue = Issue(
        javaClass.simpleName,
        Severity.CodeSmell,
        "This rule reports a file using inheritance.",
        Debt.TWENTY_MINS
    )






    override fun visitClass(klass: KtClass) {
        super.visitClass(klass)

        val inheritanceUsed = klass.getSuperNames().isNotEmpty()

        if (inheritanceUsed) {
            report(
                CodeSmell(
                    issue, Entity.from(klass),
                    message = "The class ${klass.name} is using inheritance, consider using composition instead."
                )
            )
        }
    }
}