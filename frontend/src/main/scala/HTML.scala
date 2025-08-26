/*
 * Copyright (C) 2025 Romain Reuillon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.raquo.laminar.api.L.*

object HTML:
  object Css:

    lazy val columnFlex = cls := "columnFlex"
    lazy val centerColumnFlex = cls := "centerColumnFlex"
    lazy val rowFlex = cls := "rowFlex"
    lazy val centerRowFlex = cls := "centerRowFlex"
    lazy val badgeConnect = cls := "badge-connect"
    lazy val rowGap10 = cls := "rowGap10"

    lazy val connectionTabOverlay = Seq(
      display.flex,
      flexDirection.column,
      justifyContent.right, /* center items vertically, in this case */
      alignItems.center, /* center items horizontally, in this case */
      height := "300"
    )

    lazy val openmoleLogo = Seq(
      paddingTop := "300",
      width := "500",
    )

  val centerCell = Seq(verticalAlign.middle, textAlign.center)

  def textBlock(title: String, text: String) =
    div(Css.centerColumnFlex,
      div(cls := "statusBlock",
        div(title, cls := "info"),
        div(text, cls := "infoContent")
      )
    )
