package com.hr.hrapp.util;

import java.time.LocalDate;
import java.time.YearMonth;

public final class PayrollMonthUtil {

    private PayrollMonthUtil() {
    }

    public static String format(YearMonth month) {
        return month.getMonth() + " " + month.getYear();
    }

    public static String format(LocalDate date) {
        return format(YearMonth.from(date));
    }
}
