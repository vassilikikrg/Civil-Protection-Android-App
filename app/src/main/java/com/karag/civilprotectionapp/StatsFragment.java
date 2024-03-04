package com.karag.civilprotectionapp;

import static android.content.ContentValues.TAG;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.karag.civilprotectionapp.helpers.NetworkUtils;;

import java.net.NetworkInterface;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsFragment extends Fragment {
    FirebaseFirestore firestore;
    TextView textViewOverallStats;
    List<String> emergenciesList;
    BarChart barChart;
    public StatsFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);
        if(NetworkUtils.isInternetAvailable(requireContext())) {
            textViewOverallStats = view.findViewById(R.id.textViewsOverallStatsNumber);
            barChart = view.findViewById(R.id.barChart);
            // Stats for incidents filtered by emergency type - Pie Chart set up
            PieChart pieChart = view.findViewById(R.id.pieChart);
            ArrayList<PieEntry> entries = new ArrayList<>();
            //get emergency types from firestore
            firestore.collection("emergencies")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            emergenciesList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                emergenciesList.add(document.getString("Name"));
                            }
                            // Iterate over each type of emergency
                            CollectionReference collection = firestore.collection("incidents");
                            for (String emergency : emergenciesList) {
                                Query query = collection.whereEqualTo("emergencyType", emergency);
                                AggregateQuery countQuery = query.count();
                                countQuery.get(AggregateSource.SERVER).addOnCompleteListener(innerTask -> {
                                    if (innerTask.isSuccessful()) {
                                        // Count fetched successfully
                                        AggregateQuerySnapshot snapshot = innerTask.getResult();
                                        Log.d(TAG, "Count: " + snapshot.getCount());
                                        long count = snapshot.getCount();
                                        entries.add(new PieEntry(count, emergency));
                                        // Update the chart when all counts are fetched
                                        if (entries.size() == emergenciesList.size()) {
                                            setUpPieChart(entries, pieChart);
                                        }
                                    } else {
                                        Log.d(TAG, "Count failed: ", innerTask.getException());
                                    }
                                });
                            }
                        } else {
                            Log.w(TAG, "Error getting emergency types.", task.getException());
                        }
                    });
            // Stats for incidents reported per month - Bar Chart set up
            loadIncidentsMonth();
            // Stats for total number of incidents
            Query query = firestore.collection("incidents");
            AggregateQuery countQuery = query.count();
            countQuery.get(AggregateSource.SERVER).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Count fetched successfully
                    AggregateQuerySnapshot snapshot = task.getResult();
                    startCountAnimation(textViewOverallStats, (int) snapshot.getCount());
                } else {
                    Log.d(TAG, "Count failed: ", task.getException());
                }
            });
        }
        else{
            Snackbar.make(requireActivity().findViewById(android.R.id.content), getResources().getString(R.string.no_internet),Snackbar.LENGTH_LONG).show();
        }
        return view;
    }

    private void setUpPieChart(ArrayList<PieEntry> entries, PieChart pieChart) {
        PieDataSet pieDataSet = new PieDataSet(entries,"");
        // Add an extra color to the ColorTemplate.PASTEL_COLORS array
        int[] pastelColorsWithExtra = Arrays.copyOf(ColorTemplate.PASTEL_COLORS, ColorTemplate.PASTEL_COLORS.length + 1);
        pastelColorsWithExtra[ColorTemplate.PASTEL_COLORS.length] = Color.LTGRAY; // Add extra color
        pieDataSet.setColors(pastelColorsWithExtra);// set color palette
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.setEntryLabelTextSize(11f);
        pieChart.getDescription().setEnabled(false);
        pieChart.animateY(1000);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setNoDataTextColor(Color.BLACK);
        pieChart.invalidate();
    }
    private void startCountAnimation(TextView textView,int upperValue) {
        ValueAnimator animator = ValueAnimator.ofInt(0, upperValue);
        if(upperValue<20)animator.setDuration(2000);
        else animator.setDuration(5000);
        animator.addUpdateListener(animation -> textView.setText(animation.getAnimatedValue().toString()));
        animator.start();
    }
    private void loadIncidentsMonth() {
        firestore.collection("incidents")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();
                        Map<String, Integer> monthCounts = new HashMap<>();
                        for (DocumentSnapshot document : documents) {
                            Date timestamp = document.getDate("dateTime");
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(timestamp);
                            int month = calendar.get(Calendar.MONTH);
                            String monthName = new DateFormatSymbols().getShortMonths()[month];
                            monthCounts.put(monthName, monthCounts.getOrDefault(monthName, 0) + 1);
                        }
                        setupBarChart(monthCounts);
                    } else {
                        Toast.makeText(getContext(), getResources().getString(R.string.failed_load_incidents), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void setupBarChart(Map<String, Integer> monthCounts) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        // Create an array to store the months in order
        String[] orderedMonths = new DateFormatSymbols().getShortMonths();

        // Loop through the ordered months array to populate the entries and labels
        for (int i = 0; i < orderedMonths.length; i++) {
            String monthName = orderedMonths[i];
            Integer count = monthCounts.getOrDefault(monthName, 0);
            entries.add(new BarEntry(i, count));
            labels.add(monthName);
        }

        BarDataSet dataSet = new BarDataSet(entries, "Number of Incidents");
        dataSet.setColors(ColorTemplate.PASTEL_COLORS);

        BarData data = new BarData(dataSet);
        barChart.setData(data);
        barChart.getDescription().setEnabled(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setNoDataTextColor(Color.BLACK);
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(labels.size());
        xAxis.setDrawLabels(true);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);

        barChart.invalidate();
    }

}