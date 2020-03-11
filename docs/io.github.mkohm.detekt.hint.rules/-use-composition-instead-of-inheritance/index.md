[io.github.mkohm.detekt.hint.rules](../index.md) / [UseCompositionInsteadOfInheritance](./index.md)

# UseCompositionInsteadOfInheritance

`class UseCompositionInsteadOfInheritance : Rule`

A rule suggesting the use of composition instead of inheritance. It will help you test for Liskov Substitution and make sure that correct

The rule will fire every time inheritance is introduced, unless you derive from a class that exists in another package.
This will reduce the amount of warnings created where the framework or library you are working with are forcing you to introduce inheritance.

Remember to configure this rule correctly by adding:
"yourUniquePackageName" : "io.github.mkohm"
replacing "io.github.com" with your unique package name.

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `UseCompositionInsteadOfInheritance(config: Config = Config.empty)`<br>A rule suggesting the use of composition instead of inheritance. It will help you test for Liskov Substitution and make sure that correct |

### Properties

| Name | Summary |
|---|---|
| [issue](issue.md) | `val issue: Issue` |

### Functions

| Name | Summary |
|---|---|
| [visitClass](visit-class.md) | `fun visitClass(klass: KtClass): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
