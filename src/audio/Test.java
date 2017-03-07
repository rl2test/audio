package audio;


public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int modulus = 3;
		
		System.out.println("modulus=" + modulus);
		System.out.println("i\tjavaRem\trem");

		for (int i = -6; i < 7; i++) {
			int javaRem = i % modulus; 
			int rem = (i % modulus + modulus) % modulus;
			System.out.println(i + "\t" + javaRem + "\t" + rem);
		}
		
		System.getProperties().list(System.out);
		
		/*
			java.runtime.name=Java(TM) SE Runtime Environment
			sun.boot.library.path=/Library/Java/JavaVirtualMachines/jdk...
			java.vm.version=24.79-b02
			gopherProxySet=false
			java.vm.vendor=Oracle Corporation
			java.vendor.url=http://java.oracle.com/
			path.separator=:
			java.vm.name=Java HotSpot(TM) 64-Bit Server VM
			file.encoding.pkg=sun.io
			user.country=US
			sun.java.launcher=SUN_STANDARD
			sun.os.patch.level=unknown
			java.vm.specification.name=Java Virtual Machine Specification
			user.dir=/Users/rlowe/rob/apps/audio
			java.runtime.version=1.7.0_79-b15
			java.awt.graphicsenv=sun.awt.CGraphicsEnvironment
			java.endorsed.dirs=/Library/Java/JavaVirtualMachines/jdk...
			os.arch=x86_64
			java.io.tmpdir=/var/folders/tw/b7y98pf52tb6n_qm9fg_p...
			line.separator=
			
			java.vm.specification.vendor=Oracle Corporation
			os.name=Mac OS X
			sun.jnu.encoding=UTF-8
			java.library.path=/Users/rlowe/Library/Java/Extensions:...
			java.specification.name=Java Platform API Specification
			java.class.version=51.0
			sun.management.compiler=HotSpot 64-Bit Tiered Compilers
			os.version=10.12
			http.nonProxyHosts=local|*.local|169.254/16|*.169.254/16
			user.home=/Users/rlowe
			user.timezone=
			java.awt.printerjob=sun.lwawt.macosx.CPrinterJob
			file.encoding=UTF-8
			java.specification.version=1.7
			user.name=rlowe
			java.class.path=/Users/rlowe/rob/apps/audio/bin:/User...
			java.vm.specification.version=1.7
			sun.arch.data.model=64
			java.home=/Library/Java/JavaVirtualMachines/jdk...
			sun.java.command=audio.Test
			java.specification.vendor=Oracle Corporation
			user.language=en
			awt.toolkit=sun.lwawt.macosx.LWCToolkit
			java.vm.info=mixed mode
			java.version=1.7.0_79
			java.ext.dirs=/Users/rlowe/Library/Java/Extensions:...
			sun.boot.class.path=/Library/Java/JavaVirtualMachines/jdk...
			java.vendor=Oracle Corporation
			file.separator=/
			java.vendor.url.bug=http://bugreport.sun.com/bugreport/
			sun.cpu.endian=little
			sun.io.unicode.encoding=UnicodeBig
			socksNonProxyHosts=local|*.local|169.254/16|*.169.254/16
			ftp.nonProxyHosts=local|*.local|169.254/16|*.169.254/16
			sun.cpu.isalist=
		 */
		
	}

}
