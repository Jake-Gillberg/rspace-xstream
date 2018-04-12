package rxspace.examples

import java.nio.file.{Files, Path}

import coop.rchain.rspace._
import rxspace._
import coop.rchain.rspace.extended.runKs

object AddressBookExample {

  /* Here we define a type for channels */

  case class Channel(name: String)

  /* Here we define a type for data */

  case class Name(first: String, last: String)
  case class Address(street: String, city: String, state: String, zip: String)
  case class Entry(name: Name, address: Address, email: String, phone: String)

  /* Here we define a type for patterns */

  sealed trait Pattern                       extends Product with Serializable
  final case class NameMatch(last: String)   extends Pattern
  final case class CityMatch(city: String)   extends Pattern
  final case class StateMatch(state: String) extends Pattern

  /* Here we define a type for continuations */

  class Printer extends ((List[Entry]) => Unit) with Serializable {

    def apply(entries: List[Entry]): Unit =
      entries.foreach {
        case Entry(name, address, email, phone) =>
          val nameStr = s"${name.last}, ${name.first}"
          val addrStr = s"${address.street}, ${address.city}, ${address.state} ${address.zip}"
          Console.printf(s"""|
                             |=== ENTRY ===
                             |name:    $nameStr
                             |address: $addrStr
                             |email:   $email
                             |phone:   $phone
                             |""".stripMargin)
      }
  }

  object implicits {

    /**
      * An instance of [[Match]] for [[Pattern]] and [[Entry]]
      */
    implicit object matchPatternEntry extends Match[Pattern, Entry] {
      def get(p: Pattern, a: Entry): Option[Entry] =
        p match {
          case NameMatch(last) if a.name.last == last        => Some(a)
          case CityMatch(city) if a.address.city == city     => Some(a)
          case StateMatch(state) if a.address.state == state => Some(a)
          case _                                             => None
        }

    }
  }

  import implicits._

  // Let's define some Entries
  val alice = Entry(name = Name("Alice", "Lincoln"),
                    address = Address("777 Ford St.", "Crystal Lake", "Idaho", "223322"),
                    email = "alicel@ringworld.net",
                    phone = "787-555-1212")

  val bob = Entry(name = Name("Bob", "Lahblah"),
                  address = Address("1000 Main St", "Crystal Lake", "Idaho", "223322"),
                  email = "blablah@tenex.net",
                  phone = "698-555-1212")

  val carol = Entry(name = Name("Carol", "Lahblah"),
                    address = Address("22 Goldwater Way", "Herbert", "Nevada", "334433"),
                    email = "carol@blablah.org",
                    phone = "232-555-1212")

  def exampleOne(): Unit = {

    // Here we define a temporary place to put the store's files
    val storePath: Path = Files.createTempDirectory("rspace-address-book-example-")

    // Let's define our store
    val store: LMDBStore[Channel, Pattern, Entry, Printer] =
      LMDBStore.create[Channel, Pattern, Entry, Printer](storePath, 1024L * 1024L)

    Console.printf("\nExample One: Let's consume and then produce...\n")

    val cres =
      consume(store,
              List(Channel("friends")),
              List(CityMatch(city = "Crystal Lake")),
              new Printer,
              persist = true)

    assert(cres.isEmpty)

    val pres1 = produce(store, Channel("friends"), alice, persist = false)
    val pres2 = produce(store, Channel("friends"), bob, persist = false)
    val pres3 = produce(store, Channel("friends"), carol, persist = false)

    assert(pres1.nonEmpty)
    assert(pres2.nonEmpty)
    assert(pres3.isEmpty)

    runKs(List(pres1, pres2))

    store.close()
  }

  def exampleTwo(): Unit = {

    // Here we define a temporary place to put the store's files
    val storePath: Path = Files.createTempDirectory("rspace-address-book-example-")

    // Let's define our store
    val store: LMDBStore[Channel, Pattern, Entry, Printer] =
      LMDBStore.create[Channel, Pattern, Entry, Printer](storePath, 1024L * 1024L)

    Console.printf("\nExample Two: Let's produce and then consume...\n")

    val pres1 = produce(store, Channel("friends"), alice, persist = false)
    val pres2 = produce(store, Channel("friends"), bob, persist = false)
    val pres3 = produce(store, Channel("friends"), carol, persist = false)

    assert(pres1.isEmpty)
    assert(pres2.isEmpty)
    assert(pres3.isEmpty)

    val consumer = () =>
      consume(store,
              List(Channel("friends")),
              List(NameMatch(last = "Lahblah")),
              new Printer,
              persist = false)

    val cres1 = consumer()
    val cres2 = consumer()
    val cres3 = consumer()

    assert(cres1.isDefined)
    assert(cres2.isDefined)
    assert(cres3.isEmpty)

    runKs(List(cres1, cres2))

    Console.printf(store.toMap.toString())

    store.close()
  }
}
