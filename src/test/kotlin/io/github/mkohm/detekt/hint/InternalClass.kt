package io.github.mkohm.detekt.hint

import io.gitlab.arturbosch.detekt.api.DefaultContext


// should not warn
class InternalClass: DefaultContext()

// should warn
//class AnotherInternalClass : UseCompositionInsteadOfInheritance

//By knowing gruop-id we can know which classes that are internal or external.