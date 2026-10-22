package books

import dto.NewBook
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import repository.BookRepository
import java.util.UUID

fun Application.booksModule(bookRepo: BookRepository) {
    routing {
        route("/api/books") {
            get {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20
                call.respond(bookRepo.findAll(page, size))
            }
            get("/search") {
                val q = call.request.queryParameters["q"] ?: ""
                call.respond(bookRepo.search(q))
            }
            get("/{id}") {
                val id = UUID.fromString(call.parameters["id"])
                val book = bookRepo.findById(id)
                if (book != null) call.respond(book)
                else call.respond(HttpStatusCode.NotFound)
            }
            authenticate("auth-jwt") {
                post {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = UUID.fromString(principal.payload.getClaim("userId").asString())
                    val req = call.receive<NewBook>()
                    val book = bookRepo.create(req.copy(uploadedBy = userId))
                    call.respond(HttpStatusCode.Created, book)
                }
            }
        }
    }
}