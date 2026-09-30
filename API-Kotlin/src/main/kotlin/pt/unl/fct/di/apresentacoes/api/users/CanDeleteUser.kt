package pt.unl.fct.di.apresentacoes.api.users

import org.intellij.lang.annotations.Language
import org.springframework.security.access.prepost.PreAuthorize
import java.lang.annotation.Inherited

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
@PreAuthorize(CanDeleteUser.condition)
annotation class CanDeleteUser{
    companion object{
        @Language("SpEL")
        const val condition: String =
            "@SecurityService.isPrincipal(authentication.principal,#uid)" +
                    "or" +
                    "@SecurityService.isAdmin(authentication.principal)"
                    // +
                    // "or" +
                    // "@SecurityService.canDeleteUser(authentication.principal,#uid)"
    }
}
