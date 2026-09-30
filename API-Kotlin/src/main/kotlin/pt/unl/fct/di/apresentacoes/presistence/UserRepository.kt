package pt.unl.fct.di.apresentacoes.presistence

import org.springframework.data.repository.CrudRepository
import pt.unl.fct.di.apresentacoes.domain.UserDAO
import java.util.*

interface UserRepository : CrudRepository<UserDAO, Long> {

    fun existsByEmail(email: String): Boolean
    fun existsByName(name: String): Boolean
    fun findOneByEmail(email: String): Optional<UserDAO>
    fun findByName(name:String): Optional<UserDAO>
}