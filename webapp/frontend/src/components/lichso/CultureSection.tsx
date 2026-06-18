"use client";

import Link from "next/link";
import { Clock, Eye, MapPin, ArrowRight, CalendarDays } from "lucide-react";
import {
  usePublicArticles,
  usePublicFamousPeople,
  usePublicEvents,
  usePublicFolkFestivals,
} from "@/hooks/usePublicContent";
import { ROUTES } from "@/lib/constants";
import { getImageUrl } from "@/lib/utils";

/**
 * "Đặc trưng văn hóa Việt" — tổng hợp nội dung từ database:
 * Bài viết · Nhân vật nổi tiếng · Sự kiện lịch sử · Lễ hội dân gian.
 */
export function CultureSection() {
  const articles = usePublicArticles({ limit: 6, sort_by: "published_at", sort_order: "desc" });
  const people = usePublicFamousPeople({ limit: 6 });
  const events = usePublicEvents({ limit: 5, sort_by: "event_month", sort_order: "asc" });
  const festivals = usePublicFolkFestivals({ limit: 5 });

  const articleList = articles.data?.data ?? [];
  const peopleList = people.data?.data ?? [];
  const eventList = events.data?.data ?? [];
  const festivalList = festivals.data?.data ?? [];

  return (
    <section>
      <SectionHeading
        title="Đặc trưng văn hóa Việt"
        subtitle="Bài viết, nhân vật, sự kiện và lễ hội — tinh hoa văn hóa dân tộc"
      />

      {/* ── Bài viết nổi bật ── */}
      <GroupHeader title="Bài viết nổi bật" href={ROUTES.ARTICLES} />
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {articles.isLoading
          ? skeletons(3, "h-64 rounded-2xl")
          : articleList.slice(0, 6).map((a) => (
              <Link
                key={a.id}
                href={`${ROUTES.ARTICLES}/${a.slug}`}
                className="group overflow-hidden rounded-2xl transition-all hover:-translate-y-1"
                style={{
                  background: "var(--v2-bg-card)",
                  border: "1px solid var(--v2-border-primary)",
                  boxShadow: "var(--v2-shadow-xs)",
                }}
              >
                <Thumb
                  src={a.featured_image}
                  label={a.category?.name || "Bài viết"}
                  ratio="16/10"
                />
                <div className="p-4">
                  {a.category?.name && (
                    <span
                      className="text-[11px] font-semibold tracking-wide uppercase"
                      style={{ color: "var(--v2-text-accent)" }}
                    >
                      {a.category.name}
                    </span>
                  )}
                  <h4
                    className="mt-1 line-clamp-2 text-[15px] leading-snug font-semibold"
                    style={{ color: "var(--v2-text-primary)" }}
                  >
                    {a.title}
                  </h4>
                  <p
                    className="mt-1.5 line-clamp-2 text-[13px]"
                    style={{ color: "var(--v2-text-muted)" }}
                  >
                    {a.excerpt}
                  </p>
                  <div
                    className="mt-3 flex items-center gap-3 text-[12px]"
                    style={{ color: "var(--v2-text-muted)" }}
                  >
                    {a.reading_time > 0 && (
                      <span className="inline-flex items-center gap-1">
                        <Clock className="h-3.5 w-3.5" /> {a.reading_time} phút
                      </span>
                    )}
                    <span className="inline-flex items-center gap-1">
                      <Eye className="h-3.5 w-3.5" /> {a.view_count}
                    </span>
                  </div>
                </div>
              </Link>
            ))}
      </div>

      {/* ── Nhân vật nổi tiếng ── */}
      <GroupHeader title="Nhân vật nổi tiếng" href={ROUTES.FAMOUS_PEOPLE} className="mt-9" />
      <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-6">
        {people.isLoading
          ? skeletons(6, "h-44 rounded-2xl")
          : peopleList.slice(0, 6).map((p) => (
              <Link
                key={p.id}
                href={`${ROUTES.FAMOUS_PEOPLE}/${p.id}`}
                className="group flex flex-col items-center rounded-2xl p-4 text-center transition-all hover:-translate-y-1"
                style={{
                  background: "var(--v2-bg-card)",
                  border: "1px solid var(--v2-border-primary)",
                  boxShadow: "var(--v2-shadow-xs)",
                }}
              >
                <Avatar src={p.image_url} name={p.name} />
                <h4
                  className="mt-2.5 line-clamp-1 text-[14px] font-semibold"
                  style={{ color: "var(--v2-text-primary)" }}
                >
                  {p.name}
                </h4>
                <p className="line-clamp-1 text-[12px]" style={{ color: "var(--v2-text-muted)" }}>
                  {p.nationality || p.category}
                </p>
              </Link>
            ))}
      </div>

      {/* ── Sự kiện & Lễ hội ── */}
      <div className="mt-9 grid gap-6 lg:grid-cols-2">
        {/* Sự kiện lịch sử */}
        <div>
          <GroupHeader title="Sự kiện lịch sử" href={ROUTES.EVENTS} />
          <div className="flex flex-col gap-2.5">
            {events.isLoading
              ? skeletons(4, "h-16 rounded-xl")
              : eventList.slice(0, 5).map((e) => (
                  <Link
                    key={e.id}
                    href={`${ROUTES.EVENTS}/${e.slug}`}
                    className="group flex items-center gap-3 rounded-xl p-3 transition-all hover:-translate-y-0.5"
                    style={{
                      background: "var(--v2-bg-card)",
                      border: "1px solid var(--v2-border-primary)",
                    }}
                  >
                    <DateBadge day={e.event_day} month={e.event_month} />
                    <div className="min-w-0 flex-1">
                      <h4
                        className="line-clamp-1 text-[14px] font-semibold"
                        style={{ color: "var(--v2-text-primary)" }}
                      >
                        {e.title}
                      </h4>
                      <p
                        className="line-clamp-1 text-[12px]"
                        style={{ color: "var(--v2-text-muted)" }}
                      >
                        {e.is_lunar ? "Âm lịch" : "Dương lịch"} · {e.event_day}/{e.event_month}
                        {e.country ? ` · ${e.country}` : ""}
                      </p>
                    </div>
                    <ArrowRight
                      className="h-4 w-4 shrink-0 opacity-0 transition-opacity group-hover:opacity-100"
                      style={{ color: "var(--v2-text-accent)" }}
                    />
                  </Link>
                ))}
          </div>
        </div>

        {/* Lễ hội dân gian */}
        <div>
          <GroupHeader title="Lễ hội dân gian" href={ROUTES.FESTIVALS} />
          <div className="flex flex-col gap-2.5">
            {festivals.isLoading
              ? skeletons(4, "h-16 rounded-xl")
              : festivalList.slice(0, 5).map((f) => (
                  <Link
                    key={f.id}
                    href={`${ROUTES.FESTIVALS}/${f.slug}`}
                    className="group flex items-center gap-3 rounded-xl p-3 transition-all hover:-translate-y-0.5"
                    style={{
                      background: "var(--v2-bg-card)",
                      border: "1px solid var(--v2-border-primary)",
                    }}
                  >
                    <div
                      className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl text-lg"
                      style={{ background: "var(--v2-bg-gold-soft)" }}
                    >
                      🏮
                    </div>
                    <div className="min-w-0 flex-1">
                      <h4
                        className="line-clamp-1 text-[14px] font-semibold"
                        style={{ color: "var(--v2-text-primary)" }}
                      >
                        {f.name}
                      </h4>
                      <p
                        className="line-clamp-1 text-[12px]"
                        style={{ color: "var(--v2-text-muted)" }}
                      >
                        <MapPin className="mr-1 inline h-3 w-3" />
                        {f.region}
                        {f.lunar_day && f.lunar_month
                          ? ` · ${f.lunar_day}/${f.lunar_month} âm lịch`
                          : ""}
                      </p>
                    </div>
                    <ArrowRight
                      className="h-4 w-4 shrink-0 opacity-0 transition-opacity group-hover:opacity-100"
                      style={{ color: "var(--v2-text-accent)" }}
                    />
                  </Link>
                ))}
          </div>
        </div>
      </div>
    </section>
  );
}

