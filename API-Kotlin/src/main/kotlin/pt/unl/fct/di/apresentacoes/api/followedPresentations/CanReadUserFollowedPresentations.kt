package pt.unl.fct.di.apresentacoes.api.followedPresentations

import org.intellij.lang.annotations.Language
import org.springframework.security.access.prepost.PreAuthorize
import java.lang.annotation.Inherited

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
@PreAuthorize(CanReadUserFollowedPresentations.condition)
annotation class CanReadUserFollowedPresentations{
    companion object{
        @Language("SpEL")
        const val condition: String =
            "@SecurityService.isAdminOrOrganizer(authentication.principal)" +
                    "or" +
                    "@SecurityService.isPrincipal(authentication.principal,#uid)"
    }
}
