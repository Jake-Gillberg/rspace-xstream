import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectOutputStream}

import com.thoughtworks.xstream.XStream
import coop.rchain.rspace.Serialize
import coop.rchain.rspace.util.withResource
import cats.syntax.either._

package object rxspace {

  def makeSerializeFromSerializable[T <: Serializable](implicit xstream: XStream = new XStream()) = new Serialize[T] {

    override def encode(a: T): Array[Byte] =
      withResource(new ByteArrayOutputStream) { baos =>
        withResource(xstream.createObjectOutputStream(baos)) { (oos: ObjectOutputStream) =>
          oos.writeObject(a)
        }
        baos.toByteArray
      }

    override def decode(bytes: Array[Byte]): Either[Throwable, T] =
      Either.catchNonFatal {
        withResource(new ByteArrayInputStream(bytes)) { bais =>
          withResource(xstream.createObjectInputStream(bais)) { ois =>
            ois.readObject.asInstanceOf[T]
          }
        }
      }
  }

  implicit def serializableToSerialize[T <: Serializable](implicit xstream: XStream = new XStream()): Serialize[T] =
      makeSerializeFromSerializable[T](xstream)


}
