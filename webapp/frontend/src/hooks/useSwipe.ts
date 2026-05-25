"use client";

import { useRef, useCallback } from "react";

interface SwipeHandlers {
  onSwipeLeft?: () => void;
  onSwipeRight?: () => void;
}

interface SwipeState {
  startX: number;
  startY: number;
  startTime: number;
}

const MIN_SWIPE_DISTANCE = 50;
const MAX_SWIPE_TIME = 500;
const MAX_VERTICAL_RATIO = 0.75; // Prevent triggering on scroll

/**
 * Hook to detect horizontal swipe gestures on touch devices.
 * Returns touch event handlers to spread onto a target element.
 */
export function useSwipe({ onSwipeLeft, onSwipeRight }: SwipeHandlers) {
  const swipeRef = useRef<SwipeState | null>(null);

  const onTouchStart = useCallback((e: React.TouchEvent) => {
    const touch = e.touches[0];
    swipeRef.current = {
      startX: touch.clientX,
      startY: touch.clientY,
      startTime: Date.now(),
    };
  }, []);

  const onTouchEnd = useCallback(
    (e: React.TouchEvent) => {
      if (!swipeRef.current) return;

      const touch = e.changedTouches[0];
      const deltaX = touch.clientX - swipeRef.current.startX;
      const deltaY = touch.clientY - swipeRef.current.startY;
      const elapsed = Date.now() - swipeRef.current.startTime;

      swipeRef.current = null;

      // Must be fast enough and long enough horizontally
      if (elapsed > MAX_SWIPE_TIME) return;
      if (Math.abs(deltaX) < MIN_SWIPE_DISTANCE) return;
      // Must be more horizontal than vertical
      if (Math.abs(deltaY) > Math.abs(deltaX) * MAX_VERTICAL_RATIO) return;

      if (deltaX < 0) {
        onSwipeLeft?.();
      } else {
        onSwipeRight?.();
      }
    },
    [onSwipeLeft, onSwipeRight]
  );

  return { onTouchStart, onTouchEnd };
}
