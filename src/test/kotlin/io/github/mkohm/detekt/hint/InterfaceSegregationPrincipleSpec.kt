package io.github.mkohm.detekt.hint

import io.github.mkohm.detekt.hint.rules.InterfaceSegregationPrinciple
import io.gitlab.arturbosch.detekt.test.KtTestCompiler
import io.gitlab.arturbosch.detekt.test.compileAndLint
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class InterfaceSegregationPrincipleSpec : Spek({

    val wrapper by memoized(
        factory = { KtTestCompiler.createEnvironment() },
        destructor = { it.dispose() }
    )

    describe("Empty overridden method") {
        it("Should be reported as a violation of ISP") {

            // language=kotlin
            val code = """
        interface AllInOnePrinter {
            fun print()
            fun scan()
        }

        class LuxuryPrinter : AllInOnePrinter {
            override fun print() {
                println("Print")
            }

            override fun scan() {}
        }
        """.trimIndent()

            val rule =
                InterfaceSegregationPrinciple()
            val findings = rule.compileAndLintWithContext(wrapper.env, code)
            assertThat(findings).hasSize(1)
        }
    }


    describe("Overridden method with only comment inside") {
        it("Should be reported as a violation of ISP") {

            // language=kotlin
            val code = """
        interface AllInOnePrinter {
            fun print()
            fun scan()
        }

        class LuxuryPrinter : AllInOnePrinter {
            override fun print() {
                println("Print")
            }

            override fun scan() {
            // A nice comment
            
            /*
            Block comment
             */
            }
        }
        """.trimIndent()

            val rule = InterfaceSegregationPrinciple()
            val findings = rule.compileAndLintWithContext(wrapper.env, code)
            assertThat(findings).hasSize(1)
        }
    }


    describe("Overridden method that only throws exception") {
        it("Should be reported as a violation of ISP") {

            // language=kotlin
            val code = """
        interface AllInOnePrinter {
            fun print()
            fun scan()
        }

        class LuxuryPrinter : AllInOnePrinter {
            override fun print() {
                println("Print")
            }

            override fun scan() {
            throw UnsupportedOperationException("Unsupported")
            }
        }
        """.trimIndent()

            val rule = InterfaceSegregationPrinciple()
            val findings = rule.compileAndLintWithContext(wrapper.env, code)
            assertThat(findings).hasSize(1)
        }
    }
    describe("Overridden method that only throws exception in an single expression function") {
        it("Should be reported as a violation of ISP") {

            // language=kotlin
            val code = """
        interface AllInOnePrinter {
            fun print()
            fun scan()
        }

        class LuxuryPrinter : AllInOnePrinter {
            override fun print() {
                println("Print")
            }

            override fun scan() = throw UnsupportedOperationException("Unsupported")
            
        }
        """.trimIndent()

            val rule = InterfaceSegregationPrinciple()
            val findings = rule.compileAndLintWithContext(wrapper.env, code)
            assertThat(findings).hasSize(1)
        }
    }


    describe("Overridden method that only throws exception") {
        it("Should be reported as a violation of ISP") {

            // language=kotlin
            val code = """
        class SampleProvider : RuleSetProvider {

            override val ruleSetId: String = "sample"

            override fun instance(config: Config): RuleSet = RuleSet(
                ruleSetId,
                listOf(
                    UseCompositionInsteadOfInheritance(config), LackOfCohesionOfMethods(config), InterfaceSegregationPrinciple(config)
                )
            )
        }
        """.trimIndent()

            val rule = InterfaceSegregationPrinciple()
            val findings = rule.compileAndLintWithContext(wrapper.env, code)
            assertThat(findings).hasSize(0)
        }
    }

    describe("Overridden method that only throws exception") {
        it("Should be reported as a violation of ISP") {

            // language=kotlin
            val code = """
        class SampleProvider : RuleSetProvider {

            override val ruleSetId: String = "sample"

            override fun instance(config: Config): RuleSet = throw UnsupportedOperationException("not supported")
        }
        """.trimIndent()

            val rule = InterfaceSegregationPrinciple()
            val findings = rule.compileAndLintWithContext(wrapper.env, code)
            assertThat(findings).hasSize(1)
        }
    }

    describe("Overridden method that throws exception and does something else") {
        it("Should not be reported as a violation of ISP") {

            // language=kotlin
            val code = """
        class SampleProvider : RuleSetProvider {

            override val ruleSetId: String = "sample"

            override fun instance(config: Config): RuleSet {
            val a = 1
            throw UnsupportedOperationException("not supported")
            } 
        }
        """.trimIndent()

            val rule = InterfaceSegregationPrinciple()
            val findings = rule.compileAndLintWithContext(wrapper.env, code)
            assertThat(findings).hasSize(0)
        }
    }

    describe("Overridden method that throws exception") {
        it("Should not be reported as a violation of ISP") {

            // language=kotlin
            val code = """
class A : B {                    
override fun beginFwUpdate(processListener: FwUpdateProcess.ProcessListener) {
                        this.firmwareSource.checkUpdate(context, serialNumber, interactionClient) { shouldUpdate, updates ->
                            if (updates is Success && shouldUpdate) {
                                val firmMap = updates.value.filterValues { value -> value != null }.mapValues { entry -> entry.value!! }
                                writeUiSettingsAndUpgradeImagesAsNecessary(processListener, firmMap)
                            } else if (updates is Failure) {
                                val error = FwUpdateProcess.AbortReason.NOT_ABLE_TO_READ_FIRMWARE_VERSIONS
                                error.description = updates.e.message
                                processListener.upgradeAborted(error)
                            } else {
                                throw RuntimeException("Triggered update when not necessary")
                            }
                        }
                    }
                    }
        """.trimIndent()

            val rule = InterfaceSegregationPrinciple()
            val findings = rule.compileAndLintWithContext(wrapper.env, code)
            assertThat(findings).hasSize(0)
        }
    }

    describe("Overridden method that only throws exception, but has comment in front") {
        it("Should be reported as a violation of ISP") {

            // language=kotlin
            val code = """
 import java.lang.RuntimeException
interface A {
                            fun print()
                        }

                        class B : A {
                            override fun print() = /* Try me */ throw RuntimeException("Not supported")
                        }
            """.trimIndent()

            val rule = InterfaceSegregationPrinciple()
            val findings = rule.compileAndLintWithContext(wrapper.env, code)
            assertThat(findings).hasSize(1)
        }

    }

    describe("Overridden method that only throws exception, but has comment in the back") {
        it("Should be reported as a violation of ISP") {

            // language=kotlin
            val code = """
 import java.lang.RuntimeException
interface A {
                            fun print()
                        }

                        class B : A {
                            override fun print() = throw RuntimeException("Not supported")  /* Try me */ 
                        }
            """.trimIndent()

            val rule = InterfaceSegregationPrinciple()
            val findings = rule.compileAndLintWithContext(wrapper.env, code)
            assertThat(findings).hasSize(1)
        }

    }

    describe("Overridden method that is single expression, but has implementation") {
        it("Should not be reported as a violation of ISP") {

            // language=kotlin
            val code = """
interface A {
                            fun print(): String
                        }

                        class B : A {
                            override fun print() = "printed" 
                        }
            """.trimIndent()

            val rule = InterfaceSegregationPrinciple()
            val findings = rule.compileAndLintWithContext(wrapper.env, code)
            assertThat(findings).hasSize(0)
        }

    }


    describe("Overridden method that is single expression, but has implementation and comment in the back") {
        it("Should not be reported as a violation of ISP") {

            // language=kotlin
            val code = """
interface A {
                            fun print(): String
                        }

                        class B : A {
                            override fun print() = "printed"  /* Try me */ 
                        }
            """.trimIndent()

            val rule = InterfaceSegregationPrinciple()
            val findings = rule.compileAndLintWithContext(wrapper.env, code)
            assertThat(findings).hasSize(0)
        }

    }


    describe("Overridden method that is of type body exression and that is implemented") {
        it("Should not be reported as a violation of ISP") {

            // language=kotlin
            val code = """
                package io.gitlab.arturbosch.detekt.api.internal

                interface Notification {
                fun toString(): String
                }

                data class SimpleNotification(
                    override val message: String,
                    override val level: Notification.Level = Notification.Level.Error
                ) : Notification {

                    override fun toString(): String = message
                }
            """.trimIndent()

            val rule = InterfaceSegregationPrinciple()
            val findings = rule.compileAndLintWithContext(wrapper.env, code)
            assertThat(findings).hasSize(0)
        }

    }
})