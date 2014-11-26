package leo.datastructures

import leo.datastructures.term.Term

/**
 * A `TermBank` is a special term `Factory` that caches previously
 * created terms. Each term is only created once and reused for each
 * invocation of factory methods.
 * Unshared (local) terms can be created using the `local` factory.
 *
 * @author Alexander Steen
 * @since 20.08.2014
 */
trait TermBank extends TermFactory {
  /** Return the factory for local terms, that is, terms that are not globally shared */
  def local: TermFactory

  /** Insert (unshared) terms to the term bank. Has no effect on already shared terms.
    * Returns the syntactically equal but now shared term. */
  def insert(term: Term): Term = term.locality match {
    case LOCAL => insert0(term)
    case GLOBAL => term
  }
  protected[datastructures] def insert0(localTerm: Term): Term

  /** Clear the term bank, i.e. delete all cached terms */
  def reset(): Unit
}
