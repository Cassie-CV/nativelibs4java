package com.nativelibs4java.scalacl


import scala.collection.mutable.Stack
import scala.reflect.Manifest

import java.nio._

import com.nativelibs4java.opencl.OpenCL4Java._
import SyntaxUtils._

trait CLValue
trait Val1 extends CLValue
trait Val2 extends CLValue
trait Val4 extends CLValue

abstract class Expr {
	def typeDesc: TypeDesc;

	def accept(visitor: (Expr, Stack[Expr]) => Unit, stack: Stack[Expr]) : Unit;
	def accept(visitor: (Expr, Stack[Expr]) => Unit) : Unit = accept(visitor, new Stack[Expr]);

	def ~(other: Expr) = BinOp("=", this, other)
	def +~(other: Expr) = BinOp("+=", this, other)
	def -~(other: Expr) = BinOp("-=", this, other)
	def *~(other: Expr) = BinOp("*=", this, other)
	def /~(other: Expr) = BinOp("/=", this, other)
	def ==(other: Expr) = BinOp("==", this, other)
	def <=(other: Expr) = BinOp("<=", this, other)
	def >=(other: Expr) = BinOp(">=", this, other)
	def <(other: Expr) = BinOp("<", this, other)
	def >(other: Expr) = BinOp(">", this, other)

	def !() = UnOp("!", true, this)

	//def apply(index: Expr) = ArrayIndex(this, index)

	def +(other: Expr) = BinOp("+", this, other)
	def -(other: Expr) = BinOp("-", this, other)
	def *(other: Expr) = BinOp("*", this, other)
	def /(other: Expr) = BinOp("/", this, other)
	def %(other: Expr) = BinOp("%", this, other)
	def >>(other: Expr) = BinOp(">>", this, other)
	def <<(other: Expr) = BinOp("<<", this, other)

	def includes : List[String] = {
			this match {
			case Fun(name, outType, vars, include) => List(include);
			case BinOp(op, first, second) => first.includes ++ second.includes;
			case UnOp(op, isPrefix, operand) => operand.includes
			case _ => Nil
			}
	}
	def variables: List[AbstractVar] = {
			this match {
			case Fun(name, outType, vars, include) => vars(0).variables//TODO (vars map (x => x variables)) collapse
			case BinOp(op, first, second) => first.variables ++ second.variables
			case UnOp(op, isPrefix, operand) => operand.variables
			case v: AbstractVar => List(v)
			case _ => Nil
			}
	}
}
abstract class TypedExpr(td: TypeDesc) extends Expr {
	override def typeDesc = td
	override def accept(visitor: (Expr, Stack[Expr]) => Unit, stack: Stack[Expr]) : Unit = {
	  visitor(this, stack)
	}
}

abstract case class PrimScal(v: Number, t: PrimType) extends TypedExpr(TypeDesc(1, Scalar, t)) with CLValue {
  override def toString = v.toString
}

case class Dim(size: Int) extends PrimScal(size, IntType) with Val1


case class BinOp(var op: String, var first: Expr, var second: Expr) extends Expr {
	override def toString() = first + " " + op + " " + second
	override def typeDesc = first.typeDesc combineWith second.typeDesc
	override def accept(visitor: (Expr, Stack[Expr]) => Unit, stack: Stack[Expr]): Unit = {
	  stack push this;
	  first.accept(visitor, stack)
	  second.accept(visitor, stack)
	  stack pop;
	  visitor(this, stack);

	}

}

case class UnOp(var op: String, var isPrefix: Boolean, var value: Expr) extends Expr {
	override def toString() = {
		if (isPrefix) op + " " + value
		else value.toString() + " " + op
	}
	override def typeDesc = value.typeDesc
	override def accept(visitor: (Expr, Stack[Expr]) => Unit, stack: Stack[Expr]): Unit = {
	  stack push this
	  value.accept(visitor, stack)
	  stack pop;
	  visitor(this, stack);
	}

}

case class Fun(name: String, outType: PrimType, args: List[Expr], include: String) extends Expr {

			def implode(elts: List[String], sep: String) = {
			  if (elts == Nil) ""
			  else elts reduceLeft { _ + sep + _ } //map { _.toString }

			}

	override def toString() = name + "(" + implode(args map {_.toString}, ", ") + ")"
	override def typeDesc = {
		var argType = args map (a => a.typeDesc) reduceLeft {(a, b) => a combineWith b};
		TypeDesc(argType.channels, argType.valueType, outType)
	}
	override def accept(visitor: (Expr, Stack[Expr]) => Unit, stack: Stack[Expr]) : Unit = {
	  stack push this
	  args foreach { _.accept(visitor, stack) }
	  stack pop;
	  visitor(this, stack);
	}
}


case class Int1(value: Int) extends PrimScal(value, IntType) with Val1
case class Int2(x: Int, y: Int) extends TypedExpr(TypeDesc(2, Scalar, IntType)) with CLValue with Val2
case class Int4(x: Int, y: Int, z: Int, w: Int) extends TypedExpr(TypeDesc(4, Scalar, IntType)) with CLValue with Val4 {
  def this(xy: Int2, zw: Int2) = {
    this(xy.x, xy.y, zw.x, zw.y)
  }
}

