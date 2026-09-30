package pt.unl.fct.di.apresentacoes.services.mail

import java.time.LocalDate
import java.time.LocalTime
import java.time.Month

import jakarta.mail.internet.MimeMessage;

import pt.unl.fct.di.apresentacoes.api.dto.AssignmentSuccessDTO
import pt.unl.fct.di.apresentacoes.api.dto.RestrictionFlagDTO

import org.springframework.mail.MailException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

private const val TEST_EMAIL = "testeaplicacao322@gmail.com"
private const val TEST_EMAIL_SENDER = "testeaplicacao322@gmail.com"

private const val REGISTRATION_SUBJECT = "Registered to Presentations App"
private const val REGISTRATION_TEXT = "You have been registered in the Presentations Application!\n\n" +
        "This application helps to schedule presentations for all the dissertation preparation documents.\n\n" +
        "Your username is your email and your password is: "
private const val REGISTRATION_TEXT_END = "We highly recommend you change your password for security reasons. " +
        "To do so, log in to the app and navigate to the user section on the left-hand side of the page. " +
        "Then, click on \"Change my password\" and select a new, secure password."

private const val FORGOT_PASSWORD_SUBJECT = "Forgot my password - Presentations App"
private const val FORGOT_PASSWORD_TEXT = "You have requested a new password for the presentations application.\n\n" +
        "Your new password is: "

private const val ASSIGNMENT_SUBJECT = "Assignment scheduling completed"
private const val ASSIGNMENT_TEXT = "The assignment scheduling has been completed.\n\n" +
        "You can check this assignment in the application, by checking the \"Current Assignment\" page."
    
private const val COMPUTED_ASSIGNMENT_SUBJECT = "Assignment computation - Assignment successful"
private const val COMPUTED_ASSIGNMENT_TEXT = "Presentation assignment was successful.\n" +
        "The assignment is: "
private const val COMPUTED_ASSIGNMENT_INFO = "Presentation Id: %d\n" +
        "Presentation thesis: %s        Slot date: %s        Slot time: %s        Room: Building %s Room %s"

private const val OPTIONAL_FLAG_SUBJECT = "Assignment computation - Optional restriction triggered"
private const val OPTIONAL_FLAG_TEXT = "No assignment was found that satisfies all optional restrictions.\n"
// private const val OPTIONAL_FLAG_TEXT1 = "No assignment was found that satisfies all optional restrictions.\n" +
//         "The following presentations could not be assigned: "
// private const val OPTIONAL_FLAG_TEXT2 = "The following users could not be assigned: "
private const val OPTIONAL_FLAG_PRESENTATION_PREFIX = "The following presentations could not be assigned:\n"
private const val OPTIONAL_FLAG_PRESENTATION_INFO = "Presentation Id: %d\n" +
        "Presentation thesis: %s        Slot date: %s        Slot time: %s"
private const val OPTIONAL_FLAG_USER_PREFIX = "The following users could not be assigned:\n"
private const val OPTIONAL_FLAG_USER_INFO = "User Id: %d\n" +
        "User name: %s        Slot date: %s        Slot time: %s"
private const val OPTIONAL_FLAG_ROOM_PREFIX = "The following rooms could not be assigned:\n"
private const val OPTIONAL_FLAG_ROOM_INFO = "Room Id: %d\n" +
        "Room Number: %s        Slot date: %s        Slot time: %s"
private const val OPTIONAL_FLAG_ADDITIONAL_PRESENTATION_PREFIX = "The following presentations could not be assigned due to additional restrictions:\n"
private const val OPTIONAL_FLAG_ADDITIONAL_PRESENTATION_INFO = "Presentation Id: %d\n" +
        "Presentation thesis: %s        Slot date: %s        Slot time: %s"
private const val OPTIONAL_FLAG_ADDITIONAL_USER_PREFIX = "The following users could not be assigned due to additional restrictions:\n"
private const val OPTIONAL_FLAG_ADDITIONAL_USER_INFO = "User Id: %d\n" +
        "User name: %s        Slot date: %s        Slot time: %s"

