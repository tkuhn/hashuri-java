package net.trustyuri.rdf;

import java.util.Comparator;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

public class SerStatementComparator implements Comparator<String> {

	public SerStatementComparator() {
	}

	@Override
	public int compare(String s1, String s2) {
		String[] parts1 = s1.split("\\t");
		String[] parts2 = s2.split("\\t");
		int c;
		c = parts1[0].compareTo(parts2[0]);
		if (c != 0) return c;
		c = parts1[1].compareTo(parts2[1]);
		if (c != 0) return c;
		c = parts1[2].compareTo(parts2[2]);
		if (c != 0) return c;
		boolean objuri1 = !parts1[3].isEmpty();
		boolean objuri2 = !parts2[3].isEmpty();
		if (objuri1 && !objuri2) return -1;
		if (!objuri1 && objuri2) return 1;
		c = parts1[3].compareTo(parts2[3]);
		if (c != 0) return c;
		String o1 = "  ";
		if (parts1.length > 4) o1 = parts1[4];
		String o2 = "  ";
		if (parts2.length > 4) o2 = parts2[4];
		int i11 = o1.indexOf(32);
		int i21 = o2.indexOf(32);
		int i12 = o1.indexOf(32, i11+1);
		int i22 = o2.indexOf(32, i21+1);
		String v1 = unescape(o1.substring(i12+1));
		String v2 = unescape(o2.substring(i22+1));
		c = v1.compareTo(v2);
		if (c != 0) return c;
		String d1 = o1.substring(0, i11);
		String d2 = o2.substring(0, i21);
		c = d1.compareTo(d2);
		if (c != 0) return c;
		String l1 = o1.substring(i11+1, i12);
		String l2 = o2.substring(i21+1, i22);
		return l1.compareTo(l2);
	}

	public static Statement fromString(String string) {
		ValueFactory vf = SimpleValueFactory.getInstance();
		String[] parts = string.split("\\t");
		Resource context = null;
		if (!parts[0].isEmpty()) {
			context = vf.createIRI(parts[0]);
		}
		Resource subj = vf.createIRI(parts[1]);
		IRI pred = vf.createIRI(parts[2]);
		Value obj = null;
		if (!parts[3].isEmpty()) {
			obj = SimpleValueFactory.getInstance().createIRI(parts[3]);
		} else {
			String o = parts[4];
			int i1 = o.indexOf(32);
			int i2 = o.indexOf(32, i1+1);
			String d = o.substring(0, i1);
			String l = o.substring(i1+1, i2);
			String v = unescape(o.substring(i2+1));
			if (!d.isEmpty()) {
				obj = vf.createLiteral(v, vf.createIRI(d));
			} else if (!l.isEmpty()) {
				obj = vf.createLiteral(v, l);
			} else {
				obj = vf.createLiteral(v);
			}
		}
		if (context == null) {
			return vf.createStatement(subj, pred, obj);
		} else {
			return vf.createStatement(subj, pred, obj, context);
		}
	}

	public static String toString(Statement st) {
		StringBuffer sb = new StringBuffer();
		Resource context = st.getContext();
		if (context instanceof BNode) {
			throw new RuntimeException("Unexpected blank node");
		} else if (context == null) {
			sb.append("\t");
		} else {
			sb.append(context.stringValue() + "\t");
		}
		Resource subj = st.getSubject();
		if (subj instanceof BNode) {
			throw new RuntimeException("Unexpected blank node");
		} else {
			sb.append(subj.stringValue() + "\t");
		}
		IRI pred = st.getPredicate();
		sb.append(pred.stringValue() + "\t");
		Value obj = st.getObject();
		if (obj instanceof BNode) {
			throw new RuntimeException("Unexpected blank node");
		} else if (obj instanceof Literal) {
			sb.append("\t");
			Literal objl = (Literal) obj;
			if (!objl.getLanguage().isPresent()) {
				IRI dataType = objl.getDatatype();
				if (dataType == null) dataType = XMLSchema.STRING;
				sb.append(dataType.stringValue() + "  ");
			} else {
				sb.append(" " + objl.getLanguage().get().toLowerCase() + " ");
			}
			sb.append(escape(obj.stringValue()));
		} else {
			sb.append(obj.stringValue());
		}
		return sb.toString();
	}

	public static String escape(String s) {
		return s.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
	}

	public static String unescape(String s) {
		return s.replace("\\t", "\t").replace("\\r", "\r").replace("\\n", "\n").replace("\\\\", "\\");
	}

}
