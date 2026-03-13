package org.example.model;

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
    if (this == SPECULATOR) {
      return "Speculator: Requires 20 weeks of trading and a doubling of net worth";
    } else if (this == INVESTOR) {
      return "Investor: Requires 10 weeks of trading and 20% growth of net worth";
    } else {
      return "Novice: No requirements";
    }
  }

}
