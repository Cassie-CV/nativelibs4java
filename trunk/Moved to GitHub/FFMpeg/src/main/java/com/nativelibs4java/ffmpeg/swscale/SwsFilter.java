package com.nativelibs4java.ffmpeg.swscale;
import org.bridj.Pointer;
import org.bridj.StructObject;
import org.bridj.ann.Field;
import org.bridj.ann.Library;
/**
 * <i>native declaration : libswscale/swscale.h</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.free.fr/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> or <a href="http://bridj.googlecode.com/">BridJ</a> .
 */
@Library("swscale") 
public class SwsFilter extends StructObject {
	public SwsFilter() {
		super();
	}
	public SwsFilter(Pointer pointer) {
		super(pointer);
	}
	/// C type : SwsVector*
	@Field(0) 
	public Pointer<SwsVector > lumH() {
		return this.io.getPointerField(this, 0);
	}
	/// C type : SwsVector*
	@Field(0) 
	public SwsFilter lumH(Pointer<SwsVector > lumH) {
		this.io.setPointerField(this, 0, lumH);
		return this;
	}
	/// C type : SwsVector*
	public final Pointer<SwsVector > lumH_$eq(Pointer<SwsVector > lumH) {
		lumH(lumH);
		return lumH;
	}
	/// C type : SwsVector*
	@Field(1) 
	public Pointer<SwsVector > lumV() {
		return this.io.getPointerField(this, 1);
	}
	/// C type : SwsVector*
	@Field(1) 
	public SwsFilter lumV(Pointer<SwsVector > lumV) {
		this.io.setPointerField(this, 1, lumV);
		return this;
	}
	/// C type : SwsVector*
	public final Pointer<SwsVector > lumV_$eq(Pointer<SwsVector > lumV) {
		lumV(lumV);
		return lumV;
	}
	/// C type : SwsVector*
	@Field(2) 
	public Pointer<SwsVector > chrH() {
		return this.io.getPointerField(this, 2);
	}
	/// C type : SwsVector*
	@Field(2) 
	public SwsFilter chrH(Pointer<SwsVector > chrH) {
		this.io.setPointerField(this, 2, chrH);
		return this;
	}
	/// C type : SwsVector*
	public final Pointer<SwsVector > chrH_$eq(Pointer<SwsVector > chrH) {
		chrH(chrH);
		return chrH;
	}
	/// C type : SwsVector*
	@Field(3) 
	public Pointer<SwsVector > chrV() {
		return this.io.getPointerField(this, 3);
	}
	/// C type : SwsVector*
	@Field(3) 
	public SwsFilter chrV(Pointer<SwsVector > chrV) {
		this.io.setPointerField(this, 3, chrV);
		return this;
	}
	/// C type : SwsVector*
	public final Pointer<SwsVector > chrV_$eq(Pointer<SwsVector > chrV) {
		chrV(chrV);
		return chrV;
	}
}