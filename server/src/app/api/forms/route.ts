import { db } from "@/lib/db";
import { formTemplate, formField } from "@/db/schema";
import { requireAuth } from "@/lib/auth-guard";
import { NextRequest, NextResponse } from "next/server";
import { nanoid } from "nanoid";
import { eq, asc } from "drizzle-orm";

export async function GET(req: NextRequest) {
  const { error } = await requireAuth(req);
  if (error) return error;

  const forms = await db.select().from(formTemplate).orderBy(asc(formTemplate.createdAt));
  const fields = await db.select().from(formField);

  const result = forms.map((f) => ({
    ...f,
    fields: fields
      .filter((ff) => ff.formId === f.id)
      .sort((a, b) => a.order - b.order)
      .map((ff) => ({ ...ff, options: JSON.parse(ff.options) as string[] })),
  }));

  return NextResponse.json(result);
}

export async function POST(req: NextRequest) {
  const { error } = await requireAuth(req);
  if (error) return error;

  const body = await req.json();
  const { title, description = "", fields = [] } = body;

  if (!title) {
    return NextResponse.json({ error: "title is required" }, { status: 400 });
  }

  const formId = nanoid();
  const now = new Date();

  const [form] = await db.insert(formTemplate).values({
    id: formId,
    title,
    description,
    createdAt: now,
    updatedAt: now,
  }).returning();

  const insertedFields = fields.length > 0
    ? await db.insert(formField).values(
        fields.map((f: { label: string; type: string; required?: boolean; options?: string[]; order?: number }, i: number) => ({
          id: nanoid(),
          formId,
          label: f.label,
          type: f.type,
          required: f.required ?? false,
          options: JSON.stringify(f.options ?? []),
          order: f.order ?? i,
        }))
      ).returning()
    : [];

  return NextResponse.json({
    ...form,
    fields: insertedFields.map((ff) => ({ ...ff, options: JSON.parse(ff.options) as string[] })),
  }, { status: 201 });
}
