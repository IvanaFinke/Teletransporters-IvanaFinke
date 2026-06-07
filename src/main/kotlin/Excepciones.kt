class ExcepcionHoraIngreso(mensaje : String) : RuntimeException(mensaje){}
class ExcepcionLimiteIngreso(mensaje : String) : RuntimeException(mensaje){}
class ExcepcionLimiteDeuda(mensaje : String) : RuntimeException(mensaje){}
class ExcepcionFechaAdquisicionTarjeta(mensaje : String) : RuntimeException(mensaje){}