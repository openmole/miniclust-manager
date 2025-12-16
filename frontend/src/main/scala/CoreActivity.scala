package miniclust.manager

import miniclust.facade.echarts.mod.*
import miniclust.facade.echarts.*
import miniclust.facade.echarts.echarts.*
import com.raquo.laminar.api.L.{*, given}

import scala.scalajs.js
import scala.scalajs.js.*
import miniclust.facade.echarts.anon.{Origin, ShadowBlur}
import miniclust.facade.echarts.echarts.EChartOption.BasicComponents.CartesianAxis
import miniclust.facade.echarts.echarts.EChartOption.BasicComponents.CartesianAxis.DataObject
import miniclust.facade.echarts.echarts.EChartOption.{LineStyle, Series, SeriesLine, XAxis, YAxis}


//type LineData =
//  scala.scalajs.js.UndefOr[
//    scala.scalajs.js.Array[String | Double |
//      CartesianAxis.DataObject
//      ]
//    ]
//
//type SeriesData =
//  js.UndefOr[
//    js.Array[
//      (js.Array[
//        Unit | String | Double | miniclust.facade.echarts.echarts.EChartOption.SeriesLine.DataObject
//      ]) | miniclust.facade.echarts.echarts.EChartOption.SeriesLine.DataObject | Double | String | Unit
//    ]
//  ]

object CoreActivity:

  val colors = Seq(
    "#FF6347",
    "#4682B4",
    "#32CD32",
    "#FFD700",
    "#8A2BE2",
    "#DC143C",
    "#20B2AA",
    "#FF4500",
    "#7FFF00",
    "#9400D3",
    "#008080",
    "#B8860B",
    "#C0C0C0",
    "#2F4F4F",
    "#6A5ACD",
    "#FF8C00",
    "#00FA9A",
    "#B22222",
    "#D2B48C",
    "#48D1CC"
  )

  def build(xAxisData: js.Array[String], series: Array[Array[Double]])=
    var optChart: Option[ECharts] = None

    val eChartOption =
      EChartOption[EChartOption.Series]()
        .setBackgroundColor("#333")
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
                  .setOpacity(0.8)
                  .setColor(colors(zi))
              )
              .setDataVarargs(ol.toSeq*)
        )

    div(
      width := "1000px",
      height := "700px",
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