case class Double1(value: Double) extends PrimScal(value, DoubleType) with Val1
case class Double2(x: Double, y: Double) extends TypedExpr(TypeDesc(2, Scalar, DoubleType)) with CLValue with Val2
case class Double4(x: Double, y: Double, z: Double, w: Double) extends TypedExpr(TypeDesc(4, Scalar, DoubleType)) with CLValue  with Val4 {
  def this(xy: Int2, zw: Int2) = {
    this(xy.x, xy.y, zw.x, zw.y)
  }
}


abstract sealed class VarMode {
  def union(mode: VarMode): VarMode;
}
case object UnknownMode extends VarMode {
  override def union(mode: VarMode) = mode
}

case object ReadMode extends VarMode {
  override def union(mode: VarMode) = if (mode == AggregatedMode) AggregatedMode else if (mode == WriteMode) ReadWriteMode else this
}
case object WriteMode extends VarMode {
  override def union(mode: VarMode) = if (mode == AggregatedMode) AggregatedMode else if (mode == WriteMode) ReadWriteMode else this
}
case object ReadWriteMode extends VarMode {
  override def union(mode: VarMode) = if (mode == AggregatedMode) AggregatedMode else this
}
case object AggregatedMode extends VarMode {
  override def union(mode: VarMode) = this
}

abstract class AbstractVar extends Expr {
  var kernel: CLKernel = null;
  var argIndex = -2;
  var name: String = null;
  var mode: VarMode = UnknownMode;

  def setup
  override def toString() = name;
  override def accept(visitor: (Expr, Stack[Expr]) => Unit, stack: Stack[Expr]) : Unit = {
    visitor(this, stack)
  }

  def getTypeDesc[T](implicit t: Manifest[T], valueType: ValueType) = {
    var ch = {
      if (valueType.isInstanceOf[Val1]) 1
      else if (valueType.isInstanceOf[Val2]) 2
      else if (valueType.isInstanceOf[Val4]) 4
      else throw new IllegalArgumentException("Unable to guess channels for valueType " + valueType)
    }
    var c = t.erasure
    var pt = {
      if (c.isAnyOf(classOf[Int1], classOf[Int2], classOf[Int4])) IntType
      else if (c.isAnyOf(classOf[Double1], classOf[Double2], classOf[Double4])) DoubleType
      else throw new IllegalArgumentException("Unable to guess primType for class " + c.getName)
    }
    TypeDesc(ch, valueType, pt)
  }
}


class Var[T](implicit t: Manifest[T]) extends AbstractVar {
  private var value: Option[T] = None;
  def apply() : T = {
    value getOrElse { throw new RuntimeException("Cannot get variable value before setting things up !")}
  }
  override def typeDesc = getTypeDesc[T](t, Scalar)

  def defaultValue[K](implicit k: Manifest[K]): K = {
    var c = k.erasure;
    (
      if (c == classOf[Int])
        0
      else if (c == classOf[Double])
        0.0
      else if (c == classOf[Float])
        0.0f
      else if (c == classOf[Long])
        0l
      else
        c.newInstance()
    ).asInstanceOf[K]
  }
  override def setup = {
    var value = if (this.value == None) defaultValue[T] else this.value
    kernel.setObjectArg(argIndex, value)
  }
}

class ArrayVar[T](implicit t: Manifest[T]) extends AbstractVar {
  var dim: Option[Dim] = None

  override def setup = {

  }
  override def typeDesc = getTypeDesc[T](t, Parallel)
  private var mem: Option[CLMem] = None;
  private var value: Option[T] = None;

  def apply(index: Expr) : Expr = new ArrayElement[T](this, index)

  def apply(index: Int) : T = {
    var value = this.value getOrElse { throw new RuntimeException("Cannot get variable value before setting things up !")}
    return value.asInstanceOf[DoubleBuffer].get(index).asInstanceOf[T];
  }
}

class ArrayElement[T](/*implicit t: Manifest[T], */var array: ArrayVar[T], var index: Expr) extends Expr {
  override def typeDesc = {
    var td = array.typeDesc
    TypeDesc(td.channels, Scalar, td.primType)
  }
  override def toString() = array + "[" + index + "]"
  override def accept(visitor: (Expr, Stack[Expr]) => Unit, stack: Stack[Expr]) : Unit = {
    stack push this
    visitor(array, stack)
    visitor(index, stack)
    stack pop;
    visitor(this, stack)
  }

}

class ImageVar[T] extends AbstractVar {
  var width: Option[Dim] = None
  var height: Option[Dim] = None
  override def setup = throw new UnsupportedOperationException("IMPLEMENT ME: ImageVar.setup")
  override def typeDesc : TypeDesc = throw new UnsupportedOperationException("IMPLEMENT ME: ImageVar.typeDesc")
  def apply(x: Expr, y: Expr) = new Pixel[T](this, x, y)
}
class Pixel[T](/*implicit t: Manifest[T], */var image: ImageVar[T], var x: Expr, var y: Expr) extends Expr {
  override def typeDesc = {
    var td = image.typeDesc
    TypeDesc(td.channels, Scalar, td.primType)
  }
  override def toString() = image + "[" + x + ", " + y + "]"
  override def accept(visitor: (Expr, Stack[Expr]) => Unit, stack: Stack[Expr]) : Unit = {
    stack push this
	visitor(image, stack)
    visitor(x, stack)
    visitor(x, stack)
    stack pop;
    visitor(this, stack)
  }

}

