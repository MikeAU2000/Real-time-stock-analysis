// ... 现有代码 ...

// 修改K线图配置，增加宽度
const candlestickOptions = {
  width: 8,  // 增加K线宽度，原来可能是3-4左右
  upColor: '#26a69a',
  downColor: '#ef5350',
  borderVisible: false,
  wickUpColor: '#26a69a',
  wickDownColor: '#ef5350'
};

// ... 现有代码 ...

// 修改成交量图表配置
const volumeSeries = chart.addHistogramSeries({
  color: '#26a69a',
  priceFormat: {
    type: 'volume',
  },
  priceScaleId: 'volume',
  scaleMargins: {
    top: 0.7,  // 调整这个值，使成交量图表占据底部30%的空间
    bottom: 0.05
  },
});

// 或者如果您使用的是highcharts或echarts等其他库，可能需要修改类似的配置
// ... 现有代码 ...