package com.example

import com.google.inject.Guice
import com.google.inject.Injector
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.locations.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*


fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(Locations)
    install(ContentNegotiation) {
        jackson()
    }
    configureRouting()
}

private val defaultInjector: Injector = Guice.createInjector()
private val getStudentsUseCase: GetStudentsUsecase = defaultInjector.getInstance(GetStudentsUsecase::class.java)

fun Application.configureRouting() {
    routing {
        get("/") {
            val response = getStudentsUseCase.execute()
            println(response)
            call.respond(HttpStatusCode.OK, response)
        }
    }
}

// usecase
interface IContractsUseCase {
    fun execute(): List<StudentResponse>
}

data class StudentResponse(
    val id: UUID,
    val name: String
)

class GetStudentsUsecase: IContractsUseCase {
    private val defaultInjector: Injector = Guice.createInjector()
    private val studentRepository: IStudentRepository = defaultInjector.getInstance(StudentRepository ::class.java)

    override fun execute(): List<StudentResponse> {
        val students = studentRepository.findMany()
        return listOf( StudentResponse(id = students.first() .id, name = "foo"))
    }
}

// repository
interface IStudentRepository {
    fun findMany(): List<StudentEntity>
}

class StudentRepository : IStudentRepository {
    override fun findMany(): List<StudentEntity> {
        val sql = """
            select id, name from student
        """.trimIndent()
        return listOf(StudentEntity(id = UUID.randomUUID(), name = "foo"))
    }
}

// domain
data class StudentEntity(
    val id: UUID,
    val name: String,
)
