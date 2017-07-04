package profig

trait ConfigApplication {
  def main(args: Array[String]): Unit = {
    Config.merge(args)
    run()
  }

  protected def run(): Unit
}