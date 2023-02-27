package wbs.magic.spellmanagement.configuration.options;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class OptionParameter {
    private final String optionName;
    private final List<String> suggestions = new LinkedList<>();

    public OptionParameter(@NotNull String optionName) {
        this.optionName = optionName;
    }

    public OptionParameter(@NotNull String optionName, @NotNull Collection<String> suggestions) {
        this(optionName);
        this.suggestions.addAll(suggestions);
    }

    public OptionParameter(@NotNull String optionName, @NotNull Object suggestion) {
        this(optionName, Collections.singletonList(suggestion.toString()));
    }

    public OptionParameter(@NotNull String optionName, @NotNull Object ... suggestions) {
        this(optionName, Arrays.stream(suggestions).map(Object::toString).collect(Collectors.toList()));
    }

    public String getOptionName() {
        return optionName;
    }

    public List<String> getSuggestions() {
        return Collections.unmodifiableList(suggestions);
    }

    public void addSuggestion(String suggestion) {
        suggestions.add(suggestion);
    }
}
