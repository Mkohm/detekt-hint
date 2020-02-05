package io.github.mkohm.detekt.hint.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.js.resolve.diagnostics.findPsi
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.psiUtil.getSuperNames
import org.jetbrains.kotlin.psi.psiUtil.isPublic
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes.ENUM_ENTRY
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes.SUPER_TYPE_CALL_ENTRY
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

class UseCompositionInsteadOfInheritance(config: Config = Config.empty) : Rule(config) {
    override val issue = Issue(
        javaClass.simpleName,
        Severity.CodeSmell,
        "This rule reports a file using inheritance.",
        Debt.TWENTY_MINS
    )

    override fun visitClass(klass: KtClass) {
        super.visitClass(klass)
        val uniquePackageName = valueOrNull<String>("yourUniqueJavaPackage") ?: error("You must specify your unique package name in detekt.yml")

        if (klass.getSuperNames().isEmpty() || noSuperTypeCallEntry(klass) || isEnumEntry(klass)) return

        val superClass =
            klass.superTypeListEntries[0].getResolvedCall(bindingContext)?.resultingDescriptor?.containingDeclaration
                ?: return

        val superClassFqName = superClass.fqNameSafe.toString()
        val isLocalInheritanceUsed = superClassFqName.contains(uniquePackageName)

        if (isLocalInheritanceUsed) {

            val functions = (superClass.findPsi() as KtClass).body?.functions
            val publicFunctions = functions?.filter { it.isPublic }

            val toPrint = if (publicFunctions.isNullOrEmpty()) {
                "empty public interface"
            } else {
                publicFunctions.map { it.name }.reduceRight { ktNamedFunction, acc -> "$acc, $ktNamedFunction" }
            }

            val typeA = superClass.name
            val typeB = klass.name
            val message =
                "The class `${klass.name}` is using inheritance, consider using composition instead.\n\nDoes `${typeB}` want to expose the complete interface (`$toPrint`) of `${typeA}` such that `${typeB}` can be used where `${typeA}` is expected? Indicates __inheritance__.\n\nDoes `${typeB}`? want only some/part of the behavior exposed by `${typeA}`? Indicates __Composition__."

            report(CodeSmell(issue, Entity.from(klass), message))
        }
    }

    private fun isEnumEntry(klass: KtClass) = klass.elementType == ENUM_ENTRY

    private fun noSuperTypeCallEntry(klass: KtClass) =
        (klass.superTypeListEntries[0].elementType != SUPER_TYPE_CALL_ENTRY)
}
