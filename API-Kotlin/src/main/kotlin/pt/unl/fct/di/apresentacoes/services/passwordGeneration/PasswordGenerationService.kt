package pt.unl.fct.di.apresentacoes.services.passwordGeneration

import org.springframework.stereotype.Service

@Service
class PasswordGenerationService {

    fun generateRandomPassword(): String {
        val uppercaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lowercaseLetters = "abcdefghijklmnopqrstuvwxyz"
        val numbers = "0123456789"

        val charset = uppercaseLetters + lowercaseLetters + numbers

        val password = StringBuilder()
        // Add at least one uppercase letter
        password.append(uppercaseLetters.random())
        // Add at least one lowercase letter
        password.append(lowercaseLetters.random())
        // Add at least one number
        password.append(numbers.random())

        // Fill the rest of the password with random characters
        repeat(5) {
            password.append(charset.random())
        }

        return password.toString().shuffle()
    }

    // Function to shuffle the generated password
    private fun String.shuffle(): String {
        return this.toList().shuffled().joinToString("")
    }
}