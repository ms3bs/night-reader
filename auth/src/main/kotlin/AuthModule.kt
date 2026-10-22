package auth

import dto.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.mindrot.jbcrypt.BCrypt
import repository.UserRepository
import java.util.Date

fun Application.authModule(userRepo: UserRepository) {
    val secret = System.getenv("JWT_SECRET") ?: "dev-secret"
    val issuer = "web-reader"

    install(Authentication) {
        jwt("auth-jwt") {
            realm = issuer
            verifier(
                JWT.require(Algorithm.HMAC256(secret)).withIssuer(issuer).build()
            )
            validate { cred ->
                if (cred.payload.getClaim("userId").asString() != null) JWTPrincipal(cred.payload) else null
            }
        }
    }

    routing {
        post("/api/auth/register") {
            val req = call.receive<RegisterRequest>()
            val user = userRepo.create(req.email, req.password, req.displayName)
            call.respond(HttpStatusCode.Created, user)
        }
        post("/api/auth/login") {
            val req = call.receive<LoginRequest>()
            val found = userRepo.findByEmail(req.email)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            if (!BCrypt.checkpw(req.password, found.second)) {
                return@post call.respond(HttpStatusCode.Unauthorized)
            }
            val token = JWT.create()
                .withIssuer(issuer)
                .withClaim("userId", found.first.id.toString())
                .withExpiresAt(Date(System.currentTimeMillis() + 86_400_000))
                .sign(Algorithm.HMAC256(secret))
            call.respond(LoginResponse(token))
        }
    }
}