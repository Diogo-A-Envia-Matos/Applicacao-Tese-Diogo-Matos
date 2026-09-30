package pt.unl.fct.di.apresentacoes

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@SpringBootApplication
class ApresentacoesApplication

fun main(args: Array<String>) {
	runApplication<ApresentacoesApplication>(*args)
}
