# detekt-hint
An extension for Detekt

To use the extension one must build a .jar using `./gradlew jar`. One can then feed the jar into the detekt-cli using: 
```
java -jar ../../../detekt/detekt-cli/build/libs/detekt-cli-1.4.0-all.jar --plugins build/libs/detekt-sample-extensions.jar
```
