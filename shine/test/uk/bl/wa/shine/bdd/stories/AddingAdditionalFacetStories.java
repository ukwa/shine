package uk.bl.wa.shine.bdd.stories;

import java.util.Arrays;
import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;

/**
 * @author kli
 *
 */

public class AddingAdditionalFacetStories extends JUnitStory {

	@Override
	public Configuration configuration() {
		return super.configuration().useStoryReporterBuilder(
				new StoryReporterBuilder().withDefaultFormats().withFormats(
						Format.CONSOLE, Format.TXT));
	}

	// Here we specify the steps classes
	@Override
	public InjectableStepsFactory stepsFactory() {
		return new InstanceStepsFactory(configuration(), new AddFacetSteps());
	}

	public List<String> storyPaths() {
		return Arrays.asList("uk/bl/wa/shine/bdd/stories/adding_additional_facet_stories.story");
	}
}
