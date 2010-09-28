package scalacl

import scala.collection.immutable.Stack
import scala.reflect.generic.Names
import scala.reflect.generic.Trees
import scala.tools.nsc.Global
import scala.tools.nsc.plugins.Plugin
import scala.tools.nsc.symtab.Definitions

/**
 * http://www.scala-lang.org/node/140
 * http://lamp.epfl.ch/~emir/bqbase/2005/06/02/nscTutorial.html
 * http://code.google.com/p/simple-build-tool/wiki/CompilerPlugins
 * mvn scala:run -DmainClass=scalacl.Compile "-DaddArgs=-d|out|src/main/examples/BasicExample.scala|-Xprint:scalaclfunctionstransform"
 * scala -cp target/scalacl-compiler-1.0-SNAPSHOT-shaded.jar scalacl.Main -d out src/examples/BasicExample.scala
 * javap -c -classpath out/ scalacl.examples.BasicExample
 */
class ScalaCLPlugin(val global: Global) extends Plugin {
  override val name = "ScalaCL Optimizer"
  override val description =
    "This plugin transforms some Scala functions into OpenCL kernels (for CLCol[T].map and filter's arguments), so they can run on a GPU.\n" +
  "It will also soon feature autovectorization of ScalaCL programs, detecting parallelizable loops and unnecessary collection creations."

  val runsAfter = List[String]("refchecks")
  override val components = ScalaCLPlugin.components(global)
}

object ScalaCLPlugin {
  def components(global: Global) = List(new ScalaCLFunctionsTransformComponent(global))
}

import scala.reflect.NameTransformer
import scala.tools.nsc.Global
import scala.tools.nsc.plugins.PluginComponent
import scala.tools.nsc.transform.{Transform, TypingTransformers}

