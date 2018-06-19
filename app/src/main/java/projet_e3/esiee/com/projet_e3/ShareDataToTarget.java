package projet_e3.esiee.com.projet_e3;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import projet_e3.esiee.com.projet_e3.Services.ShareDataFlowService;

public class ShareDataToTarget extends Thread {
    private String target;
    private Context context;
    private String[] allObjects;

    public ShareDataToTarget(String trgt, Context app,String[] things) {
        target = trgt;
        context = app;
        allObjects = things;
    }

    @Override
    public void run() {
        Intent serviceIntent = new Intent(context, ShareDataFlowService.class);
        serviceIntent.setAction(ShareDataFlowService.ACTION_SEND_FLOW);
        if (!TextUtils.isEmpty(target) && target.length() > 0) {
            ShareDataFlowService.PORT = 10014;
            int sub_port = ShareDataFlowService.PORT;
            Log.i("addre",target);

            serviceIntent.putExtra(ShareDataFlowService.EXTRAS_URL_BMP,allObjects[0]);
            serviceIntent.putExtra(ShareDataFlowService.EXTRAS_BMP_NAME,allObjects[1]);
            serviceIntent.putExtra(ShareDataFlowService.EXTRAS_URL_NEXT_BMP,allObjects[2]);
            serviceIntent.putExtra(ShareDataFlowService.EXTRAS_NEXT_BMP_NAME,allObjects[3]);

            serviceIntent.putExtra(ShareDataFlowService.EXTRAS_TARGET_ADDRESS,target);
            serviceIntent.putExtra(ShareDataFlowService.EXTRAS_TARGET_PORT, ShareDataFlowService.PORT);

            if (target != null && sub_port != -1) {
                context.startService(serviceIntent);
            }
        }
    }
}
