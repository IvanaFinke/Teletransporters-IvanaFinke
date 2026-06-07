import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.cos

abstract class Viaje(val listaPersonas : MutableList<Cliente>, val topeDeuda : Double, val fechaViaje : LocalDateTime, var condiciones : CondicionesViaje, var partida : Lugar, var destino : Lugar) {
    val costoBase : Double = 5.0
    var listaObservers : MutableList<InformacionFinal> = mutableListOf()

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
    }

    fun completarViaje(){
        listaPersonas.forEach { cliente -> listaObservers.forEach { observer -> observer.informar(cliente,this) } }
    }
}

//Sub-tipos de Viajes
class ViajeProgramado(val topePersonas : Int, listaPersonas: MutableList<Cliente>, topeDeuda : Double, fechaViaje : LocalDateTime, condiciones : CondicionesViaje, partida : Lugar, destino : Lugar): Viaje(listaPersonas, topeDeuda, fechaViaje, condiciones, partida, destino){
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

class Itinerario(val puntosIntermedios : MutableList<Lugar>, listaPersonas: MutableList<Cliente>, topeDeuda: Double, fechaViaje : LocalDateTime, condiciones : CondicionesViaje, partida : Lugar, destino : Lugar): Viaje(listaPersonas,topeDeuda, fechaViaje, condiciones, partida, destino){
    override fun validaciones(cliente: Cliente){
        validacionSaldo(cliente)
        validacionFechaTarjeta(cliente)
    }
    
    fun validacionSaldo(cliente : Cliente){if(cliente.saldoDeudor() < topeDeuda)
        throw ExcepcionLimiteDeuda("Limite deuda excedido para este viaje")}

    fun validacionFechaTarjeta(cliente: Cliente){
        if(cliente.fechaAdquTarjeta().isAfter(LocalDate.now().minusMonths(6))){
            throw ExcepcionFechaAdquisicionTarjeta("Adquirió la tarjeta hace menos de 6 meses")
        }
    }
    fun agregarItinerario(itinerario : Lugar){
        puntosIntermedios.add(itinerario)
    }
}
//Condiciones para el viaje:
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

//Observers al completar viaje
interface InformacionFinal{
    fun informar(cliente : Cliente, viaje : Viaje)
}

object registrarEnBilletera : InformacionFinal{
    override fun informar(cliente: Cliente,viaje: Viaje) {
        val valorViaje = viaje.costo(cliente)
        cliente.comprarViaje(valorViaje)
    }
}

class InformarDeuda(val mailSender : ServicioMail) : InformacionFinal{
    override fun informar(cliente : Cliente, viaje : Viaje) {
        mailSender.enviarMail(Mail(
            emisor = "agenciaviajes@gmail.com",
            receptor = cliente.nombre,
            titulo = "Aviso de deuda",
            contenido = "Se le informa al cliente ${cliente.nombre} que no posee saldo para poder realizar otro viaje. Su saldo es ${cliente.saldoDeudor()}"
        ))
    }
}

class InformarDistanciaTotal : InformacionFinal{
    override fun informar(cliente: Cliente, viaje: Viaje) {

        }


}

//Interfaz de Mail
data class Mail(var emisor : String, var receptor : String, var titulo: String, var contenido: String){}

interface ServicioMail{
    fun enviarMail(mail : Mail)
}

//Lugares para el itinerario
data class Lugar(val nombre : String, val direccion: Direccion){

}