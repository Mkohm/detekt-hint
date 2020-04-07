---
title: OpenClosedPrinciple.<init> - 
---

[io.github.mkohm.detekt.hint.rules](../index.html) / [OpenClosedPrinciple](index.html) / [&lt;init&gt;](./-init-.html)

# &lt;init&gt;

`OpenClosedPrinciple(config: Config = Config.empty)`

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

