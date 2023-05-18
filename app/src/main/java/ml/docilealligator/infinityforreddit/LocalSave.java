package ml.docilealligator.infinityforreddit;

import android.content.ComponentCallbacks;
import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

import ml.docilealligator.infinityforreddit.post.Post;

public class LocalSave
{
    public static class SavedPost implements Serializable, Comparable<SavedPost>
    {
        String id;
        String title;
        String subreddit;
        String flair;
        long time;

        String tags;

        public SavedPost(String _id, String _title, String _subreddit, String _flair, long _time)
        {
            id = _id;
            title = _title;
            subreddit = _subreddit;
            flair = _flair;
            time = _time;
            tags = "";
        }

        public String getId()
        {
            return id;
        }

        public long getTime() { return time; }

        public String getTags() { return tags; }
        public void setTags(String newTags) { tags = newTags; }

        private boolean Filter(String filter)
        {
            filter = filter.toLowerCase();
            if(title.toLowerCase().contains(filter)) { return true; }
            if(subreddit.toLowerCase().contains(filter)) { return true; }
            if(flair.toLowerCase().contains(filter)) { return true; }

            if(tags.toLowerCase().contains(filter)) { return true; }

            return false;
        }

        public boolean GetFilters(String[] filters)
        {
            for(String filter : filters)
            {
                filter = filter.replace("_", " ");
                if(Filter(filter))
                {
                    return true;
                }
            }

            return false;
        }

        @Override
        public int compareTo(SavedPost o) {
            return Long.compare(time, o.getTime());
        }
    }

    public static final int SORT_RANDOM = 0;
    public static final int SORT_NEWEST_ADDED = 1;
    public static final int SORT_OLDEST_ADDED = 2;
    public static final int SORT_NEWEST_UPLOAD= 3;
    public static final int SORT_OLDEST_UPLOAD= 4;
    public static final CharSequence[] SortTypes = new CharSequence[] { "Random", "Newest Added", "Oldest Added", "Newest Upload", "Oldest Upload" };
    public static int sortType = SORT_NEWEST_ADDED;

    public static Context globalCtx;

    private static boolean useFilter;
    private static LinkedHashMap<String, SavedPost> savedPosts = new LinkedHashMap<String, SavedPost>();
    private static ArrayList<SavedPost> filteredPosts = new ArrayList<SavedPost>();

    private static LinkedHashMap<String, Post> cachedPosts = new LinkedHashMap<String, Post>();

    public static SavedPost GetPost(String postId)
    {
        return savedPosts.get(postId);
    }

    public static boolean cacheHistory = false;
    public static boolean cacheSaved = true;


    public static int GetCachedPostsCount()
    {
        return cachedPosts.size();
    }

    public static void CachePosts(LinkedHashSet<Post> posts)
    {
        for(Post post : posts)
        {
            cachedPosts.put(post.getId(), post);
        }
    }

    public static List<SavedPost> GetPosts()
    {
        ArrayList<SavedPost> posts;
        posts = useFilter ? filteredPosts : new ArrayList<SavedPost>(savedPosts.values());

        switch(sortType)
        {
            default:
            case SORT_OLDEST_ADDED:
                break;
            case SORT_NEWEST_ADDED:
                Collections.reverse(posts);
                break;
            case SORT_RANDOM:
                Collections.shuffle(posts, new SecureRandom());
                break;
            case SORT_OLDEST_UPLOAD:
                Collections.sort(posts);
                break;
            case SORT_NEWEST_UPLOAD:
                Collections.sort(posts);
                Collections.reverse(posts);
                break;
        }

        //return posts.stream().limit(100).collect(Collectors.toList());    // This requires API level 24 at least
        if(posts.size() <= 100)
        {
            return posts;
        }
        else
        {
            return posts.subList(0, 100);
        }
    }

    public static void LoadLocalPosts()
    {
        ObjectInputStream input = null;
        try
        {
            File directory = new File(globalCtx.getFilesDir(), "localPosts.txt");
            if(!directory.exists()) { return; }

            input = new ObjectInputStream(new FileInputStream(directory));
            savedPosts = (LinkedHashMap<String, SavedPost>) input.readObject();
            input.close();

            Toast.makeText(globalCtx, "Loaded Local Posts", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public static void SaveLocalPosts()
    {
        ObjectOutputStream output = null;
        File f = new File(globalCtx.getFilesDir(), "localPosts.txt");
        try {
            f.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try
        {
            FileOutputStream fo = new FileOutputStream(f, false);
            output = new ObjectOutputStream(fo);
            output.writeObject(savedPosts);
            output.close();
            fo.close();

            Toast.makeText(globalCtx, "Saved Local Posts", Toast.LENGTH_SHORT).show();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void AddPost(String id, String title, String subreddit, String flair, long time)
    {
        savedPosts.put(id, new SavedPost(id, title, subreddit, flair, time));

        Toast.makeText(globalCtx, "Saved Local Post", Toast.LENGTH_SHORT).show();
    }

    public static void RemovePost(String id)
    {
        savedPosts.remove(id);
        Toast.makeText(globalCtx, "Removed Local Post", Toast.LENGTH_SHORT).show();
    }

    public static void GetAllSaved()
    {
        for(Post post : cachedPosts.values())
        {
            savedPosts.put(post.getId(), new SavedPost(post.getId(), post.getTitle(), post.getSubredditName(), post.getFlair(), post.getPostTimeMillis()));
        }
        Toast.makeText(globalCtx, "Saved Cached Posts Locally", Toast.LENGTH_SHORT).show();
    }

    public static void ClearCachedPosts()
    {
        cachedPosts.clear();

        Toast.makeText(globalCtx, "Cached Posts Cleard", Toast.LENGTH_SHORT).show();
    }

    public static void ClearSavedPosts()
    {
        savedPosts.clear();

        Toast.makeText(globalCtx, "Removed All Local Posts", Toast.LENGTH_SHORT).show();
    }

    public static void Filter(String text)
    {
        if(text.isBlank())
        {
            useFilter = false;
            return;
        }

        useFilter = true;
        filteredPosts.clear();

        String[] filters = text.split(" ");
        int i = 0;
        for(SavedPost post : savedPosts.values())
        {
            i++;
            if(i == 900)
            {
                useFilter = true;
            }

            if(post.GetFilters(filters))
            {
                filteredPosts.add(post);
            }
        }
    }

    public static void LoadBackup(Uri uri)
    {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = globalCtx.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //TODO: Maybe make this a bit cleaner. Save HashMap directly instead of recreating it from an array?
        Gson gson = new Gson();
        SavedPost[] posts = gson.fromJson(stringBuilder.toString(), SavedPost[].class);
        savedPosts.clear();
        for(SavedPost post : posts)
        {
            savedPosts.put(post.getId(), post);
        }
    }
    public static void SaveBackup(Uri uri)
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(savedPosts.values());

        try {
            ParcelFileDescriptor pfd = globalCtx.getContentResolver().
                    openFileDescriptor(uri, "w");
            FileOutputStream fileOutputStream =
                    new FileOutputStream(pfd.getFileDescriptor());
            fileOutputStream.write(json.getBytes());
            fileOutputStream.close();
            pfd.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
