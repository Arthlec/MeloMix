package projet_e3.esiee.com.projet_e3;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class RequestClass extends Thread {

    private Context context;
    private String Token;
    private TextView TrackName;
    private ImageView TrackImage;
    private TextView NextText;
    private ImageView NextImg;
    public RequestClass(String ptok, ImageView imageView, ImageView pNext, TextView textView, TextView pText){
        Token = ptok;
        TrackImage = imageView;
        TrackName = textView;
        NextImg = pNext;
        NextText = pText;
    }

    @Override
    public void run() {
        super.run();
        new HostActivity.RequestAsyncTask(Token,TrackImage,NextImg,TrackName,NextText).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);//.execute();
    }
}
