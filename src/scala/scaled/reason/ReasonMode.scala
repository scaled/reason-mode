//
// Scaled Reason Mode - a Scaled major mode for editing Reason code
// http://github.com/scaled/reason-mode/blob/master/LICENSE

package scaled.reason

import scaled._
import scaled.code.Indenter
import scaled.grammar._
import scaled.code.{CodeConfig, Commenter}

@Plugin(tag="textmate-grammar")
class ReasonGrammarPlugin extends GrammarPlugin {
  import EditorConfig._
  import CodeConfig._

  override def grammars = Map("source.reason" -> "Reason.ndf")

  override def effacers = List(
    effacer("comment.line", commentStyle),
    effacer("comment.block.string", stringStyle),
    effacer("comment.block", docStyle),
    effacer("constant", constantStyle),
    effacer("invalid", warnStyle),
    effacer("keyword", keywordStyle),
    effacer("string", stringStyle),
    effacer("variable", variableStyle),
    effacer("support.function", functionStyle),
    effacer("support.constant", constantStyle),
    effacer("support.class", typeStyle),
    effacer("support.other.module", moduleStyle),
    effacer("storage", variableStyle)
  )
}

@Major(name="reason",
       tags=Array("code", "project", "reason"),
       pats=Array(".*\\.re"),
       desc="A major mode for editing Reason code.")
class ReasonMode (env :Env) extends GrammarCodeMode(env) {

  override def dispose () {} // nada for now

  override def langScope = "source.reason"

  override def keymap = super.keymap.
    bind("self-insert-command", "'"); // don't auto-pair single quote

  // override def createIndenter() = new XmlIndenter(buffer, config)
  override val commenter = new Commenter() {
    import scaled.code.CodeConfig._

    override def linePrefix  = "//"
    override def blockOpen = "/*"
    override def blockClose = "*/"
    override def blockPrefix = "*"
    override def docPrefix   = "/**"

    // look for longer prefix first, then shorter
    override def commentDelimLen (line :LineV, col :Int) :Int = {
      if (line.matches(blockPrefixM, col)) blockPrefixM.matchLength
      else if (line.matches(linePrefixM, col)) linePrefixM.matchLength
      else 0
    }
  }
}
