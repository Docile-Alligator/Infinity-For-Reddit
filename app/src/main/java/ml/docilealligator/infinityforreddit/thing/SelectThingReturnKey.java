package ml.docilealligator.infinityforreddit.thing;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SelectThingReturnKey {
    public static final String RETURN_EXTRA_SUBREDDIT_OR_USER_NAME = "RESOUN";
    public static final String RETURN_EXTRA_SUBREDDIT_OR_USER_ICON = "RESOUI";
    public static final String RETRUN_EXTRA_MULTIREDDIT = "REM";
    public static final String RETURN_EXTRA_THING_TYPE = "RETT";

    @IntDef({THING_TYPE.SUBREDDIT, THING_TYPE.USER, THING_TYPE.MULTIREDDIT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface THING_TYPE {
        int SUBREDDIT = 0;
        int USER = 1;
        int MULTIREDDIT = 2;
    }
}
