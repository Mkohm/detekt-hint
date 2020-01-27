package io.github.mkohm.detekt.hint

import io.github.mkohm.detekt.hint.rules.LackOfCohesionOfMethodsRule
import io.github.mkohm.detekt.hint.rules.UseCompositionInsteadOfInheritance
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class SampleProvider : RuleSetProvider {

    override val ruleSetId: String = "sample"

    override fun instance(config: Config): RuleSet = RuleSet(
        ruleSetId,
        listOf(
            UseCompositionInsteadOfInheritance(config), LackOfCohesionOfMethodsRule(config)
        )
    )
}
