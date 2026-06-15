import java.time.LocalDate
import java.time.LocalDateTime

abstract class Viaje(val topeDeuda : Double, val fechaViaje : LocalDateTime, var condiciones : CondicionesViaje, var partida : Lugar, var destino : Lugar) {
    open var listaPersonas : MutableList<Cliente> = mutableListOf()
    val costoBase : Double = 5.0
    var listaObservers : MutableList<InformacionFinal> = mutableListOf()
    abstract fun tramos(): List<Tramo>

    fun costo(cliente: Cliente) : Double = condiciones.aplicar(costoBase, cliente,this)

    fun reasignarCondicion(nueva : CondicionesViaje){
        condiciones = nueva
    }

    fun ingresarPersonas(cliente : Cliente){
        validaciones(cliente)
        agregarPersonas(cliente)
    }

    abstract fun validaciones(cliente: Cliente)

    fun agregarPersonas(cliente : Cliente){
        listaPersonas.add(cliente)
        cliente.agregarViaje(this)
    }

    fun completarViaje(){
        listaPersonas.forEach { cliente -> listaObservers.forEach { observer -> observer.informar(cliente,this) } }
    }

}

//Sub-tipos de Viajes
class ViajeProgramado(val topePersonas : Int, listaPersonas: MutableList<Cliente>, topeDeuda : Double, fechaViaje : LocalDateTime, condiciones : CondicionesViaje, partida : Lugar, destino : Lugar): Viaje( topeDeuda, fechaViaje, condiciones, partida, destino){
    override fun tramos() = listOf(Tramo(partida,destino))

    override fun validaciones(cliente : Cliente){
        validacionTopePersona() //verifica que la cantidad de gente no supere el tope por viaje
        val horaIngreso = LocalDateTime.now()
        validacionHoraIngreso(horaIngreso) //verifica que la hora que entro el cliente no sea superior a 2 hs antes
        validacionSaldo(cliente) //valida que el cliente tenga saldo mayor al tope del viaje
    }

    fun validacionTopePersona(){if(listaPersonas.size > topePersonas)
        throw ExcepcionLimiteIngreso("Ya no se permite el ingreso de más personas")}

    fun validacionHoraIngreso(horaIngreso : LocalDateTime){if(horaIngreso.isBefore(fechaViaje.minusHours(2)))
            throw ExcepcionHoraIngreso("No puede ingresar más de 2 horas antes")}

    fun validacionSaldo(cliente : Cliente){if(cliente.saldoDeudor() > topeDeuda)
            throw ExcepcionLimiteDeuda("Limite deuda excedido para este viaje")}
}

data class Lugar(val nombre : String, val direccion: Direccion)

data class Tramo(val partida: Lugar, val destino : Lugar)

class Itinerario(val tramos : MutableList<Tramo>, topeDeuda: Double, fechaViaje : LocalDateTime, condiciones : CondicionesViaje, partida : Lugar, destino : Lugar): Viaje(topeDeuda, fechaViaje, condiciones, partida, destino){
    override fun tramos() = this.tramos

    override fun validaciones(cliente: Cliente){
        validacionSaldo(cliente)
        validacionFechaTarjeta(cliente)
    }

    fun validacionSaldo(cliente : Cliente){if(cliente.saldoDeudor() > topeDeuda)
        throw ExcepcionLimiteDeuda("Limite deuda excedido para este viaje")}

    fun validacionFechaTarjeta(cliente: Cliente){
        if(cliente.fechaAdquTarjeta().isAfter(LocalDate.now().minusMonths(6))){
            throw ExcepcionFechaAdquisicionTarjeta("Adquirió la tarjeta hace menos de 6 meses")
        }
    }
    fun agregarTramo(tramo: Tramo){
        tramos.add(tramo)
    }
}
