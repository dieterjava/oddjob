/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.SimpleDesignProperty;
import org.oddjob.arooa.design.SimpleTextAttribute;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;

/**
 * 
 */
public class DeleteDC implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new DeleteDesign(element, parentContext);
	}
		
}

class DeleteDesign extends BaseDC {

	private final SimpleDesignProperty files;
	
	private final SimpleTextAttribute force;
	
	public DeleteDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		files = new SimpleDesignProperty(
				"files", this);
		
		force = new SimpleTextAttribute("force", this);
	}

	public DesignProperty[] children() {
		return new DesignProperty[] { name, files, force };
	}
	
	public Form detail() {
		return new StandardForm(this)
			.addFormItem(basePanel())
			.addFormItem(
					new BorderedGroup("File/Directory Details")
					.add(files.view().setTitle("File(s)/Dir(s)"))
					.add(force.view().setTitle("Force")));
	}
		
	
}
