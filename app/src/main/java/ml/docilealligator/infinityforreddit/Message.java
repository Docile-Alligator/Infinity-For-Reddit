package ml.docilealligator.infinityforreddit;

class Message {

  static final String TYPE_COMMENT = "t1";
  static final String TYPE_ACCOUNT = "t2";
  static final String TYPE_LINK = "t3";
  static final String TYPE_MESSAGE = "t4";
  static final String TYPE_SUBREDDIT = "t5";
  static final String TYPE_AWARD = "t6";

  private final String kind;
  private final String subredditName;
  private final String subredditNamePrefixed;
  private final String id;
  private final String fullname;
  private final String subject;
  private final String author;
  private final String parentFullName;
  private final String title;
  private final String body;
  private final String context;
  private final String distinguished;
  private final String formattedTime;
  private final boolean wasComment;
  private final int score;
  private final int nComments;
  private final long timeUTC;
  private boolean isNew;

  Message(String kind, String subredditName, String subredditNamePrefixed, String id,
      String fullname,
      String subject, String author, String parentFullName, String title, String body,
      String context,
      String distinguished, String formattedTime, boolean wasComment, boolean isNew, int score,
      int nComments, long timeUTC) {
    this.kind = kind;
    this.subredditName = subredditName;
    this.subredditNamePrefixed = subredditNamePrefixed;
    this.id = id;
    this.fullname = fullname;
    this.subject = subject;
    this.author = author;
    this.parentFullName = parentFullName;
    this.title = title;
    this.body = body;
    this.context = context;
    this.distinguished = distinguished;
    this.formattedTime = formattedTime;
    this.wasComment = wasComment;
    this.isNew = isNew;
    this.score = score;
    this.nComments = nComments;
    this.timeUTC = timeUTC;
  }


  public String getKind() {
    return kind;
  }

  public String getSubredditName() {
    return subredditName;
  }

  public String getSubredditNamePrefixed() {
    return subredditNamePrefixed;
  }

  public String getId() {
    return id;
  }

  public String getFullname() {
    return fullname;
  }

  public String getSubject() {
    return subject;
  }

  public String getAuthor() {
    return author;
  }

  public String getParentFullName() {
    return parentFullName;
  }

  public String getTitle() {
    return title;
  }

  public String getBody() {
    return body;
  }

  public String getContext() {
    return context;
  }

  public String getDistinguished() {
    return distinguished;
  }

  public String getFormattedTime() {
    return formattedTime;
  }

  public boolean wasComment() {
    return wasComment;
  }

  public boolean isNew() {
    return isNew;
  }

  public void setNew(boolean isNew) {
    this.isNew = isNew;
  }

  public int getScore() {
    return score;
  }

  public int getnComments() {
    return nComments;
  }

  public long getTimeUTC() {
    return timeUTC;
  }
}
