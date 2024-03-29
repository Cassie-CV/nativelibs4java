package com.nativelibs4java.directx.d3d10;
import com.nativelibs4java.directx.d3d10.D3d10Library.D3D10_USAGE;
import org.bridj.Pointer;
import org.bridj.StructObject;
import org.bridj.ValuedEnum;
import org.bridj.ann.Field;
import org.bridj.ann.Library;
/**
 * <i>native declaration : d3d10.h:405</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.free.fr/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> or <a href="http://bridj.googlecode.com/">BridJ</a> .
 */
@Library("d3d10") 
public class D3D10_TEXTURE2D_DESC extends StructObject {
	public D3D10_TEXTURE2D_DESC() {
		super();
	}
	public D3D10_TEXTURE2D_DESC(Pointer pointer) {
		super(pointer);
	}
	@Field(0) 
	public int Width() {
		return this.io.getIntField(this, 0);
	}
	@Field(0) 
	public D3D10_TEXTURE2D_DESC Width(int Width) {
		this.io.setIntField(this, 0, Width);
		return this;
	}
	@Field(1) 
	public int Height() {
		return this.io.getIntField(this, 1);
	}
	@Field(1) 
	public D3D10_TEXTURE2D_DESC Height(int Height) {
		this.io.setIntField(this, 1, Height);
		return this;
	}
	@Field(2) 
	public int MipLevels() {
		return this.io.getIntField(this, 2);
	}
	@Field(2) 
	public D3D10_TEXTURE2D_DESC MipLevels(int MipLevels) {
		this.io.setIntField(this, 2, MipLevels);
		return this;
	}
	@Field(3) 
	public int ArraySize() {
		return this.io.getIntField(this, 3);
	}
	@Field(3) 
	public D3D10_TEXTURE2D_DESC ArraySize(int ArraySize) {
		this.io.setIntField(this, 3, ArraySize);
		return this;
	}
	/// Conversion Error : DXGI_FORMAT (Unsupported type)
	/// Conversion Error : DXGI_SAMPLE_DESC (Unsupported type)
	/// C type : D3D10_USAGE
	@Field(4) 
	public ValuedEnum<D3D10_USAGE > Usage() {
		return this.io.getEnumField(this, 4);
	}
	/// C type : D3D10_USAGE
	@Field(4) 
	public D3D10_TEXTURE2D_DESC Usage(ValuedEnum<D3D10_USAGE > Usage) {
		this.io.setEnumField(this, 4, Usage);
		return this;
	}
	@Field(5) 
	public int BindFlags() {
		return this.io.getIntField(this, 5);
	}
	@Field(5) 
	public D3D10_TEXTURE2D_DESC BindFlags(int BindFlags) {
		this.io.setIntField(this, 5, BindFlags);
		return this;
	}
	@Field(6) 
	public int CPUAccessFlags() {
		return this.io.getIntField(this, 6);
	}
	@Field(6) 
	public D3D10_TEXTURE2D_DESC CPUAccessFlags(int CPUAccessFlags) {
		this.io.setIntField(this, 6, CPUAccessFlags);
		return this;
	}
	@Field(7) 
	public int MiscFlags() {
		return this.io.getIntField(this, 7);
	}
	@Field(7) 
	public D3D10_TEXTURE2D_DESC MiscFlags(int MiscFlags) {
		this.io.setIntField(this, 7, MiscFlags);
		return this;
	}
}