/* ─────────── sub-components ─────────── */

function SectionHeading({ title, subtitle }: { title: string; subtitle?: string }) {
  return (
    <div className="mb-5 flex items-center gap-3">
      <span
        className="h-7 w-1 rounded-full"
        style={{ background: "linear-gradient(var(--v2-bg-accent), var(--v2-bg-gold))" }}
      />
      <div>
        <h2 className="font-playfair text-xl font-bold" style={{ color: "var(--v2-text-primary)" }}>
          {title}
        </h2>
        {subtitle && (
          <p className="text-[13px]" style={{ color: "var(--v2-text-muted)" }}>
            {subtitle}
          </p>
        )}
      </div>
    </div>
  );
}

function GroupHeader({
  title,
  href,
  className = "",
}: {
  title: string;
  href: string;
  className?: string;
}) {
  return (
    <div className={`mb-3.5 flex items-center justify-between ${className}`}>
      <h3 className="text-[15px] font-bold" style={{ color: "var(--v2-text-primary)" }}>
        {title}
      </h3>
      <Link
        href={href}
        className="inline-flex items-center gap-1 text-[13px] font-medium transition-colors"
        style={{ color: "var(--v2-text-accent)" }}
      >
        Xem tất cả <ArrowRight className="h-3.5 w-3.5" />
      </Link>
    </div>
  );
}

