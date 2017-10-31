/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.taxonomy.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyModule;
import org.olat.modules.taxonomy.TaxonomyService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyConfigurationAdminController extends FormBasicController {

	private static final String[] onKeys = new String[] { "on" };
	
	private MultipleSelectionElement enableEl;
	private SingleSelection taxonomyTreeEl;
	private SingleSelection taxonomyQPoolEl;
	
	@Autowired
	private TaxonomyModule taxonomyModule;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private QuestionPoolModule qpoolModule;

	public TaxonomyConfigurationAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.configuration.title");
		
		String[] onValues = new String[] { translate("on") };
		enableEl = uifactory.addCheckboxesHorizontal("taxonomy.admin.enabled", formLayout, onKeys, onValues);
		enableEl.addActionListener(FormEvent.ONCHANGE);
		if(taxonomyModule.isEnabled()) {
			enableEl.select(onKeys[0], true);
		}
		
		String selectedTaxonomyTreeKey = taxonomyModule.getTaxonomyTreeKey();
		List<Taxonomy> taxonomyList = taxonomyService.getRootTaxonomyList();
		String[] taxonomyKeys = new String[taxonomyList.size() + 1];
		String[] taxonomyValues = new String[taxonomyList.size() + 1];
		taxonomyKeys[0] = "";
		taxonomyValues[0] = "-";
		for(int i=taxonomyList.size(); i-->0; ) {
			Taxonomy taxonomy = taxonomyList.get(i);
			taxonomyKeys[i + 1] = taxonomy.getKey().toString();
			taxonomyValues[i + 1] = taxonomy.getDisplayName();
		}
		taxonomyTreeEl = uifactory.addDropdownSingleselect("selected.taxonomy.tree", formLayout, taxonomyKeys, taxonomyValues, null);
		boolean found = false;
		if(StringHelper.containsNonWhitespace(selectedTaxonomyTreeKey)) {
			for(String taxonomyKey:taxonomyKeys) {
				if(taxonomyKey.equals(selectedTaxonomyTreeKey)) {
					taxonomyTreeEl.select(taxonomyKey, true);
					found = true;
				}
			}
		}
		if(!found && taxonomyKeys.length > 0) {
			taxonomyTreeEl.select(taxonomyKeys[0], true);
		}
		
		String selectedTaxonomyQPoolKey = qpoolModule.getTaxonomyQPoolKey();
		taxonomyQPoolEl = uifactory.addDropdownSingleselect("selected.taxonomy.qpool", formLayout, taxonomyKeys, taxonomyValues, null);
		taxonomyQPoolEl.setEnabled(false);
		if(StringHelper.containsNonWhitespace(selectedTaxonomyQPoolKey)) {
			for(String taxonomyKey:taxonomyKeys) {
				if(taxonomyKey.equals(selectedTaxonomyQPoolKey)) {
					taxonomyQPoolEl.select(taxonomyKey, true);
				}
			}
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		taxonomyTreeEl.clearError();
		if(!taxonomyTreeEl.isOneSelected()) {
			taxonomyTreeEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		taxonomyQPoolEl.clearError();
		if(!taxonomyQPoolEl.isOneSelected()) {
			taxonomyTreeEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = enableEl.isAtLeastSelected(1);
		taxonomyModule.setEnabled(enabled);
		
		String selectedTaxonomyTreeKey = taxonomyTreeEl.getSelectedKey();
		taxonomyModule.setTaxonomyTreeKey(selectedTaxonomyTreeKey);
		
		String selectedTaxonomyQPoolKey = taxonomyQPoolEl.getSelectedKey();
		qpoolModule.setTaxonomyQPoolKey(selectedTaxonomyQPoolKey);
	}
}
