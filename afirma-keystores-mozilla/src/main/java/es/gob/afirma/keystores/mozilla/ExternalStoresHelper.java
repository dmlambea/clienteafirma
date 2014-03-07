package es.gob.afirma.keystores.mozilla;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import es.gob.afirma.core.misc.Platform;

final class ExternalStoresHelper {

	private static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

	private ExternalStoresHelper() {
		// NO permitimos la instanciacion
	}

	private static final String[] DNI_P11_NAMES = new String[] {
		"libopensc-dnie.dylib", //$NON-NLS-1$
		"libopensc-dnie.so", //$NON-NLS-1$
		"usrpkcs11.dll", //$NON-NLS-1$
		"dnie_p11_priv.dll", //$NON-NLS-1$
		"dnie_p11_pub.dll", //$NON-NLS-1$
		"opensc-pkcs11.dll", //$NON-NLS-1$
	};

	static Map<String, String> cleanExternalStores(final Map<String, String> externalStores) {
		if (externalStores == null) {
			return new HashMap<String, String>(0);
		}
		// Eliminamos los controladores de DNIe, ya que se usa el controlador 100% Java
		for (final String key : externalStores.keySet()) {
			if (isDniePkcs11LibraryForWindows(externalStores.get(key))) {
				externalStores.remove(key);
			}
		}
		// Eliminamos los posibles duplicados de la tabla
		return purgeStoresTable(externalStores);
	}

	private static boolean isDniePkcs11LibraryForWindows(final String driverName) {
		if (driverName == null) {
			return false;
		}
		for (final String libName : DNI_P11_NAMES) {
			if (driverName.toLowerCase().endsWith(libName)) {
				return true;
			}
		}
		return false;
	}

	/** Dada una tabla que indexa por descripci&oacute;n los m&oacute;dulos
	 * PKCS#11, elimina las entradas necesarias para que aparezca una
	 * &uacute;nica vez cada uno de los m&oacute;dulo PKCS#11.
	 * @param table Tabla con las descripciones de los m&oacute;dulos pkcs11 y las
	 *              librer&iacute;as asociadas.
	 * @return Tabla con los duplicados eliminados. */
	private static Map<String, String> purgeStoresTable(final Map<String, String> table) {

		if (table == null) {
			return new Hashtable<String, String>(0);
		}

		final Map<String, String> purgedTable = new Hashtable<String, String>();
		final Set<String> revisedLibs = new HashSet<String>();

		String tmpLib;
		for (final String key : table.keySet()) {
			tmpLib = table.get(key);
			if (tmpLib.toLowerCase().endsWith(".dll")) { //$NON-NLS-1$
				tmpLib = tmpLib.toLowerCase();
			}

			if (!revisedLibs.contains(tmpLib) && !tmpLib.toLowerCase().contains("nssckbi")) { //$NON-NLS-1$
				purgedTable.put(key, table.get(key));
				revisedLibs.add(tmpLib);
			}
			else {
				LOGGER.warning("Se eliminara el modulo '" + key //$NON-NLS-1$
						+ "' porque ya existe uno con la misma biblioteca o es un modulo de certificados raiz: " //$NON-NLS-1$
						+ table.get(key));
			}
		}

		return purgedTable;
	}

	private static String getWindowsSystemDirWithFinalSlash() {
		final String winDir = System.getenv("SystemRoot"); //$NON-NLS-1$
		if (winDir == null) {
			return ""; //$NON-NLS-1$
		}
		// Si java es 64 bits, no hay duda de que Windows es 64 bits y buscamos bibliotecas
		// de 64 bits
		if ("64".equals(Platform.getJavaArch())) { //$NON-NLS-1$
			return winDir + "\\System32\\";  //$NON-NLS-1$
		}
		if (new File(winDir + "\\SysWOW64\\").exists()) { //$NON-NLS-1$
			return winDir + "\\SysWOW64\\"; //$NON-NLS-1$
		}
		return winDir + "\\System32\\"; //$NON-NLS-1$
	}

}
