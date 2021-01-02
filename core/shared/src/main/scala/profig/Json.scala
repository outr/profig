package profig

import upickle.default._

class Json private(val value: ujson.Value) extends AnyVal {
  def as[T: Reader]: T = read[T](value)
  def get(path: String*): Option[Json] = if (path.isEmpty) {
    Some(this)
  } else {
    val head = path.head
    value.objOpt match {
      case Some(map) if map.contains(head) => Json(map(head)).get(path.tail: _*)
      case _ => None
    }
  }
  def apply(path: String*): Json = get(path: _*).getOrElse(throw new RuntimeException(s"Path not found: ${path.mkString(".")}"))
  def obj(path: String*): Json = if (path.isEmpty) {
    this
  } else {
    val head = path.head
    value.objOpt match {
      case Some(map) if map.contains(head) => Json(map(head)).obj(path.tail: _*)
      case Some(map) => {
        val child = Json()
        map += head -> child.value
        child.obj(path.tail: _*)
      }
      case None if path.tail.isEmpty => Json(value)
      case None => throw new RuntimeException(s"Value in path expected as object but received: $value ($head)")
    }
  }
  def set[T: Writer](value: T, path: String*): Unit = {
    val o = obj(path.dropRight(1): _*)
    println(s"Setting $value, Object? $o")
    o.value.obj += path.last -> Json(value).value
  }
  def merge[T: Writer](value: T, path: String*): Unit = {
    val o = Json(value)
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
    val o = Json(value)
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
  def copy(): Json = Json(toString)

  override def toString: String = value.render()
}

object Json {
  def apply(value: ujson.Value): Json = new Json(value)
  def apply[T: Writer](value: T): Json = apply(writeJs(value))
  def apply(): Json = apply(ujson.Obj())
  def obj(tuples: (String, Json)*): Json = {
    val obj = ujson.Obj()
    tuples.foreach {
      case (key, json) => obj.value += key -> json.value
    }
    apply(obj)
  }

  def fromString(s: String): Json = Json(ujson.Str(s))

  def parse(json: String): Json = apply(read[ujson.Value](json))
}