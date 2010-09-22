/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package scalacl

class CLLongRange(fromUntil: CLGuardedBuffer[Long])(implicit val context: ScalaCLContext) extends CLCol[Long] {
  //type ThisCol[T] = CLLongRange

  def this(from: Long, until: Long)(implicit context: ScalaCLContext) =
    this(new CLGuardedBuffer[Long](Array(from, until)))
    
  lazy val Array(from, until) = fromUntil.toArray
  
  override def filterFun(f: CLFunction[Long, Boolean])(implicit dataIO: CLDataIO[Long]): CLCol[Long] = notImp
  override def filter(f: Long => Boolean)(implicit dataIO: CLDataIO[Long]): CLCol[Long] = notImp

  override def mapFun[V](f: CLFunction[Long, V])(implicit dataIO: CLDataIO[Long], vIO: CLDataIO[V]): CLCol[V] = notImp
  override def map[V](f: Long => V)(implicit dataIO: CLDataIO[Long], vIO: CLDataIO[V]): CLCol[V] = notImp

  override def toCLArray: CLArray[Long] = notImp
  override def toArray: Array[Long] = (from until until).toArray

  //override def view: CLView[Long, ThisCol[Long]] = notImp
  override def slice(from: Long, to: Long) = new CLLongRange(this.from + from.toLong, this.from + to.toLong)
  override def sizeFuture: CLFuture[Long] = new CLInstantFuture[Long](until - from)

}