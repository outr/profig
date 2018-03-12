package javax.xml.parsers

object SAXParserFactory {
  def newInstance(): SAXParserFactory = new SAXParserFactory
}

class SAXParserFactory {
  def setNamespaceAware(awareness: Boolean): Unit = {}
  def newSAXParser(): SAXParser = ???
}