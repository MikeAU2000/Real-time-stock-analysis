<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { Chart, registerables } from 'chart.js'
import type { ChartTypeRegistry } from 'chart.js'
import 'chartjs-adapter-date-fns'

// 注册Chart.js组件
Chart.register(...registerables)

// 股票列表
const stockList = [
  { code: '0388.HK', name: '香港交易所' },
  { code: '0700.HK', name: '腾讯控股' },
  { code: '0005.HK', name: '汇丰控股' },
  { code: '0001.HK', name: '长和' },
  { code: '9988.HK', name: '阿里巴巴' },
  { code: '3690.HK', name: '美团' },
  { code: '1211.HK', name: '比亚迪' },
  { code: '1299.HK', name: '友邦保险' },
  { code: '0012.HK', name: '恒基地产' },
  { code: '0016.HK', name: '新鸿基地产' },
]

// 响应式状态
const stockSymbol = ref('0388.HK') // 当前选中的股票
const chartInstance = ref<any>(null) // 分时图实例，使用any避免Chart.js类型复杂性
const candleChartInstance = ref<any>(null) // K线图实例，使用any避免Chart.js类型复杂性
const updateInterval = ref<number | null>(null) // 更新定时器
const chartData = ref<Array<Record<string, any>>>([]) // 分时图数据
const candleData = ref<Array<Record<string, any>>>([]) // K线图数据
const isLoading = ref(true) // 分时图加载状态
const isCandleLoading = ref(true) // K线图加载状态
const lastUpdateTime = ref('') // 最后更新时间
const candleTimespan = ref('1month') // K线图时间跨度：1month, 1year, 5years

// 控制选项
const showPriceLine = ref(true)
const show5MA = ref(true)
const show10MA = ref(true)
const show20MA = ref(true)

// 模拟获取5分钟股票数据
function fetchStockData() {
  // 只在初始加载时显示加载中，更新时不显示
  const initialLoad = chartData.value.length === 0
  if (initialLoad) {
    isLoading.value = true
  }

  // 使用相对路径访问API，适应部署环境
  const apiUrl = `/api/StockChartDtoredisdata?symbols=${stockSymbol.value}`

  // 使用fetch调用真实API
  fetch(apiUrl)
    .then((response) => {
      if (!response.ok) {
        throw new Error('网络响应不正常')
      }
      return response.json()
    })
    .then((data) => {
      // 处理API返回的数据
      console.log('API返回数据:', data) // 开发时用于调试

      // 将API返回的数据转换为图表所需格式
      // 注意：这里的处理需要根据实际API返回的数据结构调整
      if (data && Array.isArray(data)) {
        chartData.value = data
      } else {
        console.error('API返回的数据格式不符合预期')
        chartData.value = [] // 设置为空数组避免错误
      }

      isLoading.value = false
      lastUpdateTime.value = new Date().toLocaleTimeString()

      // 渲染图表
      if (chartData.value.length > 0) {
        renderChart(chartData.value)
      }
    })
    .catch((error) => {
      console.error('获取数据失败:', error)
      isLoading.value = false
      // 可以在这里添加错误处理逻辑，比如显示错误消息给用户
    })
}

// 模拟获取K线数据
function fetchCandleData() {
  isCandleLoading.value = true

  // 根据不同时间跨度构建不同API URL，使用相对路径
  let apiUrl = ''
  switch (candleTimespan.value) {
    case '1month':
      // 1个月日K
      apiUrl = `/api/databy?symbol=${stockSymbol.value}&validRange=1d&periodType=month&period=1`
      break
    case '1year':
      // 1年周K
      apiUrl = `/api/databy?symbol=${stockSymbol.value}&validRange=1wk&periodType=year&period=1`
      break
    case '5years':
      // 5年月K
      apiUrl = `/api/databy?symbol=${stockSymbol.value}&validRange=1mo&periodType=year&period=5`
      break
    default:
      apiUrl = `/api/databy?symbol=${stockSymbol.value}&validRange=1d&periodType=month&period=1`
  }

  // 使用fetch调用API
  fetch(apiUrl)
    .then((response) => {
      if (!response.ok) {
        throw new Error('网络响应不正常')
      }
      return response.json()
    })
    .then((responseData) => {
      // 处理API返回的数据
      console.log('K线API返回数据:', responseData) // 开发时用于调试

      // 检查数据并转换为图表所需格式
      if (responseData && Array.isArray(responseData)) {
        // API直接返回的是数组，不需要.data属性
        const formattedData = responseData.map((item: any) => {
          // 日期格式化 - 使用tradeDate替代date
          return {
            date: item.tradeDate, // 使用API返回的tradeDate字段
            open: Number(item.open),
            high: Number(item.high),
            low: Number(item.low),
            close: Number(item.close),
            volume: Number(item.volume),
            // 保存均线数据用于后续显示（如果有）
            fiveMa: item.fiveMa !== undefined ? Number(item.fiveMa) : undefined,
            tenMa: item.tenMa !== undefined ? Number(item.tenMa) : undefined,
            twentyMa: item.twentyMa !== undefined ? Number(item.twentyMa) : undefined,
          }
        })

        candleData.value = formattedData

        // 渲染K线图
        if (candleData.value.length > 0) {
          renderCandleChart(candleData.value)
        }
      } else {
        console.error('K线API返回的数据格式不符合预期')
        candleData.value = [] // 设置为空数组避免错误
      }

      isCandleLoading.value = false
    })
    .catch((error) => {
      console.error('获取K线数据失败:', error)
      isCandleLoading.value = false
      // 可以在这里添加错误处理逻辑
    })
}

