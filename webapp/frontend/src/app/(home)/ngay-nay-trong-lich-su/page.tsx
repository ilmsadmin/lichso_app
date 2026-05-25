"use client";

import { useMemo } from "react";
import Link from "next/link";
import { Calendar, Clock, Crown, Landmark, Sparkles, MessageSquareQuote, ArrowRight } from "lucide-react";
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
import type { EventType } from "@/types/event";

const eventTypeLabels: Record<EventType, string> = {
  historical_event: "Lịch sử",
  national_day: "Ngày quốc gia",
  world_day: "Ngày quốc tế",
  anniversary: "Kỷ niệm",
  cultural: "Văn hóa",
  military: "Quân sự",
};

export default function TodayInHistoryPage() {
  const today = useMemo(() => new Date(), []);
  const month = today.getMonth() + 1;
  const day = today.getDate();

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

  const dateStr = today.toLocaleDateString("vi-VN", {
    weekday: "long",
    year: "numeric",
    month: "long",
    day: "numeric",
  });

  return (
    <div className="mx-auto max-w-[900px] px-4 py-8 sm:px-6">
      {/* Header */}
      <div className="mb-10 text-center">
        <div className="bg-warm-amber/10 text-warm-amber mb-4 inline-flex items-center gap-2 rounded-full px-4 py-1.5 text-sm font-medium">
          <Calendar className="h-4 w-4" />
          {dateStr}
        </div>
        <h1 className="text-text-dark mb-2 text-3xl font-[var(--font-lora)] font-bold sm:text-4xl">
          Ngày Này Trong Lịch Sử
        </h1>
        <p className="text-text-soft">
          Những sự kiện, nhân vật và lễ hội đáng nhớ trong ngày {day}/{month}
        </p>
      </div>

      {/* Quote of the Day */}
      {quoteLoading ? (
        <Skeleton className="mb-8 h-40 rounded-2xl" />
      ) : quote ? (
        <div
          className="mb-10 rounded-2xl p-8"
          style={{
            background: "linear-gradient(135deg, var(--warm-cream) 0%, var(--warm-peach) 100%)",
            border: "1px solid var(--ls-border-warm)",
          }}
        >
          <div className="mb-3 flex items-center gap-2">
            <MessageSquareQuote className="text-warm-amber/60 h-5 w-5" />
            <span className="text-warm-amber text-xs font-medium tracking-widest uppercase">
              Danh ngôn hôm nay
            </span>
          </div>
          <blockquote className="text-text-dark mb-3 text-xl leading-relaxed font-[var(--font-lora)] italic">
            &ldquo;{quote.quote}&rdquo;
          </blockquote>
          <p className="text-text-dark text-sm font-medium">
            — {quote.author}
            {quote.author_bio && (
              <span className="text-text-soft font-normal"> ({quote.author_bio})</span>
            )}
          </p>
        </div>
      ) : null}

      {/* Events */}
      <section className="mb-10">
        <div className="mb-5 flex items-center gap-2">
          <Landmark className="text-warm-amber h-5 w-5" />
          <h2 className="text-text-dark text-xl font-[var(--font-lora)] font-semibold">
            Sự kiện trong ngày
          </h2>
          <Badge variant="secondary" className="ml-auto text-xs">
            {events.length}
          </Badge>
        </div>
        {eventsLoading ? (
          <div className="space-y-3">
            {Array.from({ length: 3 }).map((_, i) => (
              <Skeleton key={i} className="h-20 rounded-xl" />
            ))}
          </div>
        ) : events.length === 0 ? (
          <p
            className="text-text-soft rounded-xl border py-6 text-center text-sm"
            style={{ borderColor: "var(--ls-border-warm)" }}
          >
            Không có sự kiện nào được ghi nhận trong ngày này
          </p>
        ) : (
          <div className="space-y-3">
            {events.map((event) => (
              <Link
                key={event.id}
                href={`${ROUTES.EVENTS}/${event.slug}`}
                className="group hover:border-warm-amber/30 flex items-start gap-4 rounded-xl border p-4 transition-all hover:shadow-md"
                style={{
                  background: "var(--ls-card-bg-solid)",
                  borderColor: "var(--ls-border-warm)",
                }}
              >
                <div className="bg-warm-cream/80 flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-lg">
                  <Calendar className="text-warm-amber h-5 w-5" />
                </div>
                <div className="min-w-0 flex-1">
                  <div className="mb-1 flex items-center gap-2">
                    <Badge variant="outline" className="text-[10px]">
                      {eventTypeLabels[event.event_type]}
                    </Badge>
                    <span className="text-text-muted-ls text-xs">
                      {(() => {
                        const y = new Date(event.event_date).getFullYear();
                        return isNaN(y) ? event.event_date : y;
                      })()}
                    </span>
                  </div>
                  <h3 className="text-text-dark group-hover:text-warm-amber line-clamp-1 font-semibold transition-colors">
                    {event.title}
                  </h3>
                  {event.short_description && (
                    <p className="text-text-soft mt-0.5 line-clamp-1 text-sm">
                      {event.short_description}
                    </p>
                  )}
                </div>
                <ArrowRight className="text-text-muted-ls group-hover:text-warm-amber mt-4 h-4 w-4 flex-shrink-0 transition-colors" />
              </Link>
            ))}
          </div>
        )}
      </section>

      <Separator className="mb-10" />

      {/* Famous Birthdays */}
      <section className="mb-10">
        <div className="mb-5 flex items-center gap-2">
          <Crown className="text-warm-amber h-5 w-5" />
          <h2 className="text-text-dark text-xl font-[var(--font-lora)] font-semibold">
            Sinh nhật danh nhân
          </h2>
          <Badge variant="secondary" className="ml-auto text-xs">
            {birthdays.length}
          </Badge>
        </div>
        {birthdaysLoading ? (
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            {Array.from({ length: 4 }).map((_, i) => (
              <Skeleton key={i} className="h-20 rounded-xl" />
            ))}
          </div>
        ) : birthdays.length === 0 ? (
          <p
            className="text-text-soft rounded-xl border py-6 text-center text-sm"
            style={{ borderColor: "var(--ls-border-warm)" }}
          >
            Không có danh nhân nào sinh vào ngày này
          </p>
        ) : (
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            {birthdays.map((person) => (
              <Link
                key={person.id}
                href={`${ROUTES.FAMOUS_PEOPLE}/${person.id}`}
                className="group hover:border-warm-amber/30 flex items-center gap-3 rounded-xl border p-4 transition-all hover:shadow-md"
                style={{
                  background: "var(--ls-card-bg-solid)",
                  borderColor: "var(--ls-border-warm)",
                }}
              >
                <div className="bg-warm-cream/80 flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-full">
                  <Crown className="text-warm-amber h-5 w-5" />
                </div>
                <div className="min-w-0 flex-1">
                  <h3 className="text-text-dark group-hover:text-warm-amber line-clamp-1 text-sm font-semibold transition-colors">
                    {person.name}
                  </h3>
                  <p className="text-text-muted-ls line-clamp-1 text-xs">
                    {person.short_bio || person.nationality}
                  </p>
                  {person.birth_date && (
                    <span className="text-text-muted-ls text-[10px]">
                      Sinh năm{" "}
                      {(() => {
                        const y = new Date(person.birth_date).getFullYear();
                        return isNaN(y) ? person.birth_date : y;
                      })()}
                    </span>
                  )}
                </div>
                {person.is_vietnamese && (
                  <Badge className="bg-warm-amber/10 text-warm-amber flex-shrink-0 border-0 text-[10px]">
                    🇻🇳
                  </Badge>
                )}
              </Link>
            ))}
          </div>
        )}
      </section>

      <Separator className="mb-10" />

      {/* Festivals */}
      <section className="mb-10">
        <div className="mb-5 flex items-center gap-2">
          <Sparkles className="text-warm-amber h-5 w-5" />
          <h2 className="text-text-dark text-xl font-[var(--font-lora)] font-semibold">
            Lễ hội trong ngày
          </h2>
          <Badge variant="secondary" className="ml-auto text-xs">
            {festivals.length}
          </Badge>
        </div>
        {festivalsLoading ? (
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            {Array.from({ length: 2 }).map((_, i) => (
              <Skeleton key={i} className="h-24 rounded-xl" />
            ))}
          </div>
        ) : festivals.length === 0 ? (
          <p
            className="text-text-soft rounded-xl border py-6 text-center text-sm"
            style={{ borderColor: "var(--ls-border-warm)" }}
          >
            Không có lễ hội nào diễn ra trong ngày này
          </p>
        ) : (
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            {festivals.map((festival) => (
              <Link
                key={festival.id}
                href={`${ROUTES.FESTIVALS}/${festival.slug}`}
                className="group hover:border-warm-amber/30 rounded-xl border p-5 transition-all hover:shadow-md"
                style={{
                  background: "var(--ls-card-bg-solid)",
                  borderColor: "var(--ls-border-warm)",
                }}
              >
                <h3 className="text-text-dark group-hover:text-warm-amber mb-1 font-semibold transition-colors">
                  {festival.name}
                </h3>
                {festival.short_description && (
                  <p className="text-text-soft line-clamp-2 text-sm">
                    {festival.short_description}
                  </p>
                )}
                {festival.region && (
                  <div className="mt-2 flex flex-wrap gap-1">
                    <Badge variant="secondary" className="text-[10px]">
                      {festival.region}
                    </Badge>
                  </div>
                )}
              </Link>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
