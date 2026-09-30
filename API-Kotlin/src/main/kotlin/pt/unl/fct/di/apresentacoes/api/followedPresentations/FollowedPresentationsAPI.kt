package pt.unl.fct.di.apresentacoes.api.followedPresentations

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import pt.unl.fct.di.apresentacoes.api.dto.FollowedPresentationsDTO

@RequestMapping("/api/followedPresentations")
interface FollowedPresentationsAPI {

    @PostMapping("")
    @CanCreateFollowedPresentations
    fun addFollowedPresentations(@RequestBody presentations: FollowedPresentationsDTO): FollowedPresentationsDTO

    @GetMapping("")
    @CanReadAllFollowedPresentations
    fun getAllFollowedPresentations(): Collection<FollowedPresentationsDTO>

    @GetMapping("/user/{uid}")
    @CanReadUserFollowedPresentations
    fun getUserFollowedPresentations(@PathVariable uid:Long): Collection<FollowedPresentationsDTO>

    @DeleteMapping("/{fpid}")
    @CanDeleteFollowedPresentations
    fun deleteFollowedPresentations(@PathVariable fpid: Long)
}