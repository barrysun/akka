/**
 * Copyright (C) 2009-2011 Typesafe Inc. <http://www.typesafe.com>
 */
package akka.util

import java.io.{ PrintWriter, StringWriter }
import java.util.Comparator
import scala.annotation.tailrec

/**
 * @author <a href="http://jonasboner.com">Jonas Bon&#233;r</a>
 */
object Helpers {

  def compareIdentityHash(a: AnyRef, b: AnyRef): Int = {
    /*
     * make sure that there is no overflow or underflow in comparisons, so 
     * that the ordering is actually consistent and you cannot have a 
     * sequence which cyclically is monotone without end.
     */
    val diff = ((System.identityHashCode(a) & 0xffffffffL) - (System.identityHashCode(b) & 0xffffffffL))
    if (diff > 0) 1 else if (diff < 0) -1 else 0
  }

  val IdentityHashComparator = new Comparator[AnyRef] {
    def compare(a: AnyRef, b: AnyRef): Int = compareIdentityHash(a, b)
  }

  def intToBytes(value: Int): Array[Byte] = {
    val bytes = new Array[Byte](4)
    bytes(0) = (value >>> 24).asInstanceOf[Byte]
    bytes(1) = (value >>> 16).asInstanceOf[Byte]
    bytes(2) = (value >>> 8).asInstanceOf[Byte]
    bytes(3) = value.asInstanceOf[Byte]
    bytes
  }

  def bytesToInt(bytes: Array[Byte], offset: Int): Int = {
    (0 until 4).foldLeft(0)((value, index) ⇒ value + ((bytes(index + offset) & 0x000000FF) << ((4 - 1 - index) * 8)))
  }

  final val base64chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789*?"

  @tailrec
  def base64(l: Long, sb: StringBuilder = new StringBuilder("$")): String = {
    sb += base64chars.charAt(l.toInt & 63)
    val next = l >>> 6
    if (next == 0) sb.toString
    else base64(next, sb)
  }

  def ignore[E: Manifest](body: ⇒ Unit) {
    try {
      body
    } catch {
      case e if manifest[E].erasure.isAssignableFrom(e.getClass) ⇒ ()
    }
  }

  def withPrintStackTraceOnError(body: ⇒ Unit) {
    try {
      body
    } catch {
      case e: Throwable ⇒
        val sw = new java.io.StringWriter()
        var root = e
        while (root.getCause ne null) root = e.getCause
        root.printStackTrace(new java.io.PrintWriter(sw))
        System.err.println(sw.toString)
        throw e
    }
  }
}
