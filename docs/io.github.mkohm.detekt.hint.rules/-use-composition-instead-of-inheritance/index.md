---
title: UseCompositionInsteadOfInheritance - 
---

[io.github.mkohm.detekt.hint.rules](../index.html) / [UseCompositionInsteadOfInheritance](./index.html)

# UseCompositionInsteadOfInheritance

`class UseCompositionInsteadOfInheritance : Rule`

A rule suggesting the use of composition instead of inheritance. It will help you test for Liskov Substitution.

The rule will fire every time inheritance is introduced, unless you derive from a class that exists in a third party package.
This will reduce the amount of warnings created where the framework or library are forcing you to introduce inheritance.

Remember to configure this rule correctly by adding:
"yourUniquePackageName" : "io.github.mkohm"
replacing "io.github.com" with your unique package name.

### Constructors

| [&lt;init&gt;](-init-.html) | `UseCompositionInsteadOfInheritance(config: Config = Config.empty)`<br>A rule suggesting the use of composition instead of inheritance. It will help you test for Liskov Substitution. |

### Properties

| [issue](issue.html) | `val issue: Issue` |

### Functions

| [visitClass](visit-class.html) | `fun visitClass(klass: KtClass): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |

