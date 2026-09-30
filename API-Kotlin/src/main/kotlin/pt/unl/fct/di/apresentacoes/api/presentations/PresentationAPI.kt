package pt.unl.fct.di.apresentacoes.api.presentations

import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import pt.unl.fct.di.apresentacoes.api.dto.AddOptionalDTO
import pt.unl.fct.di.apresentacoes.api.dto.PresentationDTO

@RequestMapping("/api/presentation")
interface PresentationAPI {

    @PostMapping("/addInitial")
    fun addInitialPresentations()

    @PostMapping("")
    @CanCreatePresentation
    fun add(@RequestBody presentationDTO: PresentationDTO): PresentationDTO

    @PostMapping("/upload")
    @CanCreatePresentation
    fun uploadData(@RequestParam() excelFile: MultipartFile)

    @GetMapping("/{pid}")
    fun getPresentation(@PathVariable pid: Long): PresentationDTO

    @GetMapping("")
    fun getAllPresentations(): Collection<PresentationDTO>

    @DeleteMapping("/{pid}")
    @CanDeletePresentation
    fun deletePresentation(@PathVariable pid: Long)

    @GetMapping("/user/{uid}")
    // @CanReadUserPresentations
    fun getUserPresentations(@PathVariable uid: Long): Collection<PresentationDTO>

    @GetMapping("/adviserAndArguer/{uid}")
    // @CanReadUserPresentations
    fun getUserPresentationsAsAdviserAndAsArguer(@PathVariable uid: Long): Collection<PresentationDTO>

    @GetMapping("/adviser/{uid}")
    // @CanReadUserPresentations
    fun getUserPresentationsAsAdviser(@PathVariable uid: Long): Collection<PresentationDTO>

    @GetMapping("/arguer/{uid}")
    // @CanReadUserPresentations
    fun getUserPresentationsAsArguer(@PathVariable uid: Long): Collection<PresentationDTO>

    @GetMapping("/optional/{uid}")
    // @CanReadUserPresentations
    fun getUserPresentationsAsOptional(@PathVariable uid: Long): Collection<PresentationDTO>

    @PostMapping("/add/optional1/{pid}")
    @CanAddAndDeleteOptional1
    fun addOptional1(@PathVariable pid: Long, @RequestBody addOptionalDTO: AddOptionalDTO): PresentationDTO

    @PostMapping("/add/optional2/{pid}")
    @CanAddAndDeleteOptional2
    fun addOptional2(@PathVariable pid: Long, @RequestBody addOptionalDTO: AddOptionalDTO): PresentationDTO

    @DeleteMapping("/delete/optional1/{pid}")
    @CanAddAndDeleteOptional1
    fun deleteOptional1(@PathVariable pid: Long): PresentationDTO

    @DeleteMapping("/delete/optional2/{pid}")
    @CanAddAndDeleteOptional2
    fun deleteOptional2(@PathVariable pid: Long): PresentationDTO

}