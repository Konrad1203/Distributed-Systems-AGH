import application.AgeCategories
import application.ApplicationGrpc.ApplicationImplBase
import application.LettersFrequencyDictionary
import application.Name
import application.PersonAge
import application.PersonAgeList
import application.PersonList
import application.TextChunk
import io.grpc.stub.StreamObserver
import java.lang.Thread.sleep
import java.time.LocalDate
import kotlin.collections.set

class ApplicationImpl: ApplicationImplBase() {

    override fun avg(
        request: application.RepeatedInt32,
        responseObserver: StreamObserver<application.SingleDouble>
    ) {
        val numbers: List<Int> = request.argsList
        println("Received AVG_METHOD request with numbers: $numbers")
        val count = numbers.size
        val average = if (count > 0) numbers.sum().toDouble() / count else 0.0
        println("Calculated average: $average")

        val response = application.SingleDouble.newBuilder()
            .setRes(average)
            .build()
        responseObserver.onNext(response)
        responseObserver.onCompleted()
        println()
    }

    override fun countLetters(responseObserver: StreamObserver<LettersFrequencyDictionary>): StreamObserver<TextChunk> {
        val frequencyMap = mutableMapOf<String, Int>()
        println("Received COUNT_LETTERS_METHOD request")

        return object: StreamObserver<TextChunk> {

            override fun onNext(textChunk: TextChunk) {
                textChunk.text.forEach { char ->
                    if (char.isLetter()) {
                        val key = char.toString().lowercase()
                        frequencyMap[key] = frequencyMap.getOrDefault(key, 0) + 1
                    }
                }
            }

            override fun onError(t: Throwable) {
                println("Error in countLetters: ${t.message}")
            }

            override fun onCompleted() {
                println("Completed processing text chunks.")
                val response = LettersFrequencyDictionary.newBuilder()
                    .putAllFrequencies(frequencyMap)
                    .build()
                responseObserver.onNext(response)
                responseObserver.onCompleted()
                println(frequencyMap)
                println()
            }
        }
    }

    override fun getInfo(request: Name, responseObserver: StreamObserver<TextChunk>) {
        println("Received GET_INFO request with name: ${request.name}")
        responseObserver.onNext(TextChunk.newBuilder().setText("Hi, ${request.name}!\n").build())
        val lines = this::class.java.getResourceAsStream("about_grpc.txt")?.bufferedReader()?.lineSequence()
        if (lines == null) {
            println("No text found in about_grpc.txt")
            responseObserver.onError(Throwable("File not found or empty"))
            return
        }
        var spaces = 0
        for (line in lines) {
            //println("Sending line: $line")
            if (line.isEmpty()) spaces += 1
            else {
                if (spaces > 0)
                    responseObserver.onNext(TextChunk.newBuilder().setText("\n".repeat(spaces)).build())
                for (word in line.split(" ")) {
                    responseObserver.onNext(TextChunk.newBuilder().setText("$word ").build())
                    sleep(50)
                }
                responseObserver.onNext(TextChunk.newBuilder().setText("\n").build())
                spaces = 0
            }
        }
        responseObserver.onCompleted()
        println("Completed sending lines from about_grpc.txt\n")
    }

    override fun groupByAgeGroup(request: PersonList, responseObserver: StreamObserver<AgeCategories>) {
        println("Received GROUP_BY_AGE_GROUPS_INFO request")
        val ageMap = mutableMapOf<String, MutableList<Pair<String, Int>>>()
        val ageGroups = listOf(
            0..12 to "0-12",
            13..17 to "13-17",
            18..23 to "18-23",
            24..39 to "24-39",
            40..59 to "40-59",
            60..300 to "60+"
        )
        val today: LocalDate = LocalDate.now()
        for (person in request.peopleList) {
            val birthDate = person.birthDate.let { LocalDate.of(it.year, it.month, it.day) }
            val age = today.year - birthDate.year - if (today.dayOfYear < birthDate.dayOfYear) 1 else 0
            val group = ageGroups.find { it.first.contains(age) }?.second
            group?.let {
                ageMap[it]?.add(person.name to age)
                    ?: ageMap.put(it, mutableListOf(person.name to age))
            }
        }
        val responseMap = mutableMapOf<String, PersonAgeList>()
        for ((group, peopleList) in ageMap) {
            val people = PersonAgeList.newBuilder()
            for (person in peopleList) {
                people.addPeople(PersonAge.newBuilder().setName(person.first).setAge(person.second).build())
            }
            responseMap[group] = people.build()
        }
        val response = AgeCategories.newBuilder()
            .putAllCategories(responseMap)
            .build()
        responseObserver.onNext(response)
        responseObserver.onCompleted()
        println(ageMap)
        println()
    }
}