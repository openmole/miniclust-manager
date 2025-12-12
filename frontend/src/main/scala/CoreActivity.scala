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
    "#fc97af",
    "#87f7cf",
    "#f7f494",
    "#72ccff",
    "#f7c5a0",
    "#d4a4eb",
    "#d2f5a6",
    "#76f2f2"
  )

  def build(xAxisData: js.Array[Double], series: Array[Array[Double]])=
    var optChart: Option[ECharts] = None

    val eChartOption =
      EChartOption[EChartOption.Series]()
        .setBackgroundColor("#333")
        .setXAxis(XAxis()
          .setType(CartesianAxis.Type.category)
          .setBoundaryGap(false)
          .setDataVarargs(xAxisData.toSeq*)
        )
        .setYAxis(YAxis().setType(CartesianAxis.Type.value))
        .setSeries(
          series.zipWithIndex.map: (ol,zi) =>
            println(zi + " // " + ol)
            EChartOption.SeriesLine()
              .setStack("total")
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

