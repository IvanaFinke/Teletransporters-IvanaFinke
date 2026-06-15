import java.time.LocalDate

class Cliente(val tarjeta: Tarjeta, val nombre : String, val edad : Int, var billetera : Double) {
    var viajes : MutableList<Viaje> = mutableListOf()
    fun cargarTarjeta(monto : Double){
        tarjeta.recibirRecarga(monto)
    }
    fun saldoDeudor() = if(tarjeta.saldo < 0) -tarjeta.saldo else 0.0
    fun fechaAdquTarjeta() = tarjeta.fechaAdquisicion
    fun comprarViaje(monto : Double){ billetera-= monto }
    fun agregarViaje(viaje : Viaje) {
        viajes.add(viaje)
    }
}

class Tarjeta(var saldo : Double = 0.0, val fechaAdquisicion : LocalDate){
    fun recibirRecarga(monto : Double) {
        saldo += monto
    }
}