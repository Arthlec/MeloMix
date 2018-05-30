package projet_e3.esiee.com.projet_e3;

import android.content.Context;
import android.os.AsyncTask;

public class HostClass extends Thread{

    private Context context;

    public HostClass(Context pCo){
        context = pCo;
    }
    @Override
    public void run() {
        super.run();
        int PORT = 8988;
        new LoadingHostActivity.FileServerAsyncTask(context,PORT).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);//execute();
    }
}