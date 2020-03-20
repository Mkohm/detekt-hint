---
title: LackOfCohesionOfMethodsRule.<init> - 
---

[io.github.mkohm.detekt.hint.rules](../index.html) / [LackOfCohesionOfMethodsRule](index.html) / [&lt;init&gt;](./-init-.html)

# &lt;init&gt;

`LackOfCohesionOfMethodsRule(config: Config = Config.empty)`

A rule that notifies if there is too much lack of cohesion. Remember to configure it correctly in the detekt.yml.

LCOM for a class will range between 0 and 1, with 0 being totally cohesive and 1 being totally non-cohesive.
This makes sense since a low “lack of cohesion” score would mean a lot of cohesion.

For each property in the class, you count the methods that reference it, and then you add all of those up across all properties. This value is called referencesCount.
You then divide that by the count of methods times the count of properties, and you subtract the result from one.

LCOM = 1 - referencesCount / ( methodsCount * propertyCount)

