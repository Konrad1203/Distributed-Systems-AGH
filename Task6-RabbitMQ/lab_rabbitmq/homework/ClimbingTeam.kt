package homework

import com.rabbitmq.client.*
import homework.EquipmentItems.ADMIN_EXCHANGE
import homework.EquipmentItems.CONF_EXCHANGE
import homework.EquipmentItems.ORD_EXCHANGE


fun String.titlecase(): String {
    return this.lowercase().replaceFirstChar { it.uppercaseChar() }
}

private class ClimbingTeam(private val name: String) {

    private val queueName = "team_${name}_queue"

    fun run() {
        val factory = ConnectionFactory()
        factory.host = "localhost"
        val connection = factory.newConnection()
        connection.use {
            val channel = connection.createChannel()
            channel.use {
                bindQueues(channel)
                receiveMessages(channel)
                startOrdering(channel)
            }
        }
    }

    private fun bindQueues(channel: Channel) {
        channel.exchangeDeclare(CONF_EXCHANGE, BuiltinExchangeType.DIRECT)
        //      queueDeclare(QUEUE_NAME, durable, exclusive, autoDelete, arguments)
        channel.queueDeclare(queueName, true, false, false, null)
        channel.queueBind(queueName, CONF_EXCHANGE, "team.$name")
        channel.queueBind(queueName, ADMIN_EXCHANGE, "admin.teams")
        channel.queueBind(queueName, ADMIN_EXCHANGE, "admin.all")
    }

    private fun receiveMessages(channel: Channel) {
        val consumer = object : DefaultConsumer(channel) {
            override fun handleDelivery(consumerTag: String?, envelope: Envelope?,
                                        properties: AMQP.BasicProperties?, body: ByteArray) {
                val message = String(body, charset("UTF-8"))
                println("Otrzymana wiadomość: $message")
            }
        }
        channel.basicConsume(queueName, true, consumer)
    }

    private fun startOrdering(channel: Channel) {
        var item: String
        println("Dostępny sprzęt: [${EquipmentItems.items.joinToString(", ")}]")
        do {
            print("Podaj zlecenie: ")
            item = readLine()?.trim()?.lowercase() ?: ""
            if (item.isEmpty() || !EquipmentItems.items.contains(item)) {
                println("Ten sprzęt nie jest dostępny lub zlecenie jest puste.")
                continue
            }
            println("Wysyłam zlecenie na: '$item'")
            val key = "equipment.$item"
            val message = "$name|$item".toByteArray(charset("UTF-8"))
            channel.basicPublish(ORD_EXCHANGE, key, null, message)
            channel.basicPublish(ADMIN_EXCHANGE, "copy.orders", null , message)

        } while (item != "koniec")
    }
}


fun main() {
    print("Podaj nazwę drużyny: ")
    val teamName = readLine()?.trim()?.titlecase() ?: ""
    if (teamName.isEmpty()) {
        println("Nazwa drużyny nie może być pusta.")
        return
    }
    println("Drużyna wspinaczkowa '$teamName'")
    val team = ClimbingTeam(teamName)
    team.run()
}