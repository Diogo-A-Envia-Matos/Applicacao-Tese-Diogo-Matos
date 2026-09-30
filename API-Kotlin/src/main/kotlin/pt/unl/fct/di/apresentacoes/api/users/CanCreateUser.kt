package pt.unl.fct.di.apresentacoes.api.users

import org.intellij.lang.annotations.Language
import org.springframework.security.access.prepost.PreAuthorize
import java.lang.annotation.Inherited

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
@PreAuthorize(CanCreateUser.condition)
annotation class CanCreateUser{
    companion object{
        @Language("SpEL")
        const val condition: String =
            "@SecurityService.isAdminOrOrganizer(authentication.principal)"
    }
}
