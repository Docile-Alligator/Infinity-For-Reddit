package ml.docilealligator.infinityforreddit;

import com.android.volley.RequestQueue;

interface PaginationRequestQueueSynchronizer {
    void passQueue(RequestQueue q);
}
