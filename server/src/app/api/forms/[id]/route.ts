import { db } from "@/lib/db";
import { formTemplate, formField } from "@/db/schema";
import { requireAuth } from "@/lib/auth-guard";
import { NextRequest, NextResponse } from "next/server";
import { nanoid } from "nanoid";
import { eq, asc } from "drizzle-orm";

export async function GET(req: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  const { error } = await requireAuth(req);
  if (error) return error;

  const { id } = await params;
  const forms = await db.select().from(formTemplate).where(eq(formTemplate.id, id));
  if (forms.length === 0) return NextResponse.json({ error: "Form not found" }, { status: 404 });

  const fields = await db.select().from(formField).where(eq(formField.formId, id)).orderBy(asc(formField.order));

  return NextResponse.json({
    ...forms[0],
    fields: fields.map((ff) => ({ ...ff, options: JSON.parse(ff.options) as string[] })),
  });
}

export async function PUT(req: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  const { error } = await requireAuth(req);
  if (error) return error;

  const { id } = await params;
  const body = await req.json();
  const { title, description = "", fields = [] } = body;

  const updated = await db.update(formTemplate)
    .set({ title, description, updatedAt: new Date() })
    .where(eq(formTemplate.id, id))
    .returning();

  if (updated.length === 0) return NextResponse.json({ error: "Form not found" }, { status: 404 });

  // Replace all fields
  await db.delete(formField).where(eq(formField.formId, id));

  const insertedFields = fields.length > 0
    ? await db.insert(formField).values(
        fields.map((f: { label: string; type: string; required?: boolean; options?: string[]; order?: number }, i: number) => ({
          id: nanoid(),
          formId: id,
          label: f.label,
          type: f.type,
          required: f.required ?? false,
          options: JSON.stringify(f.options ?? []),
          order: f.order ?? i,
        }))
      ).returning()
    : [];

  return NextResponse.json({
    ...updated[0],
    fields: insertedFields.map((ff) => ({ ...ff, options: JSON.parse(ff.options) as string[] })),
  });
}

export async function DELETE(req: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  const { error } = await requireAuth(req);
  if (error) return error;

  const { id } = await params;
  const deleted = await db.delete(formTemplate).where(eq(formTemplate.id, id)).returning();
  if (deleted.length === 0) return NextResponse.json({ error: "Form not found" }, { status: 404 });

  return NextResponse.json({ success: true });
}
