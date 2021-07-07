package ml.docilealligator.infinityforreddit.apis;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface Strapi {
    @GET("/broadcasts")
    Call<String> getAllBroadcasts(@HeaderMap Map<String ,String> headers);

    /*
    Response:
    {"status": "success", "status_message": ""}
     */
    @FormUrlEncoded
    @POST("/videos/{rpan_id}/vote/up")
    Call<String> upvote(@HeaderMap Map<String, String> headers, @Path("rpan_id") String rpanId);

    @FormUrlEncoded
    @POST("/videos/{rpan_id}/vote/down")
    Call<String> downvote(@HeaderMap Map<String, String> headers, @Path("rpan_id") String rpanId);

    @FormUrlEncoded
    @POST("/videos/{rpan_id}/vote/unset")
    Call<String> unsetVote(@HeaderMap Map<String, String> headers, @Path("rpan_id") String rpanId);

    /*
    Request payload: {text: "Noice"}

    Response:
    {"status": "success", "status_message": "", "data": {"auto_mute_status": {"level": 0, "level_changed": false},
    "r2_comment": {"total_awards_received": 0, "approved_at_utc": null, "comment_type": null, "edited": false,
    "mod_reason_by": null, "banned_by": null, "author_flair_type": "text", "removal_reason": null, "link_id": "t3_of7btc",
    "author_flair_template_id": null, "likes": true, "rtjson": {"document": [{"c": [{"e": "text", "t": "Noice"}], "e": "par"}]},
    "replies": "", "user_reports": [], "saved": false, "id": "h4avk42", "banned_at_utc": null, "mod_reason_title": null,
    "gilded": 0, "archived": false, "no_follow": false, "author": "Hostilenemy", "can_mod_post": false, "send_replies": true,
    "parent_id": "t3_of7btc", "score": 1, "author_fullname": "t2_z40p4", "approved_by": null, "mod_note": null,
    "all_awardings": [], "subreddit_id": "t5_3psukr", "body": "Noice", "awarders": [], "downs": 0, "author_flair_css_class": null,
    "name": "t1_h4avk42", "author_patreon_flair": false, "collapsed": false, "author_flair_richtext": [], "is_submitter": false,
    "body_html": "<div class=\"md\"><p>Noice</p>\n</div>", "gildings": {}, "collapsed_reason": null, "distinguished": null,
    "associated_award": null, "stickied": false, "author_premium": false, "can_gild": false, "top_awarded_type": null,
    "author_flair_text_color": null, "score_hidden": false, "permalink": "/r/RedditSets/comments/of7btc/whistleface_762021_house_music/h4avk42/",
    "num_reports": null, "locked": false, "report_reasons": null, "created": 1625647709.0, "subreddit": "RedditSets", "author_flair_text": null,
    "treatment_tags": [], "rte_mode": "markdown", "created_utc": 1625618909.0, "subreddit_name_prefixed": "r/RedditSets",
    "controversiality": 0, "author_flair_background_color": null, "collapsed_because_crowd_control": null, "mod_reports": [],
    "subreddit_type": "public", "ups": 1}}}
     */
    @FormUrlEncoded
    @POST("/broadcasts/{rpan_id}/comment_v2")
    Call<String> sendComment(@HeaderMap Map<String, String> headers, @Path("rpan_id") String rpanId);

    @GET("/videos/{rpan_id_or_fullname}")
    Call<String> getRPANBroadcast(@Path("rpan_id_or_fullname") String rpanIdOrFullname);
}
