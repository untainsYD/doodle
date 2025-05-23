/*
 * Copyright 2015 Creative Scala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package doodle
package image
package examples

import cats.syntax.all.*
import doodle.core.*
import doodle.random.{*, given}
import doodle.syntax.all.*

object Windswept {
  import PathElement.*

  def randomColor(meanHue: Angle) =
    for {
      hue <- Random.normal(meanHue.toDegrees, 10.0) map (_.degrees)
    } yield Color.hsl(hue, 0.8, 0.6)

  val leafGreen: Random[Color] = randomColor(80.degrees)
  val emeraldGreen: Random[Color] = randomColor(110.degrees)

  def square(fill: Color): Image =
    Image.rectangle(25, 25) fillColor fill strokeWidth 0.0

  def randomSquare(fill: Random[Color]): Random[Image] =
    (fill, fill, fill, fill) mapN { (f1, f2, f3, f4) =>
      (square(f1) beside square(f2)) above (square(f3) beside square(f4))
    }

  val leafSquare = randomSquare(leafGreen)
  val emeraldSquare = randomSquare(emeraldGreen)

  def randomColumn(
      one: Random[Image],
      two: Random[Image],
      three: Random[Image],
      four: Random[Image],
      five: Random[Image]
  ): Random[Image] =
    (one, two, three, four, five) mapN { _ above _ above _ above _ above _ }

  val columnOne =
    randomColumn(
      emeraldSquare,
      emeraldSquare,
      emeraldSquare,
      emeraldSquare,
      emeraldSquare
    )

  val columnTwo =
    randomColumn(
      emeraldSquare,
      emeraldSquare,
      leafSquare,
      emeraldSquare,
      emeraldSquare
    )

  val columnThree =
    randomColumn(
      emeraldSquare,
      leafSquare,
      emeraldSquare,
      leafSquare,
      emeraldSquare
    )

  val columnFour =
    randomColumn(
      leafSquare,
      emeraldSquare,
      emeraldSquare,
      emeraldSquare,
      leafSquare
    )

  val singleRepeat: Random[Image] =
    (
      columnOne,
      columnTwo,
      columnThree,
      columnFour,
      columnThree,
      columnTwo
    ) mapN {
      _ beside _ beside _ beside _ beside _ beside _
    }

  val pattern: Random[Image] =
    (singleRepeat, singleRepeat, singleRepeat, columnOne) mapN {
      _ beside _ beside _ beside _
    }

  def randomPoint(x: Random[Double], y: Random[Double]): Random[Point] =
    (x, y) mapN { (x, y) =>
      Point.cartesian(x, y)
    }

  def normalPoint(point: Point, stdDev: Double = 50): Random[Point] =
    randomPoint(Random.normal(point.x, stdDev), Random.normal(point.y, stdDev))

  def randomBezier(
      cp1: Point,
      cp2: Point,
      end: Point,
      stdDev: Double
  ): Random[BezierCurveTo] = {
    (
      normalPoint(cp1, stdDev),
      normalPoint(cp2, stdDev),
      normalPoint(end, stdDev)
    ) mapN { (cp1, cp2, end) =>
      BezierCurveTo(cp1, cp2, end)
    }
  }

  val tendril: Random[Image] =
    for {
      stroke <- randomColor(25.degrees) map (_.fadeOut(0.4.normalized))
      offset = -425
      start = Point.cartesian(offset.toDouble, 0)
      end <- Random.normal(800, 30)
    } yield Image.path(
      OpenPath.empty.moveTo(start).lineTo(Point.cartesian(end + offset, 0))
    ) strokeColor stroke strokeWidth 1.0

  val tendrils: Random[Image] =
    (-50 to 50).foldLeft(tendril) { (randomImage, i) =>
      for {
        accum <- randomImage
        t <- tendril
      } yield (t.at(0, i.toDouble)).on(accum)
    }

  def image =
    (for {
      t1 <- tendrils
      t2 <- tendrils
      t3 <- tendrils
      p1 <- pattern
      p2 <- pattern
      p3 <- pattern
    } yield (t1 on p1) above (t2 on p2) above (t3 on p3)).run
}