// 计算移动平均线 - 修改确保正确的起始位置
function calculateMA(days: number, values: number[]): (number | null)[] {
  const result: (number | null)[] = []

  // 确保前(days-1)个点为null而不是undefined，使图表从第days个点开始显示
  for (let i = 0; i < values.length; i++) {
    if (i < days - 1) {
      // 在达到所需天数前，返回null，这样图表就不会显示这些点
      result.push(null)
    } else {
      // 计算平均值
      let sum = 0
      for (let j = 0; j < days; j++) {
        sum += values[i - j]
      }
      result.push(sum / days)
    }
  }
  return result
}

// 渲染分时图表
function renderChart(data: Array<Record<string, any>>) {
  const chartCanvas = document.getElementById('stockChart') as HTMLCanvasElement
  if (!chartCanvas) return

  // 记录当前图表的状态，用于无缝更新
  let oldZoom = null
  let oldCenter = null

  if (chartInstance.value) {
    // 保存当前缩放和位置状态
    const scales = chartInstance.value.scales
    if (scales.x && scales.y) {
      oldZoom = {
        x: {
          min: scales.x.min,
          max: scales.x.max,
        },
        y: {
          min: scales.y.min,
          max: scales.y.max,
        },
      }
      oldCenter = {
        x: (scales.x.min + scales.x.max) / 2,
        y: (scales.y.min + scales.y.max) / 2,
      }
    }

    // 销毁旧图表
    chartInstance.value.destroy()
    chartInstance.value = null
  }

  // 准备数据
  const labels: string[] = []
  const priceData: number[] = []
  const ma5Data: (number | null)[] = [] // 改为接受null值
  const ma10Data: (number | null)[] = [] // 改为接受null值
  const ma20Data: (number | null)[] = [] // 改为接受null值

  data.forEach((item, index) => {
    // 处理时间标签 - 根据API返回的marketUnixTime字段
    let dateTime: Date
    if (item.marketUnixtime) {
      // 旧的模拟数据格式
      dateTime = new Date(Number(item.marketUnixtime) * 1000)
    } else if (item.marketUnixTime) {
      // API返回的格式，确保转换为字符串或数字
      const timeValue = item.marketUnixTime as string
      dateTime = new Date(timeValue)
    } else {
      dateTime = new Date()
    }

    labels.push(
      `${dateTime.getHours().toString().padStart(2, '0')}:${dateTime.getMinutes().toString().padStart(2, '0')}`,
    )

    // 处理价格和均线数据
    priceData.push(Number(item.regularMarketPrice))

    // 如果API直接提供了MA数据，根据索引位置决定是否显示
    if (item.smaFivePrice !== undefined) {
      ma5Data.push(index >= 5 ? Number(item.smaFivePrice) : null)
    }

    if (item.smaTenPrice !== undefined) {
      ma10Data.push(index >= 10 ? Number(item.smaTenPrice) : null)
    }

    if (item.smaTwentyPrice !== undefined) {
      ma20Data.push(index >= 20 ? Number(item.smaTwentyPrice) : null)
    }
  })

  // 如果API没有提供MA数据，计算MA数据
  const calcMa5 = ma5Data.length === 0 ? calculateMA(5, priceData) : ma5Data
  const calcMa10 = ma10Data.length === 0 ? calculateMA(10, priceData) : ma10Data
  const calcMa20 = ma20Data.length === 0 ? calculateMA(20, priceData) : ma20Data

  // 计算价格范围
  const maxPrice = Math.max(...priceData.filter((p) => !isNaN(p)))
  const minPrice = Math.min(...priceData.filter((p) => !isNaN(p)))
  const priceRange = maxPrice - minPrice
  const pricePadding = priceRange * 0.1 // 添加10%的边距
  const yAxisMin = Math.max(0, minPrice - pricePadding)
  const yAxisMax = maxPrice + pricePadding

  // 准备数据集
  const datasets = []

  // 价格线
  if (showPriceLine.value) {
    datasets.push({
      label: '价格',
      data: priceData,
      borderColor: 'rgba(75, 192, 192, 1)',
      backgroundColor: 'rgba(75, 192, 192, 0.1)',
      borderWidth: 2,
      fill: true,
      tension: 0.4,
      pointRadius: 0,
      pointHoverRadius: 5,
      hidden: !showPriceLine.value,
    })
  }

  // 5MA均线
  if (show5MA.value) {
    datasets.push({
      label: '5MA',
      data: calcMa5,
      borderColor: 'rgba(0, 183, 255, 1)',
      backgroundColor: 'transparent',
      borderWidth: 1.5,
      pointRadius: 0,
      fill: false,
      tension: 0.4,
      hidden: !show5MA.value,
    })
  }

  // 10MA均线
  if (show10MA.value) {
    datasets.push({
      label: '10MA',
      data: calcMa10,
      borderColor: 'rgba(255, 147, 0, 1)',
      backgroundColor: 'transparent',
      borderWidth: 1.5,
      pointRadius: 0,
      fill: false,
      tension: 0.4,
      hidden: !show10MA.value,
    })
  }

  // 20MA均线
  if (show20MA.value) {
    datasets.push({
      label: '20MA',
      data: calcMa20,
      borderColor: 'rgba(192, 0, 255, 1)',
      backgroundColor: 'transparent',
      borderWidth: 1.5,
      pointRadius: 0,
      fill: false,
      tension: 0.4,
      hidden: !show20MA.value,
    })
  }

  // 创建图表
  const ctx = chartCanvas.getContext('2d')
  if (!ctx) return

  chartInstance.value = new Chart(ctx, {
    type: 'line',
    data: {
      labels,
      datasets,
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      animation: false, // 禁用所有动画
      transitions: {
        active: {
          animation: {
            duration: 0, // 确保无过渡动画
          },
        },
      },
      plugins: {
        legend: {
          display: false, // 使用自定义图例
        },
        tooltip: {
          mode: 'index',
          intersect: false,
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          titleColor: '#ffffff',
          bodyColor: '#ffffff',
          borderColor: '#555555',
          borderWidth: 1,
          padding: 10,
          displayColors: true,
          callbacks: {
            label: function (context) {
              const value = context.raw as number
              if (context.dataset.label === '价格') {
                return `价格: ${Number(value).toFixed(2)}`
              } else {
                return `${context.dataset.label}: ${Number(value).toFixed(2)}`
              }
            },
          },
        },
      },
      interaction: {
        mode: 'index',
        intersect: false,
      },
      scales: {
        x: {
          grid: {
            color: 'rgba(255, 255, 255, 0.1)',
          },
          ticks: {
            color: '#aaaaaa',
            maxRotation: 0,
            callback: function (value, index) {
              // 5分钟图表每个点都显示时间
              return labels[index]
            },
          },
        },
        y: {
          position: 'right',
          grid: {
            color: 'rgba(255, 255, 255, 0.1)',
          },
          min: yAxisMin, // 设置Y轴最小值
          max: yAxisMax, // 设置Y轴最大值
          ticks: {
            color: '#aaaaaa',
            callback: function (value) {
              return Number(value).toFixed(2)
            },
          },
        },
      },
    },
  })

  // 恢复之前的缩放和位置状态，实现无缝更新
  if (oldZoom && chartInstance.value) {
    const scales = chartInstance.value.scales
    if (scales.x && scales.y) {
      scales.x.min = oldZoom.x.min
      scales.x.max = oldZoom.x.max
      scales.y.min = oldZoom.y.min
      scales.y.max = oldZoom.y.max
      chartInstance.value.update('none') // 不使用动画更新
    }
  }
}

