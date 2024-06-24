package io.openems.edge.core.appmanager.validator;

import java.util.Map;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import io.openems.common.session.Language;
import io.openems.edge.common.component.ComponentManager;

@Component(//
		name = CheckComponentInstalledOfFactoryIdAvailable.COMPONENT_NAME, //
		scope = ServiceScope.PROTOTYPE //
)
public class CheckComponentInstalledOfFactoryIdAvailable extends AbstractCheckable implements Checkable {

	public static final String COMPONENT_NAME = "Validator.Checkable.CheckComponentInstalledOfFactoryIdAvailable";
	public static final String FACTORY_ID = "factoryId";

	private final ComponentManager componentManager;

	private String factoryId;

	@Activate
	public CheckComponentInstalledOfFactoryIdAvailable(@Reference ComponentManager componentManager,
			ComponentContext componentContext) {
		super(componentContext);
		this.componentManager = componentManager;
	}

	@Override
	public void setProperties(Map<String, ?> properties) {
		this.factoryId = (String) properties.get(FACTORY_ID);
	}

	@Override
	public boolean check() {
		return !this.componentManager.getEdgeConfig().getComponentIdsByFactory(this.factoryId).isEmpty();
	}

	@Override
	public String getErrorMessage(Language language) {
		return AbstractCheckable.getTranslation(language, COMPONENT_NAME + ".Message", this.factoryId);
	}

}
