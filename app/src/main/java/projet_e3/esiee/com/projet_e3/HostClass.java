package projet_e3.esiee.com.projet_e3;

import android.content.Context;
import android.os.AsyncTask;

import projet_e3.esiee.com.projet_e3.Activities.LoadingHostActivity;

/**
 * Execute les asynctask de l'hote en parallèle.
 */
public class HostClass extends Thread{

    private Context context;
    public HostClass(Context pCo){
        context = pCo;
    }
    @Override
    public void run() {
        super.run();
        int PORT_FILE = 8988;
        int PORT_DECO = 9899;
        LoadingHostActivity.FileServerAsyncTask serverAsyncTask = new LoadingHostActivity.FileServerAsyncTask(context,PORT_FILE);
        serverAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new LoadingHostActivity.onDecoAsyncTask(context,PORT_DECO).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}