package io.gitlab.arturbosch.detekt.sample.extensions.rules

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.getSuperNames

class UseCompositionInsteadOfInheritance : Rule() {
    override val issue = Issue(
        javaClass.simpleName,
        Severity.Minor,
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
