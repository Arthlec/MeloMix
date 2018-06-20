package projet_e3.esiee.com.projet_e3;

import android.os.AsyncTask;

import projet_e3.esiee.com.projet_e3.Activities.LoadingGuestActivity;

public class ReceiveDataFlow extends Thread {

    private int PORT;
    private String reason;
    private LoadingGuestActivity loadingGuestActivity;


    public ReceiveDataFlow(int Port, String rString,LoadingGuestActivity activity){
        PORT=Port;
        reason = rString;
        loadingGuestActivity = activity;

    }

    @Override
    public void run() {
        super.run();
        LoadingGuestActivity.onSignalReceiveAsyncTask onSignalReceiveAsyncTask = new LoadingGuestActivity.onSignalReceiveAsyncTask(PORT,reason,loadingGuestActivity);
        onSignalReceiveAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
