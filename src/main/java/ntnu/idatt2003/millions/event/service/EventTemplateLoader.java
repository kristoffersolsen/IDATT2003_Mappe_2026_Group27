package ntnu.idatt2003.millions.event.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import ntnu.idatt2003.millions.event.model.EventSeverity;
import ntnu.idatt2003.millions.event.model.EventTemplate;
import ntnu.idatt2003.millions.market.model.Sector;

/**
 * Static utility that loads {@link EventTemplate} instances from the bundled
 * {@code data/events/events.json} resource.
 *
 * <p>Validates that:
 * <ul>
 *   <li>every template's {@code sectors} values are known {@link Sector} names;</li>
 *   <li>every template's {@code severity} is a known {@link EventSeverity} name;</li>
 *   <li>every template's {@code direction} is {@code "POSITIVE"} or {@code "NEGATIVE"};</li>
 *   <li>every severity level has at least one template.</li>
 * </ul>
 * An {@link IOException} with a clear message is thrown for any violation.
 */
public final class EventTemplateLoader {

  static final String EVENTS_PATH = "/data/events/events.json";

  private EventTemplateLoader() {
  }

  /**
   * Loads and validates all {@link EventTemplate}s from the classpath resource.
   *
   * @return an unmodifiable list of templates, grouped by severity
   * @throws IOException if the resource is missing, unparseable, or fails validation
   */
  public static List<EventTemplate> loadFromClasspath() throws IOException {
    return loadFrom(EVENTS_PATH);
  }

  /**
   * Loads and validates templates from the given classpath path.
   *
   * <p>Package-private to allow injection of a different path in tests.
   *
   * @param path classpath resource path
   * @return an unmodifiable list of validated templates
   * @throws IOException if the resource is missing, unparseable, or fails validation
   */
  static List<EventTemplate> loadFrom(String path) throws IOException {
    InputStream stream = EventTemplateLoader.class.getResourceAsStream(path);
    if (stream == null) {
      throw new IOException("Events resource not found on classpath: " + path);
    }
    EventsDto dto;
    try (stream) {
      dto = new ObjectMapper().readValue(stream, EventsDto.class);
    }
    if (dto.templates == null || dto.templates.isEmpty()) {
      throw new IOException("events.json contains no templates");
    }

    List<EventTemplate> result = new ArrayList<>();
    for (int i = 0; i < dto.templates.size(); i++) {
      result.add(parseTemplate(dto.templates.get(i), i));
    }

    validateSeverityCoverage(result);
    return List.copyOf(result);
  }

  private static EventTemplate parseTemplate(TemplateDto dto, int index) throws IOException {
    String prefix = "Template at index " + index + ": ";
    requireField(dto.id, prefix + "missing 'id'");
    requireField(dto.headline, prefix + "missing 'headline'");
    requireField(dto.body, prefix + "missing 'body'");
    if (dto.sectors == null || dto.sectors.isEmpty()) {
      throw new IOException(prefix + "missing 'sectors'");
    }

    Set<Sector> sectors = parseSectors(dto.sectors, prefix);
    EventSeverity severity = parseSeverity(dto.severity, prefix);
    boolean positive = parseDirection(dto.direction, prefix);

    return new EventTemplate(dto.id, dto.headline, dto.body, sectors, severity, positive);
  }

  private static Set<Sector> parseSectors(List<String> names, String prefix) throws IOException {
    List<Sector> sectors = new ArrayList<>();
    for (String name : names) {
      try {
        sectors.add(Sector.valueOf(name));
      } catch (IllegalArgumentException e) {
        throw new IOException(prefix + "unknown sector '" + name + "'");
      }
    }
    return Set.copyOf(sectors);
  }

  private static EventSeverity parseSeverity(String name, String prefix) throws IOException {
    if (name == null) {
      throw new IOException(prefix + "missing 'severity'");
    }
    try {
      return EventSeverity.valueOf(name);
    } catch (IllegalArgumentException e) {
      throw new IOException(prefix + "unknown severity '" + name + "'");
    }
  }

  private static boolean parseDirection(String name, String prefix) throws IOException {
    if (name == null) {
      throw new IOException(prefix + "missing 'direction'");
    }
    return switch (name) {
      case "POSITIVE" -> true;
      case "NEGATIVE" -> false;
      default -> throw new IOException(prefix + "unknown direction '" + name + "'");
    };
  }

  private static void validateSeverityCoverage(List<EventTemplate> templates) throws IOException {
    for (EventSeverity severity : EventSeverity.values()) {
      boolean found = templates.stream().anyMatch(t -> t.severity() == severity);
      if (!found) {
        throw new IOException(
            "events.json has no templates for severity " + severity.name());
      }
    }
  }

  private static void requireField(String value, String message) throws IOException {
    if (value == null || value.isBlank()) {
      throw new IOException(message);
    }
  }

  // ------------- Jackson DTOs -------------

  /** Top-level DTO mirroring the JSON root object. */
  static class EventsDto {
    public List<TemplateDto> templates;
  }

  /** Per-template DTO mirroring one entry in the {@code templates} array. */
  static class TemplateDto {
    public String id;
    public String headline;
    public String body;
    public List<String> sectors;
    public String severity;
    public String direction;
  }
}
