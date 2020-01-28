package io.github.mkohm.detekt.hint.rules

import io.gitlab.arturbosch.detekt.api.DefaultContext


 // should not warn
 open class InternalClass: DefaultContext()

 // should warn about this
 class AnotherInternalClass : InternalClass()

 // should report on this one
 open class A
 class B: A()
