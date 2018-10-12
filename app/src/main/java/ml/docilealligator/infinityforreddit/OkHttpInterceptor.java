package ml.docilealligator.infinityforreddit;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

class OkHttpInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        /*Request request = chain.request();

        //Build new request
        Request.Builder builder = request.newBuilder();
        builder.header("Accept", "application/json"); //if necessary, say to consume JSON

        String token = settings.getAccessToken(); //save token of this request for future
        setAuthHeader(builder, token); //write current token to request

        request = builder.build(); //overwrite old request
        Response response = chain.proceed(request); //perform request, here original request will be executed

        if (response.code() == 401) { //if unauthorized
            synchronized (this) { //perform all 401 in sync blocks, to avoid multiply token updates
                String currentToken = settings.getAccessToken(); //get currently stored token

                if(currentToken != null && currentToken.equals(token)) { //compare current token with token that was stored before, if it was not updated - do update

                    int code = refreshToken() / 100; //refresh token
                    if(code != 2) { //if refresh token failed for some reason
                        if(code == 4) //only if response is 400, 500 might mean that token was not updated
                            logout(); //go to login screen
                        return response; //if token refresh failed - show error to user
                    }
                }

                if(settings.getAccessToken() != null) { //retry requires new auth token,
                    setAuthHeader(builder, settings.getAccessToken()); //set auth token to updated
                    request = builder.build();
                    return chain.proceed(request); //repeat request with new token
                }
            }
        }

        return response;*/
        return null;
    }
}
