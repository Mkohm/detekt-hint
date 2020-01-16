package io.gitlab.arturbosch.detekt.sample.extensions

import io.gitlab.arturbosch.detekt.sample.extensions.processors.NumberOfLoopsProcessor

import io.gitlab.arturbosch.detekt.api.DetektVisitor
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.sample.extensions.rules.UseCompositionInsteadOfInheritance
import io.gitlab.arturbosch.detekt.test.compileContentForTest
import io.gitlab.arturbosch.detekt.test.compileForTest
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.nio.file.Path
import java.nio.file.Paths

class UseCompositionInsteadOfInheritanceSpec : Spek({

    describe("Using inheritance") {

        it("should find inheritance") {

            // language=kotlin
            val code = """
                class ClassContainingInheritance : ClassToInheritFrom()
                
                open class ClassToInheritFrom    
            """.trimIndent()

            val findings = UseCompositionInsteadOfInheritance().lint(code)

            assertThat(findings).hasSize(1)
        }

        it("Should find two superclasses") {
            // language=kotlin
            val code = """
                class ClassWithInheritance : ClassToInheritFrom()
                
                open class ClassToInheritFrom : AnotherClassToInheritFrom()
                
                open class AnotherClassToInheritFrom()
            """.trimIndent()

            val findings = UseCompositionInsteadOfInheritance().lint(code)

            assertThat(findings).hasSize(2)
        }
    }

    describe("Not using inheritance") {
        it("Should not find any superclass") {

            // language=kotlin
            val code = """
                class ClassWithNoInheritance
            """.trimIndent()

            val findings = UseCompositionInsteadOfInheritance().lint(code)

            assertThat(findings).isEmpty()
        }
    }
})


