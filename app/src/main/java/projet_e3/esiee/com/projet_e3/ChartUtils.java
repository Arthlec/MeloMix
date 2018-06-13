package projet_e3.esiee.com.projet_e3;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Collections;
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
        barChart.setDrawValueAboveBar(false);
        barChart.setMaxVisibleValueCount(numberOfgenre);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(false);
        barChart.animateY(1000);

        Description description = new Description();
        description.setText(" ");
        barChart.setDescription(description);

        for (int i = 0; i < 5; i++) {
            Float currentFloat = Float.valueOf(String.valueOf(values.get(i)));
            dataEntries.add(new BarEntry(i,currentFloat));
        }
        Float maxWidth = Float.valueOf(String.valueOf(Collections.max(values)));

        BarDataSet barDataSet = new BarDataSet(dataEntries,"");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        BarData genreData = new BarData(barDataSet);
        genreData.setBarWidth(maxWidth);
        genreData.setBarWidth(0.5f);
        genreData.setDrawValues(false);
        barChart.setData(genreData);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new MyXAxisValueFormatter(genres));
        xAxis.setPosition(XAxis.XAxisPosition.TOP_INSIDE);
        xAxis.setGranularity(1);
        xAxis.setDrawGridLines(false);
        YAxis yAxisRight = barChart.getAxisRight();
        yAxisRight.setEnabled(false);

        YAxis yAxisLeft = barChart.getAxisLeft();
        yAxisLeft.setAxisMaximum(maxWidth+0.01f);
        yAxisLeft.setDrawGridLines(false);
        yAxisLeft.setEnabled(false);
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
