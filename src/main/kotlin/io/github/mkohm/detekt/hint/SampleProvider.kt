package io.github.mkohm.detekt.hint

import io.github.mkohm.detekt.hint.rules.InterfaceSegregationPrinciple
import io.github.mkohm.detekt.hint.rules.LackOfCohesionOfMethods
import io.github.mkohm.detekt.hint.rules.UseCompositionInsteadOfInheritance
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class SampleProvider : RuleSetProvider {

    override val ruleSetId: String = "sample"

    override fun instance(config: Config): RuleSet = RuleSet(
        ruleSetId,
        listOf(
            UseCompositionInsteadOfInheritance(config), LackOfCohesionOfMethods(config), InterfaceSegregationPrinciple(config)
        )
    )
}
