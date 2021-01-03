package profig

import upickle.default._

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
//      case None if path.tail.isEmpty => new Json(value)
      case None => {
        val child = Json()
        child.value.obj += head -> child.value
        child.value.obj += "value" -> value
        child.obj(path.tail: _*)
      }
//      case None => throw new RuntimeException(s"Value in path expected as object but received: $value ($head)")
    }
  }
  def set[T: Writer](value: T, path: String*): Unit = {
    val parentPath = path.dropRight(1)
    val o = obj(parentPath: _*)
    val json = writeJs(value)
    if (o.value.objOpt.isEmpty) {
      upgradeToObj(parentPath: _*)
      set[T](value, path: _*)
    } else {
      try {
        o.value.obj += path.last -> json
      } catch {
        case t: Throwable => throw new RuntimeException(s"Failed to write: $value ($path) - ${o.value} - ${this.value}")
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
    val o = new Json(writeJs(value))
    if (o.value.objOpt.isEmpty) {
      set[T](value, path: _*)   // Not an object, so we can't merge, just replace
    } else {
      val parent = obj(path: _*)
      val parentObj = parent.value.obj
      o.value.obj.foreach {
        case (key, value) => parentObj += key -> value
      }
    }
  }
  def defaults[T: Writer](value: T, path: String*): Unit = {
    val o = new Json(writeJs(value))
    if (o.value.objOpt.isEmpty) {
      set[T](value, path: _*)   // Not an object, so we can't merge, just replace
    } else {
      val parent = obj(path: _*)
      val parentObj = parent.value.obj
      o.value.obj.foreach {
        case (key, value) => if (!parentObj.contains(key)) {
          parentObj += key -> value
        }
      }
    }
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

  /*def apply(value: ujson.Value): Json = {
    println(s"1: $value")
    new Json(value)
  }
  def apply[T: Writer](value: T): Json = {
    println(s"2: $value")
    apply(writeJs(value))
  }*/
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