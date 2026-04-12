package org.example.model;

/**
 * Enums for player status depending on time trading and growth of net worth.
 */
public enum Status {
  NOVICE("Novice"),
  INVESTOR("Investor"),
  SPECULATOR("Speculator");

  final String status;


  Status(String status) {
    this.status = status;
  }

  public String getStatus() {
    return status;
  }

  /**
   * Returns an explanation of the current status.
   *
   * @return String of explanation.
   */
  public String explainStatus() {
    return switch (this) {
      case SPECULATOR -> "Speculator: Requires 20 weeks of trading and a doubling of net worth";
      case INVESTOR -> "Investor: Requires 10 weeks of trading and 20% growth of net worth";
      default -> "Novice: No requirements";
    };
  }

}