private const val NO_SOLUTION_SUBJECT = "Assignment computation - No solution found"
// private const val NO_SOLUTION_TEXT1 = "No possible assignment was found.\n" +
//         "The following presentations could not be assigned: "
// private const val NO_SOLUTION_TEXT2 = "The following users could not be assigned: "
private const val NO_SOLUTION_TEXT = "No possible assignment was found.\n"
private const val NO_SOLUTION_PRESENTATION_PREFIX = "The following presentations could not be assigned:\n"
private const val NO_SOLUTION_PRESENTATION_INFO = "Presentation Id: %d\n" +
        "Presentation thesis: %s        Slot date: %s        Slot time: %s"
private const val NO_SOLUTION_USER_PREFIX = "The following users could not be assigned:\n"
private const val NO_SOLUTION_USER_INFO = "User Id: %d\n" +
        "User name: %s        Slot date: %s        Slot time: %s"
private const val NO_SOLUTION_ROOM_PREFIX = "The following rooms could not be assigned:\n"
private const val NO_SOLUTION_ROOM_INFO = "Room Id: %d\n" +
        "Room Number: %s        Slot date: %s        Slot time: %s"
private const val NO_SOLUTION_ADDITIONAL_PRESENTATION_PREFIX = "The following presentations could not be assigned due to additional restrictions:\n"
private const val NO_SOLUTION_ADDITIONAL_PRESENTATION_INFO = "Presentation Id: %d\n" +
        "Presentation thesis: %s        Slot date: %s        Slot time: %s"
private const val NO_SOLUTION_ADDITIONAL_USER_PREFIX = "The following users could not be assigned due to additional restrictions:\n"
private const val NO_SOLUTION_ADDITIONAL_USER_INFO = "User Id: %d\n" +
        "User name: %s        Slot date: %s        Slot time: %s"

private const val TEST_SUCCESS_SUBJECT = "Assignment testing - Testing successful"
private const val TEST_SUCCESS_TEXT = "Solution testing was successful. No restrictions are violated"

private const val TEST_RESTRICTIONS_SUBJECT = "Assignment testing - Testing unsuccessful"
private const val TEST_RESTRICTIONS_TEXT = "Solution testing was unsuccessful. Some restrictions were violated"

private const val TEST_RESTRICTIONS_FOUND_TEXT = "Solution testing violated some restrictions.\n"
private const val TEST_RESTRICTIONS_FOUND_PRESENTATION_PREFIX = "The following presentations triggered necessary restrictions:\n"
private const val TEST_RESTRICTIONS_FOUND_PRESENTATION_INFO = "Presentation Id: %d\n" +
        "Presentation thesis: %s        Slot date: %s        Slot time: %s"
private const val TEST_RESTRICTIONS_FOUND_ADDITIONAL_PRESENTATION_PREFIX = "The following presentations triggered additional necessary restrictions:\n"
private const val TEST_RESTRICTIONS_FOUND_ADDITIONAL_PRESENTATION_INFO = "Presentation Id: %d\n" +
        "Presentation thesis: %s        Slot date: %s        Slot time: %s"
private const val TEST_RESTRICTIONS_FOUND_USER_PREFIX = "The following users triggered necessary restrictions:\n"
private const val TEST_RESTRICTIONS_FOUND_USER_INFO = "User Id: %d\n" +
        "User name: %s        Slot date: %s        Slot time: %s"
private const val TEST_RESTRICTIONS_FOUND_ADDITIONAL_USER_PREFIX = "The following users triggered additional necessary restrictions:\n"
private const val TEST_RESTRICTIONS_FOUND_ADDITIONAL_USER_INFO = "User Id: %d\n" +
        "User name: %s        Slot date: %s        Slot time: %s"
private const val TEST_RESTRICTIONS_FOUND_ROOM_PREFIX = "The following rooms triggered necessary restrictions:\n"
private const val TEST_RESTRICTIONS_FOUND_ROOM_INFO = "Room Id: %d\n" +
        "Room Number: %s        Slot date: %s        Slot time: %s"

// private const val OPTIONAL_FLAG_TEXT1 = "No assignment was found that satisfies all optional restrictions.\n" +
//         "The following presentations could not be assigned: "
// private const val OPTIONAL_FLAG_TEXT2 = "The following users could not be assigned: "
private const val TEST_RESTRICTIONS_FOUND_OPTIONAL_PRESENTATION_PREFIX = "The following presentations triggered optional restrictions:\n"
private const val TEST_RESTRICTIONS_FOUND_OPTIONAL_PRESENTATION_INFO = "Presentation Id: %d\n" +
        "Presentation thesis: %s        Slot date: %s        Slot time: %s"
