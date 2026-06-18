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
//objeto global para usar en informar deuda
object deudaMaxima{
    var valor : Double = 100.0
}

class InformarDeuda(val mailSender : ServicioMail) : InformacionFinal{
    override fun informar(cliente : Cliente, viaje : Viaje) {
        if(cliente.saldoDeudor() > deudaMaxima.valor){ //chequeamos el valor del objeto global para deuda maxima
            mailSender.enviarMail(Mail(
                emisor = "agenciaviajes@gmail.com",
                receptor = cliente.mailCliente,
                titulo = "Aviso de deuda",
                contenido = "Se le informa al cliente ${cliente.nombre} que no posee saldo para poder realizar otro viaje. Su saldo es ${cliente.saldoDeudor()}"
            ))
        }

    }
}

class InformarDistanciaTotal(var servicioDistancia: ServicioDistanciaAdapter) : InformacionFinal{
    var kilometrosPorClienteYMes = mutableMapOf<ClaveDistancia,Double>() //lista que guarda al cliente y su distancia mensual

    override fun informar(cliente: Cliente, viaje: Viaje) {
        val clave = ClaveDistancia(cliente, viaje.fechaViaje.monthValue)
        val distancia = servicioDistancia.calcularDistanciaTotalTramos(viaje.tramos())
        //cada vez que completamos el viaje le sumamos la distancia del viaje actual a la lista de clientes por mes
        kilometrosPorClienteYMes[clave] = kilometrosPorClienteYMes.getOrDefault(clave,0.0) + distancia
    }
    fun distanciaClienteEnMes(cliente: Cliente,mes: Int) : Double =
        kilometrosPorClienteYMes.getOrDefault(ClaveDistancia(cliente,mes),0.0)
}

//clase para usar en el informe como clave
data class ClaveDistancia(val cliente :Cliente,val mes : Int)
