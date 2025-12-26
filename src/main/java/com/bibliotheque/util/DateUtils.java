package com.bibliotheque.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.sql.Date;

public final class DateUtils {
	private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

	private DateUtils() {}

	public static LocalDate now() {
		return LocalDate.now();
	}

	public static String format(LocalDate date) {
		if (date == null) return null;
		return date.format(DEFAULT_FORMATTER);
	}

	public static String format(LocalDate date, DateTimeFormatter formatter) {
		if (date == null) return null;
		return date.format(formatter != null ? formatter : DEFAULT_FORMATTER);
	}

	public static LocalDate parse(String text) {
		if (text == null || text.trim().isEmpty()) return null;
		try {
			return LocalDate.parse(text.trim(), DEFAULT_FORMATTER);
		} catch (DateTimeParseException ex) {
			return null;
		}
	}

	public static LocalDate addDays(LocalDate date, long days) {
		if (date == null) return null;
		return date.plusDays(days);
	}

	public static long daysBetween(LocalDate from, LocalDate to) {
		if (from == null || to == null) return 0;
		return ChronoUnit.DAYS.between(from, to);
	}

	public static Date toSqlDate(LocalDate localDate) {
		return localDate == null ? null : Date.valueOf(localDate);
	}

	public static LocalDate fromSqlDate(Date sqlDate) {
		return sqlDate == null ? null : sqlDate.toLocalDate();
	}
}