function Thumb({ src, label, ratio }: { src?: string | null; label: string; ratio: string }) {
  const url = getImageUrl(src ?? undefined);
  return (
    <div className="relative w-full overflow-hidden" style={{ aspectRatio: ratio }}>
      {url ? (
        // eslint-disable-next-line @next/next/no-img-element
        <img
          src={url}
          alt={label}
          loading="lazy"
          className="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105"
        />
      ) : (
        <div
          className="flex h-full w-full items-center justify-center"
          style={{ background: "var(--v2-bg-hero)" }}
        >
          <span className="font-playfair text-3xl" style={{ color: "var(--v2-bg-gold)" }}>
            {label}
          </span>
        </div>
      )}
    </div>
  );
}

function Avatar({ src, name }: { src?: string | null; name: string }) {
  const url = getImageUrl(src ?? undefined);
  const initial = name?.trim().charAt(0).toUpperCase() || "?";
  return url ? (
    // eslint-disable-next-line @next/next/no-img-element
    <img
      src={url}
      alt={name}
      loading="lazy"
      className="h-16 w-16 rounded-full object-cover ring-2 ring-offset-2"
      style={{ ["--tw-ring-color" as string]: "var(--v2-bg-gold)" }}
    />
  ) : (
    <div
      className="font-playfair flex h-16 w-16 items-center justify-center rounded-full text-2xl font-bold text-white"
      style={{ background: "var(--v2-bg-hero)" }}
    >
      {initial}
    </div>
  );
}

function DateBadge({ day, month }: { day: number; month: number }) {
  return (
    <div
      className="flex h-11 w-11 shrink-0 flex-col items-center justify-center rounded-xl"
      style={{ background: "var(--v2-bg-accent-soft)" }}
    >
      <span
        className="text-[15px] leading-none font-bold"
        style={{ color: "var(--v2-text-accent)" }}
      >
        {day || "—"}
      </span>
      <span className="text-[9px] font-medium" style={{ color: "var(--v2-text-accent)" }}>
        <CalendarDays className="inline h-2.5 w-2.5" /> {month}
      </span>
    </div>
  );
}

function skeletons(n: number, cls: string) {
  return Array.from({ length: n }).map((_, i) => (
    <div key={i} className={`animate-pulse ${cls}`} style={{ background: "var(--v2-bg-hover)" }} />
  ));
}
