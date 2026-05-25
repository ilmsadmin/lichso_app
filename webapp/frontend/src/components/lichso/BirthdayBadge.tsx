"use client";

import { Cake } from "lucide-react";
import { getImageUrl } from "@/lib/utils";

interface FamousPerson {
  id?: string;
  name: string;
  title?: string;
  birth_year?: number;
  death_year?: number;
  description?: string;
  avatar?: string;
}

interface BirthdayBadgeProps {
  people: FamousPerson[];
}

/**
 * Shows famous people's birthdays for the current date in DayDetailModal.
 */
export function BirthdayBadge({ people }: BirthdayBadgeProps) {
  if (!people || people.length === 0) return null;

  return (
    <div>
      <div className="mb-2.5 flex items-center gap-1.5">
        <Cake className="h-3.5 w-3.5 text-[#D4A020]" />
        <span className="text-[9px] font-semibold tracking-[2px] text-[#D4A020] uppercase">
          Sinh nhật nhân vật nổi tiếng
        </span>
      </div>

      <div className="flex flex-wrap gap-2">
        {people.map((person, idx) => (
          <div
            key={person.id ?? idx}
            className="flex items-center gap-2 rounded-lg px-2.5 py-1.5"
            style={{
              background: "rgba(212,160,32,0.05)",
              border: "1px solid rgba(212,160,32,0.2)",
            }}
          >
            {/* Avatar or emoji */}
            <div className="flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-[#D4A020]/10">
              {person.avatar ? (
                <img
                  src={getImageUrl(person.avatar)}
                  alt={person.name}
                  className="h-7 w-7 rounded-full object-cover"
                />
              ) : (
                <span className="text-xs">🎂</span>
              )}
            </div>

            <div className="min-w-0">
              <p className="text-text-dark max-w-[140px] truncate text-[11px] font-medium">
                {person.name}
              </p>
              <p className="text-text-muted-ls text-[9px]">
                {person.title && <span>{person.title}</span>}
                {person.birth_year && (
                  <span>
                    {person.title ? " · " : ""}
                    {person.birth_year}
                    {person.death_year ? `–${person.death_year}` : ""}
                  </span>
                )}
              </p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
