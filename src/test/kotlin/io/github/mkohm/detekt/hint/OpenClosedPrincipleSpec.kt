package io.github.mkohm.detekt.hint

import io.github.mkohm.detekt.hint.rules.OpenClosedPrinciple
import io.gitlab.arturbosch.detekt.test.KtTestCompiler
import io.gitlab.arturbosch.detekt.test.TestConfig
import io.gitlab.arturbosch.detekt.test.compileAndLint
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class OpenClosedPrincipleSpec : Spek({

    val testConfig = TestConfig(mapOf("yourUniquePackageName" to "io.github.mkohm"))

    val subject by memoized { OpenClosedPrinciple(testConfig) }
    val wrapper by memoized(
        factory = { KtTestCompiler.createEnvironment() },
        destructor = { it.dispose() }
    )



    describe("Switching on enums") {
        it("Should be reported as a possible violation of OCP") {

            // language=kotlin
            val code = """
                
enum class Color {
    RED, BLUE, BLACK
}

class A {
    fun switch(a: Color) {
        when (a) {
            Color.RED -> print("red")
            Color.BLUE -> print("blue")
            Color.BLACK -> print("black")
        }
    }
}
        """.trimIndent()

            val rule =
                OpenClosedPrinciple()
            val findings = rule.compileAndLintWithContext(wrapper.env, code)
            assertThat(findings).hasSize(1)
        }
    }


    describe("Switching on logic values") {
        it("Should not be reported as a possible violation of OCP") {

            // language=kotlin
            val code = """
class A {
    fun switch() {
        when {
            true -> print("red")
            1 == 1 -> print("blue")
            false  -> print("black")
        }
    }
}
        """.trimIndent()

            val rule =
                OpenClosedPrinciple()
            val findings = rule.compileAndLint(code)
            assertThat(findings).hasSize(0)
        }
    }

    describe("Switching on types") {
        it("Should be reported as a possible violation of OCP") {

            // language=kotlin
            val code = """enum class Color {
    RED, BLUE, BLACK
}

class A {
    fun switch(a: Color) {

        val a = ""
        when {
            a is String -> print("red")
            a is Int -> print("number")
            else -> print("something else")
        }
    }
}
        """.trimIndent()

            val rule =
                OpenClosedPrinciple()
            val findings = rule.compileAndLintWithContext(wrapper.env, code)
            assertThat(findings).hasSize(1)
        }
    }


    describe("Switching on types") {
        it("Should be reported as a possible violation of OCP") {

            // language=kotlin
            val code = """
                
class Drawer {
    fun drawAllShapes() {
        val shapes = Provider.getShapes()

        for (shape in shapes) {
            when (shape) {
                is Rectangle -> drawRectangle(shape)
                is Circle -> drawCircle(shape)
            }
        }
    }

    private fun drawRectangle(shape: Rectangle) {
        println("drawing rectangle")
    }

    private fun drawCircle(shape: Circle) {
        println("drawing circle")
    }
}

object Provider {
    fun getShapes(): List<Any> {
        return listOf(Rectangle(10, 10), Circle(5), Circle(5))
    }
}""".trimIndent()

            val rule =
                OpenClosedPrinciple()
            val findings = rule.compileAndLintWithContext(wrapper.env, code)
            assertThat(findings).hasSize(1)
        }
    }

})