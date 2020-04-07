---
title: OpenClosedPrinciple - 
---

[io.github.mkohm.detekt.hint.rules](../index.html) / [OpenClosedPrinciple](./index.html)

# OpenClosedPrinciple

`class OpenClosedPrinciple : Rule`

Open closed principle rule. Only supports catching the easiest cases. Not complex when expressions, with type checking and use of enums.

Supported:
when (enum) {
    Color.RED -&gt; ...
    Color.Blue -&gt; ...
    ...
}

when {
    a is Square -&gt; ...
    b is Circle -&gt; ...
    ...
}

### Constructors

| [&lt;init&gt;](-init-.html) | `OpenClosedPrinciple(config: Config = Config.empty)`<br>Open closed principle rule. Only supports catching the easiest cases. Not complex when expressions, with type checking and use of enums. |

### Properties

| [issue](issue.html) | `val issue: Issue` |

### Functions

| [visitWhenExpression](visit-when-expression.html) | `fun visitWhenExpression(expression: KtWhenExpression): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |

