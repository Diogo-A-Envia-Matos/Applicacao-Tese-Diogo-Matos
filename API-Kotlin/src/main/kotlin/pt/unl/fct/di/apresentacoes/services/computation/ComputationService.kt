package pt.unl.fct.di.apresentacoes.services

import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import pt.unl.fct.di.apresentacoes.api.dto.ComputationDTO
import pt.unl.fct.di.apresentacoes.domain.ComputationDAO
import pt.unl.fct.di.apresentacoes.presistence.*

private const val THERE_IS_A_COMPUTATION_STATE = "There is a computation state in the table. This operation is made to add the initial computation state."

private const val COMPUTATION_NOT_FOUND = "The computation state is not present in the system."
private const val COMPUTATION_ID: Long = 1L
private const val COMPUTATION_INIT_STATE: Boolean = false

@Service
class ComputationService(val computation: ComputationRepository) {

    @Transactional
    fun addInitialComputation() {
        if(computation.count().toInt() != 0)
            throw ThereIsAComputationState()

        computation.save(ComputationDAO(COMPUTATION_ID, COMPUTATION_INIT_STATE))
    }

    fun getComputationStatus(): Boolean {
        val comp = computation.findById(COMPUTATION_ID)
            .orElseThrow { ComputationNotFound() }

        // println("(ComputationService getComputationStatus) comp.inProgress: " + comp.inProgress)
        return comp.inProgress
    }

    @Transactional
    fun changeComputationStatus(newState: Boolean): ComputationDAO {
        val comp = computation.findById(COMPUTATION_ID)
            .orElseThrow { ComputationNotFound() }
        
        // println("(ComputationService changeComputationStatus) newState: " + newState)
        // println("Before comp.inProgress: " + comp.inProgress)
        comp.inProgress = newState
        // println("After comp.inProgress: " + comp.inProgress)

        return computation.save(ComputationDAO(COMPUTATION_ID, newState))
    }

}

class ThereIsAComputationState(message: String? = THERE_IS_A_COMPUTATION_STATE) : Exception(message)
class ComputationNotFound(message: String? = COMPUTATION_NOT_FOUND) : RuntimeException(message)