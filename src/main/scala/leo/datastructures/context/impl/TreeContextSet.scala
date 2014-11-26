package leo
package datastructures.context
package impl

import scala.collection._



/**
 *
 * Implementation of a {@see ContextSet} that mimics the tree structure
 * of the contexts itself to obtain the different context elements.
 * The elements are hereby saved exactly once. Looking through the
 * complete context will require to walk backwards through the graph
 * and collect the elements.
 *
 *
 * @author Max Wisniewski
 * @since 11/24/14
 */
class TreeContextSet[A] extends ContextSet[A] {

  /**
   * A map from context to the sets of elements. Since pointers
   * are stored in the contexts, there is no need to implement
   * the tree again.
   */
  private val contextSets : mutable.Map[Context,mutable.Set[A]] = new mutable.HashMap[Context,mutable.Set[A]] with mutable.SynchronizedMap[Context,mutable.Set[A]]

  /**
   * Parses from a context the complete sequence of contexts
   * from the root context to the given one, if one exists.
   *
   * @param c - The node we want to reach
   * @return Path from the root context to c, in this direction.
   */
  protected def getPath(c : Context) : Seq[Context] = {
    var res = List(c)
    var akk : Context = c
    while(akk.parentContext != null) {
      res = akk.parentContext :: res
      akk = akk.parentContext
    }
    if(res.head.contextID != Context().contextID) {
      Out.severe(s"Deattached head context found contextID=${akk.contextID} reached from contextID=${c.contextID}. (Path = ${pathToString(res)}) (Root =${Context().contextID})")
    }
    res
  }

  private def pathToString(c : List[Context]) : String = c.map(_.contextID).mkString(" , ")

  /**
   * Checks if an element `a` is contained in a given context.
   *
   * @param a - The element to check
   * @param c - The context to check
   * @return true, iff a is contained in c
   */
  override def contains(a: A, c: Context): Boolean = getPath(c) exists {c1 => contextSets.get(c1).fold(false)(_.contains(a))}

  /**
   * Clears a context and all its sub contexts of all elements.
   * @param c
   */
  override def clear(c: Context): Unit = ???

  /**
   * Resets the data structure to an initial state
   */
  override def clear(): Unit = {
    contextSets.clear()
  }

  /**
   *
   * Removes an element form the context c
   *
   * @param a - The element to be removed
   * @param c - The context in which it is removed
   * @return true, iff deletion was successful
   */
  override def remove(a: A, c: Context): Boolean = {
    val p = getPath(c).iterator

    while(p.hasNext) {
      val n = p.next()
      contextSets.get(n) match {
        case Some(s) => if(s.contains(a)) {s.remove(a); return true}
        case None  => ()
      }
    }
    return false
  }

  /**
   * Returns a sequence of all context c, in which a is contained
   *
   * @param a - Element to be searched
   * @return All containing contexts
   */
  override def inContext(a: A): Iterable[Context] = contextSets.filter{case (_,s) => s.contains(a)}.keys

  /**
   *
   * Inserts an element `a` into the context c.
   *
   * @param a - The element to insert
   * @param c - The context to insert
   * @return true, iff insertion was successful
   */
  override def add(a: A, c: Context): Boolean = {
    (contextSets.get(c) match {
      case Some(s) => s
      case None =>
          val s = new mutable.HashSet[A] with mutable.SynchronizedSet[A]
          contextSets.put(c,s)
          s
    }).add(a)
  }

  /**
   * Returns a set of all elements in the context c.
   *
   * @param c - The context of the elements
   * @return All elements in c
   */
  override def getAll(c: Context): Set[A] = contextSets.filter{case (c1,_) => getPath(c).contains(c1)}.values.toSet.flatten
}