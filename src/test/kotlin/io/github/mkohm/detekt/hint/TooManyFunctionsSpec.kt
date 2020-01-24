package io.github.mkohm.detekt.hint

import io.gitlab.arturbosch.detekt.api.Config
import io.github.mkohm.detekt.hint.rules.TooManyFunctions
import io.gitlab.arturbosch.detekt.test.TestConfig
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class TooManyFunctionsSpec : Spek({

    describe("a simple test") {
    val subject by memoized {
        TooManyFunctions(
            TestConfig(
                mapOf(
                    Config.ACTIVE_KEY to "true"
                )
            )
        )
    }
        it("should find one file with too many functions") {
            val findings = subject.lint(code)
            assertThat(findings).hasSize(1)
        }
    }

    describe("should not report issues if rule is inactive") {
        val rule =
            TooManyFunctions(TestConfig(mapOf(Config.ACTIVE_KEY to "false")))
        val findings = rule.lint(code)
        assertThat(findings).isEmpty()
    }

    describe("should report issues if rule is active") {
        val rule =
            TooManyFunctions(TestConfig(mapOf(Config.ACTIVE_KEY to "true")))
        val findings = rule.lint(code)
        assertThat(findings).isNotEmpty
    }
})

const val code: String =
    """
            class TooManyFunctions : Rule("TooManyFunctions") {

                override fun visitUserType(type: KtUserType) {
                    super.visitUserType(type)
                }

                override fun visitReferenceExpression(expression: KtReferenceExpression) {
                    super.visitReferenceExpression(expression)
                }

                override fun visitCallExpression(expression: KtCallExpression) {
                    super.visitCallExpression(expression)
                }

                override fun visitBlockStringTemplateEntry(entry: KtBlockStringTemplateEntry) {
                    super.visitBlockStringTemplateEntry(entry)
                }

                override fun visitUnaryExpression(expression: KtUnaryExpression) {
                    super.visitUnaryExpression(expression)
                }

                override fun visitDynamicType(type: KtDynamicType) {
                    super.visitDynamicType(type)
                }

                override fun visitDynamicType(type: KtDynamicType, data: Void?): Void {
                    return super.visitDynamicType(type, data)
                }

                override fun visitSuperTypeCallEntry(call: KtSuperTypeCallEntry) {
                    super.visitSuperTypeCallEntry(call)
                }

                override fun visitParenthesizedExpression(expression: KtParenthesizedExpression) {
                    super.visitParenthesizedExpression(expression)
                }

                override fun visitFinallySection(finallySection: KtFinallySection) {
                    super.visitFinallySection(finallySection)
                }

                override fun visitStringTemplateExpression(expression: KtStringTemplateExpression) {
                    super.visitStringTemplateExpression(expression)
                }

                override fun visitDeclaration(dcl: KtDeclaration) {
                    super.visitDeclaration(dcl)
                }

                override fun visitLabeledExpression(expression: KtLabeledExpression) {
                    super.visitLabeledExpression(expression)
                }

                override fun visitEscapeStringTemplateEntry(entry: KtEscapeStringTemplateEntry) {
                    super.visitEscapeStringTemplateEntry(entry)
                }

                override fun visitScript(script: KtScript) {
                    super.visitScript(script)
                }

                override fun visitTypeConstraintList(list: KtTypeConstraintList) {
                    super.visitTypeConstraintList(list)
                }

            }
        """
