class Viaje {
    val costoBase : Double = 5.0
    fun costo() : Double = costoBase
    fun descuentoPor(cliente: Cliente) : Double =
        if(cliente.edad < 18) 0.3 else if(cliente.edad > 65) 0.5 else 0.0
    fun extraHoraPico() : Double = 0.0
}