package legends.gfx

import java.awt.Graphics
import java.lang.Math.PI
import java.util.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Point(val x : Double, val y : Double)
fun Point(p : java.awt.Point) = Point(p.x, p.y)
fun Point(x : Int, y : Int) = Point(x.toDouble(), y.toDouble())
infix fun Point.rotate(dAngle : Double) = polarPoint(abs, angle + dAngle)
val Point.abs : Double
    get() = sqrt(sqr(x) + sqr(y))

operator fun Point.plus(p : Point) = Point(x + p.x, y + p.y)

val Point.norm : Point
    get() = Point(x / abs, y / abs)

operator fun Point.minus(p : Point) = Point(x - p.x, y - p.y)
operator fun Point.times(length : Double) = Point(x * length, y * length)
operator fun Point.div(factor : Double) = Point(x / factor, y / factor)
fun Point.plus(length : Double) = Point(x + length, y + length)
fun Point.minus(length : Double) = Point(x - length, y - length)

fun polarPoint(r : Double, angle : Double) = Point(r * cos(angle), r * sin(angle))

val Point.angle : Double
    get() = atan2(y, x)

val random = Random()
fun randomPoint(maxX : Int, maxY : Int) = Point(random.nextInt(maxX), random.nextInt(maxY))

////////////////////////

class Segment(val a : Point, val b : Point)

fun Point.to(other : Point) = Segment(this, other)

fun Segment.ofLength(length : Double) = Segment(a, a + (b - a).norm * length)

fun Segment.rotateAroundB(angle : Double) = Segment(b + ((a - b) rotate angle), b)

fun randomSegment(maxX : Int, maxY : Int, minR : Int, maxR : Int) : Segment{
    val a = randomPoint(maxX, maxY)
    val ang = random.nextDouble() * 2 * PI
    val r = minR + random.nextDouble() * (maxR - minR)
    return Segment(a, a + polarPoint(r, ang))

}

fun Segment.toPoint() = b - a

val Segment.angle : Double
    get() = toPoint().angle

val Segment.abs : Double
    get() = toPoint().abs

val Segment.middle : Point
    get() = a + toPoint() / 2.0

fun Segment.ofAngle(ang : Double) = Segment(a, a + polarPoint(abs, ang))

///////////////////////

///////////////////////

fun Graphics.drawCircle(center : Point, r : Double) {
    val intR = (r * 2).toInt()
    drawOval((center.x - r).toInt(), (center.y - r).toInt(), intR, intR)
}

fun Graphics.fillCircle(center : Point, r : Double) {
    val intR = (r * 2).toInt()
    fillOval((center.x - r).toInt(), (center.y - r).toInt(), intR, intR)
}

fun Graphics.drawLine(a : Point, b : Point) {
    drawLine(a.x.toInt(), a.y.toInt(), b.x.toInt(), b.y.toInt())
}

fun Graphics.drawLine(s : Segment) {
    drawLine(s.a, s.b)
}

fun Graphics.drawBox(a: Point, b: Point) {
    drawLine(a, Point(b.x, a.y))
    drawLine(Point(b.x, a.y), b)
    drawLine(b, Point(a.x.toInt(), b.y.toInt()))
    drawLine(Point(a.x.toInt(), b.y.toInt()), a)
}

fun sqr(d : Double) = d * d
val Double.deg : Double
    get() = this * PI / 180.0

val Int.deg : Double
    get() = this.toDouble().deg

val Double.i : Int
    get() = toInt()

fun Double.toDeg() = (this * 180.0 / PI).i