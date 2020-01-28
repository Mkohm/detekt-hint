package io.github.mkohm.detekt.hint

import io.gitlab.arturbosch.detekt.api.DefaultContext


// should not warn
open class InternalClass: DefaultContext()

// should warn about this
class AnotherInternalClass : InternalClass()