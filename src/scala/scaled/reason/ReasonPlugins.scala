//
// Scaled Reason Mode - a Scaled major mode for editing Reason code
// http://github.com/scaled/reason-mode/blob/master/LICENSE

package scaled.project

import com.eclipsesource.json._
import java.nio.file.{Files, Path, Paths}
import scaled._

object ReasonPlugins {

  val BsFile = "bsconfig.json"
  val NodeModules = "node_modules"
  val LsModule = "reason-language-server"

  @Plugin(tag="project-root")
  class BuckeScriptRootPlugin extends RootPlugin.File(BsFile)

  @Plugin(tag="project-resolver")
  class BuckleScriptResolverPlugin extends ResolverPlugin {

    override def metaFiles (root :Project.Root) = Seq(root.path.resolve(BsFile))

    def addComponents (project :Project) {
      val rootPath = project.root.path
      val bsFile = rootPath.resolve(BsFile)
      val config = Json.parse(Files.newBufferedReader(bsFile)).asObject

      val projName = Option(config.get("name")).map(_.asString).
        getOrElse(rootPath.getFileName.toString)

      val sb = Ignorer.stockIgnores
      sb += Ignorer.ignorePath(Paths.get("node_modules"), project.root.path)
      Option(config.get("ignore")).map(_.asArray).foreach { ignores =>
        // TODO: handle glob ignores properly
        ignores.map(_.asString).foreach { sb += Ignorer.ignoreName(_) }
      }
      project.addComponent(classOf[Filer], new DirectoryFiler(project, sb))

      // TODO: is this an array if there's more than one source dir?
      val sourceDir = Option(config.get("sources")).map(_.asString).getOrElse("src")
      val sourceDirs = Seq(sourceDir).map(rootPath.resolve(_))
      project.addComponent(classOf[Sources], new Sources(sourceDirs))

      val oldMeta = project.metaV()
      project.metaV() = oldMeta.copy(name = projName)
    }
  }

  @Plugin(tag="langserver")
  class ReasonLangPlugin extends LangPlugin {
    def suffs (root :Project.Root) = Set("re")
    def canActivate (root :Project.Root) =
      Files.exists(root.path.resolve(BsFile)) &&
      Files.exists(root.path.resolve(NodeModules).resolve(LsModule))
    def createClient (proj :Project) = Future.success(
      new ReasonLangClient(proj.metaSvc, proj.root.path, serverCmd(proj.root.path)))
  }

  private def serverCmd (root :Path) :Seq[String] = {
    val path = Seq(NodeModules, LsModule, "lib", "bs", "native", "bin.native")
    val binPath = path.foldLeft(root) { _ resolve _ }
    Seq(binPath.toString)
  }
}

class ReasonLangClient (msvc :MetaService, root :Path, serverCmd :Seq[String])
    extends LangClient(msvc, root, serverCmd) {

  override def name = "Reason"
}
