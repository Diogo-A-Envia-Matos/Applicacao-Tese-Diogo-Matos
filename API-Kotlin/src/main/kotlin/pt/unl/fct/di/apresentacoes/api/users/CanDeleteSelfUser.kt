package pt.unl.fct.di.apresentacoes.api.users

import org.intellij.lang.annotations.Language
import org.springframework.security.access.prepost.PreAuthorize
import java.lang.annotation.Inherited

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
@PreAuthorize(CanDeleteSelfUser.condition)
annotation class CanDeleteSelfUser{
    companion object{
        @Language("SpEL")
        const val condition: String =
            // "@SecurityService.isPrincipal(authentication.principal,#{deleteSelfDTO.uid})" +
            "@SecurityService.isPrincipal(authentication.principal,#uid)" +
                    "and" +
                    "@SecurityService.isAdmin(authentication.principal)"
                    // +
                    // "or" +
                    // "@SecurityService.canDeleteUser(authentication.principal,#uid)"
    }
}
