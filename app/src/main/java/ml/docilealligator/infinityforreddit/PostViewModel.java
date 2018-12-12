package ml.docilealligator.infinityforreddit;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.ArrayList;

public class PostViewModel extends ViewModel {
    private MutableLiveData<ArrayList<Post>> posts = new MutableLiveData<>();

    LiveData<ArrayList<Post>> getPosts() {
        if(posts == null) {
            setPosts(new ArrayList<Post>());
        }
        return posts;
    }

    void setPosts(ArrayList<Post> posts) {
        this.posts.postValue(posts);
    }

    void addPosts(ArrayList<Post> newPosts) {
        ArrayList<Post> posts = this.posts.getValue();
        if(posts != null) {
            posts.addAll(newPosts);
            this.posts.postValue(posts);
        }
    }
}