private const val TEST_RESTRICTIONS_FOUND_OPTIONAL_USER_PREFIX = "The following users triggered optional restrictions:\n"
private const val TEST_RESTRICTIONS_FOUND_OPTIONAL_USER_INFO = "User Id: %d\n" +
        "User name: %s        Slot date: %s        Slot time: %s"
private const val TEST_RESTRICTIONS_FOUND_OPTIONAL_ROOM_PREFIX = "The following rooms triggered optional restrictions:\n"
private const val TEST_RESTRICTIONS_FOUND_OPTIONAL_ROOM_INFO = "Room Id: %d\n" +
        "Room Number: %s        Slot date: %s        Slot time: %s"
private const val TEST_RESTRICTIONS_FOUND_OPTIONAL_ADDITIONAL_PRESENTATION_PREFIX = "The following presentations triggered additional optional restrictions:\n"
private const val TEST_RESTRICTIONS_FOUND_OPTIONAL_ADDITIONAL_PRESENTATION_INFO = "Presentation Id: %d\n" +
        "Presentation thesis: %s        Slot date: %s        Slot time: %s"
private const val TEST_RESTRICTIONS_FOUND_OPTIONAL_ADDITIONAL_USER_PREFIX = "The following users triggered additional optional restrictions:\n"
private const val TEST_RESTRICTIONS_FOUND_OPTIONAL_ADDITIONAL_USER_INFO = "User Id: %d\n" +
        "User name: %s        Slot date: %s        Slot time: %s"

private const val END_MAIL = "Best Regards,\n" +
        "Diogo Matos"

//TODO Testing email service. Sending messages to test email
@Service
class MailService(private val mailSender: JavaMailSender) {

    @Async
    fun sendRegisteredMail(email: String, password: String){
        // sendEmail(email, REGISTRATION_SUBJECT, "$REGISTRATION_TEXT$password.\n\n$REGISTRATION_TEXT_END\n\n$END_MAIL")
        sendEmail(TEST_EMAIL, REGISTRATION_SUBJECT, "$REGISTRATION_TEXT$password.\n\n$REGISTRATION_TEXT_END\n\n$END_MAIL")
    }

    @Async
    fun sendForgotPasswordMail(email: String, newPassword: String){
        // sendEmail(email, FORGOT_PASSWORD_SUBJECT, "$FORGOT_PASSWORD_TEXT$newPassword.\n\n$END_MAIL")
        sendEmail(TEST_EMAIL, FORGOT_PASSWORD_SUBJECT, "$FORGOT_PASSWORD_TEXT$newPassword.\n\n$END_MAIL")
    }
    
    @Async
    fun sendAssignmentMail(emails: List<String>){
        for (email in emails) {
            println("email: " + email)
            sendEmail(email, ASSIGNMENT_SUBJECT, ASSIGNMENT_TEXT)
        }
        // sendEmail(TEST_EMAIL, ASSIGNMENT_SUBJECT, ASSIGNMENT_TEXT)
    }
    
    @Async
    fun sendComputedAssignmentMail(email: String, finalSlots: Collection<AssignmentSuccessDTO>){
        val finalSlotList : MutableList<String> = mutableListOf()
        
        for (attribution in finalSlots) {
            val slotDate = formatDate(attribution.slotDate)
            val slotTime = formatTime(attribution.slotTime)
            finalSlotList.add(String.format(COMPUTED_ASSIGNMENT_INFO, attribution.presentationID, attribution.presentationName,
                slotDate, slotTime, attribution.roomBuilding, attribution.roomNr))
        }
        
        var slotAttribution = ""
        if (finalSlotList.isNotEmpty()) {
            slotAttribution = finalSlotList.joinToString(separator="\n\n", postfix="\n\n")
        }

        // sendEmail(email, COMPUTED_ASSIGNMENT_SUBJECT, "$COMPUTED_ASSIGNMENT_TEXT$slotAttribution.\n\n$END_MAIL")
        sendEmail(TEST_EMAIL, COMPUTED_ASSIGNMENT_SUBJECT, "$COMPUTED_ASSIGNMENT_TEXT$slotAttribution.\n\n$END_MAIL")
    }

