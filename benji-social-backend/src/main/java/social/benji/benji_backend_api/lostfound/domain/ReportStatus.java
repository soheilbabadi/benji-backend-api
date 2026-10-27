package social.benji.benji_backend_api.lostfound.domain;

/** Mirrors the lost_found_report_status DB enum. Reports are never deleted (audit trail). */
public enum ReportStatus { OPEN, DISMISSED, ACTIONED }
