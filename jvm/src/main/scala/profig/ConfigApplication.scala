package profig

trait ConfigApplication {
  def main(args: Array[String]): Unit = {
    Config.init(args)
    run()
  }

  protected def run(): Unit
}
