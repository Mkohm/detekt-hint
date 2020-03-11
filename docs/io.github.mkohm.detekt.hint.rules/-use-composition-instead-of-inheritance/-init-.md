[io.github.mkohm.detekt.hint.rules](../index.md) / [UseCompositionInsteadOfInheritance](index.md) / [&lt;init&gt;](./-init-.md)

# &lt;init&gt;

`UseCompositionInsteadOfInheritance(config: Config = Config.empty)`

A rule suggesting the use of composition instead of inheritance. It will help you test for Liskov Substitution and make sure that correct

The rule will fire every time inheritance is introduced, unless you derive from a class that exists in another package.
This will reduce the amount of warnings created where the framework or library you are working with are forcing you to introduce inheritance.

Remember to configure this rule correctly by adding:
"yourUniquePackageName" : "io.github.mkohm"
replacing "io.github.com" with your unique package name.

