package homework

import com.rabbitmq.client.*
import homework.EquipmentItems.ADMIN_EXCHANGE
import homework.EquipmentItems.CONF_EXCHANGE


private class EquipmentSupplier(private val name: String, availability: List<String>) {

    private val queueName = "supplier_${name}_queue"

    private var orderId = 1

    private val availableEquipment: Set<String> = availability.mapIndexedNotNull { index, value ->
        if (value == "1") EquipmentItems.items.elementAtOrNull(index) else null
    }.toSet()

    fun bindQueues(channel: Channel) {
        val orderExchangeName = EquipmentItems.ORD_EXCHANGE
        channel.exchangeDeclare(orderExchangeName, BuiltinExchangeType.DIRECT)

        for (item in EquipmentItems.items) {
            val itemQueueName = "${item}_queue"
            channel.queueDeclare(itemQueueName, true, false, false, null)
            channel.queueBind(itemQueueName, orderExchangeName, "equipment.$item")
        }
        channel.queueDeclare(queueName, true, false, false, null)
        channel.queueBind(queueName, ADMIN_EXCHANGE, "admin.suppliers")
        channel.queueBind(queueName, ADMIN_EXCHANGE, "admin.all")
    }

    fun getOrderConsumer(channel: Channel): Consumer {
        return object : DefaultConsumer(channel) {
            override fun handleDelivery(consumerTag: String?, envelope: Envelope?,
                                        properties: AMQP.BasicProperties?, body: ByteArray) {
                val message = String(body, charset("UTF-8"))
                if (message.startsWith("ADMIN:")) {
                    println("Otrzymano: $message")
                    return
                }
                val parts = message.split("|")
                    val (team, item) = parts
                val currOrderId = orderId++
                println("Order: $team-$name$currOrderId $item")
                val confMessage = "$team-$name-$currOrderId $item"
                    .toByteArray(charset("UTF-8"))
                channel.basicPublish(CONF_EXCHANGE, "team.$team", null, confMessage)
                channel.basicPublish(ADMIN_EXCHANGE, "copy.confirmation", null, confMessage)
            }
        }
    }

    fun run() {
        println("Oferowany sprzęt: [${availableEquipment.joinToString(", ")}]")
        val factory = ConnectionFactory()
        factory.host = "localhost"
        val connection = factory.newConnection()
        connection.use {
            val channel = connection.createChannel()
            channel.use {
                bindQueues(channel)
                val consumer = getOrderConsumer(channel)
                channel.basicConsume(queueName, true, consumer)
                val queues = availableEquipment.map { "${it}_queue" }
                println("Czekamy na zamówienia...")
                queues.forEach { queueName ->
                    channel.basicConsume(queueName, true, consumer) }

                Thread.currentThread().join()
            }
        }
    }
}

fun main() {
    print("Podaj nazwę dostawcy: ")
    val supplierName = readLine()?.trim()?.titlecase() ?: ""
    if (supplierName.isEmpty()) {
        println("Nazwa dostawcy nie może być pusta.")
        return
    }
    println("Dostawca '$supplierName'")
    println("Podaj dostępny sprzęt (0 lub 1):\n${EquipmentItems.items}")
    val availabilityList = readLine()!!.trim().split(" ")
    if (availabilityList.size != EquipmentItems.items.size) {
        println("Nieprawidłowa liczba dostępnych sprzętów.")
        return
    }
    val mySupplier = EquipmentSupplier(supplierName, availabilityList)
    mySupplier.run()
}