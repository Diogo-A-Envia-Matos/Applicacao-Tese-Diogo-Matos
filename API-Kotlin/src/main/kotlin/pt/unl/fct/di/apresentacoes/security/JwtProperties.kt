package pt.unl.fct.di.apresentacoes.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("jwt")
class JwtProperties (
    val key: String,
    val accessTokenExpiration: Long,
    //val refreshTokenExpiration: Long,
){
}