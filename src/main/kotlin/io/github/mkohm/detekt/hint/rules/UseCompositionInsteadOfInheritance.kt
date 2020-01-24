package io.github.mkohm.detekt.hint.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.psiUtil.getSuperNames

class UseCompositionInsteadOfInheritance(config: Config = Config.empty) : Rule(config) {
    override val issue = Issue(
        javaClass.simpleName,
        Severity.CodeSmell,
        "This rule reports a file using inheritance.",
        Debt.TWENTY_MINS
    )

    private val imports = arrayListOf<String>()

    override fun visitImportDirective(importDirective: KtImportDirective) {
        super.visitImportDirective(importDirective)

        imports.add(importDirective.text)
    }

    override fun visitClass(klass: KtClass) {
        super.visitClass(klass)

        val excludedPackageName =
            valueOrNull<String>("dont_report_if_class_inherits_from_class_in_package")
                ?: error("You must configure detekt.yml to contain your package identifier, such as io.github.mkohm - or disable the rule.")

        val inheritanceUsed = klass.getSuperNames().isNotEmpty()
        if (!inheritanceUsed) return

        val superClassName = klass.getSuperNames()[0]

        val superClassImportString = imports.find { it.contains(superClassName) }

        val localInheritanceUsed = superClassImportString?.contains(excludedPackageName) ?: true

        if (localInheritanceUsed) {
            report(
                CodeSmell(
                    issue, Entity.from(klass),
                    message = "The class ${klass.name} is using inheritance, consider using composition instead."
                )
            )
        }
    }
}