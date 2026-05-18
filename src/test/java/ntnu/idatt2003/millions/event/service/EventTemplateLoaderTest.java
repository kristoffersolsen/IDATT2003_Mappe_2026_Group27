package ntnu.idatt2003.millions.event.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import ntnu.idatt2003.millions.event.model.EventSeverity;
import ntnu.idatt2003.millions.event.model.EventTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("EventTemplateLoader")
class EventTemplateLoaderTest {

  @Nested
  @DisplayName("loadFromClasspath")
  class LoadFromClasspath {

    @Test
    @DisplayName("loads all templates from the bundled events.json")
    void loadFromClasspath_loadsTemplates() throws IOException {
      List<EventTemplate> templates = EventTemplateLoader.loadFromClasspath();
      assertTrue(templates.size() > 0);
    }

    @Test
    @DisplayName("bundled file has at least one template per severity")
    void loadFromClasspath_allSeveritiesPresent() throws IOException {
      List<EventTemplate> templates = EventTemplateLoader.loadFromClasspath();
      for (EventSeverity severity : EventSeverity.values()) {
        assertTrue(
            templates.stream().anyMatch(t -> t.severity() == severity),
            "No template found for severity " + severity);
      }
    }
  }

  @Nested
  @DisplayName("valid JSON")
  class ValidJson {

    @Test
    @DisplayName("parses a minimal valid file with all three severities")
    void loadFrom_validFile_returnsTemplates() throws IOException {
      List<EventTemplate> templates =
          EventTemplateLoader.loadFrom("/data/events/valid_events.json");
      assertEquals(3, templates.size());
    }

    @Test
    @DisplayName("returned list is unmodifiable")
    void loadFrom_returnsUnmodifiableList() throws IOException {
      List<EventTemplate> templates =
          EventTemplateLoader.loadFrom("/data/events/valid_events.json");
      assertThrows(UnsupportedOperationException.class,
          () -> templates.add(templates.get(0)));
    }
  }

  @Nested
  @DisplayName("validation failures")
  class ValidationFailures {

    @Test
    @DisplayName("throws IOException for unknown sector name")
    void loadFrom_unknownSector_throws() {
      assertThrows(IOException.class,
          () -> EventTemplateLoader.loadFrom("/data/events/unknown_sector.json"));
    }

    @Test
    @DisplayName("throws IOException for unknown severity name")
    void loadFrom_unknownSeverity_throws() {
      assertThrows(IOException.class,
          () -> EventTemplateLoader.loadFrom("/data/events/unknown_severity.json"));
    }

    @Test
    @DisplayName("throws IOException when a severity has zero templates")
    void loadFrom_missingSeverity_throws() {
      assertThrows(IOException.class,
          () -> EventTemplateLoader.loadFrom("/data/events/missing_severity.json"));
    }

    @Test
    @DisplayName("throws IOException when a required field is missing")
    void loadFrom_missingField_throws() {
      assertThrows(IOException.class,
          () -> EventTemplateLoader.loadFrom("/data/events/missing_fields.json"));
    }

    @Test
    @DisplayName("throws IOException for a non-existent resource path")
    void loadFrom_missingResource_throws() {
      assertThrows(IOException.class,
          () -> EventTemplateLoader.loadFrom("/data/events/does_not_exist.json"));
    }
  }
}
