package pt.unl.fct.di.apresentacoes.services.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import pt.unl.fct.di.apresentacoes.security.JwtProperties
import pt.unl.fct.di.apresentacoes.services.UserService
import java.util.*

@Service
class TokenService(
    jwtProperties: JwtProperties,
    val users: UserService
) {

    private val secretKey = Keys.hmacShaKeyFor(
        jwtProperties.key.toByteArray()
    )

    fun generate(
        userDetails: UserDetails,
        expriationDate: Date,
        additionalClaims: Map<String,Any> = emptyMap()
        ): String =
            Jwts.builder()
                .claims()
                .subject(users.getOneByEmail(userDetails.username).id.toString())
                .issuedAt(Date(System.currentTimeMillis()))
                .expiration(expriationDate)
                .add(additionalClaims)
                .and()
                .signWith(secretKey)
                .compact()

    fun extractID(token:String):String? =
        getAllClaims(token).subject

    fun isExpired(token:String): Boolean =
        getAllClaims(token)
            .expiration
            .before(Date(System.currentTimeMillis()))

    fun isValid(token:String, userDetails: UserDetails): Boolean{
        val id = extractID(token)

        return users.getOneByEmail(userDetails.username).id.toString() == id && !isExpired(token)
    }

    private fun getAllClaims(token: String): Claims {
        val parser = Jwts.parser()
            .verifyWith(secretKey)
            .build()

        return parser.parseSignedClaims(token)
            .payload
    }

    fun extractEmail(token:String): String? = 
        this.extractID(token)?.let { users.getOneById(it.toLong())?.email }

    fun doesNotContainBearerToken(header: String): Boolean =
        header == null || !header.startsWith("Bearer ")

    fun extractTokenValue(header: String): String =
        header.substringAfter("Bearer ")

}