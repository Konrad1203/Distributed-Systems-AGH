
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import java.net.InetSocketAddress

fun main() {
    println("Hello World!")
    val server = GrpcServer()
    server.start()
}

class GrpcServer {

    fun start() {
        println("Starting gRPC server...")
        val socket = InetSocketAddress("localhost", 50051)

        val server = ServerBuilder
            .forPort(socket.port)
            .addService(ApplicationImpl())
            .addService(ProtoReflectionService.newInstance())
            .build()

        println("Server started on port ${socket.port}")

        Runtime.getRuntime().addShutdownHook(Thread {
            System.err.println("Shutting down gRPC server...")
            server.shutdown()
            server.awaitTermination()
            System.err.println("Server shut down.")
        })

        server.start()
        server.awaitTermination()
    }
}