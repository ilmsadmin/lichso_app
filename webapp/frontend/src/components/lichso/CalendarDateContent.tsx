"use client";

import { useMemo } from "react";
import {
  useEventsByDate,
  useFamousPeopleByBirthday,
  useFestivalsBySolarDate,
} from "@/hooks/usePublicContent";

interface CalendarDateContentProps {
  /** Solar month (1–12) */
  month: number;
  /** Solar day (1–31) */
  day: number;
}

/**
 * Small content indicator dots for a calendar date cell.
 * Shows colored dots to indicate events, birthdays, and festivals.
 */
export function CalendarDateContent({ month, day }: CalendarDateContentProps) {
  const { data: eventsData } = useEventsByDate(month, day);
  const { data: birthdaysData } = useFamousPeopleByBirthday(month, day);
  const { data: festivalsData } = useFestivalsBySolarDate(month, day);

  const events = eventsData?.data ?? [];
  const birthdays = birthdaysData?.data ?? [];
  const festivals = festivalsData?.data ?? [];

  const dots = useMemo(() => {
    const result: Array<{ color: string; title: string }> = [];
    if (events.length > 0) {
      result.push({ color: "bg-blue-400", title: `${events.length} sự kiện` });
    }
    if (birthdays.length > 0) {
      result.push({
        color: "bg-amber-400",
        title: `${birthdays.length} sinh nhật`,
      });
    }
    if (festivals.length > 0) {
      result.push({
        color: "bg-emerald-400",
        title: `${festivals.length} lễ hội`,
      });
    }
    return result;
  }, [events.length, birthdays.length, festivals.length]);

  if (dots.length === 0) return null;

  return (
    <div className="mt-0.5 flex items-center justify-center gap-0.5">
      {dots.map((dot, i) => (
        <span
          key={i}
          className={`inline-block h-1 w-1 rounded-full ${dot.color}`}
          title={dot.title}
        />
      ))}
    </div>
  );
}
