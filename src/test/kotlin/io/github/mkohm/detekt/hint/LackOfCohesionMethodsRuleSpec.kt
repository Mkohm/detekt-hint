package io.github.mkohm.detekt.hint

import io.github.mkohm.detekt.hint.rules.LackOfCohesionOfMethodsRule
import io.gitlab.arturbosch.detekt.test.KotlinCoreEnvironmentWrapper
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import io.gitlab.arturbosch.detekt.test.lint
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class LackOfCohesionMethodsRuleSpec : Spek({

    val environment = KotlinCoreEnvironmentWrapper()

    describe("Full cohesion of all the method and properties") {
        it("Should give lcom 1") {

            // language=kotlin
            val code = """
                    class Foo {
                        private val number = 0
                        
                        fun add():Int  {
                            number++
                        }
                        
                        fun subtract() {
                            number--
                        }
                    }
                """.trimIndent()

            val findings = LackOfCohesionOfMethodsRule().compileAndLintWithContext( , code)


        }
    }

})
