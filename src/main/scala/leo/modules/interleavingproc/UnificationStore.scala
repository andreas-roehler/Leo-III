package leo.modules.interleavingproc

import leo.datastructures.AnnotatedClause
import leo.datastructures.blackboard.{DataStore, DataType, Result}

import scala.collection.mutable

/**
  *
  * Stores Clauses with open Unification Constraints.
  *
  * @author Max Wisniewski
  * @since 11/17/16
  */
class UnificationStore[T <: AnnotatedClause] extends DataStore{

  private val openUnifications : mutable.Set[T] = new mutable.HashSet[T]()

  def getOpenUni : Seq[T] = openUnifications.toSeq

  /**
    * This method returns all Types stored by this data structure.
    *
    * @return all stored types
    */
  override val storedTypes: Seq[DataType] = Seq(OpenUnification)

  /**
    *
    * Inserts all results produced by an agent into the datastructure.
    *
    * @param r - A result inserted into the datastructure
    */
  override def updateResult(r: Result): Boolean = synchronized {
    val itr = r.removes(OpenUnification).iterator
    while(itr.hasNext){
      val r = itr.next().asInstanceOf[T]
      openUnifications.remove(r)
    }
    val iti = r.inserts(OpenUnification).iterator
    while(iti.hasNext){
      val i = iti.next().asInstanceOf[T]
      openUnifications.add(i)
    }

    iti.nonEmpty    // TODO Check in loop for already existence
  }

  /**
    * Removes everything from the data structure.
    * After this call the ds should behave as if it was newly created.
    */
  override def clear(): Unit = synchronized(openUnifications.clear())

  /**
    * Returns a list of all stored data.
    *
    * @param t
    * @return
    */
  override def all(t: DataType): Set[Any] = if(t == OpenUnification) synchronized(openUnifications.toSet) else Set.empty
}

case object OpenUnification extends DataType {}
