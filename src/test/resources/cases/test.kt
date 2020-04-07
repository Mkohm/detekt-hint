package cases

enum class Color {
    RED, BLUE, BLACK
}

class A {
    fun switch(a: Color) {
        when {
            a == Color.RED -> print("red")
            "" is String -> print("")
            else -> print("")
            // test() -> print("true")
        }
    }

    private fun test(): Boolean {
        return true
    }
}
