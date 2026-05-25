"use client";

import Link from "next/link";
import { Calendar, Star, Landmark, PartyPopper, Quote, ChevronRight } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Separator } from "@/components/ui/separator";
import {
  useQuoteOfTheDay,
  useEventsByDate,
  useFamousPeopleByBirthday,
  useFestivalsBySolarDate,
} from "@/hooks/usePublicContent";
import { ROUTES } from "@/lib/constants";

interface DateContentPanelProps {
  /** Solar month (1–12) */
  month: number;
  /** Solar day (1–31) */
  day: number;
  /** Optional: show compact view */
  compact?: boolean;
}

/**
 * Shows content related to a specific date:
 * - Quote of the day
 * - Events on that date
 * - Famous birthdays on that date
 * - Folk festivals on that date
 */
export function DateContentPanel({ month, day, compact = false }: DateContentPanelProps) {
  const { data: quoteData, isLoading: quoteLoading } = useQuoteOfTheDay();
  const { data: eventsData, isLoading: eventsLoading } = useEventsByDate(month, day);
  const { data: birthdaysData, isLoading: birthdaysLoading } = useFamousPeopleByBirthday(
    month,
    day
  );
  const { data: festivalsData, isLoading: festivalsLoading } = useFestivalsBySolarDate(month, day);

  const quote = quoteData?.data;
  const events = eventsData?.data ?? [];
  const birthdays = birthdaysData?.data ?? [];
  const festivals = festivalsData?.data ?? [];

  const totalItems = events.length + birthdays.length + festivals.length;

  if (
    !quoteLoading &&
    !eventsLoading &&
    !birthdaysLoading &&
    !festivalsLoading &&
    !quote &&
    totalItems === 0
  ) {
    return null;
  }

  return (
    <div className="space-y-4">
      {/* Quote */}
      {quoteLoading ? (
        <Skeleton className="h-20 rounded-xl" />
      ) : quote ? (
        <div
          className="rounded-xl p-4"
          style={{
            background: "linear-gradient(135deg, var(--warm-cream) 0%, var(--warm-peach) 50%)",
            border: "1px solid var(--ls-border-warm)",
          }}
        >
          <div className="mb-2 flex items-center gap-1.5">
            <Quote className="text-warm-amber/60 h-3.5 w-3.5" />
            <span className="text-warm-amber text-[10px] font-medium tracking-wider uppercase">
              Danh ngôn
            </span>
          </div>
          <p className="text-text-dark line-clamp-3 text-sm leading-relaxed font-[var(--font-lora)] italic">
            &ldquo;{quote.quote}&rdquo;
          </p>
          <p className="text-text-soft mt-1 text-xs">— {quote.author}</p>
        </div>
      ) : null}

      {/* Events */}
      {eventsLoading ? (
        <Skeleton className="h-16 rounded-xl" />
      ) : events.length > 0 ? (
        <div>
          <div className="mb-2 flex items-center gap-1.5">
            <Landmark className="text-warm-amber h-3.5 w-3.5" />
            <span className="text-text-dark text-xs font-medium">Sự kiện ({events.length})</span>
          </div>
          <div className="space-y-1.5">
            {events.slice(0, compact ? 3 : 5).map((event) => (
              <Link
                key={event.id}
                href={`${ROUTES.EVENTS}/${event.slug}`}
                className="hover:bg-warm-amber/5 flex items-center gap-2 rounded-lg px-3 py-2 text-sm transition-colors"
              >
                <Calendar className="text-text-muted-ls h-3 w-3 flex-shrink-0" />
                <span className="text-text-mid line-clamp-1 flex-1">{event.title}</span>
                <span className="text-text-muted-ls flex-shrink-0 text-[10px]">
                  {new Date(event.event_date).getFullYear()}
                </span>
              </Link>
            ))}
            {events.length > (compact ? 3 : 5) && (
              <Link
                href={ROUTES.TODAY_IN_HISTORY}
                className="text-warm-amber flex items-center gap-1 px-3 text-xs hover:underline"
              >
                Xem thêm <ChevronRight className="h-3 w-3" />
              </Link>
            )}
          </div>
        </div>
      ) : null}

      {/* Birthdays */}
      {birthdaysLoading ? (
        <Skeleton className="h-12 rounded-xl" />
      ) : birthdays.length > 0 ? (
        <div>
          <div className="mb-2 flex items-center gap-1.5">
            <Star className="text-warm-amber h-3.5 w-3.5" />
            <span className="text-text-dark text-xs font-medium">
              Sinh nhật ({birthdays.length})
            </span>
          </div>
          <div className="space-y-1.5">
            {birthdays.slice(0, compact ? 3 : 5).map((person) => (
              <Link
                key={person.id}
                href={`${ROUTES.FAMOUS_PEOPLE}/${person.id}`}
                className="hover:bg-warm-amber/5 flex items-center gap-2 rounded-lg px-3 py-2 text-sm transition-colors"
              >
                <span className="flex-shrink-0 text-sm">👤</span>
                <span className="text-text-mid line-clamp-1 flex-1">{person.name}</span>
                {person.is_vietnamese && <span className="flex-shrink-0 text-[10px]">🇻🇳</span>}
              </Link>
            ))}
          </div>
        </div>
      ) : null}

      {/* Festivals */}
      {festivalsLoading ? (
        <Skeleton className="h-12 rounded-xl" />
      ) : festivals.length > 0 ? (
        <div>
          <div className="mb-2 flex items-center gap-1.5">
            <PartyPopper className="text-warm-amber h-3.5 w-3.5" />
            <span className="text-text-dark text-xs font-medium">Lễ hội ({festivals.length})</span>
          </div>
          <div className="space-y-1.5">
            {festivals.slice(0, compact ? 2 : 5).map((festival) => (
              <Link
                key={festival.id}
                href={`${ROUTES.FESTIVALS}/${festival.slug}`}
                className="hover:bg-warm-amber/5 flex items-center gap-2 rounded-lg px-3 py-2 text-sm transition-colors"
              >
                <span className="flex-shrink-0 text-sm">🏮</span>
                <span className="text-text-mid line-clamp-1 flex-1">{festival.name}</span>
              </Link>
            ))}
          </div>
        </div>
      ) : null}

      {/* View full page link */}
      <Link
        href={ROUTES.TODAY_IN_HISTORY}
        className="text-warm-amber flex items-center justify-center gap-1.5 pt-2 text-xs font-medium hover:underline"
      >
        Xem tất cả nội dung ngày này
        <ChevronRight className="h-3.5 w-3.5" />
      </Link>
    </div>
  );
}