    @Async
    fun sendNoSolutionMail(email: String, participantFlags: Collection<RestrictionFlagDTO>, assignFlags: Collection<RestrictionFlagDTO>, locationFlags: Collection<RestrictionFlagDTO>, additionalParticipantFlags: Collection<RestrictionFlagDTO>, additionalAssignFlags: Collection<RestrictionFlagDTO>){
    
        val flagsList1 : MutableList<String> = mutableListOf()
        val flagsList2 : MutableList<String> = mutableListOf()
        val flagsList3 : MutableList<String> = mutableListOf()

        val flagsList4 : MutableList<String> = mutableListOf()
        val flagsList5 : MutableList<String> = mutableListOf()

        for (flag in participantFlags) {
            val slotDate = formatDate(flag.slotDate)
            val slotTime = formatTime(flag.slotTime)
            flagsList1.add(String.format(NO_SOLUTION_PRESENTATION_INFO, flag.itemID, flag.itemName, slotDate, slotTime))
        }

        for (flag in assignFlags) {
            val slotDate = formatDate(flag.slotDate)
            val slotTime = formatTime(flag.slotTime)
            flagsList2.add(String.format(NO_SOLUTION_USER_INFO, flag.itemID, flag.itemName, slotDate, slotTime))
        }

        for (flag in locationFlags) {
            val slotDate = formatDate(flag.slotDate)
            val slotTime = formatTime(flag.slotTime)
            flagsList3.add(String.format(NO_SOLUTION_ROOM_INFO, flag.itemID, flag.itemName, slotDate, slotTime))
        }

        for (flag in additionalParticipantFlags) {
            val slotDate = formatDate(flag.slotDate)
            val slotTime = formatTime(flag.slotTime)
            flagsList4.add(String.format(NO_SOLUTION_ADDITIONAL_PRESENTATION_INFO, flag.itemID, flag.itemName, slotDate, slotTime))
        }

        for (flag in additionalAssignFlags) {
            val slotDate = formatDate(flag.slotDate)
            val slotTime = formatTime(flag.slotTime)
            flagsList5.add(String.format(NO_SOLUTION_ADDITIONAL_USER_INFO, flag.itemID, flag.itemName, slotDate, slotTime))
        }

        var flags1 = ""
        if (flagsList1.isNotEmpty()) {
            flags1 = flagsList1.joinToString(separator="\n\n", prefix=NO_SOLUTION_PRESENTATION_PREFIX, postfix="\n\n")
        }

        var flags2 = ""
        if (flagsList2.isNotEmpty()) {
            flags2 = flagsList2.joinToString(separator="\n\n", prefix=NO_SOLUTION_USER_PREFIX, postfix="\n\n")
        }

        var flags3 = ""
        if (flagsList3.isNotEmpty()) {
            flags3 = flagsList3.joinToString(separator="\n\n", prefix=NO_SOLUTION_USER_PREFIX, postfix="\n\n")
        }

        var flags4 = ""
        if (flagsList4.isNotEmpty()) {
            flags4 = flagsList4.joinToString(separator="\n\n", prefix=NO_SOLUTION_ADDITIONAL_PRESENTATION_PREFIX, postfix="\n\n")
        }

        var flags5 = ""
        if (flagsList5.isNotEmpty()) {
            flags5 = flagsList5.joinToString(separator="\n\n", prefix=NO_SOLUTION_ADDITIONAL_USER_PREFIX, postfix="\n\n")
        }

        // sendEmail(email, NO_SOLUTION_SUBJECT, "$NO_SOLUTION_TEXT\n$flags1$flags2$flags3$flags4$flags5$END_MAIL")
        sendEmail(TEST_EMAIL, NO_SOLUTION_SUBJECT, "$NO_SOLUTION_TEXT\n$flags1$flags2$flags3$flags4$END_MAIL")
    }

