package in.quantumtech.xmpp.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import in.quantumtech.xmpp.api.ChatAPI;
import in.quantumtech.xmpp.model.ChatAttachmentModel;
import in.quantumtech.xmpp.model.ChatAttachmentResponse;
import in.quantumtech.xmpp.utils.CountingTypedFile;
import in.quantumtech.xmpp.utils.ProgressListener;
import in.quantumtech.xmpp.utils.Utils;
import in.quantumtech.xmpp.views.CircleProgressBar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;


/**
 * Created by android on 16/12/16.
 */

public class SendFileTask extends AsyncTask<String, Integer, ChatAttachmentModel> {
    private ProgressListener listener;
    private String fileType;
    private File file;
    private Context context;
    private long totalSize;
    private CircleProgressBar progressBar;
    private ChatAttachmentModel chatAttachmentModel;
    private OnAttachmentUpload attachmentUploadListener;

    public SendFileTask(Context context, File file, String fileType, CircleProgressBar progressBar, OnAttachmentUpload attachmentUploadListener) {
        this.file = file;
        this.fileType = fileType;
        this.context = context;
        this.progressBar = progressBar;
        this.attachmentUploadListener = attachmentUploadListener;
    }

    @Override
    protected ChatAttachmentModel doInBackground(String... params) {
        totalSize = file.length();
        Log.d("SendFileTask", "Upload FileSize " + totalSize);
        listener = new ProgressListener() {
            @Override
            public void transferred(long num) {
                publishProgress((int) ((num / (float) totalSize) * 100));
            }
        };
        String mimeType = Utils.getMimeType(file.getAbsolutePath());
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(Utils.MAIN_API).build();
        ChatAPI api = restAdapter.create(ChatAPI.class);
        api.uploadFile(new CountingTypedFile(mimeType,file,listener), fileType, new Callback<ChatAttachmentModel>() {
            @Override
            public void success(ChatAttachmentModel chatAttachmentModel, retrofit.client.Response response) {
                ChatAttachmentModel model = new ChatAttachmentModel();
                List<ChatAttachmentResponse> responses = new ArrayList<>();
                for (ChatAttachmentResponse response1 : chatAttachmentModel.getResponse()) {
                    response1.setLocalPath(file.getAbsolutePath());
                    responses.add(response1);
                }
                model.setResponse(responses);
                if (attachmentUploadListener != null){

                    attachmentUploadListener.onComplete(model);
                }
                if (progressBar != null){
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("SendFileTask",error.getMessage());
            }
        });
        return chatAttachmentModel;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        Log.d("SendFileTask", String.format("progress[%d]", values[0]));
        //do something with values[0], its the percentage so you can easily do
        progressBar.setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(ChatAttachmentModel chatAttachmentModel) {
        super.onPostExecute(chatAttachmentModel);
        /*if (progressBar != null){
            progressBar.setVisibility(View.GONE);
        }
        if (attachmentUploadListener != null){
            attachmentUploadListener.onComplete(chatAttachmentModel);
        }*/
    }
}
