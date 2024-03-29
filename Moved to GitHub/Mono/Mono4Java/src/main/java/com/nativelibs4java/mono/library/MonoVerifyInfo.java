package com.nativelibs4java.mono.library;
/**
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.free.fr/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a>, <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class MonoVerifyInfo extends com.ochafik.lang.jnaerator.runtime.Structure<MonoVerifyInfo, MonoVerifyInfo.ByValue, MonoVerifyInfo.ByReference> {
	/// C type : char*
	public com.sun.jna.Pointer message;
	/**
	 * @see MonoVerifyStatus<br>
	 * C type : MonoVerifyStatus
	 */
	public int status;
	public MonoVerifyInfo() {
		super();
	}
	/**
	 * @param message C type : char*<br>
	 * @param status @see MonoVerifyStatus<br>
	 * C type : MonoVerifyStatus
	 */
	public MonoVerifyInfo(com.sun.jna.Pointer message, int status) {
		super();
		this.message = message;
		this.status = status;
	}
	protected ByReference newByReference() { return new ByReference(); }
	protected ByValue newByValue() { return new ByValue(); }
	protected MonoVerifyInfo newInstance() { return new MonoVerifyInfo(); }
	public static MonoVerifyInfo[] newArray(int arrayLength) {
		return com.ochafik.lang.jnaerator.runtime.Structure.newArray(MonoVerifyInfo.class, arrayLength);
	}
	public static class ByReference extends MonoVerifyInfo implements com.sun.jna.Structure.ByReference {}
	public static class ByValue extends MonoVerifyInfo implements com.sun.jna.Structure.ByValue {}
}
