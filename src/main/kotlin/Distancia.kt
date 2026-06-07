import org.uqbar.geodds.Point

data class Direccion(var x : Float, var y : Float, val distancia : Int = 18, val ubicacion : Point) {
}

class ServicioDistancia(val puntoA : Lugar, val puntoB : Lugar) {
    fun calcularDistancia() = puntoA.direccion.distance(puntoB)
}