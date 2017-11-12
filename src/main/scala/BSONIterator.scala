package reactivemongo.api.bson

import scala.util.{ Failure, Success, Try }

private[bson] object BSONIterator {
  def pretty(i: Int, it: Iterator[Try[BSONElement]], f: String => String = { name => s""""${name}": """ }): String = {
    val indent = (0 to i).map { i => "  " }.mkString("")

    it.map {
      case Success(BSONElement(name, value)) => {
        val prefix = s"${indent}${f(name)}"

        value match {
          case array: BSONArray => s"${prefix}[\n" + pretty(i + 1, array.elements.map(Success(_)).iterator, _ => "") + s"\n${indent}]"

          case BSONBoolean(b) =>
            s"${prefix}$b"

          case BSONDocument(elements) =>
            s"${prefix}{\n" + pretty(i + 1, elements.iterator) + s"\n$indent}"

          case BSONDouble(d) =>
            s"""${prefix}$d"""

          case BSONInteger(i) =>
            s"${prefix}$i"

          case BSONLong(l) =>
            s"${prefix}NumberLong($l)"

          case d @ BSONDecimal(_, _) =>
            s"${prefix}NumberDecimal($d)"

          case BSONString(s) =>
            prefix + '"' + s.replaceAll("\"", "\\\"") + '"'

          case oid @ BSONObjectID(_) =>
            s"${prefix}Object(${oid.stringify})"

          case ts @ BSONTimestamp(_) =>
            s"${prefix}Timestamp(${ts.time}, ${ts.ordinal})"

          case BSONUndefined => s"${prefix}undefined"
          case BSONMinKey    => s"${prefix}MinKey"
          case BSONMaxKey    => s"${prefix}MaxKey"

          case _ =>
            s"${prefix}$value"
        }
      }

      case Failure(e) => s"${indent}ERROR[${e.getMessage()}]"
    }.mkString(",\n")
  }

  /** Makes a pretty String representation of the given iterator of BSON elements. */
  def pretty(it: Iterator[Try[BSONElement]]): String = "{\n" + pretty(0, it) + "\n}"
}
