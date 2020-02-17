package io.github.mkohm.detekt.hint

import io.github.mkohm.detekt.hint.rules.LackOfCohesionOfMethodsRule
import io.gitlab.arturbosch.detekt.test.KtTestCompiler
import io.gitlab.arturbosch.detekt.test.TestConfig
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class LackOfCohesionMethodsRuleSpec : Spek({

    // Will always report the LCOM value so that we can verify the correct value for each of the tests.
    val testConfig = TestConfig(mapOf("threshold" to "-1"))

    val wrapper by memoized(
        factory = { KtTestCompiler.createEnvironment() },
        destructor = { it.dispose() }
    )

    describe("Using properties outside of class should not count as a property.") {
        it("Should give correct LCOM value") {

            // language=kotlin
            val code = """
                var a = 1
                
                class Foo {
                    private var number = 0
                    
                    fun bar() {
                        Baz().number++
                    }
                }
                
                class Baz {
                    var number = 0
                }
                """.trimIndent()

            val findings = LackOfCohesionOfMethodsRule(testConfig).compileAndLintWithContext(wrapper.env, code)

            val f = 1
            val m = 1
            val mf = 0
            val lcom = 1 - (mf.toDouble() / (m * f))

            assertThat(findings.first().message).contains("Foo have a too high LCOM value: $lcom")
        }
    }

    describe("Using properties outside of class should not count as a property.") {
        it("Should give correct LCOM value") {

            // language=kotlin
            val code = """
                class Foo {
                    private var number = 0
                    
                    fun bar() {
                        baz()
                        baz("arg")
                    }
                    
                    fun baz() {
                        number++
                    }
                    
                    private fun baz(arg: String) {
                        number++
                    }
                }
                """.trimIndent()

            val findings = LackOfCohesionOfMethodsRule(testConfig).compileAndLintWithContext(wrapper.env, code)

            val f = 1
            val m = 2
            val mf = 2

            val lcom = 1 - (mf.toDouble() / (m * f))

            assertThat(findings.first().message).contains("Foo have a too high LCOM value: $lcom")
        }
    }

    describe("Referencing a property with the same name in another class should not count as a reference.") {
        it("Should give correct LCOM value") {

            // language=kotlin
            val code = """
                class Foo {
                    private var number = 0
                    
                    fun bar() {
                        Baz().number++
                    }
                }
                
                class Baz {
                    var number = 0
                }
                """.trimIndent()

            val findings = LackOfCohesionOfMethodsRule(testConfig).compileAndLintWithContext(wrapper.env, code)

            val f = 1
            val m = 1
            val mf = 0
            val lcom = 1 - (mf.toDouble() / (m * f))

            assertThat(findings.first().message).contains("Foo have a too high LCOM value: $lcom")
        }
    }











    describe("Simple class") {
        it("Should give correct LCOM value") {

            // language=kotlin
            val code = """
                class Foo {

                    private var number = 0
                    private var number2 = 0
                    
                    fun bar() {
                        bar("string")
                    }
                    
                    fun bar(string: String) {
                        number++
                    }
                }
                """.trimIndent()

            val findings = LackOfCohesionOfMethodsRule(testConfig).compileAndLintWithContext(wrapper.env, code)

            val f = 2
            val m = 2
            val mf = 2
            val lcom = 1 - (mf.toDouble() / (m * f))

            assertThat(findings.first().message).contains(lcom.toString())
        }
    }

    describe("Simple class") {
        it("Should give correct LCOM value") {

            // language=kotlin
            val code = """
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

            val findings = LackOfCohesionOfMethodsRule(testConfig).compileAndLintWithContext(wrapper.env, code)

            val f = 2
            val m = 2
            val mf = 3
            val lcom = 1 - (mf.toDouble() / (m * f))

            assertThat(findings.first().message).contains(lcom.toString())
        }
    }

    describe("Class with private methods") {
        it("Should give correct LCOM value") {

            // language=kotlin
            val code = """
                    class Foo {
                        private var num1 = 0
                        private var num2 = 0
                        private var num3 = 0

                        fun inc1() {
                            num1++ 
                            
                            inc2()
                            inc3()
                        }
                        
                        private fun inc2() {
                            num2++
                            num2++
                            num1++
                            num3++
                            inc4()
                        }
                        
                        private fun inc3() {
                            num3++
                        }
                        
                        private fun inc4() {
                            num3++
                        }
                    }
                """.trimIndent()

            val findings = LackOfCohesionOfMethodsRule(testConfig).compileAndLintWithContext(wrapper.env, code)

            val f = 3
            val m = 1
            val mf = 3
            val lcom = 1 - (mf.toDouble() / (m * f))

            assertThat(findings.first().message).contains(lcom.toString())
        }
    }


    describe("Class with private methods") {
        it("Should give correct LCOM value") {

            // language=kotlin
            val code = """
                    class Foo {
                        private var num1 = 0
                        private var num2 = 0
                        private var num3 = 0

                        fun inc1() {
                            num1++
                            
                            inc2()
                            inc3()
                        }
                        
                        private fun inc2() {
                            num2++
                        }
                        
                        private fun inc3() {
                            num3++
                        }
                    }
                """.trimIndent()

            val findings = LackOfCohesionOfMethodsRule(testConfig).compileAndLintWithContext(wrapper.env, code)

            val f = 3
            val m = 1
            val mf = 3
            val lcom = 1 - (mf.toDouble() / (m * f))

            assertThat(findings.first().message).contains(lcom.toString())
        }
    }


    describe("Full cohesion of all the method and properties") {
        it("Should give LCOM 0") {

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

            val findings = LackOfCohesionOfMethodsRule(testConfig).compileAndLintWithContext(wrapper.env, code)

            val f = 1
            val m = 2
            val mf = 2
            val lcom = 1 - (mf.toDouble() / (m * f))


            assertThat(findings.first().message).contains(lcom.toString())

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

            val findings = LackOfCohesionOfMethodsRule(testConfig).compileAndLintWithContext(wrapper.env, code)

            val f = 3
            val m = 3
            val mf = 3
            val lcom = 1 - (mf.toDouble() / (m * f))

            assertThat(findings.first().message).contains(lcom.toString())
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
            val f = 3
            val m = 3
            val mf = 3
            val lcom = 1 - (mf.toDouble() / (m * f))

            val findings = LackOfCohesionOfMethodsRule(testConfig).compileAndLintWithContext(wrapper.env, code)
            assertThat(findings.first().message).contains(lcom.toString())

            val f2 = 2
            val m2 = 2
            val mf2 = 3
            val lcom2 = 1 - (mf2.toDouble() / (m2 * f2))
            assertThat(findings[1].message).contains(lcom2.toString())
        }
    }

})
