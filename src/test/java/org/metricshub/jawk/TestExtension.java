package org.metricshub.jawk;

import org.metricshub.jawk.ext.AbstractExtension;
import org.metricshub.jawk.ext.JawkExtension;
import org.metricshub.jawk.jrt.AssocArray;
import org.metricshub.jawk.jrt.JRT;
import org.metricshub.jawk.jrt.VariableManager;
import org.metricshub.jawk.util.AwkSettings;

public class TestExtension extends AbstractExtension implements JawkExtension {

	private static final String MY_EXTENSION_FUNCTION = "myExtensionFunction";

	@Override
	public void init(VariableManager vm, JRT jrt, AwkSettings settings) {
	}

	@Override
	public String getExtensionName() {
		return "TestExtension";
	}

	@Override
	public String[] extensionKeywords() {
		return new String[] { MY_EXTENSION_FUNCTION };
	}

	@Override
	public int[] getAssocArrayParameterPositions(String extensionKeyword, int numArgs) {
		if (MY_EXTENSION_FUNCTION.equals(extensionKeyword)) {
			return new int[] { 1 };
		} else {
			return new int[] {};
		}
	}

	@Override
	public Object invoke(String keyword, Object[] args) {
		if (MY_EXTENSION_FUNCTION.equals(keyword)) {
			StringBuilder result = new StringBuilder();
			int count = ((Long)args[0]).intValue();
			AssocArray array = (AssocArray)args[1];
			for (int i = 0 ; i < count ; i++) {
				for (Object item : array.keySet()) {
					result.append((String)array.get(item));
				}
			}
			return result.toString();
		} else {
			throw new NotImplementedError(keyword + " is not implemented by " + getExtensionName());
		}
	}

}
