package leo.modules.agent.rules

import leo.LeoTestSuite
import leo.agents.{AbstractAgent, Task}
import leo.datastructures.blackboard._
import leo.datastructures.blackboard.scheduler.Scheduler

import scala.collection.mutable

/**
  * Created by mwisnie on 12/8/16.
  */
class SimpleRuleTest extends LeoTestSuite {
  test("Prepend: a"){
    val self = this
    val store = new SimpleStore
    val prep = new PrependRule("a", store)

    store.strings.add("hallo")
    store.strings.add("test")
    store.strings.add("nice")

    Blackboard().addDS(store)
    val prepAgent = new RuleAgent(prep)
    prepAgent.register()

    new AbstractAgent {
      override val interest : Option[Seq[DataType]] = None
      override def init(): Iterable[Task] = Seq()
      override def filter(event: Event): Iterable[Task] = event match{
        case _ : DoneEvent => self.synchronized(self.notifyAll()); Seq()
        case _ => Seq()
      }

      override def name: String = "termination"
    }.register()

    Scheduler().signal()
    self.synchronized(self.wait())
    println(s"${store.strings.mkString(",")}")
  }

  test("Append: b"){
    val self = this
    val store = new SimpleStore
    val prep = new AppendRule("b", store)

    store.strings.add("hallo")
    store.strings.add("test")
    store.strings.add("nice")

    Blackboard().addDS(store)
    val prepAgent = new RuleAgent(prep)
    prepAgent.register()

    new AbstractAgent {
      override val interest : Option[Seq[DataType]] = None
      override def init(): Iterable[Task] = Seq()
      override def filter(event: Event): Iterable[Task] = event match{
        case _ : DoneEvent => self.synchronized(self.notifyAll()); Seq()
        case _ => Seq()
      }

      override def name: String = "termination"
    }.register()

    Scheduler().signal()
    self.synchronized(self.wait())
    println(s"${store.strings.mkString(",")}")
  }

  test("Pre/Append: a/b"){
    val self = this
    val store = new SimpleStore
    val prep = new PrependRule("a", store)
    val app = new AppendRule("b", store)

    store.strings.add("hallo")
    store.strings.add("test")
    store.strings.add("nice")

    Blackboard().addDS(store)
    val prepAgent = new RuleAgent(prep)
    val appAgent = new RuleAgent(app)
    prepAgent.register()
    appAgent.register()

    new AbstractAgent {
      override val interest : Option[Seq[DataType]] = None
      override def init(): Iterable[Task] = Seq()
      override def filter(event: Event): Iterable[Task] = event match{
        case _ : DoneEvent => self.synchronized(self.notifyAll()); Seq()
        case _ => Seq()
      }

      override def name: String = "termination"
    }.register()

    Scheduler().signal()
    self.synchronized(self.wait())
    println(s"${store.strings.mkString(",")}")
  }
}


case object StringType extends DataType
class SimpleStore extends DataStore {

  val strings : mutable.Set[String] = mutable.HashSet[String]()

  override val storedTypes: Seq[DataType] = Seq(StringType)
  override def updateResult(r: Result): Boolean = synchronized{
    var (del, ins) : (Seq[String], Seq[String])= r.updates(StringType).map{case (a,b) => (a.asInstanceOf[String], b.asInstanceOf[String])}.unzip
    del = del ++ r.removes(StringType).map(_.asInstanceOf[String])
    ins = ins ++ r.inserts(StringType).map(_.asInstanceOf[String])

    del foreach (strings.remove(_))
    ins foreach (strings.add(_))

    true
  }
  override def clear(): Unit = synchronized(strings.clear())
  override def all(t: DataType): Set[Any] = if(t == StringType) synchronized(strings.toSet) else Set()
}


class PrependRule(letter : String, observe : SimpleStore) extends Rule {
  override val name: String = s"Prepend(${letter})"
  override def canApply(r: Delta): Seq[Hint] = {
    if(r.isEmpty){
      synchronized{
        val undone = observe.strings.filterNot(s => s.startsWith(letter)).toSeq
        undone map {s => new ChangeStringHint(s, letter + s)}
      }
    } else {
      var (_, ins): (Seq[String], Seq[String]) = r.updates(StringType).map { case (a, b) => (a.asInstanceOf[String], b.asInstanceOf[String]) }.unzip
      ins = ins ++ r.inserts(StringType).map(_.asInstanceOf[String])
      val undone = observe.strings.filterNot(s => s.startsWith(letter)).toSeq
      undone map {s => new ChangeStringHint(s, letter + s)}
    }
  }
}

class AppendRule(letter : String, observe : SimpleStore) extends Rule {
  override val name: String = s"Append(${letter})"
  override def canApply(r: Delta): Seq[Hint] = {
    if(r.isEmpty){
      synchronized{
        val undone = observe.strings.filterNot(s => s.endsWith(letter)).toSeq
        undone map {s => new ChangeStringHint(s, s + letter)}
      }
    } else {
      var (_, ins): (Seq[String], Seq[String]) = r.updates(StringType).map { case (a, b) => (a.asInstanceOf[String], b.asInstanceOf[String]) }.unzip
      ins = ins ++ r.inserts(StringType).map(_.asInstanceOf[String])
      val undone = observe.strings.filterNot(s => s.endsWith(letter)).toSeq
      undone map {s => new ChangeStringHint(s, s + letter)}
    }
  }
}

class ChangeStringHint(olds : String, news : String) extends Hint {
  override def apply(): Result = {
    Result().update(StringType)(olds)(news)
  }
  override val read: Map[DataType, Set[Any]] = Map()
  override val write: Map[DataType, Set[Any]] = Map(StringType -> Set(olds))
}