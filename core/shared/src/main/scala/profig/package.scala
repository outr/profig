import java.util.Properties

import scala.language.implicitConversions

package object profig extends upickle.AttributeTagged with PlatformPickler {
  implicit def properties2JSON(properties: Properties): Json = ProfigUtil.properties2Json(properties)
  implicit def args2JSON(args: Seq[String]): Json = ProfigUtil.args2Json(args)
  implicit def json2Value(json: Json): ujson.Value = json.value

  override implicit def OptionWriter[T: Writer]: Writer[Option[T]] =
    implicitly[Writer[T]].comap[Option[T]] {
      case None => null.asInstanceOf[T]
      case Some(x) => x
    }

  override implicit def OptionReader[T: Reader]: Reader[Option[T]] = {
    new Reader.Delegate[Any, Option[T]](implicitly[Reader[T]].map(Some(_))){
      override def visitNull(index: Int): Option[T] = None
    }
  }
}
