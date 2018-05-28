package projet_e3.esiee.com.projet_e3;

import android.content.Context;

public class HostClass extends Thread{

    private Context context;

    public HostClass(Context pCo){
        context = pCo;
    }
    @Override
    public void run() {
        super.run();
        int PORT = 8988;
        new HostActivity.FileServerAsyncTask(context,PORT).execute();
    }
}