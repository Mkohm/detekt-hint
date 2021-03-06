package io.github.mkohm.detekt.hint

import io.github.mkohm.detekt.hint.rules.UseCompositionInsteadOfInheritance
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.KtTestCompiler
import io.gitlab.arturbosch.detekt.test.TestConfig
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import io.gitlab.arturbosch.detekt.test.compileForTest
import io.gitlab.arturbosch.detekt.test.lint
import io.gitlab.arturbosch.detekt.test.resource
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalStateException
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.nio.file.Path
import java.nio.file.Paths

val path: Path = Paths.get(resource("/cases"))

class UseCompositionInsteadOfInheritanceSpec : Spek({
    val testConfig = TestConfig(mapOf("yourUniquePackageName" to "io.github.mkohm"))

    val subject by memoized { UseCompositionInsteadOfInheritance(testConfig) }
    val wrapper by memoized(
        factory = { KtTestCompiler.createEnvironment() },
        destructor = { it.dispose() }
    )

    // language=kotlin
    val code = """    
            package io.github.mkohm
            
            open class InternalClass
            
            class AnotherInternalClass : InternalClass()
        """.trimIndent()
    describe("Inheritance from internal module") {
        it("Should report composition could be used instead of inheritance") {
            val rule =
                UseCompositionInsteadOfInheritance(testConfig)
            val findings = rule.compileAndLintWithContext(wrapper.env, code)
            assertThat(findings).hasSize(1)
        }
    }

    describe("Public interface of class A") {
        it("Should find all public methods and put in the report") {
            val code = compileForTest(path.resolve("SquareRectangle.kt"))

            val findings = subject.compileAndLintWithContext(wrapper.env, code.text)

            assertThat(findings).hasSize(1)
            assertThat(findings.first().message).contains("setHeight, setWidth")
        }
    }

    describe(" Private function in superclass") {
        it("Should not be part of public interface") {

            //language=kotlin
            val code = """
                package io.github.mkohm.detekt.hint.demo

                open class Airplane {                    
                    private fun takeOff() {
                    
                    }
                }

                class AirBus : Airplane() {

                }
            """.trimIndent()

            val findings = subject.compileAndLintWithContext(wrapper.env, code)

            assertThat(findings).hasSize(1)
            assertThat(findings.first().message).contains("empty public interface")
        }
    }

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

        val rule = UseCompositionInsteadOfInheritance(
            TestConfig(
                mapOf(Config.ACTIVE_KEY to "false")
            )
        )
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
            val rule =
                UseCompositionInsteadOfInheritance(testConfig)
            val findings = rule.lint(code)
            assertThat(findings).isEmpty()
        }
    }

    describe("Implementing interface") {
        it("Should not report any inheritance-warnings") {
            //language=kotlin
            val code = """

            package io.gitlab.arturbosch.detekt.sample.extensions

           import io.gitlab.arturbosch.detekt.api.DefaultContext

           class InternalClass: DefaultContext
           """.trimIndent()

            val rule = UseCompositionInsteadOfInheritance(testConfig)
            val findings = rule.lint(code)
            assertThat(findings).isEmpty()
        }
    }

    describe("Enums") {
        it("Should not give any warnings") {
            val rule = UseCompositionInsteadOfInheritance(testConfig)

            //language=kotlin
            val code = """
                enum class RealmDataProvider(val i: Int) {
                    UNDEFINED(0),
                    BLE(1),
                    CLOUD(2),
                    OLDCSV(3)
                }
            """.trimIndent()

            val findings = rule.lint(code)
            assertThat(findings).isEmpty()
        }
    }

    describe("Subclasses of external classes") {
        it("Should not give any warnings") {
            val rule =
                UseCompositionInsteadOfInheritance(testConfig)

            //language=kotlin
            val code = """
                package io.github.mkohm.utils

                import android.view.View
                import android.view.ViewGroup
                import androidx.annotation.CallSuper
                import androidx.databinding.ViewDataBinding
                import androidx.recyclerview.widget.RecyclerView

                abstract class BaseRecyclerViewAdapter<T, D : ViewDataBinding> :
                    RecyclerView.Adapter<BaseRecyclerViewAdapter.ViewHolder<D>>() {

                    class ViewHolder<D : ViewDataBinding>(val binding: D) : RecyclerView.ViewHolder(binding.root)

                    interface OnClickHandler<T, D : ViewDataBinding> {
                        fun onClick(view: View, holder: ViewHolder<D>, item: T)
                    }
                }
            """.trimIndent()

            val findings = rule.lint(code)
            assertThat(findings).isEmpty()
        }
    }

    describe("Not including package identifier in config") {
        it("Should throw runtime error") {
            val rule = UseCompositionInsteadOfInheritance()
            assertThatIllegalStateException().isThrownBy {
                val findings = rule.compileAndLintWithContext(wrapper.env, code)
            }
        }
    }
})
