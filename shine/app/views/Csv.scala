/*
package views

import play.api.http.ContentTypeOf
import play.api.mvc.Codec
import play.api.templates.BufferedContent
import play.templates.Format

class Csv(buffer: StringBuilder) extends BufferedContent[Csv](buffer) {
  val contentType = Csv.contentType
}

object Csv {
  val contentType = "text/csv"
  implicit def contentTypeCsv(implicit codec: Codec): ContentTypeOf[Csv] = ContentTypeOf[Csv](Some(Csv.contentType))

  def apply(text: String): Csv = new Csv(new StringBuilder(text))
  
  def empty: Csv = new Csv(new StringBuilder)
}

object CsvFormat extends Format[Csv] {
  def raw(text: String): Csv = Csv(text)
  def escape(text: String): Csv = {
    val sb = new StringBuilder(text.length)
    text.foreach {
      case '"' => sb.append("\"\"")
      case c => sb += c
    }
    new Csv(sb)
  }
}
*/