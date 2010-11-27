package com.nativelibs4java.ffmpeg.avfilter;
import org.bridj.Callback;
import org.bridj.Pointer;
import org.bridj.StructObject;
import org.bridj.ValuedEnum;
import org.bridj.ann.Array;
import org.bridj.ann.Field;
import org.bridj.ann.Library;
import static com.nativelibs4java.ffmpeg.avcodec.AvcodecLibrary.*;
import static com.nativelibs4java.ffmpeg.avformat.AvformatLibrary.*;
import static com.nativelibs4java.ffmpeg.avutil.AvutilLibrary.*;
import static com.nativelibs4java.ffmpeg.swscale.SwscaleLibrary.*;
/**
 * <i>native declaration : libavfilter/avfilter.h</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.free.fr/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> or <a href="http://bridj.googlecode.com/">BridJ</a> .
 */
@Library("avfilter") 
public class AVFilterPic extends StructObject {
	public AVFilterPic() {
		super();
	}
	public AVFilterPic(Pointer pointer) {
		super(pointer);
	}
	/**
	 * < picture data for each plane<br>
	 * C type : uint8_t*[4]
	 */
	@Array({4}) 
	@Field(0) 
	public Pointer<Pointer<java.lang.Byte > > data() {
		return this.io.getPointerField(this, 0);
	}
	/**
	 * < number of bytes per line<br>
	 * C type : int[4]
	 */
	@Array({4}) 
	@Field(1) 
	public Pointer<java.lang.Integer > linesize() {
		return this.io.getPointerField(this, 1);
	}
	/**
	 * < colorspace<br>
	 * C type : PixelFormat
	 */
	@Field(2) 
	public ValuedEnum<PixelFormat > format() {
		return this.io.getEnumField(this, 2);
	}
	/**
	 * < colorspace<br>
	 * C type : PixelFormat
	 */
	@Field(2) 
	public AVFilterPic format(ValuedEnum<PixelFormat > format) {
		this.io.setEnumField(this, 2, format);
		return this;
	}
	/// C type : PixelFormat
	public final ValuedEnum<PixelFormat > format_$eq(ValuedEnum<PixelFormat > format) {
		format(format);
		return format;
	}
	/// < number of references to this image
	@Field(3) 
	public int refcount() {
		return this.io.getIntField(this, 3);
	}
	/// < number of references to this image
	@Field(3) 
	public AVFilterPic refcount(int refcount) {
		this.io.setIntField(this, 3, refcount);
		return this;
	}
	public final int refcount_$eq(int refcount) {
		refcount(refcount);
		return refcount;
	}
	/**
	 * private data to be used by a custom free function<br>
	 * C type : void*
	 */
	@Field(4) 
	public Pointer<? > priv() {
		return this.io.getPointerField(this, 4);
	}
	/**
	 * private data to be used by a custom free function<br>
	 * C type : void*
	 */
	@Field(4) 
	public AVFilterPic priv(Pointer<? > priv) {
		this.io.setPointerField(this, 4, priv);
		return this;
	}
	/// C type : void*
	public final Pointer<? > priv_$eq(Pointer<? > priv) {
		priv(priv);
		return priv;
	}
	/**
	 * A pointer to the function to deallocate this image if the default<br>
	 * function is not sufficient. This could, for example, add the memory<br>
	 * back into a memory pool to be reused later without the overhead of<br>
	 * reallocating it from scratch.<br>
	 * C type : free_callback
	 */
	@Field(5) 
	public Pointer<AVFilterPic.free_callback > free() {
		return this.io.getPointerField(this, 5);
	}
	/**
	 * A pointer to the function to deallocate this image if the default<br>
	 * function is not sufficient. This could, for example, add the memory<br>
	 * back into a memory pool to be reused later without the overhead of<br>
	 * reallocating it from scratch.<br>
	 * C type : free_callback
	 */
	@Field(5) 
	public AVFilterPic free(Pointer<AVFilterPic.free_callback > free) {
		this.io.setPointerField(this, 5, free);
		return this;
	}
	/// C type : free_callback
	public final Pointer<AVFilterPic.free_callback > free_$eq(Pointer<AVFilterPic.free_callback > free) {
		free(free);
		return free;
	}
	/// < width and height of the allocated buffer
	@Field(6) 
	public int w() {
		return this.io.getIntField(this, 6);
	}
	/// < width and height of the allocated buffer
	@Field(6) 
	public AVFilterPic w(int w) {
		this.io.setIntField(this, 6, w);
		return this;
	}
	public final int w_$eq(int w) {
		w(w);
		return w;
	}
	/// < width and height of the allocated buffer
	@Field(7) 
	public int h() {
		return this.io.getIntField(this, 7);
	}
	/// < width and height of the allocated buffer
	@Field(7) 
	public AVFilterPic h(int h) {
		this.io.setIntField(this, 7, h);
		return this;
	}
	public final int h_$eq(int h) {
		h(h);
		return h;
	}
	/// <i>native declaration : libavfilter/avfilter.h:80</i>
	public static abstract class free_callback extends Callback<free_callback > {
		public abstract void apply(Pointer<AVFilterPic > pic);
	};
}