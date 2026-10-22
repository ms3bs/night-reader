package library

import dto.AddToLibraryRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import repository.LibraryRepository
import java.util.UUID

private fun JWTPrincipal.userId(): UUID =
    UUID.fromString(payload.getClaim("userId").asString())

fun Application.libraryModule(libraryRepo: LibraryRepository) {
    routing {
        authenticate("auth-jwt") {
            route("/api/library") {
                get {
                    val userId = call.principal<JWTPrincipal>()!!.userId()
                    call.respond(libraryRepo.getUserLibrary(userId))
                }
                post {
                    val userId = call.principal<JWTPrincipal>()!!.userId()
                    val req = call.receive<AddToLibraryRequest>()
                    val entry = libraryRepo.add(userId, UUID.fromString(req.bookId))
                    call.respond(HttpStatusCode.Created, entry)
                }
                delete("/{bookId}") {
                    val userId = call.principal<JWTPrincipal>()!!.userId()
                    val bookId = UUID.fromString(call.parameters["bookId"])
                    libraryRepo.remove(userId, bookId)
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }
}