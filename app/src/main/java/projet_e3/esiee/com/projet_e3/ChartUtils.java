package projet_e3.esiee.com.projet_e3;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class ChartUtils{

    public ChartUtils(){

    }

    public void buildBarChart(List[] lists, android.view.View view) {

        BarChart barChart = view.findViewById(R.id.barchart);
        List<String> genres = lists[0];
        List<Double> values = lists[1];
        List<BarEntry> dataEntries = new ArrayList<>();
        int numberOfgenre = genres.size();

        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setMaxVisibleValueCount(numberOfgenre);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(false);

        for (int i = 0; i < numberOfgenre; i++) {
            Float currentFloat = Float.valueOf(String.valueOf(values.get(i)));
            dataEntries.add(new BarEntry(i,currentFloat));
        }

        BarDataSet barDataSet = new BarDataSet(dataEntries,"genres");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        BarData genreData = new BarData(barDataSet);
        genreData.setBarWidth(0.16f);
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new MyXAxisValueFormatter(genres));
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        xAxis.setGranularity(1);

        barChart.setData(genreData);
    }

    public class MyXAxisValueFormatter implements IAxisValueFormatter {

        private List<String> mValues;

        public MyXAxisValueFormatter(List<String> values) {
            this.mValues = values;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            // "value" represents the position of the label on the axis (x or y)
            return mValues.get((int) value);
        }
        public int getDecimalDigits() { return 0; }
    }
}
