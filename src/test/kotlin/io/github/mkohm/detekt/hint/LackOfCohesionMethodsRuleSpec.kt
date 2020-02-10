package io.github.mkohm.detekt.hint

import io.github.mkohm.detekt.hint.rules.LackOfCohesionOfMethodsRule
import io.gitlab.arturbosch.detekt.test.KtTestCompiler
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class LackOfCohesionMethodsRuleSpec : Spek({

    val wrapper by memoized(
        factory = { KtTestCompiler.createEnvironment() },
        destructor = { it.dispose() }
    )

    describe("Full cohesion of all the method and properties") {
        it("Should give lcom 0") {

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

            val findings = LackOfCohesionOfMethodsRule().compileAndLintWithContext(wrapper.env, code)
            assertThat(findings).isEmpty()
        }
    }

    describe("Low cohesion") {
        it("Should give lcom over 0") {

            // language=kotlin
            val code = """
                    class Foo {
                        private var num1 = 0
                        private var num2 = 0
                        private var num3 = 0

                        fun inc1()=num1++
                        fun inc2()=num2++
                        fun inc3()=num3++
                    }
                """.trimIndent()

            val findings = LackOfCohesionOfMethodsRule().compileAndLintWithContext(wrapper.env, code)
            assertThat(findings).hasSize(1)
        }
    }

    describe("Multiple classes") {
        it("Should give correct result") {

            // language=kotlin
            val code = """
                    class Foo {
                        private var num1 = 0
                        private var num2 = 0
                        private var num3 = 0

                        fun inc1()=num1++
                        fun inc2()=num2++
                        fun inc3()=num3++
                    }
                    
                    class Bar { 
                        private var number = 0
                        private var number2 = 0

                        fun add()  {
                            number++
                            number2++
                        }
                                            
                        fun subtract() {
                            number--
                        }
                    }
                """.trimIndent()

            val findings = LackOfCohesionOfMethodsRule().compileAndLintWithContext(wrapper.env, code)
            assertThat(findings).hasSize(2)
        }
    }

})
