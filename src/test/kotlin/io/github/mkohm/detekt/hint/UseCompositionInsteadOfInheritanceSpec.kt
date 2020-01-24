package io.github.mkohm.detekt.hint

import io.github.mkohm.detekt.hint.rules.UseCompositionInsteadOfInheritance
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.TestConfig
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class UseCompositionInsteadOfInheritanceSpec : Spek({
    val testConfig = TestConfig(mapOf("dont_report_if_class_inherits_from_class_in_package" to "io.github.mkohm"))

    describe("Not using inheritance") {
        it("Should not report any warnings") {

            // language=kotlin
            val code = """
                    class ClassWithNoInheritance
                """.trimIndent()

            val findings = UseCompositionInsteadOfInheritance(testConfig).lint(code)

            assertThat(findings).isEmpty()
        }
    }

    describe("If rule is inactive") {

        val code = """
                    class ClassContainingInheritance : ClassToInheritFrom()

                    open class ClassToInheritFrom
                """.trimIndent()

        val rule = UseCompositionInsteadOfInheritance(TestConfig(mapOf(Config.ACTIVE_KEY to "false")))
        it("should not find any issues.") {

            val findings = rule.lint(code)
            assertThat(findings).isEmpty()
        }
    }

    describe("Inheritance from external module") {

        // language=kotlin
        val code = """

            package io.gitlab.arturbosch.detekt.sample.extensions

           import io.gitlab.arturbosch.detekt.api.DefaultContext

           class InternalClass: DefaultContext()
        """.trimIndent()
        it("Should not report any inheritance-warnings") {
            val rule = UseCompositionInsteadOfInheritance(testConfig)
            val findings = rule.lint(code)
            assertThat(findings).isEmpty()
        }
    }

    describe("Inheritance from internal module") {
        // language=kotlin
        val code = """
            open class InternalClass
            
            class AnotherInternalClass : InternalClass
        """.trimIndent()
        it("Should report composition could be used instead of inheritance") {
            val rule = UseCompositionInsteadOfInheritance(testConfig)
            val findings = rule.lint(code)
            assertThat(findings).hasSize(1)
        }
    }
})
