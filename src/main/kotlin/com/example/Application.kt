package com.example

import com.google.inject.Guice
import com.google.inject.ImplementedBy
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
private val getStudentsUseCase: GetStudentsUseCase = defaultInjector.getInstance(GetStudentsUseCase::class.java)

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
@ImplementedBy(GetStudentsInteractor::class)
interface GetStudentsUseCase {
    fun execute(): List<StudentResponse>
}

data class StudentResponse(
    val id: UUID,
    val name: String
)

class GetStudentsInteractor: GetStudentsUseCase {
    private val defaultInjector: Injector = Guice.createInjector()
    private val studentRepository: IStudentRepository = defaultInjector.getInstance(IStudentRepository ::class.java)

    override fun execute(): List<StudentResponse> {
        val students = studentRepository.findMany()
        return listOf(StudentResponse(id = students.first().id, name = students.first().name))
    }
}

// repository
@ImplementedBy(StudentRepository::class)
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

class MockStudentRepository : IStudentRepository {
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
