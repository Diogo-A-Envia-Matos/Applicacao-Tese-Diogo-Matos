package pt.unl.fct.di.apresentacoes.api.presentations

import org.springframework.web.bind.annotation.RestController

import org.springframework.web.multipart.MultipartFile
import pt.unl.fct.di.apresentacoes.api.dto.AddOptionalDTO
import pt.unl.fct.di.apresentacoes.api.dto.PresentationDTO
import pt.unl.fct.di.apresentacoes.api.exceptions.APIConflictException
import pt.unl.fct.di.apresentacoes.api.exceptions.APINotFoundException
import pt.unl.fct.di.apresentacoes.api.exceptions.APIUnprocessableEntityException
import pt.unl.fct.di.apresentacoes.api.exceptions.ComputationInProgress
import pt.unl.fct.di.apresentacoes.api.exceptions.ChangesNotAllowed
import pt.unl.fct.di.apresentacoes.api.exceptions.PresentationUnprocessable
import pt.unl.fct.di.apresentacoes.services.PresentationNotFound
import pt.unl.fct.di.apresentacoes.services.PresentationService
import pt.unl.fct.di.apresentacoes.services.ComputationService
import pt.unl.fct.di.apresentacoes.services.StopChangesService
// import pt.unl.fct.di.apresentacoes.services.PresentationUnprocessable
import pt.unl.fct.di.apresentacoes.services.ThereArePresentations
import pt.unl.fct.di.apresentacoes.services.StopChangesNotFound

private const val COMPUTATION_IN_PROGRESS = "There is a computation in progress."
private const val CHANGES_NOT_ALLOWED = "Currently, changes are not allowed to be performed."

@RestController
class PresentationController(val presentations: PresentationService,
                             val computation: ComputationService,
                             val stopChanges: StopChangesService
                             ) : PresentationAPI {

    override fun addInitialPresentations() {
        try{
            presentations.addInitialPresentations()
        } catch (e: ThereArePresentations){
            throw APIConflictException(e.message)
        }
    }

    override fun add(presentationDTO: PresentationDTO): PresentationDTO {
        try{
            privateCheckComputation()
            return PresentationDTO(presentations.add(presentationDTO))
        } catch (e: PresentationUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun uploadData(excelFile: MultipartFile) {
        // try{
        privateCheckComputation()
        presentations.uploadData(excelFile)
        // } catch (e:PresentationUnprocessable){
        //     throw APIUnprocessableEntityException(e.message)
        // }
    }

    override fun getPresentation(pid: Long): PresentationDTO {
        try{
            return PresentationDTO(presentations.getOnePresentation(pid))
        } catch (e:PresentationNotFound){
            throw APINotFoundException(e.message)
        }
    }

    override fun getAllPresentations(): Collection<PresentationDTO> =
        presentations.getAllPresentations().map{ e -> PresentationDTO(e) }

    override fun deletePresentation(pid: Long) {
        try{
            privateCheckComputation()
            presentations.deleteOnePresentation(pid)
        } catch (e:PresentationNotFound){
            throw APINotFoundException(e.message)
        }
    }

    override fun getUserPresentations(uid: Long): Collection<PresentationDTO> {
        return presentations.getUserPresentations(uid).map { e -> PresentationDTO(e) }
    }

    override fun getUserPresentationsAsAdviserAndAsArguer(uid: Long): Collection<PresentationDTO> {
        return presentations.getUserPresentationsAsAdviserAndAsArguer(uid).map { e -> PresentationDTO(e) }
    }

    override fun getUserPresentationsAsAdviser(uid: Long): Collection<PresentationDTO> {
        return presentations.getUserPresentationsAsAdviser(uid).map{ e -> PresentationDTO(e) }
    }

    override fun getUserPresentationsAsArguer(uid: Long): Collection<PresentationDTO> {
        return presentations.getUserPresentationsAsArguer(uid).map{ e -> PresentationDTO(e) }
    }

    override fun getUserPresentationsAsOptional(uid: Long): Collection<PresentationDTO> {
        return presentations.getUserPresentationsAsOptional(uid).map{ e -> PresentationDTO(e) }
    }

    override fun addOptional1(pid: Long, addOptionalDTO: AddOptionalDTO): PresentationDTO {
        try{
            privateCheckComputation()
            privateCheckStopChanges()
            return PresentationDTO(presentations.addOptional(pid,addOptionalDTO, true))
        } catch (e:PresentationNotFound){
            throw APINotFoundException(e.message)
        } catch (e:PresentationUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun addOptional2(pid: Long, addOptionalDTO: AddOptionalDTO): PresentationDTO {
        try{
            privateCheckComputation()
            return PresentationDTO(presentations.addOptional(pid,addOptionalDTO, false))
        } catch (e:PresentationNotFound){
            throw APINotFoundException(e.message)
        } catch (e:PresentationUnprocessable){
            throw APIUnprocessableEntityException(e.message)
        }
    }

    override fun deleteOptional1(pid: Long): PresentationDTO {
        try{
            privateCheckComputation()
            privateCheckStopChanges()
            return PresentationDTO(presentations.deleteOptional(pid, true))
        } catch (e:PresentationNotFound){
            throw APINotFoundException(e.message)
        }
    }

    override fun deleteOptional2(pid: Long): PresentationDTO {
        try{
            privateCheckComputation()
            return PresentationDTO(presentations.deleteOptional(pid, false))
        } catch (e:PresentationNotFound){
            throw APINotFoundException(e.message)
        }
    }

    private fun privateCheckComputation() {
        try{
            if (computation.getComputationStatus()) {
                throw ComputationInProgress(COMPUTATION_IN_PROGRESS)
            }
        } catch (e:PresentationNotFound) {
            throw APINotFoundException(e.message)
        }
    }

    private fun privateCheckStopChanges() {
        try{
            if (stopChanges.getStateOfChangesOfRestrictions()) {
                throw ChangesNotAllowed(CHANGES_NOT_ALLOWED)
            }
        } catch (e:StopChangesNotFound) {
            throw APINotFoundException(e.message)
        }
    }

}
