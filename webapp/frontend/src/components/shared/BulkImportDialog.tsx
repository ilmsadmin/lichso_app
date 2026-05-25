"use client";

import { useState, useRef, useCallback } from "react";
import {
  Upload,
  FileSpreadsheet,
  Download,
  Loader2,
  CheckCircle2,
  XCircle,
  AlertTriangle,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  DialogDescription,
} from "@/components/ui/dialog";
import { Badge } from "@/components/ui/badge";
import { ScrollArea } from "@/components/ui/scroll-area";
import { cn } from "@/lib/utils";

// ============================================
// Types
// ============================================

export interface CsvColumn {
  key: string;
  label: string;
  required?: boolean;
}

export interface ImportResult {
  total: number;
  success: number;
  failed: number;
  errors: string[];
}

interface BulkImportDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  title: string;
  description?: string;
  columns: CsvColumn[];
  onImport: (records: Record<string, string>[]) => Promise<ImportResult>;
  sampleData?: Record<string, string>[];
}

// ============================================
// CSV Parser
// ============================================

function parseCsv(text: string): string[][] {
  const rows: string[][] = [];
  let current = "";
  let inQuotes = false;
  let row: string[] = [];

  for (let i = 0; i < text.length; i++) {
    const char = text[i];
    const next = text[i + 1];

    if (inQuotes) {
      if (char === '"' && next === '"') {
        current += '"';
        i++; // skip next quote
      } else if (char === '"') {
        inQuotes = false;
      } else {
        current += char;
      }
    } else {
      if (char === '"') {
        inQuotes = true;
      } else if (char === ",") {
        row.push(current.trim());
        current = "";
      } else if (char === "\n" || (char === "\r" && next === "\n")) {
        row.push(current.trim());
        if (row.some((cell) => cell !== "")) {
          rows.push(row);
        }
        row = [];
        current = "";
        if (char === "\r") i++; // skip \n
      } else {
        current += char;
      }
    }
  }

  // Last row
  row.push(current.trim());
  if (row.some((cell) => cell !== "")) {
    rows.push(row);
  }

  return rows;
}

function generateCsvTemplate(columns: CsvColumn[], sampleData?: Record<string, string>[]): string {
  const header = columns.map((c) => c.key).join(",");
  const sampleRows = sampleData
    ? sampleData
        .map((row) =>
          columns
            .map((c) => {
              const val = row[c.key] || "";
              return val.includes(",") || val.includes('"') ? `"${val.replace(/"/g, '""')}"` : val;
            })
            .join(",")
        )
        .join("\n")
    : "";
  return sampleRows ? `${header}\n${sampleRows}` : header;
}

// ============================================
// Component
// ============================================

