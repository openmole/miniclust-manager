package miniclust.manager

object Viridis:

  def colors(n: Int) = viridis256.zipWithIndex.filter(_._2 % n == 0).map(_._1)

  val viridis256= Vector(
    "#440154", "#440256", "#440357", "#450458", "#45055a", "#45065c", "#45075e", "#460860",
    "#460962", "#460a64", "#460b66", "#470c68", "#470d6a", "#470e6c", "#480f6e", "#481070",
    "#481172", "#481274", "#481376", "#481478", "#49157a", "#49167c", "#49177e", "#491880",
    "#491982", "#4a1a84", "#4a1b86", "#4a1c88", "#4a1d8a", "#4a1e8c", "#4b1f8e", "#4b208f",
    "#4b2291", "#4b2393", "#4b2495", "#4b2597", "#4c2799", "#4c289b", "#4c299d", "#4c2a9f",
    "#4c2ca1", "#4c2da3", "#4c2ea5", "#4c2fa7", "#4d30a9", "#4d31ab", "#4d33ad", "#4d34af",
    "#4d35b1", "#4d36b3", "#4d37b5", "#4d38b7", "#4d39b9", "#4d3bb9", "#4d3db9", "#4d3fb9",
    "#4d41b9", "#4d43b9", "#4d45b9", "#4c47b9", "#4c49ba", "#4c4bba", "#4c4dba", "#4b4fba",
    "#4b51ba", "#4b53ba", "#4a55ba", "#4a57ba", "#4959ba", "#495bb9", "#485db9", "#475fb9",
    "#4761b9", "#4563b9", "#4465b9", "#4367b9", "#4169b9", "#406bb9", "#3f6db9", "#3e6fb8",
    "#3c71b8", "#3b73b8", "#3a75b8", "#3777b7", "#3679b7", "#347bb7", "#327db7", "#307fb7",
    "#2e81b6", "#2c83b6", "#2b85b6", "#2987b5", "#2789b5", "#258bb5", "#238db4", "#218fb4",
    "#1f91b3", "#1d93b3", "#1b95b2", "#1997b2", "#1799b1", "#159bb1", "#129db0", "#10a0af",
    "#0ea2ae", "#0ca4ad", "#09a6ac", "#07a8ab", "#05aaab", "#03acaa", "#00aeaa", "#00b0a9",
    "#00b2a8", "#00b4a7", "#00b6a6", "#00b8a5", "#00baa4", "#00bca3", "#00bea2", "#00c1a1",
    "#00c39f", "#00c59e", "#00c79c", "#00c99b", "#00cb99", "#00cd97", "#00cf96", "#00d194",
    "#00d392", "#00d590", "#00d78f", "#00d98d", "#00db8b", "#00dd8a", "#00df88", "#00e187",
    "#00e385", "#00e584", "#00e682", "#00e880", "#00ea7f", "#00ec7d", "#00ee7b", "#00f079",
    "#00f278", "#00f476", "#00f674", "#00f772", "#00f970", "#00fb6f", "#00fd6d", "#00ff6b",
    "#1aff69", "#34ff67", "#4eff65", "#68ff63", "#82ff60", "#9cff5e", "#b6ff5b", "#d0ff59",
    "#eaff56", "#f9ff54", "#f7ff51", "#f5ff4e", "#f3ff4c", "#f1ff49", "#efff47", "#edff44",
    "#ebff42", "#e9ff3f", "#e7ff3d", "#e5ff3a", "#e3ff38", "#e1ff35", "#dfff33", "#ddff30",
    "#dbff2e", "#d9ff2b", "#d7ff29", "#d5ff26", "#d3ff24", "#d1ff21", "#cfff1f", "#cdff1c",
    "#cbff1a", "#c9ff17", "#c7ff15", "#c5ff12", "#c3ff10", "#c1ff0d", "#bfff0b", "#bdff08",
    "#bbff06", "#b9ff03", "#b7ff01", "#b5fe00", "#b3fc00", "#b1fa00", "#aff800", "#adf600",
    "#abf400", "#a9f200", "#a7f000", "#a5ee00", "#a3ec00", "#a1ea00", "#9fe800", "#9de600",
    "#9be400", "#99e200", "#97e000", "#95de00", "#93dc00", "#91da00", "#8fda00", "#8dd800",
    "#8bd600", "#89d400", "#87d200", "#85d000", "#83ce00", "#81cc00", "#7fc900", "#7dc700",
    "#7bc500", "#79c300", "#77c100", "#75bf00", "#73bd00", "#71bb00", "#6fb900", "#6db700",
    "#6bb500", "#69b300", "#67b100", "#65af00", "#63ad00", "#61ab00", "#5fa900", "#5da700",
    "#5ba500", "#59a300", "#57a100", "#558f00", "#537d00", "#516b00", "#4f5900", "#4d4700"
  )


object ChalkPalette:

  def colors(sample: Int) =
    chalk128.zipWithIndex.filter(_._2 % (128 / sample) == 0).map(_._1)


  /** Génère 128 couleurs distinctes inspirées du thème Chalk */
  val chalk128: Vector[String] = {
    val n = 128
    (0 until n).map { i =>
      val h = i.toDouble / n            // teinte 0..1
      val s = 0.55                      // saturation fixe
      val l = 0.55                      // luminosité fixe

      val rgb = hslToRgb(h, s, l)
      f"#${rgb._1}%02x${rgb._2}%02x${rgb._3}%02x"
    }.toVector
  }

  /** Conversion HSL -> RGB */
  private def hslToRgb(h: Double, s: Double, l: Double): (Int, Int, Int) = {
    val q = if (l < 0.5) l * (1 + s) else l + s - l * s
    val p = 2 * l - q

    def hue2rgb(p: Double, q: Double, t0: Double): Double = {
      var t = t0
      if (t < 0) t += 1
      if (t > 1) t -= 1
      if (t < 1.0/6) p + (q - p) * 6 * t
      else if (t < 1.0/2) q
      else if (t < 2.0/3) p + (q - p) * (2.0/3 - t) * 6
      else p
    }

    val r = hue2rgb(p, q, h + 1.0/3)
    val g = hue2rgb(p, q, h)
    val b = hue2rgb(p, q, h - 1.0/3)

    ((r*255).round.toInt, (g*255).round.toInt, (b*255).round.toInt)
  }
