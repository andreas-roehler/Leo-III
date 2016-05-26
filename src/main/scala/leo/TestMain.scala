package leo

import leo.datastructures.ClauseProxy
import leo.datastructures.blackboard.Blackboard
import leo.datastructures.blackboard.impl.{FormulaDataStore, SZSDataStore}
import leo.datastructures.blackboard.scheduler.Scheduler
import leo.datastructures.context.{BetaSplit, Context}
import leo.datastructures.tptp.Commons.AnnotatedFormula
import leo.modules.agent.preprocessing.{ArgumentExtractionAgent, EqualityReplaceAgent, FormulaRenamingAgent, NormalizationAgent}
import leo.modules.agent.relevance_filter.BlackboardPreFilterSet
import leo.modules.relevance_filter.{PreFilterSet, SeqFilter}
import leo.modules.{CLParameterParser, Parsing, SZSOutput}
import leo.modules.external.ExternalCall
import leo.modules.output.{SZS_Theorem, SZS_Timeout, StatusSZS, ToTPTP}
import leo.modules.phase._
import leo.modules.Utility
import leo.datastructures.impl.Signature
import leo.modules.seqpproc.MultiSeqPProc

/**
  * Created by mwisnie on 3/7/16.
  */
object TestMain {
  def main(args : Array[String]): Unit ={
    try {
      Configuration.init(new CLParameterParser(args))
    } catch {
      case e: IllegalArgumentException => {
        Out.severe(e.getMessage)
        return
      }
    }

    val startTime : Long = System.currentTimeMillis()

    val loadphase = new LoadPhase(Configuration.PROBLEMFILE)
    val filterphase = new FilterPhase()


    Blackboard().addDS(FormulaDataStore)
    Blackboard().addDS(BlackboardPreFilterSet)
    Blackboard().addDS(SZSDataStore)

    printPhase(loadphase)
    if(!loadphase.execute()) {
      Scheduler().killAll()
      unexpectedEnd(System.currentTimeMillis() - startTime)
      return
    }

    val afterParsing = System.currentTimeMillis()
    val timeWOParsing : Long = afterParsing - startTime

    printPhase(filterphase)
    if(!filterphase.execute()){
      Scheduler().killAll()
      unexpectedEnd(System.currentTimeMillis()-startTime)
      return
    }

    val timeForFilter : Long = System.currentTimeMillis() - afterParsing
    leo.Out.info(s"Filter Time : ${timeForFilter}ms")

    leo.Out.info("Used :")
    leo.Out.info(FormulaDataStore.getFormulas.map(_.pretty).mkString("\n"))
    leo.Out.info("Unused : ")
    leo.Out.info(PreFilterSet.getFormulas.mkString("\n"))


    val searchPhase = new MultiSearchPhase(MultiSeqPProc)

    printPhase(searchPhase)
    if(!searchPhase.execute()){
      Scheduler().killAll()
      unexpectedEnd(System.currentTimeMillis() - startTime)
      return
    }

    val endTime = System.currentTimeMillis()
    val time = System.currentTimeMillis() - startTime
    Scheduler().killAll()

    val szsStatus : StatusSZS = SZSDataStore.getStatus(Context()).fold(SZS_Timeout : StatusSZS){x => x}
    Out.output("")
    Out.output(SZSOutput(szsStatus, Configuration.PROBLEMFILE, s"${time} ms resp. ${endTime - afterParsing} ms w/o parsing"))

    val proof = FormulaDataStore.getAll(_.cl.lits.isEmpty).headOption    // Empty clause suchen
    if (szsStatus == SZS_Theorem && Configuration.PROOF_OBJECT && proof.isDefined) {
      Out.comment(s"SZS output start CNFRefutation for ${Configuration.PROBLEMFILE}")
      //      Out.output(makeDerivation(derivationClause).drop(1).toString)
      Out.output(Utility.userConstantsForProof(Signature.get))
      Utility.printProof(proof.get)
      Out.comment(s"SZS output end CNFRefutation for ${Configuration.PROBLEMFILE}")
    }
  }

  private def unexpectedEnd(time : Long) {
    val szsStatus : StatusSZS = SZSDataStore.getStatus(Context()).fold(SZS_Timeout : StatusSZS){x => x}
    Out.output("")
    Out.output(SZSOutput(szsStatus, Configuration.PROBLEMFILE, s"${time} ms"))
  }

  private def printPhase(p : Phase) = {
    Out.debug(" ########################")
    Out.debug(s" Starting Phase ${p.name}")
    Out.debug(p.description)
  }
}