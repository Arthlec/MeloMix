package projet_e3.esiee.com.projet_e3;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import projet_e3.esiee.com.projet_e3.Services.ShareDataFlowService;

public class ShareDataToTarget extends Thread {
    private String target;
    private Context context;

    public ShareDataToTarget(String trgt, Context app) {
        target = trgt;
        context = app;
    }

    @Override
    public void run() {
        Intent serviceIntent = new Intent(context, ShareDataFlowService.class);
        serviceIntent.setAction(ShareDataFlowService.ACTION_SEND_FLOW);
        if (!TextUtils.isEmpty(target) && target.length() > 0) {
            ShareDataFlowService.PORT = 10014;
            int sub_port = ShareDataFlowService.PORT;
            Log.i("addre",target);
            serviceIntent.putExtra(ShareDataFlowService.EXTRAS_TARGET_ADDRESS,target);
            serviceIntent.putExtra(ShareDataFlowService.EXTRAS_TARGET_PORT, ShareDataFlowService.PORT);

            if (target != null && sub_port != -1) {
                context.startService(serviceIntent);
            }
        }
    }
}
