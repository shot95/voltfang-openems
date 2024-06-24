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
		name = CheckComponentInstalledOfStartingIdAvailable.COMPONENT_NAME, //
		scope = ServiceScope.PROTOTYPE //
)
public class CheckComponentInstalledOfStartingIdAvailable extends AbstractCheckable implements Checkable {

	public static final String COMPONENT_NAME = "Validator.Checkable.CheckComponentInstalledOfStartingIdAvailable";
	public static final String STARTING_ID = "startingId";

	private final ComponentManager componentManager;

	private String startingId;

	@Activate
	public CheckComponentInstalledOfStartingIdAvailable(@Reference ComponentManager componentManager,
			ComponentContext componentContext) {
		super(componentContext);
		this.componentManager = componentManager;
	}

	@Override
	public void setProperties(Map<String, ?> properties) {
		this.startingId = (String) properties.get(STARTING_ID);
	}

	@Override
	public boolean check() {
		return this.componentManager.getEdgeConfig().getComponents().keySet().stream() //
				.anyMatch(entry -> entry.startsWith(this.startingId));
	}

	@Override
	public String getErrorMessage(Language language) {
		return AbstractCheckable.getTranslation(language, COMPONENT_NAME + ".Message", this.startingId);
	}
}
