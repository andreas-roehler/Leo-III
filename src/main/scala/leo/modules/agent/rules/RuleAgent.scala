package leo.modules.agent.rules

import leo.agents.{Agent, Task}
import leo.datastructures.blackboard._

/**
  *
  * Agent to execute the [[Rule]] interface.
  * Used to instanciate ground inference rules
  * and composed rules.
  *
  * @author Max Wisniewski
  * @since 10/25/16
  */
class RuleAgent(rule : Rule)(implicit blackboard : Blackboard) extends Agent {
  override val name: String = s"RuleAgent(${rule.name})"
  private val rthis = this
  override def kill(): Unit = {}
  override def interest: Option[Seq[DataType[Any]]] = Some(rule.inTypes)
  override def filter(event: Event): Iterable[Task] = event match {
    case r : Delta =>
      val hints = rule.canApply(r)
      hints map (x => new RuleTask(x))
    case _ => Nil
  }
  override def init(): Iterable[Task] = {
    val res = Result()
    for (t <- rule.inTypes){
      val data = blackboard.getData(t)
      data foreach {d => res.insert(t)(d)}
    }
    val hints = rule.canApply(res)
    hints map (x => new RuleTask(x))
  }

  override def maxMoney: Double = Double.MaxValue
  override def taskFinished(t: Task): Unit = {}
  override def taskChoosen(t: Task): Unit = {}
  override def taskCanceled(t: Task): Unit = {}

  class RuleTask(h : Hint) extends Task {
    override val name: String = s"RuleTask[${rule.name}]"
    override lazy val run: Delta = h.apply()
    override val readSet: Map[DataType[Any], Set[Any]] = h.read
    override val writeSet: Map[DataType[Any], Set[Any]] = h.write
    override def bid: Double = 0.3  // TODO External Source to tweak search
    override val getAgent: Agent = rthis
    override lazy val pretty: String =
      s"${name} :\n  Read ${h.read.map{case (ty, data) => s"${ty}:\n   ${data.mkString("\n   ")}"}}\n  Write ${h.read.map{case (ty, data) => s"${ty}:\n   ${data.mkString("\n   ")}"}}"
  }
}
