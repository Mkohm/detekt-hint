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
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes.ENUM_ENTRY
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes.SUPER_TYPE_CALL_ENTRY

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

        val localPackageName =
            valueOrNull<String>("dont_report_if_class_inherits_from_class_in_package")
                ?: error("You must configure detekt.yml to contain your package identifier, such as io.github.mkohm - or disable the rule.")

        if (klass.getSuperNames().isEmpty() || noSuperTypeCallEntry(klass) || isEnumEntry(klass)) return

        val superClassName = klass.superTypeListEntries[0].firstChild.text.substringBefore(".")
        val superClassFullIdentifier =
            imports.find { it.contains(superClassName) } ?: "$localPackageName.$superClassName"

        val localInheritanceUsed = superClassFullIdentifier.contains(localPackageName)

        if (localInheritanceUsed) {

            val typeA = superClassName
            val typeB = klass.name
            val message =
                "The class ${klass.name} is using inheritance, consider using composition instead.\n\nIf `${typeB}` want to expose the complete interface (all public methods) of ${typeA} such that ${typeB} can be used where ${typeA} is expected? Indicates __inheritance__.\n\nDoes ${typeB} want only some/part of the behavior exposed by ${typeA}? Indicates __Composition__."

            report(CodeSmell(issue, Entity.from(klass), message))
        }
    }

    private fun isEnumEntry(klass: KtClass) = klass.elementType == ENUM_ENTRY

    private fun noSuperTypeCallEntry(klass: KtClass) =
        (klass.superTypeListEntries[0].elementType != SUPER_TYPE_CALL_ENTRY)
}