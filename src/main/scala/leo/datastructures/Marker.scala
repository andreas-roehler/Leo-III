package leo.datastructures


/////////////////////////////////////////////
// Collection of potentially globally used
// markers/properties.
/////////////////////////////////////////////

/**
 * Formula roles as described by TPTP.
 *
 * @author Alexander Steen
 * @since 11.11.2014
 * @see [[http://www.cs.miami.edu/~tptp/TPTP/SyntaxBNF.html]]
 */
sealed abstract class Role extends Pretty

/**
 * `Role_Axiom`s are accepted, without proof. There is no guarantee that the
 * axioms of a problem are consistent.
 */
case object Role_Axiom extends Role {
  final val pretty = "axiom"
}

/**
 * `Role_Definition`s are intended to define symbols. They are either universally
 * quantified equations, or universally quantified equivalences with an
 * atomic lefthand side.
 */
case object Role_Definition extends Role {
  final val pretty = "definition"
}

/**
 * `Role_Conjecture`s are to be proven from the "axiom"(-like) formulae. A problem
 * is solved only when all "conjecture"s are proven.
 */
case object Role_Conjecture extends Role {
  final val pretty = "conjecture"
}

/**
 * `Negated_Conjecture`s are formed from negation of a "conjecture" (usually
 * in a FOF to CNF conversion).
 */
case object Role_NegConjecture extends Role {
  final val pretty = "negated_conjecture"
}

/**
 * `Role_Type`s define the type globally for one symbol.
 */
case object Role_Type extends Role {
  final val pretty = "type"
}

/**
 * `Role_Plain`s have no specified user semantics.
 */
case object Role_Plain extends Role {
  final val pretty = "plain"
}


//////////////////////////////////////////////
//////////////////////////////////////////////


abstract sealed class ClauseOrigin extends Ordered[ClauseOrigin] {
  protected[ClauseOrigin] def priority: Int

  def compare(that: ClauseOrigin) = this.priority - that.priority
}
case object FromAxiom extends ClauseOrigin { val priority = 3 }
case object FromConjecture extends ClauseOrigin { val priority = 2 }
case object Derived extends ClauseOrigin { val priority = 1 }


//////////////////////////////////////////////
//////////////////////////////////////////////


abstract sealed class Indexing
case object INDEXED extends Indexing
case object PLAIN extends Indexing


//////////////////////////////////////////////
//////////////////////////////////////////////


abstract sealed class Locality
case object GLOBAL extends Locality
case object LOCAL extends Locality


//////////////////////////////////////////////
//////////////////////////////////////////////


/**
 * Marker type for the 'language level' of terms.
 * A Term is flagged `PROPOSITIONAL` iff it is a propositional formula,
 * analogously for `FIRSTORDER` and `HIGHERORDER`.
 *
 * @author Alexander Steen
 */
sealed abstract class LangOrder extends Ordered[LangOrder]

case object PROPOSITIONAL extends LangOrder {
  def compare(that: LangOrder) = that match {
    case PROPOSITIONAL => 0
    case _ => -1
  }
}

case object FIRSTORDER extends LangOrder {
  def compare(that: LangOrder) = that match {
    case PROPOSITIONAL => 1
    case FIRSTORDER => 0
    case HIGHERORDER => -1
  }
}

case object HIGHERORDER extends LangOrder {
  def compare(that: LangOrder) = that match {
    case HIGHERORDER => 0
    case _ => 1
  }
}


//////////////////////////////////////////////
//////////////////////////////////////////////