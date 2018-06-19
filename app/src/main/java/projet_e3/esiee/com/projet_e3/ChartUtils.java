package projet_e3.esiee.com.projet_e3;

import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChartUtils extends AppCompatActivity{

    public ChartUtils(){

    }

    public BarData buildBarChart(List[] lists) {
        List<String> genres = new ArrayList<>();
        List<Double> values = new ArrayList<>();

        if(!lists[0].isEmpty()&&!lists[1].isEmpty()) {
            genres = lists[0];
            values = lists[1];
        }
        else{
            genres = Arrays.asList("Rock","Pop","Jazz","Rap","Chill");
            values = Arrays.asList(0.1,0.2,0.3,0.4,0.5);
        }
            List<BarEntry> dataEntries = new ArrayList<>();
            int numberOfgenre = genres.size();
            int numberToUse;
            if(numberOfgenre<5){
            numberToUse = numberOfgenre;
            }
            else {
                numberToUse = 5;
            }
            for (int i = 0; i < numberToUse; i++) {
                Float currentFloat = Float.valueOf(String.valueOf(values.get(i)));
                dataEntries.add(new BarEntry(i,currentFloat));
            }
        //Float maxWidth = Float.valueOf(String.valueOf(Collections.max(values)));

            BarDataSet barDataSet = new BarDataSet(dataEntries,"");
            barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);

            BarData genreData = new BarData(barDataSet);
            genreData.setBarWidth(0.5f);
            genreData.setDrawValues(false);

            return genreData;
    }

}
