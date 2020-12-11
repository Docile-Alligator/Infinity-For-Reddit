package ml.docilealligator.infinityforreddit;

import java.util.ArrayList;

public class PostFilter {
    public int maxVote = -1;
    public int minVote = -1;
    public int maxComments = -1;
    public int minComments = -1;
    public int maxAwards = -1;
    public int minAwards = -1;
    public boolean onlyNSFW;
    public boolean onlySpoiler;
    public String postTitleRegex;
    public ArrayList<String> postTitleExcludesStrings;
    public ArrayList<String> excludesSubreddits;
    public ArrayList<String> excludesUsers;
    public ArrayList<Flair> containsFlairs;
    public ArrayList<Flair> excludesFlairs;
    public ArrayList<Integer> containsPostTypes;
}
