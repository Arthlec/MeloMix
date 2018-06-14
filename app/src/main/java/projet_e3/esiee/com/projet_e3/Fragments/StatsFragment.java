package projet_e3.esiee.com.projet_e3.Fragments;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.buffer.BarBuffer;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.listener.OnChartGestureListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import projet_e3.esiee.com.projet_e3.ChartUtils;
import projet_e3.esiee.com.projet_e3.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class StatsFragment extends Fragment{

    public static List[] dataList;
    public static StatsFragment newInstance() {
        return (new StatsFragment());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stats, container, false);
        TextView textView = view.findViewById(R.id.title);
        ListView lv = view.findViewById(R.id.chartList);
        textView.setText("Vos résultats");

        ChartUtils chartUtils = new ChartUtils();

        ArrayList<BarData> list = new ArrayList<>();
        BarData currentBar = chartUtils.buildBarChart(dataList);
        list.add(currentBar);
        ChartDataAdapter chartDataAdapter = new ChartDataAdapter(Objects.requireNonNull(getContext()),0,list,dataList[0]);
        lv.setAdapter(chartDataAdapter);

        return view;
    }


    public class ChartDataAdapter extends ArrayAdapter<BarData> {
        private List<String> genres;
        ChartDataAdapter(@NonNull Context context, int resource, @NonNull List<BarData> objects,List<String> list) {
            super(context, resource, objects);
            genres=list;
        }
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            BarData data = getItem(position);
            ViewHolder holder;
            if(convertView==null){
                holder = new ViewHolder();
                convertView = getLayoutInflater().from(getContext()).inflate(R.layout.barchart_item,null);
                holder.chart = (BarChart) convertView.findViewById(R.id.barchart);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }
            data.setValueTextColor(Color.BLACK);
            holder.chart.getDescription().setEnabled(false);
            holder.chart.setDrawGridBackground(false);
            holder.chart.setDrawBarShadow(false);
            holder.chart.setDrawValueAboveBar(true);
            holder.chart.setPinchZoom(false);
            holder.chart.setDoubleTapToZoomEnabled(false);

            XAxis xAxis =holder.chart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setValueFormatter(new MyXAxisValueFormatter(genres));
            xAxis.setDrawGridLines(false);
            xAxis.setGranularity(1);

            YAxis leftAxis = holder.chart.getAxisLeft();
            leftAxis.setLabelCount(5,false);
            leftAxis.setSpaceTop(15f);
            leftAxis.setDrawGridLines(false);
            leftAxis.setEnabled(false);

            YAxis rigthAxis = holder.chart.getAxisRight();
            rigthAxis.setLabelCount(5,false);
            rigthAxis.setSpaceTop(15f);
            rigthAxis.setEnabled(false);


            holder.chart.setData(data);
            holder.chart.setFitBars(true);
            holder.chart.setKeepPositionOnRotation(true);

            Legend legend = holder.chart.getLegend();
            legend.setEnabled(false);
            holder.chart.animateY(1000, Easing.EasingOption.EaseInOutBack);

            return convertView;
        }

        private class ViewHolder {
            BarChart chart;
        }

        private class MyXAxisValueFormatter implements IAxisValueFormatter {

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
}
