"use client";

import { useQuery } from "@tanstack/react-query";
import { Shield } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { getPermissionsGrouped } from "@/services/permissionService";

export default function PermissionsPage() {
  const { data, isLoading } = useQuery({
    queryKey: ["permissions", "grouped"],
    queryFn: getPermissionsGrouped,
  });

  const groups = data?.data ?? [];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Permissions</h1>
        <p className="text-muted-foreground">View all available permissions grouped by module.</p>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center py-12">
          <div className="flex flex-col items-center gap-3">
            <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
            <p className="text-muted-foreground text-sm">Loading permissions...</p>
          </div>
        </div>
      ) : groups.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12">
            <Shield className="text-muted-foreground/50 mb-3 h-12 w-12" />
            <p className="text-muted-foreground">No permissions found.</p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-6">
          {groups.map((group) => (
            <Card key={group.module}>
              <CardHeader>
                <div className="flex items-center gap-2">
                  <CardTitle className="capitalize">{group.module}</CardTitle>
                  <Badge variant="secondary" className="text-xs">
                    {group.permissions.length}
                  </Badge>
                </div>
                <CardDescription>Permissions for {group.module} module</CardDescription>
              </CardHeader>
              <CardContent>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Name</TableHead>
                      <TableHead>Display Name</TableHead>
                      <TableHead>Action</TableHead>
                      <TableHead>Description</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {group.permissions.map((perm) => (
                      <TableRow key={perm.id}>
                        <TableCell>
                          <code className="bg-muted rounded px-1.5 py-0.5 text-xs">
                            {perm.name}
                          </code>
                        </TableCell>
                        <TableCell className="text-sm font-medium">{perm.display_name}</TableCell>
                        <TableCell>
                          <Badge variant="outline" className="text-xs">
                            {perm.action}
                          </Badge>
                        </TableCell>
                        <TableCell className="text-muted-foreground text-sm">
                          {perm.description || "—"}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
