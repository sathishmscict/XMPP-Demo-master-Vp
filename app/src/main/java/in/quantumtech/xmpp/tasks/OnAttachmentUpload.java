package in.quantumtech.xmpp.tasks;


import in.quantumtech.xmpp.model.ChatAttachmentModel;

/**
 * Created by android on 16/12/16.
 */

public interface OnAttachmentUpload{
    void onComplete(ChatAttachmentModel chatAttachmentModel);
}