    @Async
    fun sendOptionalFlagMail(email: String, participantFlagsOptional: Collection<RestrictionFlagDTO>, assignFlagsOptional: Collection<RestrictionFlagDTO>, locationFlagsOptional: Collection<RestrictionFlagDTO>, additionalParticipantFlagsOptional: Collection<RestrictionFlagDTO>, additionalAssignFlagsOptional: Collection<RestrictionFlagDTO>){
        val flagsList1 : MutableList<String> = mutableListOf()
        val flagsList2 : MutableList<String> = mutableListOf()
        val flagsList3 : MutableList<String> = mutableListOf()
        val flagsList4 : MutableList<String> = mutableListOf()
        val flagsList5 : MutableList<String> = mutableListOf()

        for (flag in participantFlagsOptional) {
            val slotDate = formatDate(flag.slotDate)
            val slotTime = formatTime(flag.slotTime)
            flagsList1.add(String.format(OPTIONAL_FLAG_PRESENTATION_INFO, flag.itemID, flag.itemName, slotDate, slotTime))
        }

        for (flag in assignFlagsOptional) {
            val slotDate = formatDate(flag.slotDate)
            val slotTime = formatTime(flag.slotTime)
            flagsList2.add(String.format(OPTIONAL_FLAG_USER_INFO, flag.itemID, flag.itemName, slotDate, slotTime))
        }

        for (flag in locationFlagsOptional) {
            val slotDate = formatDate(flag.slotDate)
            val slotTime = formatTime(flag.slotTime)
            flagsList3.add(String.format(OPTIONAL_FLAG_ROOM_INFO, flag.itemID, flag.itemName, slotDate, slotTime))
        }

        for (flag in additionalParticipantFlagsOptional) {
            val slotDate = formatDate(flag.slotDate)
            val slotTime = formatTime(flag.slotTime)
            flagsList4.add(String.format(OPTIONAL_FLAG_ADDITIONAL_PRESENTATION_INFO, flag.itemID, flag.itemName, slotDate, slotTime))
        }

        for (flag in additionalAssignFlagsOptional) {
            val slotDate = formatDate(flag.slotDate)
            val slotTime = formatTime(flag.slotTime)
            flagsList5.add(String.format(OPTIONAL_FLAG_ADDITIONAL_USER_INFO, flag.itemID, flag.itemName, slotDate, slotTime))
        }

        var flags1 = ""
        if (flagsList1.isNotEmpty()) {
            flags1 = flagsList1.joinToString(separator="\n\n", prefix=OPTIONAL_FLAG_PRESENTATION_PREFIX, postfix="\n\n")
        }

        var flags2 = ""
        if (flagsList2.isNotEmpty()) {
            flags2 = flagsList2.joinToString(separator="\n\n", prefix=OPTIONAL_FLAG_USER_PREFIX, postfix="\n\n")
        }

        var flags3 = ""
        if (flagsList3.isNotEmpty()) {
            flags3 = flagsList3.joinToString(separator="\n\n", prefix=OPTIONAL_FLAG_ROOM_PREFIX, postfix="\n\n")
        }

        var flags4 = ""
        if (flagsList4.isNotEmpty()) {
            flags4 = flagsList4.joinToString(separator="\n\n", prefix=OPTIONAL_FLAG_ADDITIONAL_PRESENTATION_PREFIX, postfix="\n\n")
        }

        var flags5 = ""
        if (flagsList5.isNotEmpty()) {
            flags5 = flagsList5.joinToString(separator="\n\n", prefix=OPTIONAL_FLAG_ADDITIONAL_USER_PREFIX, postfix="\n\n")
        }

        // sendEmail(email, OPTIONAL_FLAG_SUBJECT, "$OPTIONAL_FLAG_TEXT\n$flags1$flags2$flags3$flags4$flags5$END_MAIL")
        sendEmail(TEST_EMAIL, OPTIONAL_FLAG_SUBJECT, "$OPTIONAL_FLAG_TEXT\n$flags1$flags2$flags3$flags4$END_MAIL")
    }

    @Async
    fun sendTestSuccessMail(email: String){
        // sendEmail(email, COMPUTED_ASSIGNMENT_SUBJECT, "$COMPUTED_ASSIGNMENT_TEXT$slotAttribution.\n\n$END_MAIL")
        sendEmail(TEST_EMAIL, TEST_SUCCESS_SUBJECT, "$TEST_SUCCESS_TEXT\n\n$END_MAIL")
    }

