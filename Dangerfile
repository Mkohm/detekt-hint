# Sometimes it's a README fix, or something like that - which isn't relevant for
# including in a project's CHANGELOG for example
declared_trivial = github.pr_title.include? "#trivial"

# Make it more obvious that a PR is a work in progress and shouldn't be merged yet
warn("PR is classed as Work in Progress") if github.pr_title.include? "[WIP]"

# Warn when there is a big PR
warn("Big PR") if git.lines_of_code > 500

# Don't let testing shortcuts get into master by accident
fail("fdescribe left in tests") if `grep -r fdescribe specs/ `.length > 1
fail("fit left in tests") if `grep -r fit specs/ `.length > 1

# Use the kotlin_detekt danger-plugin and tell it that the task to run is called "detekt"
#filtering = true
kotlin_detekt.filtering = true
kotlin_detekt.report_file = "build/reports/detekt/main.xml"
kotlin_detekt.gradle_task = "detektMain"
kotlin_detekt.detekt(inline_mode: true)