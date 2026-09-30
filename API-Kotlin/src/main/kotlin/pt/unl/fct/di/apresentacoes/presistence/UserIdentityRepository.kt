package pt.unl.fct.di.apresentacoes.presistence

import org.springframework.data.repository.CrudRepository
import pt.unl.fct.di.apresentacoes.domain.UserIdentityDAO
import java.util.*

interface UserIdentityRepository : CrudRepository<UserIdentityDAO, Long> {

    fun findByUserId(userID: Long): Iterable<UserIdentityDAO>
    fun findByUserIdAndProvider(userID: Long, provider: String): Iterable<UserIdentityDAO>
    fun findByUserIdAndProviderAndProviderId(userID: Long, provider: String, providerId: String): Optional<UserIdentityDAO>
}