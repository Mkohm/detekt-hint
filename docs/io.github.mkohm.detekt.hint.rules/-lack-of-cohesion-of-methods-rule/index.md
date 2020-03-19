---
title: LackOfCohesionOfMethodsRule - 
---

[io.github.mkohm.detekt.hint.rules](../index.html) / [LackOfCohesionOfMethodsRule](./index.html)

# LackOfCohesionOfMethodsRule

`class LackOfCohesionOfMethodsRule : Rule`

A rule that notifies if there is too much lack of cohesion.

### Constructors

| [&lt;init&gt;](-init-.html) | `LackOfCohesionOfMethodsRule(config: Config = Config.empty)`<br>A rule that notifies if there is too much lack of cohesion. |

### Properties

| [cachedResult](cached-result.html) | `val cachedResult: `[`MutableMap`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-mutable-map/index.html)`<KtNamedFunction, `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<KtNamedFunction>>` |
| [issue](issue.html) | `val issue: Issue` |

### Functions

| [visitClass](visit-class.html) | `fun visitClass(klass: KtClass): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [visitNamedDeclaration](visit-named-declaration.html) | `fun visitNamedDeclaration(declaration: KtNamedDeclaration): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |

