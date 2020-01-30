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
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import org.jetbrains.kotlin.psi.psiUtil.getSuperNames
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes.ENUM_ENTRY
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes.SUPER_TYPE_CALL_ENTRY
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall

class UseCompositionInsteadOfInheritance(config: Config = Config.empty) : Rule(config) {
    override val issue = Issue(
        javaClass.simpleName,
        Severity.CodeSmell,
        "This rule reports a file using inheritance.",
        Debt.TWENTY_MINS
    )

    private val imports = arrayListOf<String>()
    var methodsOfSuperclass = arrayListOf<String>()

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

        //  val supek = klass.superTypeListEntries[0].firstChild

        //  val methods = PsiTreeUtil.getChildrenOfTypeAsList(klass.superTypeListEntries[0], PsiMethod::class.java)

        //val subjectClass = klass.superTypeListEntries[0].findClassDescriptor(bindingContext)
        //val pseudocodeDescriptor = bindingContext[BindingContext.DECLARATION_TO_DESCRIPTOR, subjectClass?.toSourceElement?.getPsi()]

        val superClassFullIdentifier =
            imports.find { it.contains(superClassName) } ?: "$localPackageName.$superClassName"


        var publicInterface = try {
            klass.superTypeListEntries[0].getResolvedCall(bindingContext)!!.resultingDescriptor.findPsi()!!.parent.getChildrenOfType<KtClassBody>()[0].getChildrenOfType<KtNamedFunction>().map { it.name ?: "No name" }
        } catch (e: Exception) {
            ""
        }


        val localInheritanceUsed = superClassFullIdentifier.contains(localPackageName)

        if (localInheritanceUsed) {

            val typeA = superClassName
            val typeB = klass.name
            val message =
                "The class ${klass.name} is using inheritance, consider using composition instead.\n\nDoes `${typeB}` want to expose ($publicInterface) of `${typeA}` such that ${typeB} can be used where ${typeA} is expected (for all time)? Indicates __inheritance__.\n\nDoes ${typeB} want only some/part of the behavior exposed by ${typeA}? Indicates __Composition__."

            println(methodsOfSuperclass)
            report(CodeSmell(issue, Entity.from(klass), message))
        }
    }

    private fun isEnumEntry(klass: KtClass) = klass.elementType == ENUM_ENTRY

    private fun noSuperTypeCallEntry(klass: KtClass) =
        (klass.superTypeListEntries[0].elementType != SUPER_TYPE_CALL_ENTRY)
}
