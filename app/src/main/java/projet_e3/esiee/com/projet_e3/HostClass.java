package projet_e3.esiee.com.projet_e3;

import android.content.Context;
import android.os.AsyncTask;

public class HostClass extends Thread{

    private Context context;
    private static int instancecount = -1;
    {
        instancecount++;
    }
    HostClass(Context pCo){
        context = pCo;
    }
    @Override
    public void run() {
        super.run();
        int PORT = 8988;
        LoadingHostActivity.FileServerAsyncTask serverAsyncTask = new LoadingHostActivity.FileServerAsyncTask(context,PORT);
        serverAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new LoadingHostActivity.onDecoAsyncTask(context,9899).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}