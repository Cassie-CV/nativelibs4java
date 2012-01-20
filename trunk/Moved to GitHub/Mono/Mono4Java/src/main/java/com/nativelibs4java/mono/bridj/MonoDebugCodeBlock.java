package com.nativelibs4java.mono.bridj;
import org.bridj.Pointer;
import org.bridj.StructObject;
import org.bridj.ann.Field;
import org.bridj.ann.Library;
/**
 * <i>native declaration : mono/metadata/debug-mono-symfile.h</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.free.fr/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> or <a href="http://bridj.googlecode.com/">BridJ</a> .
 */
@Library("mono") 
public class MonoDebugCodeBlock extends StructObject {
	public MonoDebugCodeBlock() {
		super();
	}
	public MonoDebugCodeBlock(Pointer pointer) {
		super(pointer);
	}
	@Field(0) 
	public int parent() {
		return this.io.getIntField(this, 0);
	}
	@Field(0) 
	public MonoDebugCodeBlock parent(int parent) {
		this.io.setIntField(this, 0, parent);
		return this;
	}
	@Field(1) 
	public int type() {
		return this.io.getIntField(this, 1);
	}
	@Field(1) 
	public MonoDebugCodeBlock type(int type) {
		this.io.setIntField(this, 1, type);
		return this;
	}
	/// IL offsets
	@Field(2) 
	public int start_offset() {
		return this.io.getIntField(this, 2);
	}
	/// IL offsets
	@Field(2) 
	public MonoDebugCodeBlock start_offset(int start_offset) {
		this.io.setIntField(this, 2, start_offset);
		return this;
	}
	/// IL offsets
	@Field(3) 
	public int end_offset() {
		return this.io.getIntField(this, 3);
	}
	/// IL offsets
	@Field(3) 
	public MonoDebugCodeBlock end_offset(int end_offset) {
		this.io.setIntField(this, 3, end_offset);
		return this;
	}
}