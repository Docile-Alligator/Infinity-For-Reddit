package ml.docilealligator.infinityforreddit.subreddit;

import androidx.annotation.Nullable;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class SubredditSettingData {
    //  Content visibility || Posts to this profile can appear in r/all and your profile can be discovered in /users
    @SerializedName("default_set")
    private boolean defaultSet;
    @SerializedName("toxicity_threshold_chat_level")
    private int toxicityThresholdChatLevel;
    @SerializedName("crowd_control_chat_level")
    private int crowdControlChatLevel;
    @SerializedName("restrict_posting")
    private boolean restrictPosting;
    @SerializedName("public_description")
    private String publicDescription;
    @SerializedName("subreddit_id")
    private String subredditId;
    @SerializedName("allow_images")
    private boolean allowImages;
    @SerializedName("free_form_reports")
    private boolean freeFormReports;
    @SerializedName("domain")
    @Nullable
    private String domain;
    @SerializedName("show_media")
    private boolean showMedia;
    @SerializedName("wiki_edit_age")
    private int wikiEditAge;
    @SerializedName("submit_text")
    private String submitText;
    @SerializedName("allow_polls")
    private boolean allowPolls;
    @SerializedName("title")
    private String title;
    @SerializedName("collapse_deleted_comments")
    private boolean collapseDeletedComments;
    @SerializedName("wikimode")
    private String wikiMode;
    @SerializedName("should_archive_posts")
    private boolean shouldArchivePosts;
    @SerializedName("allow_videos")
    private boolean allowVideos;
    @SerializedName("allow_galleries")
    private boolean allowGalleries;
    @SerializedName("crowd_control_level")
    private int crowdControlLevel;
    @SerializedName("crowd_control_mode")
    private boolean crowdControlMode;
    @SerializedName("welcome_message_enabled")
    private boolean welcomeMessageEnabled;
    @SerializedName("welcome_message_text")
    @Nullable
    private String welcomeMessageText;
    @SerializedName("over_18")
    private boolean over18;
    @SerializedName("suggested_comment_sort")
    private String suggestedCommentSort;
    @SerializedName("disable_contributor_requests")
    private boolean disableContributorRequests;
    @SerializedName("original_content_tag_enabled")
    private boolean originalContentTagEnabled;
    @SerializedName("description")
    private String description;
    @SerializedName("submit_link_label")
    private String submitLinkLabel;
    @SerializedName("spoilers_enabled")
    private boolean spoilersEnabled;
    @SerializedName("allow_post_crossposts")
    private boolean allowPostCrossPosts;
    @SerializedName("spam_comments")
    private String spamComments;
    @SerializedName("public_traffic")
    private boolean publicTraffic;
    @SerializedName("restrict_commenting")
    private boolean restrictCommenting;
    @SerializedName("new_pinned_post_pns_enabled")
    private boolean newPinnedPostPnsEnabled;
    @SerializedName("submit_text_label")
    private String submitTextLabel;
    @SerializedName("all_original_content")
    private boolean allOriginalContent;
    @SerializedName("spam_selfposts")
    private String spamSelfPosts;
    @SerializedName("key_color")
    private String keyColor;
    @SerializedName("language")
    private String language;
    @SerializedName("wiki_edit_karma")
    private int wikiEditKarma;
    @SerializedName("hide_ads")
    private boolean hideAds;
    @SerializedName("prediction_leaderboard_entry_type")
    private int predictionLeaderboardEntryType;
    @SerializedName("header_hover_text")
    private String headerHoverText;
    @SerializedName("allow_chat_post_creation")
    private boolean allowChatPostCreation;
    @SerializedName("allow_prediction_contributors")
    private boolean allowPredictionContributors;
    @SerializedName("allow_discovery")
    private boolean allowDiscovery;
    @SerializedName("accept_followers")
    private boolean acceptFollowers;
    @SerializedName("exclude_banned_modqueue")
    private boolean excludeBannedModQueue;
    @SerializedName("allow_predictions_tournament")
    private boolean allowPredictionsTournament;
    @SerializedName("show_media_preview")
    private boolean showMediaPreview;
    @SerializedName("comment_score_hide_mins")
    private int commentScoreHideMins;
    @SerializedName("subreddit_type")
    private String subredditType;
    @SerializedName("spam_links")
    private String spamLinks;
    @SerializedName("allow_predictions")
    private boolean allowPredictions;
    @SerializedName("user_flair_pns_enabled")
    private boolean userFlairPnsEnabled;
    @SerializedName("content_options")
    private String contentOptions;

    public boolean isDefaultSet() {
        return defaultSet;
    }

    public void setDefaultSet(boolean defaultSet) {
        this.defaultSet = defaultSet;
    }

    public int getToxicityThresholdChatLevel() {
        return toxicityThresholdChatLevel;
    }

    public void setToxicityThresholdChatLevel(int toxicityThresholdChatLevel) {
        this.toxicityThresholdChatLevel = toxicityThresholdChatLevel;
    }

    public int getCrowdControlChatLevel() {
        return crowdControlChatLevel;
    }

    public void setCrowdControlChatLevel(int crowdControlChatLevel) {
        this.crowdControlChatLevel = crowdControlChatLevel;
    }

    public boolean isRestrictPosting() {
        return restrictPosting;
    }

    public void setRestrictPosting(boolean restrictPosting) {
        this.restrictPosting = restrictPosting;
    }

    public String getPublicDescription() {
        return publicDescription;
    }

    public void setPublicDescription(String publicDescription) {
        this.publicDescription = publicDescription;
    }

    public String getSubredditId() {
        return subredditId;
    }

    public void setSubredditId(String subredditId) {
        this.subredditId = subredditId;
    }

    public boolean isAllowImages() {
        return allowImages;
    }

    public void setAllowImages(boolean allowImages) {
        this.allowImages = allowImages;
    }

    public boolean isFreeFormReports() {
        return freeFormReports;
    }

    public void setFreeFormReports(boolean freeFormReports) {
        this.freeFormReports = freeFormReports;
    }

    @Nullable
    public String getDomain() {
        return domain;
    }

    public void setDomain(@Nullable String domain) {
        this.domain = domain;
    }

    public boolean isShowMedia() {
        return showMedia;
    }

    public void setShowMedia(boolean showMedia) {
        this.showMedia = showMedia;
    }

    public int getWikiEditAge() {
        return wikiEditAge;
    }

    public void setWikiEditAge(int wikiEditAge) {
        this.wikiEditAge = wikiEditAge;
    }

    public String getSubmitText() {
        return submitText;
    }

    public void setSubmitText(String submitText) {
        this.submitText = submitText;
    }

    public boolean isAllowPolls() {
        return allowPolls;
    }

    public void setAllowPolls(boolean allowPolls) {
        this.allowPolls = allowPolls;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCollapseDeletedComments() {
        return collapseDeletedComments;
    }

    public void setCollapseDeletedComments(boolean collapseDeletedComments) {
        this.collapseDeletedComments = collapseDeletedComments;
    }

    public String getWikiMode() {
        return wikiMode;
    }

    public void setWikiMode(String wikiMode) {
        this.wikiMode = wikiMode;
    }

    public boolean isShouldArchivePosts() {
        return shouldArchivePosts;
    }

    public void setShouldArchivePosts(boolean shouldArchivePosts) {
        this.shouldArchivePosts = shouldArchivePosts;
    }

    public boolean isAllowVideos() {
        return allowVideos;
    }

    public void setAllowVideos(boolean allowVideos) {
        this.allowVideos = allowVideos;
    }

    public boolean isAllowGalleries() {
        return allowGalleries;
    }

    public void setAllowGalleries(boolean allowGalleries) {
        this.allowGalleries = allowGalleries;
    }

    public int getCrowdControlLevel() {
        return crowdControlLevel;
    }

    public void setCrowdControlLevel(int crowdControlLevel) {
        this.crowdControlLevel = crowdControlLevel;
    }

    public boolean isCrowdControlMode() {
        return crowdControlMode;
    }

    public void setCrowdControlMode(boolean crowdControlMode) {
        this.crowdControlMode = crowdControlMode;
    }

    public boolean isWelcomeMessageEnabled() {
        return welcomeMessageEnabled;
    }

    public void setWelcomeMessageEnabled(boolean welcomeMessageEnabled) {
        this.welcomeMessageEnabled = welcomeMessageEnabled;
    }

    @Nullable
    public String getWelcomeMessageText() {
        return welcomeMessageText;
    }

    public void setWelcomeMessageText(@Nullable String welcomeMessageText) {
        this.welcomeMessageText = welcomeMessageText;
    }

    public boolean isOver18() {
        return over18;
    }

    public void setOver18(boolean over18) {
        this.over18 = over18;
    }

    public String getSuggestedCommentSort() {
        return suggestedCommentSort;
    }

    public void setSuggestedCommentSort(String suggestedCommentSort) {
        this.suggestedCommentSort = suggestedCommentSort;
    }

    public boolean isDisableContributorRequests() {
        return disableContributorRequests;
    }

    public void setDisableContributorRequests(boolean disableContributorRequests) {
        this.disableContributorRequests = disableContributorRequests;
    }

    public boolean isOriginalContentTagEnabled() {
        return originalContentTagEnabled;
    }

    public void setOriginalContentTagEnabled(boolean originalContentTagEnabled) {
        this.originalContentTagEnabled = originalContentTagEnabled;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubmitLinkLabel() {
        return submitLinkLabel;
    }

    public void setSubmitLinkLabel(String submitLinkLabel) {
        this.submitLinkLabel = submitLinkLabel;
    }

    public boolean isSpoilersEnabled() {
        return spoilersEnabled;
    }

    public void setSpoilersEnabled(boolean spoilersEnabled) {
        this.spoilersEnabled = spoilersEnabled;
    }

    public boolean isAllowPostCrossPosts() {
        return allowPostCrossPosts;
    }

    public void setAllowPostCrossPosts(boolean allowPostCrossPosts) {
        this.allowPostCrossPosts = allowPostCrossPosts;
    }

    public String getSpamComments() {
        return spamComments;
    }

    public void setSpamComments(String spamComments) {
        this.spamComments = spamComments;
    }

    public boolean isPublicTraffic() {
        return publicTraffic;
    }

    public void setPublicTraffic(boolean publicTraffic) {
        this.publicTraffic = publicTraffic;
    }

    public boolean isRestrictCommenting() {
        return restrictCommenting;
    }

    public void setRestrictCommenting(boolean restrictCommenting) {
        this.restrictCommenting = restrictCommenting;
    }

    public boolean isNewPinnedPostPnsEnabled() {
        return newPinnedPostPnsEnabled;
    }

    public void setNewPinnedPostPnsEnabled(boolean newPinnedPostPnsEnabled) {
        this.newPinnedPostPnsEnabled = newPinnedPostPnsEnabled;
    }

    public String getSubmitTextLabel() {
        return submitTextLabel;
    }

    public void setSubmitTextLabel(String submitTextLabel) {
        this.submitTextLabel = submitTextLabel;
    }

    public boolean isAllOriginalContent() {
        return allOriginalContent;
    }

    public void setAllOriginalContent(boolean allOriginalContent) {
        this.allOriginalContent = allOriginalContent;
    }

    public String getSpamSelfPosts() {
        return spamSelfPosts;
    }

    public void setSpamSelfPosts(String spamSelfPosts) {
        this.spamSelfPosts = spamSelfPosts;
    }

    public String getKeyColor() {
        return keyColor;
    }

    public void setKeyColor(String keyColor) {
        this.keyColor = keyColor;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getWikiEditKarma() {
        return wikiEditKarma;
    }

    public void setWikiEditKarma(int wikiEditKarma) {
        this.wikiEditKarma = wikiEditKarma;
    }

    public boolean isHideAds() {
        return hideAds;
    }

    public void setHideAds(boolean hideAds) {
        this.hideAds = hideAds;
    }

    public int getPredictionLeaderboardEntryType() {
        return predictionLeaderboardEntryType;
    }

    public void setPredictionLeaderboardEntryType(int predictionLeaderboardEntryType) {
        this.predictionLeaderboardEntryType = predictionLeaderboardEntryType;
    }

    public String getHeaderHoverText() {
        return headerHoverText;
    }

    public void setHeaderHoverText(String headerHoverText) {
        this.headerHoverText = headerHoverText;
    }

    public boolean isAllowChatPostCreation() {
        return allowChatPostCreation;
    }

    public void setAllowChatPostCreation(boolean allowChatPostCreation) {
        this.allowChatPostCreation = allowChatPostCreation;
    }

    public boolean isAllowPredictionContributors() {
        return allowPredictionContributors;
    }

    public void setAllowPredictionContributors(boolean allowPredictionContributors) {
        this.allowPredictionContributors = allowPredictionContributors;
    }

    public boolean isAllowDiscovery() {
        return allowDiscovery;
    }

    public void setAllowDiscovery(boolean allowDiscovery) {
        this.allowDiscovery = allowDiscovery;
    }

    public boolean isAcceptFollowers() {
        return acceptFollowers;
    }

    public void setAcceptFollowers(boolean acceptFollowers) {
        this.acceptFollowers = acceptFollowers;
    }

    public boolean isExcludeBannedModQueue() {
        return excludeBannedModQueue;
    }

    public void setExcludeBannedModQueue(boolean excludeBannedModQueue) {
        this.excludeBannedModQueue = excludeBannedModQueue;
    }

    public boolean isAllowPredictionsTournament() {
        return allowPredictionsTournament;
    }

    public void setAllowPredictionsTournament(boolean allowPredictionsTournament) {
        this.allowPredictionsTournament = allowPredictionsTournament;
    }

    public boolean isShowMediaPreview() {
        return showMediaPreview;
    }

    public void setShowMediaPreview(boolean showMediaPreview) {
        this.showMediaPreview = showMediaPreview;
    }

    public int getCommentScoreHideMins() {
        return commentScoreHideMins;
    }

    public void setCommentScoreHideMins(int commentScoreHideMins) {
        this.commentScoreHideMins = commentScoreHideMins;
    }

    public String getSubredditType() {
        return subredditType;
    }

    public void setSubredditType(String subredditType) {
        this.subredditType = subredditType;
    }

    public String getSpamLinks() {
        return spamLinks;
    }

    public void setSpamLinks(String spamLinks) {
        this.spamLinks = spamLinks;
    }

    public boolean isAllowPredictions() {
        return allowPredictions;
    }

    public void setAllowPredictions(boolean allowPredictions) {
        this.allowPredictions = allowPredictions;
    }

    public boolean isUserFlairPnsEnabled() {
        return userFlairPnsEnabled;
    }

    public void setUserFlairPnsEnabled(boolean userFlairPnsEnabled) {
        this.userFlairPnsEnabled = userFlairPnsEnabled;
    }

    public String getContentOptions() {
        return contentOptions;
    }

    public void setContentOptions(String contentOptions) {
        this.contentOptions = contentOptions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubredditSettingData that = (SubredditSettingData) o;
        return defaultSet == that.defaultSet && toxicityThresholdChatLevel == that.toxicityThresholdChatLevel
                && crowdControlChatLevel == that.crowdControlChatLevel && restrictPosting == that.restrictPosting
                && allowImages == that.allowImages && freeFormReports == that.freeFormReports && showMedia == that.showMedia
                && wikiEditAge == that.wikiEditAge && allowPolls == that.allowPolls && collapseDeletedComments == that.collapseDeletedComments
                && shouldArchivePosts == that.shouldArchivePosts && allowVideos == that.allowVideos
                && allowGalleries == that.allowGalleries && crowdControlLevel == that.crowdControlLevel
                && crowdControlMode == that.crowdControlMode && welcomeMessageEnabled == that.welcomeMessageEnabled
                && over18 == that.over18 && disableContributorRequests == that.disableContributorRequests
                && originalContentTagEnabled == that.originalContentTagEnabled && spoilersEnabled == that.spoilersEnabled
                && allowPostCrossPosts == that.allowPostCrossPosts && publicTraffic == that.publicTraffic
                && restrictCommenting == that.restrictCommenting && newPinnedPostPnsEnabled == that.newPinnedPostPnsEnabled
                && allOriginalContent == that.allOriginalContent && wikiEditKarma == that.wikiEditKarma
                && hideAds == that.hideAds && predictionLeaderboardEntryType == that.predictionLeaderboardEntryType
                && allowChatPostCreation == that.allowChatPostCreation && allowPredictionContributors == that.allowPredictionContributors
                && allowDiscovery == that.allowDiscovery && acceptFollowers == that.acceptFollowers
                && excludeBannedModQueue == that.excludeBannedModQueue && allowPredictionsTournament == that.allowPredictionsTournament
                && showMediaPreview == that.showMediaPreview && commentScoreHideMins == that.commentScoreHideMins
                && allowPredictions == that.allowPredictions && userFlairPnsEnabled == that.userFlairPnsEnabled
                && Objects.equals(publicDescription, that.publicDescription) && Objects.equals(subredditId, that.subredditId)
                && Objects.equals(domain, that.domain) && Objects.equals(submitText, that.submitText)
                && Objects.equals(title, that.title) && Objects.equals(wikiMode, that.wikiMode) &&
                Objects.equals(welcomeMessageText, that.welcomeMessageText) && Objects.equals(suggestedCommentSort, that.suggestedCommentSort)
                && Objects.equals(description, that.description) && Objects.equals(submitLinkLabel, that.submitLinkLabel)
                && Objects.equals(spamComments, that.spamComments) && Objects.equals(submitTextLabel, that.submitTextLabel)
                && Objects.equals(spamSelfPosts, that.spamSelfPosts) && Objects.equals(keyColor, that.keyColor)
                && Objects.equals(language, that.language) && Objects.equals(headerHoverText, that.headerHoverText)
                && Objects.equals(subredditType, that.subredditType) && Objects.equals(spamLinks, that.spamLinks)
                && Objects.equals(contentOptions, that.contentOptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(defaultSet, toxicityThresholdChatLevel, crowdControlChatLevel, restrictPosting,
                publicDescription, subredditId, allowImages, freeFormReports, domain, showMedia, wikiEditAge,
                submitText, allowPolls, title, collapseDeletedComments, wikiMode, shouldArchivePosts,
                allowVideos, allowGalleries, crowdControlLevel, crowdControlMode, welcomeMessageEnabled,
                welcomeMessageText, over18, suggestedCommentSort, disableContributorRequests, originalContentTagEnabled,
                description, submitLinkLabel, spoilersEnabled, allowPostCrossPosts, spamComments, publicTraffic,
                restrictCommenting, newPinnedPostPnsEnabled, submitTextLabel, allOriginalContent, spamSelfPosts,
                keyColor, language, wikiEditKarma, hideAds, predictionLeaderboardEntryType, headerHoverText,
                allowChatPostCreation, allowPredictionContributors, allowDiscovery, acceptFollowers,
                excludeBannedModQueue, allowPredictionsTournament, showMediaPreview, commentScoreHideMins,
                subredditType, spamLinks, allowPredictions, userFlairPnsEnabled, contentOptions);
    }
}
