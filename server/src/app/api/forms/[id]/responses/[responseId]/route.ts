import { db } from "@/lib/db";
import { formResponse } from "@/db/schema";
import { requireAuth } from "@/lib/auth-guard";
import { NextRequest, NextResponse } from "next/server";
import { eq } from "drizzle-orm";

export async function DELETE(
  req: NextRequest,
  { params }: { params: Promise<{ id: string; responseId: string }> }
) {
  const { error } = await requireAuth(req);
  if (error) return error;

  const { responseId } = await params;
  const deleted = await db.delete(formResponse).where(eq(formResponse.id, responseId)).returning();
  if (deleted.length === 0) return NextResponse.json({ error: "Response not found" }, { status: 404 });

  return NextResponse.json({ success: true });
}
