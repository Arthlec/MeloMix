package projet_e3.esiee.com.projet_e3;

import android.content.Context;
import android.os.AsyncTask;

import projet_e3.esiee.com.projet_e3.Activities.LoadingGuestActivity;

public class ReceiveDataFlow extends Thread {

    private Context context;
    private int PORT;
    private String reason;
    private LoadingGuestActivity loadingGuestActivity;


    public ReceiveDataFlow(Context app, int Port, String rString,LoadingGuestActivity activity){
        context = app;
        PORT=Port;
        reason = rString;
        loadingGuestActivity = activity;

    }

    @Override
    public void run() {
        super.run();
        LoadingGuestActivity.onSignalReceiveAsyncTask onSignalReceiveAsyncTask = new LoadingGuestActivity.onSignalReceiveAsyncTask(context,PORT,reason,loadingGuestActivity);
        onSignalReceiveAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
