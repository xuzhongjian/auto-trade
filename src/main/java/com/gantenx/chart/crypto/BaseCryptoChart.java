package com.gantenx.chart.crypto;

import com.gantenx.engine.Order;
import com.gantenx.chart.ChartUtils;
import com.gantenx.utils.CollectionUtils;
import com.gantenx.chart.OrderMarker;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

import java.awt.*;
import java.util.List;
import java.util.Map;

import static com.gantenx.constant.Constants.*;

public abstract class BaseCryptoChart extends ApplicationFrame {

    private final JFreeChart combinedChart;

    protected BaseCryptoChart(Map<Long, Double> mapLeft,
                              Map<Long, Double> mapRight,
                              XYSeriesCollection subDataset,
                              String subDataName,
                              double subDataRange,
                              List<Order> orderList) {
        super("Trading Line");
        XYPlot mainPlot = createMainPlot(mapLeft, mapRight);
        XYPlot subPlot = ChartUtils.createSubPlot(subDataset, subDataName, subDataRange);
        OrderMarker.markOrders(mainPlot, subPlot, orderList);

        DateAxis timeAxis = (DateAxis) mainPlot.getDomainAxis();
        CombinedDomainXYPlot combinedPlot = new CombinedDomainXYPlot(timeAxis);
        combinedPlot.add(mainPlot, 3);
        combinedPlot.add(subPlot, 1);
        combinedChart = new JFreeChart("Trading Chart", JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, true);
        setupChartPanel();
    }

    private XYPlot createMainPlot(Map<Long, Double> klineMap, Map<Long, Double> assetMap) {
        XYSeriesCollection[] datasets = {ChartUtils.createDataset("price", klineMap),
                ChartUtils.createDataset("asset", assetMap)};

        JFreeChart chart = ChartFactory.createXYLineChart(
                K_LINE, TIME, PRICE, null,
                PlotOrientation.VERTICAL, true, true, false);

        XYPlot plot = chart.getXYPlot();
        plot.setDomainAxis(ChartUtils.getDateAxis());
        ChartUtils.setupAxes(plot,
                             PRICE,
                             ASSET,
                             CollectionUtils.getMinValue(klineMap),
                             CollectionUtils.getMinValue(assetMap),
                             CollectionUtils.getMaxValue(klineMap),
                             CollectionUtils.getMaxValue(assetMap));
        ChartUtils.setupDatasetsAndRenderers(plot, datasets);
        return plot;
    }

    private void setupChartPanel() {
        ChartPanel chartPanel = new ChartPanel(combinedChart);
        chartPanel.setPreferredSize(new Dimension(CHART_WIDTH, CHART_HEIGHT));
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setMouseZoomable(true);
        setContentPane(chartPanel);
    }

    public JFreeChart getCombinedChart() {
        return combinedChart;
    }
}
