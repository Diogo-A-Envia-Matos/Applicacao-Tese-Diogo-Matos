package pt.unl.fct.di.apresentacoes.services


import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.unl.fct.di.apresentacoes.domain.StopChangesDAO
import pt.unl.fct.di.apresentacoes.presistence.*
import java.time.LocalDate
import java.time.LocalTime

private const val STOP_CHANGES_STATE_FOUND = "There is a stopping changes state in the table. This operation is made to add the initial stopping changes state."
private const val STOP_CHANGES_STATE_NOT_FOUND = "The state of stopping changes is not present in the system."

private const val STOP_CHANGES_ID: Long = 1L
private const val STOP_CHANGES_INIT_STATE: Boolean = false
private const val STOP_CHANGES_STOPPED_STATE: Boolean = true

@Service
class StopChangesService(val stops: StopChangesRepository) {

    @Transactional
    fun addInitialStopFutureChanges() {
        if(stops.count().toInt() != 0) {
            println(STOP_CHANGES_STATE_FOUND)
            throw StopChangesFound()
        }

        stops.save(StopChangesDAO(STOP_CHANGES_ID, STOP_CHANGES_INIT_STATE))
    }

    fun getStateOfChangesOfRestrictions() : Boolean {
        val state = stops.findById(STOP_CHANGES_ID)
            .orElseThrow { StopChangesNotFound() }

        // println("(StopChangesService getStateOfChangesOfRestrictions) state.isStopped: " + state.isStopped)
        return state.isStopped
    }

    @Transactional
    fun stopFutureChangesOfRestrictions() : Boolean {
        // println("stopFutureChangesOfRestrictions")
        val state = stops.findById(STOP_CHANGES_ID)
            .orElseThrow { StopChangesNotFound() }

        stops.save(StopChangesDAO(STOP_CHANGES_ID, STOP_CHANGES_STOPPED_STATE))
        return STOP_CHANGES_STOPPED_STATE
    }

    @Transactional
    fun allowFutureChangesOfRestrictions() : Boolean {
        println("allowFutureChangesOfRestrictions")
        val state = stops.findById(STOP_CHANGES_ID)
            .orElseThrow { StopChangesNotFound() }
        
        stops.save(StopChangesDAO(STOP_CHANGES_ID, STOP_CHANGES_INIT_STATE))
        return STOP_CHANGES_INIT_STATE
    }
}

class StopChangesFound(message: String? = STOP_CHANGES_STATE_FOUND) : Exception(message)
class StopChangesNotFound(message: String? = STOP_CHANGES_STATE_NOT_FOUND) : Exception(message)