export function BulkImportDialog({
  open,
  onOpenChange,
  title,
  description,
  columns,
  onImport,
  sampleData,
}: BulkImportDialogProps) {
  const [file, setFile] = useState<File | null>(null);
  const [parsedRecords, setParsedRecords] = useState<Record<string, string>[] | null>(null);
  const [isImporting, setIsImporting] = useState(false);
  const [result, setResult] = useState<ImportResult | null>(null);
  const [parseErrors, setParseErrors] = useState<string[]>([]);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const reset = () => {
    setFile(null);
    setParsedRecords(null);
    setResult(null);
    setParseErrors([]);
    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  const handleClose = (open: boolean) => {
    if (!open) reset();
    onOpenChange(open);
  };

  const handleFileSelect = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const selectedFile = e.target.files?.[0];
      if (!selectedFile) return;

      if (!selectedFile.name.endsWith(".csv") && !selectedFile.type.includes("csv")) {
        setParseErrors(["Vui lòng chọn file CSV (.csv)"]);
        return;
      }

      setFile(selectedFile);
      setResult(null);
      setParseErrors([]);

      const reader = new FileReader();
      reader.onload = (event) => {
        const text = event.target?.result as string;
        try {
          const rows = parseCsv(text);
          if (rows.length < 2) {
            setParseErrors(["File CSV phải có ít nhất 1 dòng dữ liệu (+ header)"]);
            return;
          }

          const headers = rows[0].map((h) => h.toLowerCase().trim());
          const columnKeys = columns.map((c) => c.key.toLowerCase());

          // Map column indices
          const columnMap: Record<string, number> = {};
          columns.forEach((col) => {
            const idx = headers.indexOf(col.key.toLowerCase());
            if (idx !== -1) {
              columnMap[col.key] = idx;
            }
          });

          // Check required columns
          const missingRequired = columns
            .filter((c) => c.required)
            .filter(
              (c) =>
                !(
                  c.key.toLowerCase() in
                  Object.fromEntries(
                    Object.entries(columnMap).map(([k, v]) => [k.toLowerCase(), v])
                  )
                )
            );

          if (missingRequired.length > 0) {
            setParseErrors([`Thiếu cột bắt buộc: ${missingRequired.map((c) => c.key).join(", ")}`]);
            return;
          }

          // Parse data rows
          const records: Record<string, string>[] = [];
          const errors: string[] = [];

          for (let i = 1; i < rows.length; i++) {
            const row = rows[i];
            const record: Record<string, string> = {};

            columns.forEach((col) => {
              const idx = columnMap[col.key];
              if (idx !== undefined && idx < row.length) {
                record[col.key] = row[idx];
              } else {
                record[col.key] = "";
              }
            });

            // Check required fields
            const missing = columns.filter((c) => c.required && !record[c.key]).map((c) => c.label);

            if (missing.length > 0) {
              errors.push(`Dòng ${i + 1}: thiếu ${missing.join(", ")}`);
            } else {
              records.push(record);
            }
          }

          setParsedRecords(records);
          if (errors.length > 0) {
            setParseErrors(errors);
          }
        } catch {
          setParseErrors(["Không thể đọc file CSV"]);
        }
      };
      reader.readAsText(selectedFile);
    },
    [columns]
  );

  const handleImport = async () => {
    if (!parsedRecords || parsedRecords.length === 0) return;
    setIsImporting(true);
    try {
      const importResult = await onImport(parsedRecords);
      setResult(importResult);
    } catch {
      setResult({
        total: parsedRecords.length,
        success: 0,
        failed: parsedRecords.length,
        errors: ["Lỗi không xác định khi import"],
      });
    } finally {
      setIsImporting(false);
    }
  };

  const handleDownloadTemplate = () => {
    const csv = generateCsvTemplate(columns, sampleData);
    const blob = new Blob(["\ufeff" + csv], {
      type: "text/csv;charset=utf-8;",
    });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "import-template.csv";
    a.click();
    URL.revokeObjectURL(url);
  };

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
          {description && <DialogDescription>{description}</DialogDescription>}
        </DialogHeader>

        <div className="space-y-4">
          {/* Download Template */}
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={handleDownloadTemplate}
            className="w-full"
          >
            <Download className="mr-2 h-4 w-4" />
            Tải file CSV mẫu
          </Button>

          {/* Column Info */}
          <div className="rounded-lg border p-3">
            <p className="text-muted-foreground mb-2 text-xs font-medium">
              Các cột trong file CSV:
            </p>
            <div className="flex flex-wrap gap-1.5">
              {columns.map((col) => (
                <Badge
                  key={col.key}
                  variant={col.required ? "default" : "outline"}
                  className="text-[10px]"
                >
                  {col.key}
                  {col.required && " *"}
                </Badge>
              ))}
            </div>
          </div>

          {/* File Upload */}
          {!result && (
            <div>
              <input
                ref={fileInputRef}
                type="file"
                accept=".csv"
                className="hidden"
                onChange={handleFileSelect}
              />
              <button
                type="button"
                onClick={() => fileInputRef.current?.click()}
                className="hover:border-primary/50 hover:bg-accent/50 flex w-full flex-col items-center justify-center rounded-lg border-2 border-dashed p-6 transition-colors"
              >
                {file ? (
                  <>
                    <FileSpreadsheet className="text-primary mb-2 h-8 w-8" />
                    <span className="text-sm font-medium">{file.name}</span>
                    {parsedRecords && (
                      <span className="text-muted-foreground mt-1 text-xs">
                        {parsedRecords.length} bản ghi hợp lệ
                      </span>
                    )}
                  </>
                ) : (
                  <>
                    <Upload className="text-muted-foreground/50 mb-2 h-8 w-8" />
                    <span className="text-muted-foreground text-sm">Nhấn để chọn file CSV</span>
                  </>
                )}
              </button>
            </div>
          )}

          {/* Parse Errors */}
          {parseErrors.length > 0 && (
            <div className="rounded-lg border border-yellow-500/30 bg-yellow-50 p-3 dark:bg-yellow-950/20">
              <div className="mb-2 flex items-center gap-2">
                <AlertTriangle className="h-4 w-4 text-yellow-600" />
                <span className="text-sm font-medium text-yellow-700 dark:text-yellow-400">
                  Cảnh báo
                </span>
              </div>
              <ScrollArea className="max-h-[120px]">
                <ul className="space-y-1">
                  {parseErrors.map((err, i) => (
                    <li key={i} className="text-xs text-yellow-700 dark:text-yellow-400">
                      {err}
                    </li>
                  ))}
                </ul>
              </ScrollArea>
            </div>
          )}

          {/* Import Result */}
          {result && (
            <div className="space-y-3">
              <div
                className={cn(
                  "rounded-lg border p-4 text-center",
                  result.failed === 0
                    ? "border-green-500/30 bg-green-50 dark:bg-green-950/20"
                    : "border-yellow-500/30 bg-yellow-50 dark:bg-yellow-950/20"
                )}
              >
                {result.failed === 0 ? (
                  <CheckCircle2 className="mx-auto mb-2 h-8 w-8 text-green-600" />
                ) : (
                  <AlertTriangle className="mx-auto mb-2 h-8 w-8 text-yellow-600" />
                )}
                <p className="text-sm font-medium">
                  Import hoàn tất: {result.success}/{result.total} thành công
                </p>
                {result.failed > 0 && (
                  <p className="text-muted-foreground mt-1 text-xs">
                    {result.failed} bản ghi thất bại
                  </p>
                )}
              </div>

              {result.errors.length > 0 && (
                <ScrollArea className="max-h-[150px]">
                  <div className="space-y-1">
                    {result.errors.map((err, i) => (
                      <div key={i} className="flex items-start gap-2 text-xs">
                        <XCircle className="text-destructive mt-0.5 h-3.5 w-3.5 shrink-0" />
                        <span>{err}</span>
                      </div>
                    ))}
                  </div>
                </ScrollArea>
              )}
            </div>
          )}
        </div>

        <DialogFooter>
          <Button type="button" variant="outline" onClick={() => handleClose(false)}>
            {result ? "Đóng" : "Hủy"}
          </Button>
          {!result && parsedRecords && parsedRecords.length > 0 && (
            <Button type="button" onClick={handleImport} disabled={isImporting}>
              {isImporting ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Đang import...
                </>
              ) : (
                <>
                  <Upload className="mr-2 h-4 w-4" />
                  Import {parsedRecords.length} bản ghi
                </>
              )}
            </Button>
          )}
          {result && (
            <Button type="button" onClick={reset}>
              Import thêm
            </Button>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
