package miniclust.manager

object ColorPalette:

  private val goldenRatioConjugate = 0.618033988749895

  def colors(
              saturation: Double = 0.5,
              value: Double = 0.85
            ): LazyList[String] =

    def next(hue: Double): LazyList[String] = {
      val newHue = (hue + goldenRatioConjugate) % 1.0
      hsvToHex(newHue, saturation, value) #:: next(newHue)
    }

    next(scala.util.Random.nextDouble())

  /** Conversion HSV → Hex (#RRGGBB) */
  private def hsvToHex(h: Double, s: Double, v: Double): String =
    val i = Math.floor(h * 6).toInt
    val f = h * 6 - i
    val p = v * (1 - s)
    val q = v * (1 - f * s)
    val t = v * (1 - (1 - f) * s)

    val (r, g, b) = i % 6 match {
      case 0 => (v, t, p)
      case 1 => (q, v, p)
      case 2 => (p, v, t)
      case 3 => (p, q, v)
      case 4 => (t, p, v)
      case _ => (v, p, q)
    }

    f"#${(r * 255).toInt}%02x${(g * 255).toInt}%02x${(b * 255).toInt}%02x"
