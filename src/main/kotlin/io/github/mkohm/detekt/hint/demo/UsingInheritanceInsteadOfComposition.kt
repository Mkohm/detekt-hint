package io.github.mkohm.detekt.hint.demo

interface Fly {
    fun fly()
}

open class Airplane : Fly {
    override fun fly() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class AirBus : Airplane() {
    override fun fly() {

    }
}

class Bird : Fly {
    override fun fly() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