class ScalaCLFunctionsTransformComponent(val global: Global)
extends PluginComponent
   with Transform
   with TypingTransformers
   with MiscMatchers
   with Printers
{
  import global._
  import global.definitions._
  import scala.tools.nsc.symtab.Flags._
  import typer.{typed, atOwner}    // methods to type trees

  override val runsAfter = List[String]("refchecks")
  override val phaseName = "scalaclfunctionstransform"

  class Ids(start: Long = 1) {
    private var nx = start
    def next = this.synchronized {
      val v = nx
      nx += 1
      v
    }
  }

  def nodeToStringNoComment(tree: Tree) =
    nodeToString(tree).replaceAll("\\s*//.*\n", "\n").replaceAll("\\s*\n\\s*", " ").replaceAll("\\(\\s+", "(").replaceAll("\\s+\\)", "")

  def fixOwner(tree: Tree, unit: CompilationUnit) = new TypingTransformer(unit) {
    override def transform(tree: Tree): Tree =
      typed { atOwner(currentOwner) { super.transform(tree) } }
  }.transform(tree)

  val eqName = N("$eq")
  def replace(varName: String, tree: Tree, by: => Tree, unit: CompilationUnit) = new TypingTransformer(unit) {
    val n = N(varName)
    override def transform(tree: Tree): Tree = tree match {
      case Ident(n()) =>
        //by.tpe = tree.tpe
        //by.symbol = tree.symbol
        //by.pos = tree.pos
        //atOwner(currentOwner) { by }
        by
      /*
      case Select(This(n), id) =>
        Ident(id)
      case Apply(Select(This(n), varEq), List(v)) =>
        //Apply(Select(a, N(NameTransformer.encode(op))), List(b))
        val dec = varEq.decode
        println("varEq = '" + varEq + "', dec = '" + dec + "'")
        val transV = super.transform(v)
        if (varEq.toString.endsWith("_$eq")) {
          val n = varEq.toString.substring(0, varEq.length - "_$eq".length)
          Assign(Ident(N(n)), transV)
        } else
          Apply(Ident(varEq), List(transV))//super.transform(tree)
      */
      case _ =>
        //println("Unknown expr: " + nodeToStringNoComment(tree))
        super.transform(tree)
    }
  }.transform(tree)
  
  def newTransformer(unit: CompilationUnit) = new TypingTransformer(unit) {
    var currentClassName: Name = null
    //implicit def O(n: Name)(implicit symbol: Symbol) = atOwner(currentOwner) { n }
    //implicit def O(t: Tree)(implicit symbol: Symbol) = atOwner(currentOwner) { t }

    val labelIds = new Ids
    val whileIds = new Ids

    def binOp(a: Tree, op: Symbol, b: Tree) = Apply(Select(a, op), List(b))
    def binOp(a: Tree, op: String, b: Tree) = Apply(Select(a, N(NameTransformer.encode(op))), List(b))
    def incrementIntVar(n: Name) = Assign(Ident(n), binOp(Ident(n), "+", Literal(Constant(1))))
    def intValOrVar(mod: Int, n: Name, initValue: Tree) = localTyper.typed { ValDef(Modifiers(mod), n, TypeTree(IntClass.tpe), initValue) }
    def intVar(n: Name, initValue: Tree) = intValOrVar(MUTABLE, n, initValue)
    def intVal(n: Name, initValue: Tree) = intValOrVar(0, n, initValue)
    def whileLoop(cond: Tree, body: Tree) = {
      val lab = "while$" + whileIds.next
      LabelDef(
          N(lab),
          Nil,
          If(
            cond,
            Block(
              if (body == null)
                Nil
              else
                List(body),
              Apply(Ident(lab), Nil)
            ),
            Literal(Constant())
          )
        )
    }

    /*
     * if (false) {
        println("case " + nodeToStringNoComment(tree) + " => ")
        val lines = tree.toString
        if (lines.contains("\n"))
          println("\t/*\n\t\t" + tree.toString.replaceAll("\n", "\n\t\t") + "\n\t*/")
        else
          println("\t// " + lines)
      }
      
     */
    override def transform(tree: Tree): Tree = fixOwner(tree match {
      /*case ClassDef(mods, name, tparams, template) =>
        currentClassName = name
        tree*/
      case IntRangeForeach(from, to, by, isUntil, Function(List(ValDef(paramMods, paramName, t1: TypeTree, rhs)), body)) =>
        val id = labelIds.next
        val iVar = N("iVar$" + id)
        val nVal = N("nVal$" + id)
        val iDef = intVar(iVar, from)
        val nDef = intVal(nVal, to)
        def iIdent = {
          val id = Ident(iVar)
          id.tpe = IntClass.tpe
          id
        }
        def nIdent = {
          val id = Ident(nVal)
          id.tpe = IntClass.tpe
          id
        }

        val trans = //super.transform(
          Block(
            List(
              iDef,
              nDef
            ),
            whileLoop(
              binOp(iIdent, if (isUntil) IntClass.tpe.member(nme.LT) else IntClass.tpe.member(nme.LE), nIdent),
              Block(
                List(
                  {
                    val r = replace(paramName.toString, body, iIdent, unit)
                    println("REPLACED <<<\n" + body + "\n>>> by <<<\n" + r + "\n>>>")
                    r//localTyper.typed { r }
                    /*atPhase(phase.next) {
                      localTyper.typedPos(tree.pos) {
                        r
                      }
                    }*/
                  }
                ),
                incrementIntVar(iVar)
              )
            )
          )
        //}}}
        //)
        println("TRANS = " + trans)
        trans
      case
        Apply(
          Apply(
            TypeApply(
              Select(collectionExpr, functionName @ (mapName() | filterName() | updateName())),
              funTypeArgs @ List(outputType @ TypeTree())
            ),
            List(functionExpr @ Function(List(ValDef(paramMods, paramName, inputType @ TypeTree(), rhs)), body))
          ),
          implicitArgs @ List(io1, io2)
        ) =>
        /*
         * if functionExpr is :
         *    ((x$1: Int) => (x$1: Int).*(2.0)),
         * functionOpenCLExprString will be :
         *    "_ * 2.0"
         */
        val uniqueSignature = Literal(Constant(tree.symbol.outerSource + "" + tree.symbol.tag + tree.symbol.pos)) // TODO
        val functionOpenCLExprString = convertExpr(Map(paramName.toString -> "_"), body).toString
        println("Converted <<< " + body + " >>> to <<< \"" + functionOpenCLExprString + "\" >>>")
        def seqExpr(typeExpr: Tree, values: Tree*) =
          Apply(
            TypeApply(
              Select(
                Select(Select(Ident(N("scala")), N("collection")), N("Seq")),
                N("apply")
              ),
              List(typeExpr)
            ),
            values.toList
          )

        val newArg =
          Apply(
            Apply(
              TypeApply(
                Select(
                  Select(
                    Ident(N("scalacl")),
                    N("package")
                  ),
                  N("CLFullFun")
                ),
                List(inputType, outputType)
              ),
              List(
                uniqueSignature,
                functionExpr,
                seqExpr(TypeTree(StringClass.tpe)), // statements TODO
                seqExpr(TypeTree(StringClass.tpe), Literal(Constant(functionOpenCLExprString))) // expressions TODO
              )
            ),
            implicitArgs
          )
        //val newArgs = List(singleArg)
        //localTyper.typed { atPos(tree.pos) { atOwner(currentOwner) {
        //super.transform(
          Apply(
            Apply(
              TypeApply(
                Select(collectionExpr, N("mapFun")),
                funTypeArgs
              ),
              List(newArg)
            ),
            implicitArgs
          )
        //} } }
        //)
      case _ =>
        tree//typed { atOwner(currentOwner) { super.transform(tree) } }
    }, unit)

    var placeHolderRefs = new Stack[String]

    def convertExpr(argNames: Map[String, String], body: Tree, b: StringBuilder = new StringBuilder): StringBuilder = {
      def out(args: Any*): Unit = args.foreach(_ match {
        case t: Tree =>
          convertExpr(argNames, t, b)
        case items: List[_] =>
          var first = false
          for (item <- items) {
            if (first)
              first = false
            else
              b.append(", ")
            out(item)
          }
        case s: Any =>
          b.append(s)
      })
      def cast(expr: Tree, clType: String) =
        out("((", clType, ")", expr, ")")

      def convertForeach(from: Tree, to: Tree, isUntil: Boolean, by: Tree, function: Tree) = {
          val Function(List(ValDef(paramMods, paramName, tpt, rhs)), body) = function
          val id = labelIds.next
          val iVar = "iVar$" + id
          val nVal = "nVal$" + id
    
          out("int ", iVar, ";\n")
          out("const int ", nVal, " = ", to, ";\n")
          out("for (", iVar, " = ", from, "; ", iVar, " ", if (isUntil) "<" else "<=", " ", nVal, "; ", iVar, " += ", by, ") {\n")
          convertExpr(argNames + (paramName.toString -> iVar), body, b)
          out("\n}")          
      }
      body match {
        case Literal(Constant(value)) =>
          out(value)
        case Ident(name) =>
          val ns = name.toString
          if (ns == "_") {
            if (placeHolderRefs.isEmpty)
              error("Not expecting a placeholder here !")
            val ph = placeHolderRefs.top
            placeHolderRefs = placeHolderRefs.pop
            out(ph)
          }
          out(argNames.getOrElse(
            ns,
            error("Unknown identifier : '" + name + "' (expected any of " + argNames.keys.map("'" + _ + "'").mkString(", ") + ")")
          ))
          //case Apply(TypeApply(fun, args1), args2) =>
          /*NameTransformer.decode(name.toString) match {
           case "foreach" =>
           out("FOREACH")
           case _ =>
           println("traversing application of "+ name)
           }*/
        case Assign(lhs, rhs) =>
          out(lhs, " = ", rhs, ";\n")
        case Block(statements, expression) =>
          out(statements.flatMap(List(_, "\n")):_*)
          if (expression != EmptyTree)
            out(expression, "\n")
        case ValDef(paramMods, paramName, tpt: TypeTree, rhs) =>
          out(convertTpt(tpt), " ", paramName)
          rhs match {
            case Block(statements, expression) =>
              out(";\n{\n")
              out(Block(statements, EmptyTree))
              out(paramName, " = ", expression, ";\n")
              out("}\n")
            case tt: Tree =>
              if (tt != EmptyTree)
                out(" = ", tt, ";\n")
          }
        //case Typed(expr, tpe) =>
        //  out(expr)
        case Match(Ident(matchName), List(CaseDef(pat, guard, body))) =>
          //for ()
          //x0$1 match {
          //  case (_1: Long,_2: Float)(Long, Float)((i @ _), (c @ _)) => i.+(c)
          //}
          //Match(Ident("x0$1"), List(CaseDef(Apply(TypeTree(), List(Bind(i, Ident("_")), Bind(c, Ident("_"))), EmptyTree Apply(Select(Ident("i"), "$plus"), List(Ident("c")

          convertExpr(argNames + (matchName.toString + "._1" -> "?"), body, b)
        case Apply(Select(expr, toSizeTName()), Nil) => cast(expr, "size_t")
        case Apply(Select(expr, toLongName()), Nil) => cast(expr, "long")
        case Apply(Select(expr, toIntName()), Nil) => cast(expr, "int")
        case Apply(Select(expr, toShortName()), Nil) => cast(expr, "short")
        case Apply(Select(expr, toByteName()), Nil) => cast(expr, "char")
        case Apply(Select(expr, toCharName()), Nil) => cast(expr, "short")
        case Apply(Select(expr, toDoubleName()), Nil) => cast(expr, "double")
        case Apply(Select(expr, toFloatName()), Nil) => cast(expr, "float")
        case Apply(
          Select(
            Select(
              Select(
                Ident(scalaName()),
                mathName()
              ),
              packageName()
            ),
            funName
          ),
          args
        ) =>
          out(funName, "(", args, ")")
        case Apply(TypeApply(Select(Apply(Select(Apply(Select(predef, intWrapperName()), List(from)), funToName), List(to)), foreachName()), List(fRetType)), List(f: Function)) =>
          convertForeach(from, to, funToName.toString == "until", Literal(Constant(1)), f)
        //case IntRangeForeach(from, to, by, isUntil, Function(List(ValDef(paramMods, paramName, tpt, rhs)), body)) =>
        case Apply(TypeApply(Select(Apply(Select(Apply(Select(Apply(Select(predef, intWrapperName()), List(from)), funToName), List(to)), byName()), List(by)), foreachName()), List(fRetType)), List(f: Function)) =>
          convertForeach(from, to, funToName.toString == "until", by, f)
        case Apply(s @ Select(expr, fun), Nil) =>
          val fn = fun.toString
          if (fn.matches("_\\d+")) {
            out(expr, ".", fn)
          } else {
            error("Unknown function " + s)
          }
        case Apply(s @ Select(left, name), args) =>
          NameTransformer.decode(name.toString) match {
            case op @ ("+" | "-" | "*" | "/" | "%" | "^" | "^^" | "&" | "&&" | "|" | "||" | "<<" | ">>") =>
              out(left, " ", op, " ", args(0))
            case n =>
              println(nodeToStringNoComment(body))
              error("Unhandled method name : " + name)
          }
        case _ =>
          println("Failed to convert " + body.getClass.getName + ": " + body)
          println(nodeToStringNoComment(body))
      }
      b
    }
    def convertTpt(tpt: TypeTree) = tpt.toString match {
      case "Int" => "int"
      case "Long" => "long"
      case "org.bridj.SizeT" => "size_t"
      case "Float" => "float"
      case "Double" => "double"
      case _ => error("Cannot convert unknown type " + tpt + " to OpenCL")
    }
  }
}