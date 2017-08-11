package tv.camment.cammentsdk.data.model;

import com.camment.clientsdk.model.Camment;

/**
 * Created by petrushka on 11/08/2017.
 */

public class CammentUpload extends Camment {

    private int transferId = -1;


    public int getTransferId() {
        return transferId;
    }

    public void setTransferId(int transferId) {
        this.transferId = transferId;
    }

}
