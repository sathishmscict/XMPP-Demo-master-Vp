package in.quantumtech.xmpp.api;

import in.quantumtech.xmpp.model.ChatAttachmentModel;

import retrofit.Callback;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;

/**
 * Created by pawneshwer on 02-Jan-17.
 */

public interface ChatAPI {
    @Multipart
    @POST("/index.php")
    void uploadFile(@Part("image[]")TypedFile file,
                    @Part("type[]") String type,
                    Callback<ChatAttachmentModel> response);
}
