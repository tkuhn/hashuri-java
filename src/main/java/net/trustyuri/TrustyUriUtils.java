package net.trustyuri;

import org.openrdf.model.URI;

import javax.activation.MimetypesFileTypeMap;
import javax.xml.bind.DatatypeConverter;

public class TrustyUriUtils {

	private static final MimetypesFileTypeMap mimeMap = new MimetypesFileTypeMap();

	public static String getTrustyUriTail(String s) {
		if (!s.matches("(.*[^A-Za-z0-9\\-_]|)[A-Za-z0-9\\-_]{25,}(\\.[A-Za-z0-9\\-_]{0,20})?")) {
			return null;
		}
		return s.replaceFirst("^(.*[^A-Za-z0-9\\-_]|)([A-Za-z0-9\\-_]{25,})(\\.[A-Za-z0-9\\-_]{0,20})?$", "$2");
	}

	public static boolean isPotentialTrustyUri(URI uri) {
		String t = getTrustyUriTail(uri.toString());
		if (t == null) return false;
		String id = getModuleId(t);
		TrustyUriModule module = ModuleDirectory.getModule(id);
		if (module == null) return false;
		int l = t.substring(2).length();
		return l == module.getHashLength();
	}

	public static String getModuleId(String tail) {
		return tail.substring(0, 2);
	}

	public static String getNiUri(String s) {
		return getNiUri(s, true);
	}

	public static String getNiUri(String s, boolean withAuthority) {
		String t = getTrustyUriTail(s);
		if (t == null) return null;
		String moduleId = getModuleId(t);
		String hash = t.substring(2);
		TrustyUriModule module = ModuleDirectory.getModule(moduleId);
		String tail = "/" + module.getAlgorithmId() + ";" + hash + "?module=" + moduleId;
		if (withAuthority) {
			try {
				String autority = (new java.net.URI(s)).getAuthority().toString();
				return "ni://" + autority + tail;
			} catch (Exception ex) {}
		}
		return "ni://" + tail;
	}

	public static String getBase64(byte[] bytes) {
		String h = DatatypeConverter.printBase64Binary(bytes);
		h = h.replaceFirst("=*$", "");
		h = h.replace('+', '-');
		h = h.replace('/', '_');
		return h;
	}

	public static String getMimetype(String filename) {
		return mimeMap.getContentType(filename);
	}

}