    @Async
    fun sendTestRestrictionsFoundMail(email: String, participantFlags: Collection<RestrictionFlagDTO>, assignFlags: Collection<RestrictionFlagDTO>, locationFlags: Collection<RestrictionFlagDTO>,
            additionalParticipantFlags: Collection<RestrictionFlagDTO>, additionalAssignFlags: Collection<RestrictionFlagDTO>, participantFlagsOptional: Collection<RestrictionFlagDTO>,
            assignFlagsOptional: Collection<RestrictionFlagDTO>, locationFlagsOptional: Collection<RestrictionFlagDTO>,
            additionalParticipantFlagsOptional: Collection<RestrictionFlagDTO>, additionalAssignFlagsOptional: Collection<RestrictionFlagDTO>){

        //Necessary restrictions
        val flagsList1 : MutableList<String> = mutableListOf()
        val flagsList2 : MutableList<String> = mutableListOf()
        val flagsList3 : MutableList<String> = mutableListOf()
        val flagsList4 : MutableList<String> = mutableListOf()
        val flagsList5 : MutableList<String> = mutableListOf()

        //Optional restrictions
        val flagsList6 : MutableList<String> = mutableListOf()
        val flagsList7 : MutableList<String> = mutableListOf()
        val flagsList8 : MutableList<String> = mutableListOf()
        val flagsList9 : MutableList<String> = mutableListOf()
        val flagsList10 : MutableList<String> = mutableListOf()

        for (flag in participantFlags) {
            val slotDate = formatDate(flag.slotDate)
            val slotTime = formatTime(flag.slotTime)
            flagsList1.add(String.format(TEST_RESTRICTIONS_FOUND_PRESENTATION_INFO, flag.itemID, flag.itemName, slotDate, slotTime))
        }

        for (flag in assignFlags) {
            val slotDate = formatDate(flag.slotDate)
            val slotTime = formatTime(flag.slotTime)
            flagsList2.add(String.format(TEST_RESTRICTIONS_FOUND_USER_INFO, flag.itemID, flag.itemName, slotDate, slotTime))
        }

        for (flag in locationFlags) {
            val slotDate = formatDate(flag.slotDate)
            val slotTime = formatTime(flag.slotTime)
            flagsList3.add(String.format(TEST_RESTRICTIONS_FOUND_ROOM_INFO, flag.itemID, flag.itemName, slotDate, slotTime))
        }

        for (flag in additionalParticipantFlags) {
            val slotDate = formatDate(flag.slotDate)
            val slotTime = formatTime(flag.slotTime)
            flagsList4.add(String.format(TEST_RESTRICTIONS_FOUND_ADDITIONAL_PRESENTATION_INFO, flag.itemID, flag.itemName, slotDate, slotTime))
        }

        for (flag in additionalAssignFlags) {
            val slotDate = formatDate(flag.slotDate)
            val slotTime = formatTime(flag.slotTime)
            flagsList5.add(String.format(TEST_RESTRICTIONS_FOUND_ADDITIONAL_USER_INFO, flag.itemID, flag.itemName, slotDate, slotTime))
        }

        for (flag in participantFlagsOptional) {
            val slotDate = formatDate(flag.slotDate)
            val slotTime = formatTime(flag.slotTime)
            flagsList1.add(String.format(TEST_RESTRICTIONS_FOUND_OPTIONAL_PRESENTATION_INFO, flag.itemID, flag.itemName, slotDate, slotTime))
        }

        for (flag in assignFlagsOptional) {
            val slotDate = formatDate(flag.slotDate)
            val slotTime = formatTime(flag.slotTime)
            flagsList2.add(String.format(TEST_RESTRICTIONS_FOUND_OPTIONAL_USER_INFO, flag.itemID, flag.itemName, slotDate, slotTime))
        }

        for (flag in locationFlagsOptional) {
            val slotDate = formatDate(flag.slotDate)
            val slotTime = formatTime(flag.slotTime)
            flagsList3.add(String.format(TEST_RESTRICTIONS_FOUND_OPTIONAL_ROOM_INFO, flag.itemID, flag.itemName, slotDate, slotTime))
        }

        for (flag in additionalParticipantFlagsOptional) {
            val slotDate = formatDate(flag.slotDate)
            val slotTime = formatTime(flag.slotTime)
            flagsList4.add(String.format(TEST_RESTRICTIONS_FOUND_OPTIONAL_ADDITIONAL_PRESENTATION_INFO, flag.itemID, flag.itemName, slotDate, slotTime))
        }

        for (flag in additionalAssignFlagsOptional) {
            val slotDate = formatDate(flag.slotDate)
            val slotTime = formatTime(flag.slotTime)
            flagsList5.add(String.format(TEST_RESTRICTIONS_FOUND_OPTIONAL_ADDITIONAL_USER_INFO, flag.itemID, flag.itemName, slotDate, slotTime))
        }

        var flags1 = ""
        if (flagsList1.isNotEmpty()) {
            flags1 = flagsList1.joinToString(separator="\n\n", prefix=TEST_RESTRICTIONS_FOUND_PRESENTATION_PREFIX, postfix="\n\n")
        }

        var flags2 = ""
        if (flagsList2.isNotEmpty()) {
            flags2 = flagsList2.joinToString(separator="\n\n", prefix=TEST_RESTRICTIONS_FOUND_USER_PREFIX, postfix="\n\n")
        }

        var flags3 = ""
        if (flagsList3.isNotEmpty()) {
            flags3 = flagsList3.joinToString(separator="\n\n", prefix=TEST_RESTRICTIONS_FOUND_USER_PREFIX, postfix="\n\n")
        }

        var flags4 = ""
        if (flagsList4.isNotEmpty()) {
            flags4 = flagsList4.joinToString(separator="\n\n", prefix=TEST_RESTRICTIONS_FOUND_ADDITIONAL_PRESENTATION_PREFIX, postfix="\n\n")
        }

        var flags5 = ""
        if (flagsList5.isNotEmpty()) {
            flags5 = flagsList5.joinToString(separator="\n\n", prefix=TEST_RESTRICTIONS_FOUND_ADDITIONAL_USER_PREFIX, postfix="\n\n")
        }

        var flags6 = ""
        if (flagsList6.isNotEmpty()) {
            flags6 = flagsList6.joinToString(separator="\n\n", prefix=TEST_RESTRICTIONS_FOUND_OPTIONAL_PRESENTATION_PREFIX, postfix="\n\n")
        }

        var flags7 = ""
        if (flagsList7.isNotEmpty()) {
            flags7 = flagsList7.joinToString(separator="\n\n", prefix=TEST_RESTRICTIONS_FOUND_OPTIONAL_USER_PREFIX, postfix="\n\n")
        }

        var flags8 = ""
        if (flagsList8.isNotEmpty()) {
            flags8 = flagsList8.joinToString(separator="\n\n", prefix=TEST_RESTRICTIONS_FOUND_OPTIONAL_ROOM_PREFIX, postfix="\n\n")
        }

        var flags9 = ""
        if (flagsList9.isNotEmpty()) {
            flags9 = flagsList9.joinToString(separator="\n\n", prefix=TEST_RESTRICTIONS_FOUND_OPTIONAL_ADDITIONAL_PRESENTATION_PREFIX, postfix="\n\n")
        }

        var flags10 = ""
        if (flagsList10.isNotEmpty()) {
            flags10 = flagsList10.joinToString(separator="\n\n", prefix=TEST_RESTRICTIONS_FOUND_OPTIONAL_ADDITIONAL_USER_PREFIX, postfix="\n\n")
        }

        // sendEmail(email, COMPUTED_ASSIGNMENT_SUBJECT, "$COMPUTED_ASSIGNMENT_TEXT$slotAttribution.\n\n$END_MAIL")
        sendEmail(TEST_EMAIL, TEST_RESTRICTIONS_SUBJECT, "$TEST_RESTRICTIONS_TEXT\n$flags1$flags2$flags3$flags4$flags5$flags6$flags7$flags8$flags9$flags10\n\n$END_MAIL")
    }


    private fun sendEmail(email: String, subject: String, text: String){
        try{
            val message = SimpleMailMessage()
            message.setFrom(TEST_EMAIL_SENDER)
            message.setTo(email)
            message.setSubject(subject)
            message.setText(text)
            mailSender.send(message)
            println("MailService: Email Sent")
        } catch (e: MailException){
            println("MailService: Something went wrong.")
            println(e.message)
        }
    }

    private fun formatTime(time: LocalTime): String {
        val hour = time.getHour()
        val minute = time.getMinute()
        val formatHour = if (hour < 10) "0${hour}" else hour
        val formatMinute = if (minute < 10) "0${minute}" else minute
        return "${formatHour}:${formatMinute}"
    }

    private fun formatDate(date: LocalDate): String {
        val day = date.getDayOfMonth()
        val month = date.getMonth().getValue()
        val year = date.getYear()
        return "${day}-${month}-${year}"
    }

}