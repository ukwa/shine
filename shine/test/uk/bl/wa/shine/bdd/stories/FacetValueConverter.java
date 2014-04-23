package uk.bl.wa.shine.bdd.stories;

import java.lang.reflect.Type;

import org.jbehave.core.steps.ParameterConverters.ParameterConverter;

import uk.bl.wa.shine.model.FacetValue;

/**
 * @author kli
 *
 */

public class FacetValueConverter implements ParameterConverter {

	
	@Override
	public boolean accept(Type type) {
        if (type instanceof Class<?>) {
            return FacetValue.class.isAssignableFrom((Class<?>) type);
        }
        return false;
	}

	@Override
	public Object convertValue(String value, Type type) {
		return new FacetValue("domain", "Domain");
	}

}
