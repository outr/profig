package profig

class Json(val value: ujson.Value) extends AnyVal {
  def as[T: Reader]: T = read[T](value)
  def get(path: String*): Option[Json] = if (path.isEmpty) {
    Some(this)
  } else {
    val head = path.head
    value.objOpt match {
      case Some(map) if map.contains(head) => new Json(map(head)).get(path.tail: _*)
      case _ => None
    }
  }
  def apply(path: String*): Json = get(path: _*).getOrElse(throw new RuntimeException(s"Path not found: ${path.mkString(".")}"))
  def obj(path: String*): Json = if (path.isEmpty) {
    this
  } else {
    val head = path.head
    value.objOpt match {
      case Some(map) if map.contains(head) => new Json(map(head)).obj(path.tail: _*)
      case Some(map) => {
        val child = Json()
        map += head -> child.value
        child.obj(path.tail: _*)
      }
      case None => {
        val child = Json()
        child.value.obj += head -> child.value
        child.value.obj += "value" -> value
        child.obj(path.tail: _*)
      }
    }
  }
  def set[T: Writer](value: T, path: String*): Unit = {
    val parentPath = path.dropRight(1)
    val o = obj(parentPath: _*)
    val json = writeJs(value)
    if (o.value.objOpt.isEmpty) {
      upgradeToObj(parentPath: _*)
      set[T](value, path: _*)
    } else if (path.nonEmpty) {
      try {
        o.value.obj += path.last -> json
      } catch {
        case t: Throwable => throw new RuntimeException(s"Failed to write: $value ($path) - ${o.value} - ${this.value}", t)
      }
    }
  }
  private def upgradeToObj(path: String*): Unit = {
    val key = path.last
    val parentPath = path.dropRight(1)
    if (obj(parentPath: _*).value.objOpt.isEmpty) {
      upgradeToObj(path.dropRight(1): _*)
    }
    val parent = obj(parentPath: _*).value
    val existing = parent(key)
    if (existing.objOpt.isEmpty) {
      parent(key) = ujson.Obj("value" -> existing)
    }
  }
  def merge[T: Writer](value: T, path: String*): Unit = {
    def recurse(v: ujson.Value, path: List[String]): Unit = v.objOpt match {
      case Some(map) => map.foreach {
        case (key, value) => recurse(value, path ::: List(key))
      }
      case None => set(v, path: _*)
    }
    recurse(writeJs(value), path.toList)
  }
  def defaults[T: Writer](value: T, path: String*): Unit = {
    def recurse(v: ujson.Value, path: List[String]): Unit = v.objOpt match {
      case Some(map) => map.foreach {
        case (key, value) => recurse(value, path ::: List(key))
      }
      case None => if (get(path: _*).isEmpty) {
        set(v, path: _*)
      }
    }
    recurse(writeJs(value), path.toList)
  }
  def remove(path: String*): Unit = {
    val o = obj(path.dropRight(1): _*)
    o.value.obj -= path.last
  }
  def copy(): Json = Json.parse(toString)

  override def toString: String = value.render()
}

object Json {
  def Null: Json = new Json(ujson.Null)

  def apply(): Json = new Json(ujson.Obj())
  def obj(tuples: (String, Json)*): Json = {
    val obj = ujson.Obj()
    tuples.foreach {
      case (key, json) => obj.value += key -> json.value
    }
    new Json(obj)
  }

  def string(s: String): Json = new Json(ujson.Str(s))

  def parse(json: String): Json = new Json(read[ujson.Value](json))
}