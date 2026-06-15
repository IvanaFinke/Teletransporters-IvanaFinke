data class Mail(var emisor : String, var receptor : String, var titulo: String, var contenido: String){}

interface ServicioMail{
    fun enviarMail(mail : Mail)
}
