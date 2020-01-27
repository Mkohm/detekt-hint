package io.github.mkohm.detekt.hint

import io.github.mkohm.detekt.hint.rules.LackOfCohesionOfMethodsRule
import io.gitlab.arturbosch.detekt.test.lint
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class LackOfCohesionMethodsRuleSpec : Spek({

    describe("Full cohesion of all the method and properties") {
        it("Should give lcom 1") {

            // language=kotlin
            val code = """
                    class Foo {
                        private val bar = 1
                        
                        fun aFunction() {
                            println(bar)
                        }
                        
                        fun anotherFunction() {
                        
                        }
                    }
                """.trimIndent()

            val findings = LackOfCohesionOfMethodsRule().lint(code)


        }
    }

})
