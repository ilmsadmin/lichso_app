"use client";

import { useEffect, useRef, useState } from "react";
import { Search, X } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";

interface SearchInputProps {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  debounceMs?: number;
  className?: string;
}

export function SearchInput({
  value,
  onChange,
  placeholder = "Search...",
  debounceMs = 300,
  className,
}: SearchInputProps) {
  const [localValue, setLocalValue] = useState(value);
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const onChangeRef = useRef(onChange);

  // Keep the callback ref up to date
  useEffect(() => {
    onChangeRef.current = onChange;
  }, [onChange]);

  // Sync from parent → local (e.g. when parent resets value)
  useEffect(() => {
    setLocalValue(value);
  }, [value]);

  // Debounced onChange: fires after user stops typing
  const handleInputChange = (newValue: string) => {
    setLocalValue(newValue);

    if (timerRef.current) {
      clearTimeout(timerRef.current);
    }

    timerRef.current = setTimeout(() => {
      onChangeRef.current(newValue);
    }, debounceMs);
  };

  // Clear timer on unmount
  useEffect(() => {
    return () => {
      if (timerRef.current) {
        clearTimeout(timerRef.current);
      }
    };
  }, []);

  return (
    <div className={`relative ${className ?? ""}`}>
      <Search className="text-muted-foreground absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2" />
      <Input
        value={localValue}
        onChange={(e) => handleInputChange(e.target.value)}
        placeholder={placeholder}
        className="pr-9 pl-9"
      />
      {localValue && (
        <Button
          variant="ghost"
          size="icon"
          className="absolute top-1/2 right-1 h-7 w-7 -translate-y-1/2"
          onClick={() => {
            setLocalValue("");
            if (timerRef.current) {
              clearTimeout(timerRef.current);
            }
            onChangeRef.current("");
          }}
        >
          <X className="h-4 w-4" />
        </Button>
      )}
    </div>
  );
}
