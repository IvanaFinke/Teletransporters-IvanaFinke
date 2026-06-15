
data class Direccion(var x : Float, var y : Float) {
}

interface ServicioCalculaDistancia{
    fun calcularDistancia(x1 : Float, y1 : Float, x2 : Float, y2 : Float, distDirecta : Int) : Pair<Int, Int>
}

class ServicioDistanciaAdapter(var servicioDistancia: ServicioCalculaDistancia) {
    fun calcularDistanciaAKilometros(origen : Lugar, destino : Lugar) : Double {
        val (parteEntera,parteDecimal)= servicioDistancia.calcularDistancia(
            origen.direccion.x,
            origen.direccion.y,
            destino.direccion.x,
            destino.direccion.y,
            18)
        val distanciaMillas = parteEntera + parteDecimal / 10.0
        return distanciaMillas * 1.609344
    }
    fun calcularDistanciaTotalTramos(tramos : List<Tramo>) : Double =
        tramos.sumOf { calcularDistanciaAKilometros(it.partida,it.destino) }


}