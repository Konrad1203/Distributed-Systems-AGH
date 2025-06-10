package homework

import com.rabbitmq.client.*
import homework.EquipmentItems.ADMIN_EXCHANGE


private class Admin {

    fun receiveAllMessages(channel: Channel) {
        channel.exchangeDeclare(ADMIN_EXCHANGE, BuiltinExchangeType.TOPIC)
        channel.queueDeclare("admin_queue", true, false, false, null)
        channel.queueBind("admin_queue", ADMIN_EXCHANGE, "#")

        val consumer: Consumer = object : DefaultConsumer(channel) {
            override fun handleDelivery(consumerTag: String?, envelope: Envelope?,
                                        properties: AMQP.BasicProperties?, body: ByteArray) {
                val message = String(body, charset("UTF-8"))
                val routingKey = envelope?.routingKey ?: "...?..."
                println("[LOG] $routingKey | $message")
            }
        }
        channel.basicConsume("admin_queue", true, consumer)
    }

    fun sendAdminMessage(channel: Channel, target: String, message: String) {
        val routingKey = when (target.lowercase()) {
            "teams" -> "admin.teams"
            "suppliers" -> "admin.suppliers"
            "all" -> "admin.all"
            else -> {
                println("Nieprawidłowy cel: $target")
                return
            }
        }
        val sendMessage = "ADMIN: $message".toByteArray(charset("UTF-8"))
        channel.basicPublish(ADMIN_EXCHANGE, routingKey, null, sendMessage)
        println("Wysłano komunikat do: $target -> '$message'")
    }

    fun run() {
        val factory = ConnectionFactory()
        factory.host = "localhost"
        val connection = factory.newConnection()
        connection.use {
            val channel = connection.createChannel()
            channel.use {
                receiveAllMessages(channel)

                println("Wyślij wiadomość: <teams / suppliers / all> <wiadomość>")
                while (true) {
                    val input = readLine()!!.trim().split(" ")
                    val target = input[0]
                    val message = input.drop(1).joinToString(" ")
                    sendAdminMessage(channel, target, message)
                }
            }
        }
    }
}

fun main() {
    println("Administrator systemu")
    val admin = Admin()
    admin.run()
}