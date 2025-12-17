package miniclust.manager

import miniclust.facade.echarts.mod.*
import miniclust.facade.echarts.*
import miniclust.facade.echarts.echarts.*
import com.raquo.laminar.api.L.{*, given}

import scala.scalajs.js
import scala.scalajs.js.*
import miniclust.facade.echarts.anon.{Origin, ShadowBlur}
import miniclust.facade.echarts.echarts.EChartOption.BasicComponents.CartesianAxis
import miniclust.facade.echarts.echarts.EChartOption.{LineStyle, Series, SeriesLine, XAxis, YAxis}

object CoreActivity:

  def build(xAxisData: js.Array[String], series: Array[Array[Double]])=
    var optChart: Option[ECharts] = None

    val colors = ColorPalette.colors().take(xAxisData.size)

    val eChartOption =
      EChartOption[EChartOption.Series]()
        .setBackgroundColor("#fff")
        .setXAxis(XAxis()
         // .setType(CartesianAxis.Type.category)
         // .setBoundaryGap(false)
         // .setDataVarargs(xAxisData.toSeq*)
          .setDataVarargs(xAxisData.toSeq*)
        )
        .setYAxis(YAxis().setType(CartesianAxis.Type.value))
        .setSeries(
          series.zipWithIndex.map: (ol,zi) =>
            EChartOption.SeriesLine()
              .setStack("x")
              .setSmooth(true)
              .setLineStyle(ShadowBlur().setWidth(0))
              .setType(echartsStrings.line.toString)
              .setShowSymbol(false)
              .setAreaStyle(
                Origin()
                  .setOpacity(0.95)
                  .setColor(colors(zi))
              )
              .setDataVarargs(ol.toSeq*)
        )

    div(
      width := "1000px",
      height := "500px",
      onMountUnmountCallback(
        mount = { nodeCtx =>
          val element = nodeCtx.thisNode.ref
          val chart = init(element)
          chart.setOption(eChartOption)

          optChart = Some(chart)
        },
        unmount = { _ =>
          for (chart <- optChart)
            chart.dispose()
          optChart = None
        }
      )
    )

