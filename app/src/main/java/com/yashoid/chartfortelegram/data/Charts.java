package com.yashoid.chartfortelegram.data;

import android.content.Context;
import android.graphics.Color;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Charts {

    private static final String COLUMNS = "columns";
    private static final String TYPES = "types";
    private static final String NAMES = "names";
    private static final String COLORS = "colors";

    private static final String TYPE_LINE = "line";
    private static final String TYPE_X = "x";

    public static List<Chart> readChartsFromAssets(Context context, String fileName) throws IOException {
        return readCharts(context.getAssets().open(fileName));
    }

    public static List<Chart> readCharts(InputStream stream) throws IOException {
        String rawJson = readStream(stream);

        try {
            return readCharts(new JSONArray(rawJson));
        } catch (JSONException e) {
            throw new IOException("Invalid chart data format.", e);
        }
    }

    private static String readStream(InputStream stream) throws IOException {
        StringBuilder sb = new StringBuilder();

        byte[] buffer = new byte[512];
        int size = 0;

        while (size != -1) {
            size = stream.read(buffer);

            if (size > 0) {
                sb.append(new String(buffer, 0, size, "UTF-8"));
            }
        }

        return sb.toString();
    }

    public static List<Chart> readCharts(JSONArray data) throws JSONException {
        final int chartCount = data.length();

        List<Chart> charts = new ArrayList<>(chartCount);

        for (int i = 0; i < chartCount; i++) {
            charts.add(readChart(data.getJSONObject(i)));
        }

        return charts;
    }

    private static Chart readChart(JSONObject data) throws JSONException {
        HashMap<String, List<Long>> columns = readColumns(data.getJSONArray(COLUMNS));

        JSONObject names = data.getJSONObject(NAMES);
        JSONObject colors = data.getJSONObject(COLORS);

        JSONObject types = data.getJSONObject(TYPES);

        long[] timeStamps = null;
        List<ChartLine> lines = new ArrayList<>();

        Iterator<String> typeIterator = types.keys();

        while (typeIterator.hasNext()) {
            String name = typeIterator.next();
            String value = types.getString(name);

            if (TYPE_LINE.equals(value)) {
                String lineName = names.getString(name);
                int lineColor = Color.parseColor(colors.getString(name));

                List<Long> valueList = columns.get(name);

                final int valueCount = valueList.size();

                int[] lineValues = new int[valueCount];

                for (int i = 0; i < valueCount; i++) {
                    lineValues[i] = valueList.get(i).intValue();
                }

                lines.add(new ChartLine(lineName, lineColor, lineValues));
            }
            else if (TYPE_X.equals(value)) {
                List<Long> xList = columns.get(name);

                final int xCount = xList.size();

                timeStamps = new long[xCount];

                for (int i = 0; i < xCount; i++) {
                    timeStamps[i] = xList.get(i).longValue();
                }
            }
            else {
                throw new RuntimeException("Undefined column type '" + value + "'.");
            }
        }

        Chart chart = new Chart(timeStamps, lines.toArray(new ChartLine[lines.size()]));

        for (ChartLine line: lines) {
            line.setChart(chart);
        }

        return chart;
    }

    private static HashMap<String, List<Long>> readColumns(JSONArray array) throws JSONException {
        final int columnCount = array.length();

        HashMap<String, List<Long>> columns = new HashMap<>(columnCount);

        for (int i = 0; i < columnCount; i++) {
            JSONArray valueArray = array.getJSONArray(i);

            final int valueCount = valueArray.length() - 1;

            List<Long> values = new ArrayList<>(valueCount);

            columns.put(valueArray.getString(0), values);

            for (int j = 1; j <= valueCount; j++) {
                values.add(valueArray.getLong(j));
            }
        }

        return columns;
    }

}