// 渲染K线图表
function renderCandleChart(data: Array<Record<string, any>>) {
  const chartCanvas = document.getElementById('candleChart') as HTMLCanvasElement
  if (!chartCanvas) return

  // 清理之前的图表
  if (candleChartInstance.value) {
    candleChartInstance.value.destroy()
    candleChartInstance.value = null
  }

  // 准备数据
  const labels: string[] = []
  const openData: number[] = []
  const highData: number[] = []
  const lowData: number[] = []
  const closeData: number[] = []
  const volumeData: number[] = []
  const ma5Data: (number | null)[] = []
  const ma10Data: (number | null)[] = []
  const ma20Data: (number | null)[] = []

  data.forEach((item, index) => {
    // 处理日期标签
    labels.push(item.date as string)

    // 处理OHLC数据
    openData.push(Number(item.open))
    highData.push(Number(item.high))
    lowData.push(Number(item.low))
    closeData.push(Number(item.close))
    volumeData.push(Number(item.volume))

    // 使用API提供的均线数据（如果有），并确保从正确的位置开始显示
    if (item.fiveMa !== undefined) {
      ma5Data.push(index >= 5 ? Number(item.fiveMa) : null)
    }
    if (item.tenMa !== undefined) {
      ma10Data.push(index >= 10 ? Number(item.tenMa) : null)
    }
    if (item.twentyMa !== undefined) {
      ma20Data.push(index >= 20 ? Number(item.twentyMa) : null)
    }
  })

  // 如果API没有提供均线数据，则计算
  const finalMa5Data = ma5Data.length > 0 ? ma5Data : calculateMA(5, closeData)
  const finalMa10Data = ma10Data.length > 0 ? ma10Data : calculateMA(10, closeData)
  const finalMa20Data = ma20Data.length > 0 ? ma20Data : calculateMA(20, closeData)

  // 创建图表
  const ctx = chartCanvas.getContext('2d')
  if (!ctx) return

  // 确定是红涨绿跌还是绿涨红跌 (这里使用红涨绿跌)
  const upColor = 'rgba(235, 47, 6, 0.7)' // 红色蜡烛
  const downColor = 'rgba(0, 180, 42, 0.7)' // 绿色蜡烛
  const upBorderColor = 'rgba(235, 47, 6, 1)'
  const downBorderColor = 'rgba(0, 180, 42, 1)'

  // 设置成交量柱状图的颜色（与蜡烛图颜色区分）
  const volumeUpColor = 'rgba(255, 132, 96, 0.7)' // 浅红色
  const volumeDownColor = 'rgba(118, 255, 170, 0.7)' // 浅绿色
  const volumeUpBorderColor = 'rgba(255, 132, 96, 0.9)'
  const volumeDownBorderColor = 'rgba(118, 255, 170, 0.9)'

  // 获取最大成交量和价格范围
  const maxVolume = Math.max(...volumeData.filter((v) => !isNaN(v) && v > 0))
  const maxPrice = Math.max(...highData.filter((p) => !isNaN(p)))
  const minPrice = Math.min(...lowData.filter((p) => !isNaN(p)))

  // 计算价格范围，添加一定的边距
  const priceRange = maxPrice - minPrice
  const pricePadding = priceRange * 0.2 // 增加到20%的边距
  const yAxisMin = Math.max(0, minPrice - pricePadding)
  const yAxisMax = maxPrice + pricePadding * 0.5 // 顶部边距可以适当减小

  // 准备蜡烛图数据 - 创建自定义绘制函数
  const drawCandlesticks = function (chart: Chart) {
    const ctx = chart.ctx
    if (!ctx) return

    const meta = chart.getDatasetMeta(0)
    const xScale = chart.scales.x
    const yScale = chart.scales.y

    ctx.save()

    // 遍历每个蜡烛
    for (let i = 0; i < closeData.length; i++) {
      const open = openData[i]
      const high = highData[i]
      const low = lowData[i]
      const close = closeData[i]

      // 获取x坐标
      let x
      if (meta.data[i]) {
        x = meta.data[i].x
      } else {
        const value = i
        x = xScale.getPixelForValue(value)
      }

      // 获取价格对应的y坐标
      const yOpen = yScale.getPixelForValue(open)
      const yHigh = yScale.getPixelForValue(high)
      const yLow = yScale.getPixelForValue(low)
      const yClose = yScale.getPixelForValue(close)

      // 蜡烛宽度 - 增加蜡烛宽度
      let barWidth = 20 // 默认宽度
      if (meta.data.length > 0 && 'width' in meta.data[0]) {
        barWidth = (meta.data[0] as { width: number }).width
      } else {
        barWidth = xScale.width / closeData.length
      }
      const candleWidth = Math.min(45, barWidth * 1.5) // 最大宽度45px，增大宽度系数为1.5

      // 设置颜色
      const isUp = close >= open
      const color = isUp ? upColor : downColor
      const borderColor = isUp ? upBorderColor : downBorderColor

      // 绘制影线
      ctx.beginPath()
      ctx.moveTo(x, yHigh)
      ctx.lineTo(x, yLow)
      ctx.strokeStyle = borderColor
      ctx.lineWidth = 1 // 将影线宽度从3px减小到1px
      ctx.stroke()

      // 绘制实体
      ctx.beginPath()
      ctx.fillStyle = color
      ctx.strokeStyle = borderColor
      ctx.lineWidth = 2 // 加粗边框至2px

      const yStart = isUp ? yClose : yOpen
      const height = Math.max(Math.abs(yClose - yOpen), 1)

      // 确保即使开盘=收盘也有最小高度
      const rectHeight = Math.max(height, 4) // 最小高度增加到4px

      ctx.rect(x - candleWidth / 2, yStart, candleWidth, rectHeight)
      ctx.fill()
      ctx.stroke()
    }

    ctx.restore()
  }

  // 创建主图表实例
  candleChartInstance.value = new Chart(ctx, {
    type: 'bar',
    plugins: [
      {
        id: 'candlestickPlugin',
        beforeDatasetDraw: function (chart) {
          // 避免绘制原始的bar图
          const meta = chart.getDatasetMeta(0)
          meta.hidden = true
        },
        afterDatasetsDraw: function (chart) {
          // 绘制自定义蜡烛图
          drawCandlesticks(chart)
        },
      },
    ],
    data: {
      labels,
      datasets: [
        // 蜡烛图占位数据集
        {
          type: 'bar',
          label: 'K线',
          data: closeData,
          barPercentage: 0.7, // 增大柱状占比
          categoryPercentage: 0.9, // 增大类别占比
          backgroundColor: 'transparent',
          borderColor: 'transparent',
        },
        // 移动平均线
        {
          type: 'line',
          label: '5MA',
          data: finalMa5Data,
          borderColor: 'rgba(0, 183, 255, 1)',
          backgroundColor: 'transparent',
          borderWidth: 2, // 加粗均线
          pointRadius: 0,
          fill: false,
          spanGaps: true, // 允许跳过空值
        },
        {
          type: 'line',
          label: '10MA',
          data: finalMa10Data,
          borderColor: 'rgba(255, 147, 0, 1)',
          backgroundColor: 'transparent',
          borderWidth: 2, // 加粗均线
          pointRadius: 0,
          fill: false,
          spanGaps: true, // 允许跳过空值
        },
        {
          type: 'line',
          label: '20MA',
          data: finalMa20Data,
          borderColor: 'rgba(192, 0, 255, 1)',
          backgroundColor: 'transparent',
          borderWidth: 2, // 加粗均线
          pointRadius: 0,
          fill: false,
          spanGaps: true, // 允许跳过空值
        },
        // 成交量部分
        {
          type: 'bar',
          label: '成交量',
          data: volumeData,
          backgroundColor: volumeData.map((_, i) => {
            return closeData[i] >= openData[i] ? volumeUpColor : volumeDownColor
          }),
          borderColor: volumeData.map((_, i) => {
            return closeData[i] >= openData[i] ? volumeUpBorderColor : volumeDownBorderColor
          }),
          borderWidth: 1,
          yAxisID: 'y1',
          barPercentage: 0.7, // 增大柱状占比
          categoryPercentage: 0.9, // 增大类别占比
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      animation: false,
      plugins: {
        legend: {
          display: false,
        },
        tooltip: {
          mode: 'index',
          intersect: false,
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          titleColor: '#ffffff',
          bodyColor: '#ffffff',
          borderColor: '#555555',
          borderWidth: 1,
          callbacks: {
            title: function (tooltipItems) {
              return tooltipItems[0].label || ''
            },
            label: function (context) {
              const datasetLabel = context.dataset.label
              const value = context.raw as number

              if (datasetLabel === 'K线') {
                const index = context.dataIndex
                return [
                  `开盘: ${Number(openData[index]).toFixed(2)}`,
                  `最高: ${Number(highData[index]).toFixed(2)}`,
                  `最低: ${Number(lowData[index]).toFixed(2)}`,
                  `收盘: ${Number(closeData[index]).toFixed(2)}`,
                ]
              } else if (datasetLabel === '成交量') {
                return `成交量: ${value.toLocaleString()}`
              } else {
                return `${datasetLabel}: ${Number(value).toFixed(2)}`
              }
            },
          },
        },
      },
      layout: {
        padding: {
          top: 20,
          right: 20,
          bottom: 0,
          left: 20,
        },
      },
      scales: {
        x: {
          grid: {
            color: 'rgba(255, 255, 255, 0.1)',
          },
          ticks: {
            color: '#aaaaaa',
            maxRotation: 0,
            callback: function (value, index) {
              // 根据时间跨度和数据点数量动态调整显示间隔和格式
              const totalLabels = labels.length

              // 1月日K线图的显示逻辑：每两个显示一个，只显示月日
              if (candleTimespan.value === '1month') {
                const step = 2 // 每两个点显示一个日期
                if (index % step === 0 && labels[index]) {
                  // 将日期字符串转换为只有月份和日子的格式
                  const dateStr = labels[index] as string
                  try {
                    // 假设日期格式为 "2025-03-31" 这样的形式
                    const parts = dateStr.split('-')
                    if (parts.length === 3) {
                      return `${parts[1]}-${parts[2]}` // 只返回月-日
                    }
                  } catch (e) {
                    // 出错则返回原始日期
                  }
                  return dateStr
                }
                return ''
              }
              // 1年周K线图的显示逻辑修改
              else if (candleTimespan.value === '1year') {
                const step = Math.max(1, Math.floor(totalLabels / 30)) // 平均每6個標籤顯示一個

                if (index % step === 0 || index === 0 || index === totalLabels - 1) {
                  const dateStr = labels[index] as string
                  try {
                    const parts = dateStr.split('-')
                    if (parts.length === 3) {
                      return `${parts[0]}-${parts[1]}-${parts[2]}`
                    }
                  } catch (e) {}
                  return dateStr
                }
                return ''
              }

              // 5年月K线图的显示逻辑：按季度均匀显示
              else if (candleTimespan.value === '5years') {
                // 计算一个较为均匀的间距，大约每季度显示一个点
                const step = Math.max(1, Math.floor(totalLabels / 20))

                if (index % step === 0 && labels[index]) {
                  const dateStr = labels[index] as string
                  try {
                    // 假设日期格式为 "2024-06" 这样的形式
                    const parts = dateStr.split('-')
                    if (parts.length >= 2) {
                      return `${parts[0]}-${parts[1]}` // 返回年-月
                    }
                  } catch (e) {
                    // 出错则返回原始日期
                  }
                  return dateStr
                }
                return ''
              }
              // 其他情况
              else {
                const step = Math.max(1, Math.floor(totalLabels / 12))
                return index % step === 0 ? labels[index] : ''
              }
            },
          },
        },
        y: {
          position: 'right',
          grid: {
            color: 'rgba(255, 255, 255, 0.1)',
          },
          min: yAxisMin, // 设置Y轴最小值为计算出的范围
          max: yAxisMax, // 设置Y轴最大值为计算出的范围
          ticks: {
            color: '#aaaaaa',
            callback: function (value) {
              return Number(value).toFixed(2)
            },
          },
        },
        y1: {
          position: 'left',
          grid: {
            color: 'rgba(255, 255, 255, 0.1)',
            drawOnChartArea: false,
          },
          // 成交量轴控制 - 减小成交量部分的高度
          weight: 1, // 权重为1，确保在y之后渲染
          min: 0,
          max: maxVolume * 8, // 将最大成交量扩大8倍，进一步减小成交量图表高度
          ticks: {
            color: '#aaaaaa',
            callback: function (value) {
              // 格式化成交量显示
              const num = Number(value)
              if (num >= 1000000) {
                return (num / 1000000).toFixed(1) + 'M'
              } else if (num >= 1000) {
                return (num / 1000).toFixed(0) + 'K'
              } else {
                return num
              }
            },
          },
        },
      },
    },
  })
}

// 切换股票
function switchStock(code: string) {
  stockSymbol.value = code
  fetchStockData()
  fetchCandleData()
}

// 切换线条可见性
function toggleLineVisibility(lineType: string) {
  switch (lineType) {
    case '价格':
      showPriceLine.value = !showPriceLine.value
      break
    case '5MA':
      show5MA.value = !show5MA.value
      break
    case '10MA':
      show10MA.value = !show10MA.value
      break
    case '20MA':
      show20MA.value = !show20MA.value
      break
  }

  // 重新渲染图表
  if (chartData.value.length > 0) {
    renderChart(chartData.value)
  }
}

// 切换K线图时间跨度
function changeCandleTimespan(timespan: string) {
  candleTimespan.value = timespan
  fetchCandleData()
}

// 启动自动刷新
function startAutoRefresh() {
  // 清除之前的定时器
  if (updateInterval.value) {
    clearInterval(updateInterval.value)
  }

  // 每30秒静默刷新一次数据
  updateInterval.value = setInterval(() => {
    // 不显示加载状态，静默刷新
    isLoading.value = false
    fetchStockData()
  }, 30000) as unknown as number
}

// 组件挂载
onMounted(() => {
  // 获取初始数据
  fetchStockData()
  fetchCandleData()

  // 启动自动刷新
  startAutoRefresh()
})

// 组件卸载
onUnmounted(() => {
  // 清除定时器
  if (updateInterval.value) {
    clearInterval(updateInterval.value)
  }

  // 销毁图表
  if (chartInstance.value) {
    chartInstance.value.destroy()
  }

  if (candleChartInstance.value) {
    candleChartInstance.value.destroy()
  }
})
</script>

<template>
  <div class="stock-chart-container">
    <!-- 头部区域 -->
    <div class="chart-header">
      <h1 class="chart-title">
        {{ stockList.find((s) => s.code === stockSymbol)?.name }} ({{ stockSymbol }})
      </h1>
      <div class="last-update">最后更新: {{ lastUpdateTime }}</div>
    </div>

    <!-- 股票选择区域 -->
    <div class="stock-tabs">
      <div
        v-for="stock in stockList"
        :key="stock.code"
        class="stock-tab"
        :class="{ active: stockSymbol === stock.code }"
        @click="switchStock(stock.code)"
      >
        {{ stock.code }}
      </div>
    </div>

    <!-- 图表区域 - 两列布局 -->
    <div class="charts-container">
      <!-- 左侧：5分钟实时图表 -->
      <div class="chart-column">
        <div class="chart-panel">
          <div class="panel-header">
            <h2 class="panel-title">实时走势 (5分钟)</h2>
            <div class="chart-legend">
              <div
                class="legend-item"
                :class="{ active: showPriceLine }"
                @click="toggleLineVisibility('价格')"
              >
                <div class="legend-color price-color"></div>
                <span>价格</span>
              </div>
              <div
                class="legend-item"
                :class="{ active: show5MA }"
                @click="toggleLineVisibility('5MA')"
              >
                <div class="legend-color ma5-color"></div>
                <span>5MA</span>
              </div>
              <div
                class="legend-item"
                :class="{ active: show10MA }"
                @click="toggleLineVisibility('10MA')"
              >
                <div class="legend-color ma10-color"></div>
                <span>10MA</span>
              </div>
              <div
                class="legend-item"
                :class="{ active: show20MA }"
                @click="toggleLineVisibility('20MA')"
              >
                <div class="legend-color ma20-color"></div>
                <span>20MA</span>
              </div>
            </div>
          </div>
          <div class="chart-wrapper">
            <div v-if="isLoading" class="loading-overlay">
              <div class="loading-spinner"></div>
              <div class="loading-text">加载中...</div>
            </div>
            <canvas id="stockChart"></canvas>
          </div>
        </div>
      </div>

      <!-- 右侧：K线图和成交量 -->
      <div class="chart-column">
        <div class="chart-panel">
          <div class="panel-header">
            <h2 class="panel-title">K线图</h2>
            <div class="timespan-selector">
              <div
                class="timespan-option"
                :class="{ active: candleTimespan === '1month' }"
                @click="changeCandleTimespan('1month')"
              >
                1月(日K)
              </div>
              <div
                class="timespan-option"
                :class="{ active: candleTimespan === '1year' }"
                @click="changeCandleTimespan('1year')"
              >
                1年(周K)
              </div>
              <div
                class="timespan-option"
                :class="{ active: candleTimespan === '5years' }"
                @click="changeCandleTimespan('5years')"
              >
                5年(月K)
              </div>
            </div>
          </div>
          <div class="chart-wrapper">
            <div v-if="isCandleLoading" class="loading-overlay">
              <div class="loading-spinner"></div>
              <div class="loading-text">加载中...</div>
            </div>
            <canvas id="candleChart"></canvas>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.stock-chart-container {
  background-color: #121826;
  color: #ffffff;
  font-family: 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
  height: 100vh;
  width: 100vw; /* 使用视窗宽度 */
  max-width: 100%; /* 确保不超出父容器 */
  display: flex;
  flex-direction: column;
  padding: 0; /* 移除内边距 */
  margin: 0; /* 移除外边距 */
  box-sizing: border-box;
  overflow: hidden;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #2a3a5a;
}

.chart-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.last-update {
  font-size: 12px;
  color: #7a8baa;
}

.stock-tabs {
  display: flex;
  overflow-x: auto;
  background-color: #1a2233;
  border-radius: 4px;
  margin: 10px 0;
  scrollbar-width: none; /* 隐藏滚动条 */
  -ms-overflow-style: none; /* IE/Edge 隐藏滚动条 */
}

.stock-tabs::-webkit-scrollbar {
  display: none; /* Chrome/Safari 隐藏滚动条 */
}

.stock-tab {
  padding: 8px 15px;
  cursor: pointer;
  white-space: nowrap;
  font-size: 14px;
  transition: all 0.2s;
  border-right: 1px solid #2a3a5a;
}

.stock-tab:hover {
  background-color: #2a3a5a;
}

.stock-tab.active {
  background-color: #3450a1;
  color: white;
}

/* 图表容器布局 */
.charts-container {
  display: flex;
  flex: 1;
  width: 100%; /* 确保使用全宽 */
  gap: 10px;
  overflow: hidden;
}

.chart-column {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.chart-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #1a2233;
  border-radius: 6px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  border-bottom: 1px solid #2a3a5a;
}

.panel-title {
  font-size: 14px;
  font-weight: 500;
  margin: 0;
  color: #7a8baa;
}

.chart-legend {
  display: flex;
  background-color: rgba(26, 34, 51, 0.8);
  border-radius: 20px;
  padding: 5px;
}

.legend-item {
  display: flex;
  align-items: center;
  margin: 0 10px;
  padding: 5px 10px;
  border-radius: 15px;
  cursor: pointer;
  opacity: 0.5;
  transition: all 0.2s;
}

.legend-item.active {
  opacity: 1;
  background-color: rgba(255, 255, 255, 0.1);
}

.legend-color {
  width: 14px;
  height: 3px;
  margin-right: 5px;
  border-radius: 2px;
}

.price-color {
  background-color: rgb(75, 192, 192);
}

.ma5-color {
  background-color: rgb(0, 183, 255);
}

.ma10-color {
  background-color: rgb(255, 147, 0);
}

.ma20-color {
  background-color: rgb(192, 0, 255);
}

/* 时间跨度选择器 */
.timespan-selector {
  display: flex;
  background-color: rgba(26, 34, 51, 0.8);
  border-radius: 20px;
  padding: 3px;
}

.timespan-option {
  padding: 5px 10px;
  border-radius: 15px;
  cursor: pointer;
  font-size: 12px;
  opacity: 0.7;
  transition: all 0.2s;
}

.timespan-option:hover {
  opacity: 0.9;
}

.timespan-option.active {
  opacity: 1;
  background-color: rgba(52, 80, 161, 0.8);
}

.chart-wrapper {
  flex: 1;
  position: relative;
  overflow: hidden;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(18, 24, 38, 0.8);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 10;
}

.loading-spinner {
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top: 3px solid #3450a1;
  width: 30px;
  height: 30px;
  animation: spin 1s linear infinite;
}

.loading-text {
  margin-top: 10px;
  font-size: 14px;
  color: #7a8baa;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 响应式适配 */
@media (max-width: 768px) {
  .charts-container {
    flex-direction: column;
  }

  .chart-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .last-update {
    margin-top: 5px;
  }

  .stock-tab {
    padding: 6px 12px;
    font-size: 12px;
  }

  .legend-item {
    margin: 0 5px;
    padding: 4px 8px;
    font-size: 12px;
  }
}
</style>
