package pt.unl.fct.di.apresentacoes.api.followedPresentations

import org.intellij.lang.annotations.Language
import org.springframework.security.access.prepost.PreAuthorize
import java.lang.annotation.Inherited

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
@PreAuthorize(CanCreateFollowedPresentations.condition)
annotation class CanCreateFollowedPresentations{
    companion object{
        @Language("SpEL")
        const val condition: String =
            "@SecurityService.isAdminOrOrganizer(authentication.principal)" +
                    "or" +
                    "@SecurityService.isAdviserOrArguerOfAllPresentations(authentication.principal, #presentations)"
    }
}
