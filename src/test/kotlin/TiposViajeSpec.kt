import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import java.time.LocalDate
import java.time.LocalDateTime
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify

class TiposViajeSpec : DescribeSpec({
    isolationMode = IsolationMode.InstancePerTest

    //Tarjetas
    val tarjeta1 = Tarjeta(saldo = 20.0, fechaAdquisicion = LocalDate.of(2011, 2, 21))
    val tarjeta2 = Tarjeta(saldo = 20.0, fechaAdquisicion = LocalDate.of(2011, 2, 21))
    val tarjeta3 = Tarjeta(saldo = 20.0, fechaAdquisicion = LocalDate.of(2011, 2, 21))
    val tarjeta4 = Tarjeta(saldo = 20.0, fechaAdquisicion = LocalDate.of(2011, 2, 21))

    //Clientes
    val clienteJoven = Cliente(tarjeta1, "Edmond", 15)
    val clienteAdulto = Cliente(tarjeta2, "Oberon", 40)
    val clienteMayor = Cliente(tarjeta3, "Don Quixote", 67)

    //Lugares
    val miserere = Lugar("Plaza Miserere", direccion = Direccion(x = 20.0f, y = 15.0f))
    val rosedal = Lugar("Rosedal", direccion = Direccion(x = 30.0f, y = 105.0f))
    val corrientes = Lugar("Corrientes", direccion = Direccion(x = 64.0f, y = 87.0f))
    val maipu = Lugar("Maipu", direccion = Direccion(x = 54.0f, y = 107.0f))
    val avSanMartin = Lugar("Avenida San Martin", direccion = Direccion(x = 180.0f, y = 98.0f))
    val carlosLopez = Lugar("Carlos lopez", direccion = Direccion(x = 280.0f, y = 198.0f))
    val joselsuarez = Lugar("Jose Leon Suarez", direccion = Direccion(x = 50.0f, y = 235.0f))
    val chilavert = Lugar("Chilavert", direccion = Direccion(x = 17.0f, y = 780.0f))

    //Tramos
    val tramoMiserereRosedal = Tramo(miserere, rosedal) //dist 90.55
    val corrientesYMaipu = Tramo(corrientes, maipu)     // dist 22.36
    val avSanMartinCarlosLopez = Tramo(avSanMartin, carlosLopez) // dist 141.42
    val joseLSuarezChilaver = Tramo(joselsuarez, chilavert) // dist 545.99

    //Viajes
    val viajeProgramado = ViajeProgramado(
        topePersonas = 3, fechaViaje = LocalDateTime.of(2016, 2, 29, 7, 0, 0),
        condiciones = condicionPorEdad, partida = miserere, destino = rosedal, topeDeuda = 105.0
    )
    viajeProgramado.listaPersonas = mutableListOf(clienteJoven, clienteMayor)

    val viajeProgramado2 = ViajeProgramado(
        topePersonas = 3, fechaViaje = LocalDateTime.of(2016, 2, 29, 7, 0, 0),
        condiciones = condicionPorEdad, partida = miserere, destino = rosedal, topeDeuda = 105.0
    )

    val itinerario1 = Itinerario(
        tramos = mutableListOf(tramoMiserereRosedal, corrientesYMaipu, avSanMartinCarlosLopez),
        topeDeuda = 100.0, fechaViaje = LocalDateTime.of(2016, 2, 10, 7, 0, 0),
        condiciones = VariasCondiciones(mutableListOf( condicionPorEdad,esHoraPico)), partida = miserere, destino = carlosLopez
    )

    val itinerario2 = Itinerario(
        tramos = mutableListOf(tramoMiserereRosedal, joseLSuarezChilaver, avSanMartinCarlosLopez),
        topeDeuda = 90.0, fechaViaje = LocalDateTime.of(2017, 3, 10, 7, 0, 0),
        condiciones = condicionPorEdad, partida = miserere, destino = carlosLopez
    )

    //Mail sender
    val mockedMailSender = mockk<ServicioMail>(relaxed = true)

    //Servicio calcular distancia
    val servicioCalcular = mockk<ServicioCalculaDistancia>(relaxed = true)
    beforeEach { every{servicioCalcular.calcularDistancia(any(),any(),any(),any(),any())} returns Pair(90,55) }
    val adapterServicioCalcularDistancia = ServicioDistanciaAdapter(servicioCalcular)

    //Clases observers
    val servicioInformarDeuda = InformarDeuda(mailSender = mockedMailSender)
    val servicioInformarDistanciaTotal = InformarDistanciaTotal(adapterServicioCalcularDistancia)

    //Asignamos observers
    viajeProgramado.listaObservers =
        mutableListOf(registrarEnBilletera, servicioInformarDeuda, servicioInformarDistanciaTotal)
    viajeProgramado2.listaObservers =
        mutableListOf(registrarEnBilletera, servicioInformarDeuda, servicioInformarDistanciaTotal)
    itinerario1.listaObservers =
        mutableListOf(registrarEnBilletera, servicioInformarDeuda, servicioInformarDistanciaTotal)
    itinerario2.listaObservers =
        mutableListOf(registrarEnBilletera, servicioInformarDeuda, servicioInformarDistanciaTotal)

    describe("Test de viaje Programado") {

        describe("Validaciones de horario de llegada de persona") {
            it("Una persona no puede pasar al viaje mas de 2 horas antes") {
                viajeProgramado.fechaViaje = LocalDateTime.now().plusHours(3)
                shouldThrow<ExcepcionHoraIngreso> { viajeProgramado.ingresarPersonas(clienteAdulto) }
            }
            it("Una persona SI puede pasar al viaje 2 horas antes") {
                viajeProgramado.fechaViaje = LocalDateTime.now().plusHours(2)
                shouldNotThrow<ExcepcionHoraIngreso> { viajeProgramado.ingresarPersonas(clienteAdulto) }
            }
        }

        describe("Validacion de tope de gente para el viaje") {
            it("No se puede ingresar a mas gente cuando el tope de viaje se cumplio") {
                val clienteAdulto2 = Cliente(tarjeta4, "Don Quixote", 50)
                viajeProgramado.ingresarPersonas(clienteAdulto2) //llega al tope
                viajeProgramado.listaPersonas.size shouldBe 3
                shouldThrow<ExcepcionLimiteIngreso> { viajeProgramado.ingresarPersonas(clienteAdulto) }
            }
            it("Si el viaje tiene espacio acepta ingresar gente. No se lanza excepcion") {
                viajeProgramado.listaPersonas.size shouldBe 2
                shouldNotThrow<ExcepcionLimiteIngreso> { viajeProgramado.ingresarPersonas(clienteAdulto) }
            }
        }
        describe("Validacion de saldo deudor de una persona") {
            it("Una persona puede viajar mientras no supere el tope de saldo deudor") {
                viajeProgramado.topeDeuda = 100.0
                val tarjeta5 = Tarjeta(saldo = -90.0, fechaAdquisicion = LocalDate.of(2016, 2, 29))
                val clienteConDeudaBien = Cliente(tarjeta5, "Agnes", 25)
                viajeProgramado.ingresarPersonas(clienteConDeudaBien) // hace un viaje
                viajeProgramado.completarViaje()
                clienteConDeudaBien.saldoDeudor() shouldBe 95.0
                shouldNotThrow<ExcepcionLimiteDeuda> { viajeProgramado2.ingresarPersonas(clienteConDeudaBien) }
            }
            it("Una persona NO puede viajar si supera el tope de saldo deudor") {
                val tarjeta5 = Tarjeta(saldo = -105.0, fechaAdquisicion = LocalDate.of(2016, 2, 29))
                val clienteConDeuda = Cliente(tarjeta5, "Agnes", 25)
                viajeProgramado.ingresarPersonas(clienteConDeuda)
                viajeProgramado.completarViaje() // hace un viaje
                clienteConDeuda.saldoDeudor() shouldBe 110.0 //supera el maximo de saldoDeudor permitido por el viaje = 105.0
                shouldThrow<ExcepcionLimiteDeuda> { viajeProgramado2.ingresarPersonas(clienteConDeuda) }
            }
        }
    }

    describe("Tet de viaje Itinerario") {
        it("Tenemos la posibilidad de tener varios destinos en un itinerario") {
            itinerario1.tramos.size shouldBe 3
        }
        it("Para sumar personas tienen que tener saldo") {
            //El cliente tiene saldo de 20 y el itinerario tiene tope deuda de 100
            shouldNotThrow<ExcepcionLimiteDeuda> { itinerario1.ingresarPersonas(clienteAdulto) }
        }
        it("No se puede sumar personas tienen que no tienen saldo") {
            //El cliente tiene saldo de -101.0 y el itinerario tiene tope deuda de 100
            clienteAdulto.tarjeta.saldo = -101.0
            shouldThrow<ExcepcionLimiteDeuda> { itinerario1.ingresarPersonas(clienteAdulto) }
        }
        it("El cliente adquirio la tarjeta viajera hace mas de 6 meses asi que puede viajar") {
            clienteAdulto.tarjeta.fechaAdquisicion = LocalDate.now().minusMonths(7)
            shouldNotThrow<ExcepcionFechaAdquisicionTarjeta> { itinerario1.ingresarPersonas(clienteAdulto) }
        }
        it("El cliente adquirio la tarjeta hace menos de 6 meses. No puede viajar") {
            clienteAdulto.tarjeta.fechaAdquisicion = LocalDate.now().minusMonths(4)
            shouldThrow<ExcepcionFechaAdquisicionTarjeta> { itinerario1.ingresarPersonas(clienteAdulto) }
        }
    }

    describe("Test de completar un viaje Programado") {
        it("Al completar un viaje se registra el valor en la billetera del cliente") {
            val saldoInicial =
                clienteMayor.tarjeta.saldo

            viajeProgramado.completarViaje()

            clienteMayor.tarjeta.saldo shouldBe
                    saldoInicial - viajeProgramado.costo(clienteMayor)
        }

        it("Al completar un viaje, si el usuario queda por debajo de los mangos de deuda deba enviar mail") {

            clienteMayor.tarjeta.saldo = -101.0
            viajeProgramado.completarViaje()

            verify(exactly = 1) {
                mockedMailSender.enviarMail(
                    mail = Mail(
                        emisor = "agenciaviajes@gmail.com",
                        receptor = "Don Quixote@gmail.com",
                        titulo = "Aviso de deuda",
                        contenido = "Se le informa al cliente Don Quixote que no posee saldo para poder realizar otro viaje. Su saldo es ${clienteMayor.saldoDeudor()}"
                    )
                )
            }
        }

        it("Al completar un viaje se generan los totales de distancia por usuario x mes") {
                viajeProgramado.listaPersonas.clear()
            every {
                servicioCalcular.calcularDistancia(
                    any(),any(),any(),any(),any()
                )
            } returns Pair(90, 55) // Distancia = \sqrt{\left(30.0-20.0\right)^2+\left(105.0-15.0\right)^2} = 90.55
            viajeProgramado.ingresarPersonas(clienteMayor)
            viajeProgramado.completarViaje()

            val kmEsperados = (90 + 55 / 100.0) * 1.609344
            servicioInformarDistanciaTotal.distanciaClienteEnMes(
                clienteMayor,
                viajeProgramado.fechaViaje.monthValue
            ) shouldBe kmEsperados

        }
    }

    describe("Test de completar un Itinerario"){
        it("Al completar un itinerario se registra el valor en la billetera del cliente") {
            val saldoInicial =
                clienteMayor.tarjeta.saldo
            itinerario1.ingresarPersonas(clienteMayor)
            itinerario1.completarViaje()

            itinerario1.costo(clienteMayor) shouldBe 4.5 //porque tiene condicion por edad y es hora pico. Primero hace 5 * 0.5 + 2 = 4.5
        }

        it("Al completar un Itinerario, si el usuario queda por debajo de los mangos de deuda deba enviar mail") {
            clienteMayor.tarjeta.saldo = -98.0
            itinerario1.ingresarPersonas(clienteMayor)
            itinerario1.completarViaje()

            verify(exactly = 1) {
                mockedMailSender.enviarMail(
                    mail = Mail(
                        emisor = "agenciaviajes@gmail.com",
                        receptor = "Don Quixote@gmail.com",
                        titulo = "Aviso de deuda",
                        contenido = "Se le informa al cliente Don Quixote que no posee saldo para poder realizar otro viaje. Su saldo es ${clienteMayor.saldoDeudor()}"
                    )
                )
            }
        }
            it("Al completar un Itinerario se generan los totales de distancia por usuario x mes") {
                itinerario1.ingresarPersonas(clienteJoven)

                every {
                    servicioCalcular.calcularDistancia(
                        any(), any(), any(), any(), any()
                    )
                } returnsMany listOf(
                    Pair(90,55),
                    Pair(22,36),
                    Pair(141,42)
                )

                itinerario1.completarViaje()

                val kmEsperados = (90.55 + 22.36 + 141.42) * 1.609344

                servicioInformarDistanciaTotal.distanciaClienteEnMes(
                    clienteJoven,
                    itinerario1.fechaViaje.monthValue
                ) shouldBe kmEsperados

                //verificamos que se haya llamado para la cantidad de tramos (3)
                verify(exactly = 3) {
                    servicioCalcular.calcularDistancia(
                        any(), any(), any(), any(), any()
                    )
                }

            }

    }


})