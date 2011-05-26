package org.oddjob.values;

import org.oddjob.arooa.ArooaBeanDescriptor;
import org.oddjob.arooa.ArooaConstants;
import org.oddjob.arooa.ConfiguredHow;
import org.oddjob.arooa.ParsingInterceptor;

public class VariablesJobArooa implements ArooaBeanDescriptor {

	public String getComponentProperty() {
		return null;
	}

	public ParsingInterceptor getParsingInterceptor() {
		return null;
	}

	public String getTextProperty() {
		return null;
	}

	public ConfiguredHow getConfiguredHow(String property) {
		if (ArooaConstants.ID_PROPERTY.equals(property)) {
			return ConfiguredHow.ATTRIBUTE;
		}
		return ConfiguredHow.ELEMENT;
	}

	public boolean isAuto(String property) {
		return false;
	}
	
	public String getFlavour(String property) {
		return null;
	}
}
