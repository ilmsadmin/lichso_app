"use client";

import { Flower2 } from "lucide-react";

interface Festival {
  id?: string;
  name: string;
  description?: string;
  lunar_day?: number;
  lunar_month?: number;
  solar_day?: number;
  solar_month?: number;
  region?: string;
}

interface FestivalCardProps {
  festivals: Festival[];
}

/**
 * Shows folk festivals for the current date inside DayDetailModal.
 */
export function FestivalCard({ festivals }: FestivalCardProps) {
  if (!festivals || festivals.length === 0) return null;

  return (
    <div>
      <div className="mb-2.5 flex items-center gap-1.5">
        <Flower2 className="h-3.5 w-3.5 text-[#C06090]" />
        <span className="text-[9px] font-semibold tracking-[2px] text-[#C06090] uppercase">
          Lễ hội & Phong tục
        </span>
      </div>

      <div className="space-y-2">
        {festivals.map((festival, idx) => (
          <div
            key={festival.id ?? idx}
            className="rounded-lg px-3 py-2.5"
            style={{
              background: "rgba(192,96,144,0.04)",
              border: "1px solid rgba(192,96,144,0.15)",
            }}
          >
            <div className="flex items-start gap-2">
              <span className="mt-0.5 text-sm">🏮</span>
              <div className="min-w-0 flex-1">
                <h5 className="text-text-dark text-[12px] font-medium">{festival.name}</h5>
                {festival.description && (
                  <p className="text-text-soft mt-0.5 line-clamp-2 text-[11px]">
                    {festival.description}
                  </p>
                )}
                {festival.region && (
                  <span className="mt-1 inline-block rounded bg-[#C06090]/8 px-1.5 py-0.5 text-[9px] text-[#C06090]">
                    📍 {festival.region}
                  </span>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
