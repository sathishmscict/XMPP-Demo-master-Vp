package in.quantumtech.xmpp.activities;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.mstr.letschat.R;
import in.quantumtech.xmpp.utils.ShareUtil;
import in.quantumtech.xmpp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PreviewActivity extends AppCompatActivity {
    private ImageView imageView;
    private VideoView videoView;
    private String url,jsonUrl;
    private String toForward;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        url = getIntent().getStringExtra("url");
        toForward = getIntent().getStringExtra("forward");
        setContentView(R.layout.activity_preview);
        imageView = (ImageView) findViewById(R.id.preview_image);
        videoView = (VideoView) findViewById(R.id.preview_video);
        videoView.setMediaController(new MediaController(this));
        try {
            JSONObject jsonBody = new JSONObject(toForward);
            if (jsonBody.has(ShareUtil.RESPONSE)) {
                JSONArray jsonArray = jsonBody.getJSONArray(ShareUtil.RESPONSE);
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonUrl = jsonArray.getJSONObject(i).getString(ShareUtil.URL);
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (url != null){
            url = Utils.getFullPath(url);
            if (url.contains(".jpg")){
                imageView.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.GONE);
                Glide.with(this).load(url).listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        //Image failed to display because of wrong local path
                        //display image from json response.
                        setJsonImage(imageView);
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        System.out.println();
                        return false;
                    }
                }).into(imageView);
            }
            else if (url.contains(".mp4")){
                imageView.setVisibility(View.GONE);
                videoView.setVisibility(View.VISIBLE);
                videoView.setVideoPath(url);
                videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        setJsonVideo(videoView);
                        return false;
                    }
                });
                videoView.start();
            }
        }

    }

    /**
     *
     * @param imageView set url from json to this image if local path fail to load.
     */
    private void setJsonImage(ImageView imageView){
        Glide.with(this).load(Utils.getFullPath(jsonUrl)).into(imageView);
    }

    /**
     *
     * @param videoView set url from json to this video if local path fail to load.
     */
    private void setJsonVideo(VideoView videoView){
        videoView.setVideoPath(Utils.getFullPath(jsonUrl));
        videoView.start();
    }
}
