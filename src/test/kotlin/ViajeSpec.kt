import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

class ViajeSpec : DescribeSpec({
    isolationMode = IsolationMode.InstancePerTest
    //Assets
    val tarjeta1 = Tarjeta(saldo = 20.0)
    val clienteJoven = Cliente(tarjeta1, 15)
    val viaje1 = Viaje(fechaViaje = LocalDateTime.of(2016, 2, 29, 7, 0, 0),
       condiciones = condicionPorEdad)

    describe("Un Cliente de 15 años viaja"){
        it("El cliente joven viaja con condicion por edad"){
            viaje1.costo(clienteJoven) shouldBe 8.5
        }
        it("El cliente joven se le agrega la condicion de hora pico, se quita la otra"){
            viaje1.reasignarCondicion(esHoraPico)
            viaje1.costo(clienteJoven) shouldBe 12.0
        }
        it("El cliente tendra una condicion combinada. Primero por edad luego hora pico"){
            val listaEdadPico = mutableListOf<CondicionesViaje>(condicionPorEdad,esHoraPico)
            val edadYHora = VariasCondiciones(listaEdadPico)
            viaje1.reasignarCondicion(edadYHora)
            viaje1.costo(clienteJoven) shouldBe 10.5
        }
        it("El cliente tendra una condicion combinada. Primero por hora pico luego por edad"){
            val listaPicoEdad = mutableListOf<CondicionesViaje>(esHoraPico, condicionPorEdad)
            val HoraEdad = VariasCondiciones(listaPicoEdad)
            viaje1.reasignarCondicion(HoraEdad)
            viaje1.costo(clienteJoven) shouldBe (9.8 plusOrMinus 0.8888888888888888 )
        }
    }
})