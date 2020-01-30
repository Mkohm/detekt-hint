package cases

open class Rectangle(width: Int, height: Int) {
    private var width = 0
    private var height = 0

    init {
        this.width = width
        this.height = height
    }

    fun setWidth(newWidth: Int) {
        this.width = newWidth
    }

    fun setHeight(newHeight: Int) {
        this.height = newHeight
    }
}

class Square(width: Int, height: Int) : Rectangle(width, height) {

}
