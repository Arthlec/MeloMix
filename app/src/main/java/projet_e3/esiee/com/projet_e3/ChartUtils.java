package projet_e3.esiee.com.projet_e3;


import com.anychart.anychart.AnyChart;
import com.anychart.anychart.AnyChartView;
import com.anychart.anychart.Cartesian;
import com.anychart.anychart.CartesianSeriesColumn;
import com.anychart.anychart.DataEntry;
import com.anychart.anychart.EnumsAnchor;
import com.anychart.anychart.HoverMode;
import com.anychart.anychart.Position;
import com.anychart.anychart.TooltipPositionMode;
import com.anychart.anychart.ValueDataEntry;

import java.util.ArrayList;
import java.util.List;

public class ChartUtils extends AnalyseData {

    public ChartUtils(){

    }

    public void buildBarChart(List[] lists) {

        AnyChartView anyChartView = findViewById(R.id.barchart);

        List<String> genres = lists[0];
        List<Double> values = lists[1];

        Cartesian cartesian = AnyChart.column();
        List<DataEntry> dataEntries = new ArrayList<>();
        for (int i = 0; i < genres.size(); i++) {
        dataEntries.add(new ValueDataEntry(genres.get(i),values.get(i)));
        }
        CartesianSeriesColumn column = cartesian.column(dataEntries);
        column.getTooltip()
                .setTitleFormat("{%X}")
                .setPosition(Position.CENTER_BOTTOM)
                .setAnchor(EnumsAnchor.CENTER_BOTTOM)
                .setOffsetX(0d)
                .setOffsetY(5d)
                .setFormat("${%Value}{groupsSeparator: }");

        cartesian.setAnimation(true);
        cartesian.setTitle("Top 10 Cosmetic Products by Revenue");

        cartesian.getYScale().setMinimum(0d);

        cartesian.getYAxis().getLabels().setFormat("${%Value}{groupsSeparator: }");

        cartesian.getTooltip().setPositionMode(TooltipPositionMode.POINT);
        cartesian.getInteractivity().setHoverMode(HoverMode.BY_X);

        cartesian.getXAxis().setTitle("Product");
        cartesian.getYAxis().setTitle("Revenue");

        anyChartView.setChart(cartesian);
    }
}
