import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.LocalDateTime

class CondicionesViajeSpec : DescribeSpec({
    isolationMode = IsolationMode.InstancePerTest
    //Tarjetas
    val tarjeta1 = Tarjeta(saldo = 20.0, fechaAdquisicion = LocalDate.of(2011,2,21))

    //Clientes
    val clienteJoven = Cliente(tarjeta1, "Edmond",15)
    val clienteAdulto = Cliente(tarjeta1, "Oberon",40)
    val clienteMayor = Cliente(tarjeta1, "Don Quixote",67)

    //Lugares
    val miserere = Lugar("Plaza Miserere",direccion= Direccion(x= 20.0f, y = 15.0f))
    val rosedal = Lugar("Rosedal",direccion= Direccion(x= 30.0f, y = 105.0f))

    //Viajes
    val viaje1 = ViajeProgramado(topePersonas = 3, fechaViaje = LocalDateTime.of(2016, 2, 29, 7, 0, 0),
       condiciones = condicionPorEdad, partida = miserere, destino = rosedal, topeDeuda = 105.0)
    viaje1.listaPersonas = mutableListOf(clienteJoven,clienteMayor)

    describe("Un Cliente de 15 años viaja"){
        it("El cliente joven viaja con condicion por edad"){
            viaje1.costo(clienteJoven) shouldBe 3.5
        }
        it("El cliente joven se le agrega la condicion de hora pico, se quita la otra"){
            viaje1.reasignarCondicion(esHoraPico)
            viaje1.costo(clienteJoven) shouldBe 7.0
        }
        it("El cliente tendra una condicion combinada. Primero por edad luego hora pico"){
            val listaEdadPico = mutableListOf<CondicionesViaje>(condicionPorEdad,esHoraPico)
            val edadYHora = VariasCondiciones(listaEdadPico)
            viaje1.reasignarCondicion(edadYHora)
            viaje1.costo(clienteJoven) shouldBe 5.5
        }
        it("El cliente tendra una condicion combinada. Primero por hora pico luego por edad"){
            val listaPicoEdad = mutableListOf<CondicionesViaje>(esHoraPico, condicionPorEdad)
            val HoraEdad = VariasCondiciones(listaPicoEdad)
            viaje1.reasignarCondicion(HoraEdad)
            viaje1.costo(clienteJoven) shouldBe (4.9 plusOrMinus 0.89999 )
        }

    }
})