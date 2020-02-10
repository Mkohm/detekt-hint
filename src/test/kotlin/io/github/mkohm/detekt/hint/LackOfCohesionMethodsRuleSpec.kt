package io.github.mkohm.detekt.hint

import io.github.mkohm.detekt.hint.rules.LackOfCohesionOfMethodsRule
import io.gitlab.arturbosch.detekt.test.KtTestCompiler
import io.gitlab.arturbosch.detekt.test.TestConfig
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class LackOfCohesionMethodsRuleSpec : Spek({

    val testConfig = TestConfig(mapOf("yourUniquePackageName" to "io.github.mkohm"))

    val wrapper by memoized(
        factory = { KtTestCompiler.createEnvironment() },
        destructor = { it.dispose() }
    )

    describe("Full cohesion of all the method and properties") {
        it("Should give lcom 1") {

            // language=kotlin
            val code = """
                    class Foo {
                        private var number = 0
                        
                        fun add()  {
                            number++
                        }
                        
                        fun subtract() {
                            number--
                        }
                    }
                """.trimIndent()

            val findings = LackOfCohesionOfMethodsRule().compileAndLintWithContext(wrapper.env , code)


        }
    }

})
