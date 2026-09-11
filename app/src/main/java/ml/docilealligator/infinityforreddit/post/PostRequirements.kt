package ml.docilealligator.infinityforreddit.post

data class SubmissionRequirements(
    // Max 5 regexes
    val titleRegexes: List<String>?,
    val titleTextMinLength: Int?,
    val titleTextMaxLength: Int?,
    // Max 15 terms
    val titleBlacklistedStrings: List<String>?,
    // Max 15 terms
    val titleRequiredStrings: List<String>?,

    // Max 5 regexes
    val bodyRegexes: List<String>?,
    val bodyTextMinLength: Int?,
    val bodyTextMaxLength: Int?,
    // Max 15 terms
    val bodyBlacklistedStrings: List<String>?,
    // Max 15 terms
    val bodyRequiredStrings: List<String>?,
    /*
        One of "required", "notAllowed", or "none", meaning that a self-post body is
        required, not allowed, or optional, respectively.
     */
    val bodyRestrictionPolicy: String,

    val guidelinesText: String?,
    val guidelinesDisplayPolicy: String,

    val galleryMinItems: Int?,
    val galleryMaxItems: Int?,
    val galleryCaptionsRequirement: String,
    val galleryUrlsRequirement: String,

    val domainBlacklist: List<String>?,
    val domainWhitelist: List<String>?,

    val linkRestrictionPolicy: String,
    val linkRepostAge: Int?,

    val isFlairRequired: Boolean
)