# detekt-hint
[![Maintainability](https://api.codeclimate.com/v1/badges/307995daba5f21506f4d/maintainability)](https://codeclimate.com/github/Mkohm/detekt-hint/maintainability) [![codecov](https://codecov.io/gh/Mkohm/detekt-hint/branch/master/graph/badge.svg)](https://codecov.io/gh/Mkohm/detekt-hint)

detekt-hint is a plugin for [detekt](https://github.com/arturbosch/detekt) that includes detection of violation of programming principles. Since such violations are hard to detect with low false-positive rates, detekt-hint will provide hints during QA, minimizing noise during development. The idea is that a higher false-positive rate can be accepted if the detection could be of high value, and is easy to ignore. Detections on the architectural level of code is therefore most likely to provide value.

Through integration with [Danger](https://github.com/danger/danger) comments are added to the PR.

Contributions are very much welcome. Especially help in which rules to implement is of high value.

![demo](demo.png)
## Currently supported detections
- Use composition instead of inheritance - Will report if you inherit from a class that is in the same module.


## Using detekt-hint
This repository is using detekt-hint itself, and serves as an example setup.

### With Gradle
You need to already use detekt. Look for instructions [here](https://github.com/arturbosch/detekt). Then, to add detekt-hint add 
```
dependencies {
    detektPlugins "io.github.mkohm:detekt-hint:[version]"
}
```
to your build.gradle. Remember to enter the [latest version](https://mvnrepository.com/artifact/io.github.mkohm/detekt-hint) of detekt-hint to use.

### With the command line
To use the extension one must first clone this repository (and the detekt repository), and then build the jar.
```
git clone https://github.com/Mkohm/detekt-hint
cd detekt-hint
./gradlew jar
```

One can then feed the jar into the detekt-cli using: 
```
java -jar <path to detekt-cli-jar> --plugins <path to detekt-hint-jar> --config <path to config file>
```
For example: `java -jar ../../../detekt/detekt-cli/build/libs/detekt-cli-1.4.0-all.jar --plugins build/libs/detekt-hint-0.0.1.jar --config detekt/detekt.yml
`
Remember to configure `detekt.yml` to include the additional rules from detekt-hint. Look inside this repository for an example.

## Integration with Danger
Will update this soon.
