package ml.docilealligator.infinityforreddit.moderation.modmail

/*
{"explanation": "Participant has been muted from the subreddit.", "message": "Bad Request", "reason": "UNKNOWN_ERROR"}
 */
class ModMailSendMessageError(
    val explanation: String?,
    val message: String?,
    val reason: String?
)