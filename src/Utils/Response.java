package Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Response implements Serializable {
    private int sequenceNumber;
    private String managerID;

    private String content;
    private boolean isSuccess;

    public Response(Request request, boolean isSuccess, String result) {
        this.sequenceNumber = request.getSequenceNumber();
        this.managerID = request.getManagerID();
        this.isSuccess = isSuccess;
        String overall = isSuccess ? "successful" : "failed";
        this.content = String.format(Config.RESPONSE.RESPONSE_CONTENT, request.getFullInvocation(), overall, result);
    }

    // Getters & Setters
    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public String getManagerID() {
        return managerID;
    }

    public String getContent() {
        return content;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(this);
        return out.toByteArray();
    }
}
