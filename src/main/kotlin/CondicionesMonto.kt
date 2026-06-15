//Condiciones para el viaje, de aca provienen las variaciones en el costo:
interface CondicionesViaje {
    fun aplicar(costoViaje: Double, cliente: Cliente, viaje: Viaje) : Double
}

object condicionPorEdad : CondicionesViaje {
    override fun aplicar(costoViaje : Double,cliente: Cliente,viaje: Viaje) : Double =
        if(cliente.edad < 18) costoViaje * 0.7 else if(cliente.edad > 65) costoViaje * 0.5 else costoViaje
}

object esHoraPico: CondicionesViaje {
    override fun aplicar(costoViaje : Double,cliente: Cliente,viaje: Viaje) : Double=
        if(viaje.fechaViaje.hour in 7..10 || viaje.fechaViaje.hour in 16..19) costoViaje + 2.0 else costoViaje
}

class VariasCondiciones(val listaCondiciones : MutableList<CondicionesViaje>) : CondicionesViaje {
    override fun aplicar(costoViaje : Double,cliente: Cliente,viaje: Viaje) : Double =
        listaCondiciones.fold(costoViaje){acumulador, condicion -> condicion.aplicar(acumulador, cliente, viaje)}

    fun agregarCondicion(condicionesViaje: CondicionesViaje){
        listaCondiciones.add(condicionesViaje)
    }
    fun eliminarCondicion(condicionesViaje: CondicionesViaje){
        listaCondiciones.remove(condicionesViaje)
    }
}
