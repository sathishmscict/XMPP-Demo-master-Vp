package in.quantumtech.xmpp.model;

import java.util.List;

/**
 * Created by android on 16/12/16.
 */

public class ChatAttachmentModel {
    private int count;
    private List<ChatAttachmentResponse> response;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<ChatAttachmentResponse> getResponse() {
        return response;
    }

    public void setResponse(List<ChatAttachmentResponse> response) {
        this.response = response;
    }
}
